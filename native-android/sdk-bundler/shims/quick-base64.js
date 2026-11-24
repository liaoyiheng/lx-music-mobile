export const btoa = (str) => globalThis.__sdk_btoa(String(str ?? ''))
export const atob = (str) => globalThis.__sdk_atob(String(str ?? ''))
export default { btoa, atob }
