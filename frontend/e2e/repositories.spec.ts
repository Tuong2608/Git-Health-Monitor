import { test, expect } from '@playwright/test'

// UI contract checks, separate from live PostgreSQL API tests.
test('empty list, registration and labelled learning chart', async ({ page }) => {
  let saved = false
  const repository = { id: 1, url: 'https://github.com/toan/demo', owner: 'toan', name: 'demo', createdAt: '2026-09-30T00:00:00Z' }
  await page.route('**/api/repositories**', async (route) => {
    if (route.request().method() === 'POST') {
      saved = true
      await route.fulfill({ status: 201, json: repository })
    } else await route.fulfill({ json: saved ? [repository] : [] })
  })
  await page.goto('/')
  await expect(page.getByText('Chưa có repository ở trang này.')).toBeVisible()
  await page.getByLabel('URL repository GitHub').fill(repository.url)
  await page.getByRole('button', { name: 'Lưu repository', exact: true }).click()
  await expect(page.getByRole('link', { name: 'toan/demo', exact: true })).toBeVisible()
  await expect(page.getByText('Dữ liệu minh họa', { exact: true })).toBeVisible()
})

test('server conflict is visible', async ({ page }) => {
  await page.route('**/api/repositories**', (route) => route.request().method() === 'POST'
    ? route.fulfill({ status: 409, json: { detail: 'Repository đã được đăng ký.' } })
    : route.fulfill({ json: [] }))
  await page.goto('/')
  await page.getByLabel('URL repository GitHub').fill('https://github.com/toan/demo')
  await page.getByRole('button', { name: 'Lưu repository', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('Repository đã được đăng ký.')
})

test('network failure and mobile viewport', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await page.route('**/api/repositories**', (route) => route.abort())
  await page.goto('/')
  await expect(page.getByRole('alert')).toContainText('Không kết nối được máy chủ')
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
})
