import { execFileSync } from 'node:child_process'
import { cpSync, mkdirSync, rmSync, statSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const repoRoot = path.resolve(__dirname, '../..')
const outDir = path.resolve(repoRoot, 'native-android/app/src/main/assets/script')
mkdirSync(outDir, { recursive: true })
const outFile = path.join(outDir, 'music-sdk-bundle.js')

// Stage a clean copy of src/utils so we can swap request.js without touching repo src/
const stageDir = '/tmp/lx-sdk-stage'
rmSync(stageDir, { recursive: true, force: true })
mkdirSync(path.join(stageDir, 'src/utils'), { recursive: true })

cpSync(path.join(repoRoot, 'src/utils/musicSdk'), path.join(stageDir, 'src/utils/musicSdk'), { recursive: true })
cpSync(path.join(repoRoot, 'src/utils/simplify-chinese-main'), path.join(stageDir, 'src/utils/simplify-chinese-main'), { recursive: true })
cpSync(path.join(repoRoot, 'src/utils/index.ts'), path.join(stageDir, 'src/utils/index.ts'))
cpSync(path.join(repoRoot, 'src/utils/common.ts'), path.join(stageDir, 'src/utils/common.ts'))
cpSync(path.join(repoRoot, 'src/utils/message.ts'), path.join(stageDir, 'src/utils/message.ts'))
cpSync(path.join(__dirname, 'shims/request.js'), path.join(stageDir, 'src/utils/request.js'))
cpSync(path.join(__dirname, 'polyfills.js'), path.join(stageDir, 'native-android/sdk-bundler/polyfills.js'))
cpSync(path.join(__dirname, 'entry.js'), path.join(stageDir, 'native-android/sdk-bundler/entry.js'))

// Fix import path inside staged request.js (originally relative to shims/)
import { readFileSync, writeFileSync } from 'node:fs'
const reqPath = path.join(stageDir, 'src/utils/request.js')
writeFileSync(reqPath, readFileSync(reqPath, 'utf8').replace('../../../src/utils/musicSdk/options', './musicSdk/options'))

// Patch staged wy/utils/crypto.js so weapi secretKey is ALWAYS exact 16 ASCII bytes
// (prevents ~10% InvalidKeyException when String(Math.random()).substring(2, 18) is < 16 chars or uses scientific notation)
const wyCryptoPath = path.join(stageDir, 'src/utils/musicSdk/wy/utils/crypto.js')
const wyCryptoOrig = readFileSync(wyCryptoPath, 'utf8')
const wyCryptoPatched = wyCryptoOrig.replace(
  'const secretKey = String(Math.random()).substring(2, 18)',
  "const secretKey = Array.from({ length: 16 }, () => 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'[Math.floor(Math.random() * 62)]).join('')"
)
if (wyCryptoPatched === wyCryptoOrig) {
  throw new Error('Failed to patch secretKey generation in staged wy/utils/crypto.js')
}
writeFileSync(wyCryptoPath, wyCryptoPatched)

// Patch upstream typo in staged wy/songList.js getHotTag() retry (called this.getTag instead of this.getHotTag, aborting concurrent getTag())
const wySongListPath = path.join(stageDir, 'src/utils/musicSdk/wy/songList.js')
const wySongListOrig = readFileSync(wySongListPath, 'utf8')
const wySongListPatched = wySongListOrig.replace(
  /getHotTag\(tryNum = 0\) \{[\s\S]*?return this\.getTag\(\+\+tryNum\)/,
  (m) => m.replace('return this.getTag(++tryNum)', 'return this.getHotTag(++tryNum)')
)
if (wySongListPatched === wySongListOrig) {
  throw new Error('Failed to patch getHotTag retry in staged wy/songList.js')
}
writeFileSync(wySongListPath, wySongListPatched)

// Patch upstream bug in staged mg/musicInfo.js (Promise.all wrapped around Promise from createGetMusicInfosTask)
const mgMusicInfoPath = path.join(stageDir, 'src/utils/musicSdk/mg/musicInfo.js')
const mgMusicInfoOrig = readFileSync(mgMusicInfoPath, 'utf8')
const mgMusicInfoPatched = mgMusicInfoOrig.replace(
  'await Promise.all(createGetMusicInfosTask(copyrightIds))',
  'await createGetMusicInfosTask(copyrightIds)'
)
if (mgMusicInfoPatched !== mgMusicInfoOrig) {
  writeFileSync(mgMusicInfoPath, mgMusicInfoPatched)
}

// Link node_modules into stageDir so esbuild resolves buffer/pako/he
import { symlinkSync } from 'node:fs'
symlinkSync(path.join(repoRoot, 'node_modules'), path.join(stageDir, 'node_modules'))

const args = [
  path.join(stageDir, 'native-android/sdk-bundler/entry.js'),
  '--bundle',
  '--format=iife',
  '--target=es2020',
  '--platform=browser',
  '--minify',
  `--outfile=${outFile}`,
  `--alias:@/utils/nativeModules/crypto=${path.join(__dirname, 'shims/native-crypto.js')}`,
  `--alias:@/store/setting/state=${path.join(__dirname, 'shims/setting-state.js')}`,
  `--alias:react-native-quick-md5=${path.join(__dirname, 'shims/quick-md5.js')}`,
  `--alias:react-native-quick-base64=${path.join(__dirname, 'shims/quick-base64.js')}`,
  `--alias:@=${path.join(stageDir, 'src')}`,
]

execFileSync('esbuild', args, { cwd: stageDir, stdio: 'inherit' })
rmSync(stageDir, { recursive: true, force: true })
const sizeKB = (statSync(outFile).size / 1024).toFixed(1)
console.log(`Built ${outFile} (${sizeKB} KB)`)
