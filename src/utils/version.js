import { httpGet } from '@/utils/request'
import { downloadFile, stopDownload, temporaryDirectoryPath } from '@/utils/fs'
import { getSupportedAbis, installApk } from '@/utils/nativeModules/utils'
import { APP_PROVIDER_NAME } from '@/config/constant'

const FORK_OWNER = 'liaoyiheng'
const FORK_REPO = 'lx-music-mobile'
const FORK_BRANCH = 'apple-style'
const TAG_PREFIX = 'apple-v'
const APK_PREFIX = 'lx-music-apple-v'

const abis = [
  'arm64-v8a',
  'armeabi-v7a',
  'x86_64',
  'x86',
  'universal',
]

const address = [
  [`https://raw.githubusercontent.com/${FORK_OWNER}/${FORK_REPO}/${FORK_BRANCH}/publish/apple-version.json`, 'direct'],
  [`https://cdn.jsdelivr.net/gh/${FORK_OWNER}/${FORK_REPO}@${FORK_BRANCH}/publish/apple-version.json`, 'direct'],
  [`https://fastly.jsdelivr.net/gh/${FORK_OWNER}/${FORK_REPO}@${FORK_BRANCH}/publish/apple-version.json`, 'direct'],
  [`https://gcore.jsdelivr.net/gh/${FORK_OWNER}/${FORK_REPO}@${FORK_BRANCH}/publish/apple-version.json`, 'direct'],
  [`https://api.github.com/repos/${FORK_OWNER}/${FORK_REPO}/releases?per_page=10`, 'github'],
]

const request = async(url, retryNum = 0, headers = {}) => {
  return new Promise((resolve, reject) => {
    httpGet(url, {
      timeout: 10000,
      headers,
    }, (err, resp, body) => {
      if (err || resp.statusCode != 200) {
        ++retryNum >= 3
          ? reject(err || new Error(resp.statusMessage || resp.statusCode))
          : request(url, retryNum, headers).then(resolve).catch(reject)
      } else resolve(body)
    })
  })
}

const getDirectInfo = async(url) => {
  return request(url).then(info => {
    if (!info || info.version == null) throw new Error('failed')
    return info
  })
}

const getGithubReleaseInfo = async(url) => {
  return request(url, 0, {
    Accept: 'application/vnd.github+json',
    'User-Agent': 'lx-music-apple',
  }).then(list => {
    if (!Array.isArray(list)) throw new Error('failed')
    const releases = list
      .filter(r => r && !r.draft && !r.prerelease && typeof r.tag_name === 'string' && r.tag_name.startsWith(TAG_PREFIX))
      .map(r => ({
        version: r.tag_name.slice(TAG_PREFIX.length),
        desc: typeof r.body === 'string' ? r.body.trim() : '',
      }))
    if (!releases.length) throw new Error('no apple release found')
    const [latest, ...history] = releases
    return {
      version: latest.version,
      desc: latest.desc,
      history,
    }
  })
}

export const getVersionInfo = async(index = 0) => {
  const [url, source] = address[index]
  let promise
  switch (source) {
    case 'direct':
      promise = getDirectInfo(url)
      break
    case 'github':
      promise = getGithubReleaseInfo(url)
      break
  }

  return promise.catch(async(err) => {
    index++
    if (index >= address.length) throw err
    return getVersionInfo(index)
  })
}

const getTargetAbi = async() => {
  const supportedAbis = await getSupportedAbis()
  for (const abi of abis) {
    if (supportedAbis.includes(abi)) return abi
  }
  return abis[abis.length - 1]
}
let downloadJobId = null
const noop = (total, download) => {}
let apkSavePath

export const downloadNewVersion = async(version, onDownload = noop) => {
  const abi = await getTargetAbi()
  const url = `https://github.com/${FORK_OWNER}/${FORK_REPO}/releases/download/${TAG_PREFIX}${version}/${APK_PREFIX}${version}-${abi}.apk`
  let savePath = temporaryDirectoryPath + '/lx-music-apple.apk'

  if (downloadJobId) stopDownload(downloadJobId)

  const { jobId, promise } = downloadFile(url, savePath, {
    progressInterval: 500,
    connectionTimeout: 20000,
    readTimeout: 30000,
    begin({ statusCode, contentLength }) {
      onDownload(contentLength, 0)
    },
    progress({ contentLength, bytesWritten }) {
      onDownload(contentLength, bytesWritten)
    },
  })
  downloadJobId = jobId
  return promise.then(() => {
    apkSavePath = savePath
    return updateApp()
  })
}

export const updateApp = async() => {
  if (!apkSavePath) throw new Error('apk Save Path is null')
  await installApk(apkSavePath, APP_PROVIDER_NAME)
}
