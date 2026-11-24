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
cpSync(path.join(__dirname, 'entry.js'), path.join(stageDir, 'native-android/sdk-bundler/entry.js'))

// Fix import path inside staged request.js (originally relative to shims/)
import { readFileSync, writeFileSync } from 'node:fs'
const reqPath = path.join(stageDir, 'src/utils/request.js')
writeFileSync(reqPath, readFileSync(reqPath, 'utf8').replace('../../../src/utils/musicSdk/options', './musicSdk/options'))

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
