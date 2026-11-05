import { test, expect } from '@playwright/test'
import type { Page } from '@playwright/test'

// Deliberately labelled browser fixtures. Live Git/PostgreSQL evidence is recorded separately.
const repository = { id: 1, url: 'https://github.com/fixture/week8', owner: 'fixture', name: 'week8', createdAt: '2026-11-05T00:00:00Z' }
const snapshot = { id: 2, repositoryId: 1, headSha: 'a'.repeat(40), windowStart: '2026-05-01T00:00:00Z', windowEnd: '2026-11-01T00:00:00Z', configVersion: 'metric-v1', configHash: 'config-a', commitCount: 10, evaluationStatus: 'COMPLETE', summary: { coverageComplete: true }, truncated: false }
const file = { fileId: 11, pathAtSnapshot: 'src/Order.java', changeFrequency: 10, churn: 100, maxCcn: 3, hotspotScore: .8, hhi: .5, evidenceAvailable: true }
async function setup(page: Page, provider = true) {
  let status = 'OPEN'
  const mutations: { path: string; body: Record<string, unknown> }[] = []
  await page.route('**/api/**', async route => {
    const req = route.request(), url = new URL(req.url()), path = url.pathname
    const body = req.method() === 'GET' ? {} : req.postDataJSON() as Record<string, unknown>
    if (req.method() !== 'GET') mutations.push({ path, body })
    const collection = (items: unknown[]) => ({ items, page: 0, size: 20, hasNext: false })
    let response: unknown = {}
    if (path === '/api/repositories') response = [repository]
    else if (path.endsWith('/analysis-capabilities')) response = { canAnalyze: true, writesEnabled: true }
    else if (path.endsWith('/analyses')) response = { jobId: 5, status: 'QUEUED' }
    else if (path.endsWith('/jobs')) response = collection([{ id: 5, status: 'FAILED', errorCode: 'ANALYSIS_FAILED' }])
    else if (path.endsWith('/retry')) response = { jobId: 6, status: 'QUEUED', retryOf: 5 }
    else if (path.endsWith('/snapshots')) response = collection([snapshot, { ...snapshot, id: 1 }])
    else if (path.endsWith('/hotspots')) response = collection([file, { ...file, fileId: 12, pathAtSnapshot: 'src/Unknown.java', hotspotScore: null, maxCcn: null, unavailableReason: 'CCN_UNAVAILABLE' }])
    else if (path.endsWith('/files/11')) response = file
    else if (path.endsWith('/contributions')) response = collection([{ contributorId: 7, displayName: 'Dev', additions: 75, deletions: 25, share: 1 }])
    else if (path.endsWith('/evidence')) response = collection([{ evidenceIndex: 0, evidence: { commitSha: 'b'.repeat(40) } }])
    else if (path.endsWith('/couplings')) response = collection([{ relatedFileId: 12, relatedPath: 'src/OrderDto.java', sharedCommits: 6, fileCommitCount: 10, relatedCommitCount: 8, couplingDegree: .75 }])
    else if (path.endsWith('/trend')) response = collection([{ ...snapshot, snapshotId: 1, ...file, hotspotScore: 0, coverageComplete: true }, { ...snapshot, snapshotId: 2, ...file, coverageComplete: true }])
    else if (path.endsWith('/snapshot-comparison')) return route.fulfill({ status: 409, json: { detail: 'SNAPSHOTS_NOT_COMPARABLE_CONFIG' } })
    else if (path.endsWith('/monitoring')) response = { monitoringEnabled: false, intervalMinutes: 1440, monitoringVersion: 3, nextScanAt: null }
    else if (path.endsWith('/alert-rules')) response = collection([{ id: 7, type: 'STRONG_COUPLING', parameters: { threshold: .5, minShared: 5 }, enabled: true, version: 2 }])
    else if (path.endsWith('/alerts')) response = collection([{ id: 8, status, snapshotId: 2, observedValue: .8, threshold: .5, evidence: { sharedCommits: 6 } }])
    else if (path === '/api/alerts/8') { status = String(body.status); response = { status } }
    else if (path.endsWith('/ai-review-preview')) response = { contextHash: 'preview-hash', providerAvailable: provider, context: { headSha: snapshot.headSha, excerpts: [{ content: 'class Order {}' }] } }
    else if (path.endsWith('/ai-reviews')) response = { aiGenerated: true, result: { riskSummary: 'Fixture AI advisory' } }
    else if (/\/snapshots\/\d+$/.test(path)) response = snapshot
    else if (!path.startsWith('/api/alert-rules/')) return route.fulfill({ status: 404, json: { detail: `Unhandled fixture: ${path}` } })
    return route.fulfill({ status: path.endsWith('/analyses') || path.endsWith('/retry') ? 202 : 200, json: response })
  })
  await page.goto('/')
  await page.getByRole('button', { name: 'Mở phân tích week8' }).click()
  return mutations
}
async function openFile(page: Page) {
  await page.getByRole('button', { name: 'Mở #2', exact: true }).click()
  await page.getByRole('button', { name: 'src/Order.java', exact: true }).click()
}

