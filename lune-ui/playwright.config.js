import { defineConfig } from '@playwright/test'

// ============================================================
// 可选 E2E 冒烟测试。默认指向生产站点，可用环境变量覆盖：
//   E2E_BASE_URL=https://ztcools.com npx playwright test
// 本地跑需先: npx playwright install --with-deps chromium
// ============================================================

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  reporter: process.env.CI ? [['list'], ['github']] : 'list',
  use: {
    baseURL: process.env.E2E_BASE_URL || 'https://ztcools.com',
    headless: true,
    locale: 'zh-CN'
  }
})
