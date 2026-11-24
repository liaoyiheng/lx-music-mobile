export const RSA_PADDING = {
  OAEPWithSHA1AndMGF1Padding: 'RSA/ECB/OAEPWithSHA1AndMGF1Padding',
  NoPadding: 'RSA/ECB/NoPadding',
}

export const AES_MODE = {
  CBC_128_PKCS7Padding: 'AES/CBC/PKCS7Padding',
  ECB_128_NoPadding: 'AES',
}

const stripPem = (key) => String(key || '')
  .replace('-----BEGIN PUBLIC KEY-----', '')
  .replace('-----END PUBLIC KEY-----', '')
  .replace('-----BEGIN PRIVATE KEY-----', '')
  .replace('-----END PRIVATE KEY-----', '')
  .replace(/\s+/g, '')

export const aesEncryptSync = (text, key, iv, mode) =>
  globalThis.__sdk_aes_encrypt(String(text ?? ''), String(key ?? ''), String(iv ?? ''), String(mode ?? ''))

export const aesDecryptSync = (text, key, iv, mode) =>
  globalThis.__sdk_aes_decrypt(String(text ?? ''), String(key ?? ''), String(iv ?? ''), String(mode ?? ''))

export const rsaEncryptSync = (text, key, padding) =>
  globalThis.__sdk_rsa_encrypt(String(text ?? ''), stripPem(key), String(padding ?? ''))

export const rsaDecryptSync = (text, key, padding) =>
  globalThis.__sdk_rsa_decrypt(String(text ?? ''), stripPem(key), String(padding ?? ''))

export const aesEncrypt = async (text, key, iv, mode) => aesEncryptSync(text, key, iv, mode)
export const aesDecrypt = async (text, key, iv, mode) => aesDecryptSync(text, key, iv, mode)
export const rsaEncrypt = async (text, key, padding) => rsaEncryptSync(text, key, padding)
export const rsaDecrypt = async (text, key, padding) => rsaDecryptSync(text, key, padding)
export const hashSHA1 = async (text) => globalThis.__sdk_sha1(String(text ?? ''))
