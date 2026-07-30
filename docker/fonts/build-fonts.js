/**
 * 下载 Latin 展示字体的 latin / latin-ext 分片并生成自托管 fonts.css。
 * 只处理 Latin 家族：CJK 家族在 Google 那边被切成 92~404 个分片，
 * 自托管等于往仓库里塞近千个二进制文件，不划算。
 */
const fs = require('fs')
const path = require('path')
const https = require('https')

const OUT = process.argv[2]
const UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36'
// 只要 latin：latin-ext 是东欧带音符字母，中英文站点用不到，却要多 255 KB
const KEEP = new Set(['latin'])

const FAMILIES = [
  { name: 'Fredoka', q: 'Fredoka:wght@400;500;600;700' },
  { name: 'Comfortaa', q: 'Comfortaa:wght@400;600;700' },
  { name: 'Quicksand', q: 'Quicksand:wght@400;500;600;700' },
  // Caveat 只在 --handwriting-font 里作 Long Cang 之后的兜底，且单字重 73 KB，
  // 三个字重要 220 KB —— 占全部字体的四成。只留 500。
  { name: 'Caveat', q: 'Caveat:wght@500' },
]

function get(url, binary) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': UA } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return get(res.headers.location, binary).then(resolve, reject)
      }
      if (res.statusCode !== 200) return reject(new Error(url + ' -> ' + res.statusCode))
      const chunks = []
      res.on('data', (c) => chunks.push(c))
      res.on('end', () => resolve(binary ? Buffer.concat(chunks) : Buffer.concat(chunks).toString('utf8')))
    }).on('error', reject)
  })
}

/** 把 Google 的 CSS 切成 { subset, family, weight, style, unicodeRange, url } */
function parse(css) {
  const out = []
  // 每个 @font-face 前面有一行 /* subset */ 注释
  const re = /\/\*\s*([a-z0-9-]+)\s*\*\/\s*@font-face\s*\{([^}]*)\}/g
  let m
  while ((m = re.exec(css)) !== null) {
    const subset = m[1]
    const body = m[2]
    const pick = (k) => {
      const r = new RegExp(k + ':\\s*([^;]+);')
      const x = body.match(r)
      return x ? x[1].trim() : null
    }
    const url = (body.match(/url\(([^)]+)\)/) || [])[1]
    out.push({
      subset,
      family: (pick('font-family') || '').replace(/^['"]|['"]$/g, ''),
      weight: pick('font-weight'),
      style: pick('font-style'),
      unicodeRange: pick('unicode-range'),
      url,
    })
  }
  return out
}

;(async () => {
  fs.mkdirSync(OUT, { recursive: true })
  const blocks = []
  let bytes = 0

  for (const fam of FAMILIES) {
    const css = await get(
      `https://fonts.googleapis.com/css2?family=${fam.q}&display=swap`,
      false
    )
    const faces = parse(css).filter((f) => KEEP.has(f.subset))
    for (const f of faces) {
      const file = `${f.family.replace(/\s+/g, '')}-${f.weight}-${f.subset}.woff2`
      const buf = await get(f.url, true)
      fs.writeFileSync(path.join(OUT, file), buf)
      bytes += buf.length
      blocks.push({ ...f, file, size: buf.length })
      console.log(`  ${file}  ${(buf.length / 1024).toFixed(1)} KB`)
    }
  }

  const header = `/* ============================================================
 * Lune 自托管字体（Latin 展示字体）
 *
 * 为什么自托管：fonts.googleapis.com 在中国大陆不可访问。
 * 原先 index.html 用 <link rel="stylesheet"> 直连 Google，
 * 而外部样式表是「渲染阻塞」资源 —— 国内用户要等这个请求超时
 * 才会看到首屏，白屏数秒。自托管后这部分字体零外部依赖。
 *
 * 为什么只有 Latin：Google 把 CJK 字体按 unicode-range 切片，
 * 本站用到的中文家族合计 983 个分片（Noto Sans SC 一家就 404 个），
 * 自托管等于往仓库塞近千个二进制文件。中文正文改用系统字体
 * （苹方 / 微软雅黑 / 思源黑体），观感与加载都更好；
 * 三款书法体（Ma Shan Zheng / Zhi Mang Xing / Long Cang）
 * 系统没有等价物，走国内可达镜像 + 非阻塞加载，见 index.html。
 *
 * 本文件由 docker/fonts/download-fonts.sh 生成，勿手改。
 * 分片：${[...KEEP].join(' + ')}，共 ${blocks.length} 个文件 / ${(bytes / 1024).toFixed(1)} KB
 * ============================================================ */

`
  const body = blocks
    .map(
      (b) => `/* ${b.family} ${b.weight} — ${b.subset} (${(b.size / 1024).toFixed(1)} KB) */
@font-face {
  font-family: '${b.family}';
  font-style: ${b.style};
  font-weight: ${b.weight};
  font-display: swap;
  src: url('./${b.file}') format('woff2');
  unicode-range: ${b.unicodeRange};
}`
    )
    .join('\n\n')

  fs.writeFileSync(path.join(OUT, 'fonts.css'), header + body + '\n')
  console.log(`\n共 ${blocks.length} 个 woff2 / ${(bytes / 1024).toFixed(1)} KB`)
})().catch((e) => {
  console.error('失败:', e.message)
  process.exit(1)
})
