import { test, expect } from '@playwright/test'

test('live React to API to PostgreSQL persists across reload', async ({ page }) => {
  test.skip(process.env.GHM_LIVE_BACKEND !== '1', 'Opt in with a disposable live backend at localhost:8080')
  const name = `week3-${Date.now()}`
  await page.goto('/')
  await page.getByLabel('URL repository GitHub').fill(`https://github.com/toan/${name}`)
  await page.getByRole('button', { name: 'Lưu repository', exact: true }).click()
  await expect(page.getByRole('link', { name: `toan/${name}`, exact: true })).toBeVisible()
  await page.reload()
  await expect(page.getByRole('link', { name: `toan/${name}`, exact: true })).toBeVisible()
  await expect(page.getByText('Dữ liệu minh họa', { exact: true })).toBeVisible()
  await page.screenshot({ path: 'test-results/week3-desktop.png', fullPage: true })
  await page.setViewportSize({ width: 375, height: 812 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/week3-mobile.png', fullPage: true })
})
