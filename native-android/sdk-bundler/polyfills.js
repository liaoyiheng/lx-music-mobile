import { Buffer } from 'buffer'

// Polyfill globals expected by LX Music SDK inside QuickJS.
// Kept in its own module and imported FIRST in entry.js so ESM import hoisting
// evaluates these globals BEFORE any musicSdk sub-module executes top-level code.
globalThis.Buffer = Buffer

if (!globalThis.process) {
  globalThis.process = { versions: { app: '1.9.2' }, env: {} }
} else if (!globalThis.process.versions) {
  globalThis.process.versions = { app: '1.9.2' }
}

if (typeof globalThis.btoa !== 'function') {
  globalThis.btoa = (str) => globalThis.__sdk_btoa(String(str ?? ''))
}
if (typeof globalThis.atob !== 'function') {
  globalThis.atob = (str) => globalThis.__sdk_atob(String(str ?? ''))
}

if (!globalThis.lx) {
  globalThis.lx = {
    qualityList: {
      kw: ['128k', '320k', 'flac', 'flac24bit'],
      kg: ['128k', '320k', 'flac', 'flac24bit'],
      tx: ['128k', '320k', 'flac', 'flac24bit'],
      wy: ['128k', '320k', 'flac', 'flac24bit'],
      mg: ['128k', '320k', 'flac', 'flac24bit'],
    },
    apis: {},
  }
}

// Timers backed by native Android Handler
const timerCallbacks = new Map()
let timerSeq = 0
if (typeof globalThis.setTimeout !== 'function') {
  globalThis.setTimeout = (fn, delay = 0) => {
    const id = `t_${++timerSeq}`
    timerCallbacks.set(id, fn)
    globalThis.__sdk_set_timeout(id, Math.max(0, Number(delay) || 0))
    return id
  }
  globalThis.clearTimeout = (id) => {
    timerCallbacks.delete(id)
    if (typeof globalThis.__sdk_clear_timeout === 'function') {
      globalThis.__sdk_clear_timeout(String(id))
    }
  }
  globalThis.__sdk_timer_fire = (id) => {
    const fn = timerCallbacks.get(id)
    if (!fn) return
    timerCallbacks.delete(id)
    try { fn() } catch (e) { console.error('[timer]', e) }
  }
}
