import { deflateRaw } from 'pako'
import { bHh } from '../../../src/utils/musicSdk/options'

const defaultHeaders = {
  'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36',
}

const pendingHttp = new Map()
let reqSeq = 0

globalThis.__sdk_http_resolve = (reqId, respJson) => {
  const entry = pendingHttp.get(reqId)
  if (!entry) return
  pendingHttp.delete(reqId)
  try {
    const resp = typeof respJson === 'string' ? JSON.parse(respJson) : respJson
    if (entry.binary) {
      resp.body = Buffer.from(resp.body || '', 'base64')
    } else if (typeof resp.body === 'string') {
      try {
        resp.body = JSON.parse(resp.body)
      } catch {}
    }
    entry.resolve(resp)
  } catch (err) {
    entry.reject(err)
  }
}

globalThis.__sdk_http_reject = (reqId, errMsg) => {
  const entry = pendingHttp.get(reqId)
  if (!entry) return
  pendingHttp.delete(reqId)
  entry.reject(new Error(String(errMsg || 'Network request failed')))
}

const regx = /(?:\d\w)+/g

const prepareOptions = (url, rawOptions = {}) => {
  const method = (rawOptions.method || 'get').toLowerCase()
  const headers = Object.assign({ Accept: 'application/json' }, rawOptions.headers || {})
  let body = rawOptions.body

  if (method === 'post' && !headers['Content-Type'] && !headers['content-type']) {
    if (rawOptions.form) {
      headers['Content-Type'] = 'application/x-www-form-urlencoded'
      const parts = []
      for (const [k, v] of Object.entries(rawOptions.form)) {
        parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      }
      body = parts.join('&')
    } else if (rawOptions.formData) {
      headers['Content-Type'] = 'application/x-www-form-urlencoded'
      body = typeof rawOptions.formData === 'string' ? rawOptions.formData : JSON.stringify(rawOptions.formData)
    } else {
      headers['Content-Type'] = 'application/json'
    }
  }

  const ct = headers['Content-Type'] || headers['content-type'] || ''
  if (ct.includes('application/json') && body != null && typeof body !== 'string') {
    body = JSON.stringify(body)
  }

  if (headers[bHh]) {
    let s = Buffer.from(bHh, 'hex').toString()
    s = s.replace(s.substr(-1), '')
    s = Buffer.from(s, 'base64').toString()
    const appVer = (globalThis.process && globalThis.process.versions && globalThis.process.versions.app) || '1.9.2'
    const v = appVer.split('-')[0].split('.').map(n => n.length < 3 ? n.padStart(3, '0') : n).join('')
    const v2 = appVer.split('-')[1] || ''
    const raw = Buffer.from(JSON.stringify(`${url}${v}`.match(regx), null, 1).concat(v)).toString('base64')
    headers[s] = !s || `${Buffer.from(deflateRaw(raw)).toString('hex')}&${parseInt(v)}${v2}`
    delete headers[bHh]
  }

  const mergedHeaders = Object.assign({}, defaultHeaders, headers)
  // Normalize all header values to strings
  for (const k of Object.keys(mergedHeaders)) {
    if (mergedHeaders[k] == null) delete mergedHeaders[k]
    else mergedHeaders[k] = String(mergedHeaders[k])
  }

  return {
    url: String(url),
    method: method.toUpperCase(),
    headers: mergedHeaders,
    body: body == null ? null : (Buffer.isBuffer(body) ? body.toString('utf8') : String(body)),
    binary: Boolean(rawOptions.binary),
    timeout: Number(rawOptions.timeout || 15000),
    followMax: Number(rawOptions.follow_max ?? 5),
  }
}

export const httpFetch = (url, options = { method: 'get' }) => {
  const reqId = `req_${++reqSeq}`
  let cancelled = false
  const prepared = prepareOptions(url, options)

  const promise = new Promise((resolve, reject) => {
    pendingHttp.set(reqId, { resolve, reject, binary: prepared.binary })
    try {
      globalThis.__sdk_http(reqId, JSON.stringify(prepared))
    } catch (err) {
      pendingHttp.delete(reqId)
      reject(err)
    }
  })

  return {
    promise,
    cancelHttp() {
      if (cancelled) return
      cancelled = true
      const entry = pendingHttp.get(reqId)
      if (entry) {
        pendingHttp.delete(reqId)
        entry.reject(new Error('Aborted'))
      }
      if (typeof globalThis.__sdk_http_cancel === 'function') {
        try { globalThis.__sdk_http_cancel(reqId) } catch {}
      }
    },
  }
}

export const httpGet = (url, options, callback) => {
  if (typeof options === 'function') {
    callback = options
    options = {}
  }
  const req = httpFetch(url, { ...(options || {}), method: 'get' })
  req.promise.then(resp => callback(null, resp, resp.body)).catch(err => callback(err, null, null))
  return () => req.cancelHttp()
}

export const checkUrl = async (url, options = {}) => {
  const resp = await httpFetch(url, { method: 'head', ...options }).promise
  if (resp.statusCode === 200) return
  throw new Error(String(resp.statusCode))
}