test('snapshot, hotspot, null, evidence, coupling and measured zero trend', async ({ page }) => {
  await setup(page); await openFile(page)
  await expect(page.getByRole('heading', { name: 'src/Order.java' })).toBeVisible()
  await expect(page.getByText('CCN_UNAVAILABLE')).toBeVisible()
  await expect(page.getByText('Chưa đo được', { exact: true }).first()).toBeVisible()
  await page.getByRole('button', { name: 'src/OrderDto.java' }).click()
  await expect(page.getByRole('heading', { name: 'Bằng chứng cặp file' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Xu hướng file' }).getByRole('cell', { name: '0', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Chọn mốc #1' }).click()
  await expect(page.getByRole('alert')).toContainText('SNAPSHOTS_NOT_COMPARABLE_CONFIG')
})
test('job enqueue and retry use backend state', async ({ page }) => {
  const requests = await setup(page)
  await page.getByRole('button', { name: 'Phân tích repository', exact: true }).click()
  await expect.poll(() => requests.some(r => r.path.endsWith('/analyses') && r.body.observationDays === 180)).toBe(true)
  await page.getByRole('button', { name: 'Lịch sử phân tích', exact: true }).click()
  await page.getByRole('button', { name: 'Thử lại', exact: true }).click()
  await expect.poll(() => requests.some(r => r.path === '/api/jobs/5/retry')).toBe(true)
})
test('monitoring preserves versions and alert transitions', async ({ page }) => {
  const requests = await setup(page)
  await page.getByRole('button', { name: 'Giám sát & cảnh báo' }).click()
  await page.getByRole('button', { name: 'Lưu và bật lịch' }).click()
  await expect.poll(() => requests.find(r => r.path.endsWith('/monitoring'))?.body.version).toBe(3)
  await page.getByRole('button', { name: 'Tắt quy tắc #7' }).click()
  await expect.poll(() => requests.find(r => r.path === '/api/alert-rules/7')?.body.version).toBe(2)
  await page.getByRole('button', { name: 'Đã xem #8' }).click()
  await expect(page.getByText('#8 · ACKNOWLEDGED · snapshot #2')).toBeVisible()
  await page.getByRole('button', { name: 'Đóng #8' }).click()
  await expect(page.getByText('#8 · CLOSED · snapshot #2')).toBeVisible()
})
test('AI requires preview-bound consent and clears it on range change', async ({ page }) => {
  const requests = await setup(page); await openFile(page)
  await page.getByRole('button', { name: 'Xem trước dữ liệu gửi' }).click()
  const submit = page.getByRole('button', { name: 'Gửi yêu cầu AI review' })
  await expect(submit).toBeDisabled()
  await page.getByRole('checkbox').check()
  await page.getByLabel('Đến dòng').fill('10')
  await expect(submit).toHaveCount(0)
  await page.getByRole('button', { name: 'Xem trước dữ liệu gửi' }).click()
  await page.getByRole('checkbox').check(); await submit.click()
  await expect.poll(() => requests.find(r => r.path.endsWith('/ai-reviews'))?.body).toEqual({ startLine: 1, endLine: 10, consent: true, contextHash: 'preview-hash' })
  await expect(page.getByText('Nội dung do AI tạo; đối chiếu bằng chứng trước khi quyết định.')).toBeVisible()
})
test('missing provider stays explicit and mobile layout fits', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await setup(page, false); await openFile(page)
  await page.getByRole('button', { name: 'Xem trước dữ liệu gửi' }).click()
  await expect(page.getByText('Máy chủ chưa cấu hình nhà cung cấp AI. Dữ liệu xem trước chưa được gửi ra ngoài.')).toBeVisible()
  await page.getByRole('checkbox').check()
  await expect(page.getByRole('button', { name: 'Gửi yêu cầu AI review' })).toBeDisabled()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/week8-mobile-fixture.png', fullPage: true })
  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.screenshot({ path: 'test-results/week8-desktop-fixture.png', fullPage: true })
})
test('stale snapshot response is ignored after changing selection', async ({ page }) => {
  await setup(page)
  await page.route('**/api/snapshots/1', async route => { await new Promise(resolve => setTimeout(resolve, 600)); await route.fulfill({ json: { ...snapshot, id: 1, headSha: 'OLD_RESPONSE' } }) })
  await page.getByRole('button', { name: 'Mở #1', exact: true }).click()
  await page.getByRole('button', { name: 'Mở #2', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Snapshot #2', exact: true })).toBeVisible()
  await page.waitForTimeout(800)
  await expect(page.getByText('OLD_RESPONSE', { exact: false })).toHaveCount(0)
})

test('live published snapshot opens through React with no API mocks', async ({ page }) => {
  test.skip(!process.env.GHM_WEEK8_LIVE, 'Requires a completed public-repository analysis in the disposable live database')
  await page.goto('/')
  await page.getByRole('button', { name: `Mở phân tích ${process.env.GHM_WEEK8_LIVE}` }).click()
  await page.getByRole('button', { name: /^Mở #/ }).first().click()
  await expect(page.getByRole('heading', { name: /^Snapshot #/ })).toBeVisible()
  const table = page.getByRole('region', { name: 'Điểm nóng', exact: true }).getByRole('table')
  await expect(table.getByRole('row')).not.toHaveCount(1)
  await table.getByRole('button').first().click()
  await expect(page.getByRole('region', { name: 'Chi tiết file', exact: true })).toBeVisible()
  await page.screenshot({ path: 'test-results/week8-live-desktop.png', fullPage: true })
  await page.setViewportSize({ width: 375, height: 812 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/week8-live-mobile.png', fullPage: true })
})
