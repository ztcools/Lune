import { test, expect } from '@playwright/test'

// 站点冒烟测试：验证关键页面可访问、能正常渲染（不校验业务数据）。
// 默认跑在生产站点上（见 playwright.config.js 的 baseURL）。

test('首页正常加载并可渲染', async ({ page }) => {
  const response = await page.goto('/')
  expect(response.status()).toBeLessThan(500)
  // Vue 应用挂载后 #app 内应有内容
  await expect(page.locator('#app')).not.toBeEmpty()
})

test('后台登录页可访问', async ({ page }) => {
  const response = await page.goto('/admin/login')
  expect(response.status()).toBeLessThan(500)
})
