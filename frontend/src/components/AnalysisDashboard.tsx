import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { request, send } from '../api'
import type { TrackedRepository } from '../api'

type Row = Record<string, unknown>
type Page = { items: Row[]; page: number; hasNext: boolean }
type Capabilities = { canAnalyze: boolean; writesEnabled: boolean }
const text = (value: unknown) => value == null ? 'Chưa đo được' : String(value)
const num = (value: unknown) => typeof value === 'number' ? value.toLocaleString('vi-VN', { maximumFractionDigits: 4 }) : 'Chưa đo được'
const date = (value: unknown) => typeof value === 'string' ? new Date(value).toLocaleString('vi-VN') : '—'

// Cancel on selection changes; an older request may never overwrite a new selection.
function useRemote<T>(path: string, refresh = 0, poll = 0) {
  const [state, setState] = useState<{ path: string; refresh: number; data?: T; error?: string }>({ path: '', refresh: -1 })
  useEffect(() => {
    const controller = new AbortController()
    let timer: ReturnType<typeof setTimeout>
    const load = async () => {
      try {
        const data = await request<T>(path, { signal: controller.signal })
        if (!controller.signal.aborted) setState({ path, refresh, data })
      } catch (error) {
        if (!controller.signal.aborted) setState({ path, refresh, error: error instanceof Error ? error.message : 'Không tải được dữ liệu.' })
      } finally {
        if (poll && !controller.signal.aborted) timer = setTimeout(load, poll)
      }
    }
    void load()
    return () => { controller.abort(); clearTimeout(timer) }
  }, [path, refresh, poll])
  return state.path === path && state.refresh === refresh ? state : { data: undefined, error: undefined }
}

function Failure({ message }: { message?: string }) { return message ? <p className="error" role="alert">{message}</p> : null }
function Json({ value }: { value: unknown }) { return <pre className="evidence-json">{JSON.stringify(value, null, 2)}</pre> }

function Paged({ path, title, render, refresh = 0, poll = 0 }: { path: string; title: string; render: (rows: Row[]) => ReactNode; refresh?: number; poll?: number }) {
  const [page, setPage] = useState(0)
  const { data, error } = useRemote<Page>(`${path}${path.includes('?') ? '&' : '?'}page=${page}&size=20`, refresh, poll)
  return <section className="subsection" aria-label={title}><h3>{title}</h3><Failure message={error} />
    {!data && !error ? <p role="status">Đang tải…</p> : data && (data.items.length ? render(data.items) : <p>Chưa có dữ liệu phù hợp.</p>)}
    <nav className="pagination" aria-label={`Trang ${title}`}><button className="secondary" disabled={!data || page === 0} onClick={() => setPage(page - 1)}>Trước</button><span>Trang {page + 1}</span><button className="secondary" disabled={!data?.hasNext} onClick={() => setPage(page + 1)}>Sau</button></nav>
  </section>
}

export function AnalysisDashboard({ repository }: { repository: TrackedRepository }) {
  const root = `/api/repositories/${repository.id}`
  const [refresh, setRefresh] = useState(0)
  const [days, setDays] = useState(180)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [snapshot, setSnapshot] = useState<number | null>(null)
  const [baseline, setBaseline] = useState<number | null>(null)
  const [tab, setTab] = useState('snapshot')
  const caps = useRemote<Capabilities>('/api/analysis-capabilities', refresh)
  async function mutate(path: string, method: string, body: unknown) {
    setBusy(true); setError(''); setNotice('')
    try { await send(path, method, body); setRefresh(x => x + 1); setNotice('Đã tiếp nhận yêu cầu. Xem trạng thái và kết quả bên dưới.') }
    catch (e) { setError(e instanceof Error ? e.message : 'Thao tác thất bại.') }
    finally { setBusy(false) }
  }
  return <section className="dashboard panel" aria-label="Bảng phân tích repository">
    <p className="eyebrow">DỮ LIỆU PHÂN TÍCH</p><h2>{repository.owner}/{repository.name}</h2>
    <p className="muted">Hotspot giúp ưu tiên review, không khẳng định file có lỗi. Giá trị chưa đo được được giữ riêng với số 0.</p>
    <div className="toolbar"><label>Cửa sổ quan sát (ngày)<input type="number" min="1" max="365" value={days} onChange={e => setDays(Number(e.target.value))} /></label>
      <button disabled={busy || !caps.data?.canAnalyze || days < 1 || days > 365 || !Number.isInteger(days)} onClick={() => void mutate(`${root}/analyses`, 'POST', { observationDays: days })}>Phân tích repository</button>
      <button className="secondary" onClick={() => setRefresh(x => x + 1)}>Tải lại dữ liệu</button></div>
    {caps.data && !caps.data.canAnalyze && <p className="notice">Máy chủ chưa bật phân tích. Bạn vẫn có thể xem các snapshot đã lưu.</p>}
    <Failure message={error || caps.error} />{notice && <p role="status">{notice}</p>}
    <nav className="tabs" aria-label="Chức năng phân tích">{[['snapshot', 'Snapshot & điểm nóng'], ['monitoring', 'Giám sát & cảnh báo'], ['jobs', 'Lịch sử phân tích']].map(([id, label]) => <button key={id} className={tab === id ? '' : 'secondary'} aria-pressed={tab === id} onClick={() => setTab(id)}>{label}</button>)}</nav>
    {tab === 'jobs' && <Paged path={`${root}/jobs`} title="Lịch sử job" refresh={refresh} poll={3000} render={rows => <div className="table-scroll"><table><thead><tr><th>Job</th><th>Trạng thái</th><th>Kết quả / lỗi</th><th>Thao tác</th></tr></thead><tbody>{rows.map(r => <tr key={text(r.id)}><td>#{text(r.id)}<small>{date(r.requestedAt)}</small></td><td>{text(r.status)}</td><td>{text(r.errorCode ?? r.outcome ?? r.phase ?? 'Đang chờ')}{r.retryOf != null && <small>Chạy lại #{text(r.retryOf)}</small>}</td><td>{r.status === 'FAILED' && <button disabled={busy || !caps.data?.canAnalyze} onClick={() => void mutate(`/api/jobs/${r.id}/retry`, 'POST', {})}>Thử lại</button>}{r.snapshotId != null && <button onClick={() => { setSnapshot(Number(r.snapshotId)); setTab('snapshot') }}>Xem snapshot #{text(r.snapshotId)}</button>}</td></tr>)}</tbody></table></div>} />}
    {tab === 'monitoring' && <Monitoring key={repository.id} root={root} refresh={refresh} mutate={mutate} busy={busy} writes={!!caps.data?.writesEnabled} canAnalyze={!!caps.data?.canAnalyze} />}
    {tab === 'snapshot' && <>
      <Paged path={`${root}/snapshots`} title="Snapshot đã lưu" refresh={refresh} poll={5000} render={rows => <ul className="snapshot-list">{rows.map(r => <li key={text(r.id)}><div><strong>#{text(r.id)} · {date(r.windowEnd)}</strong><small>{text(r.commitCount)} commit trong cửa sổ · HEAD {text(r.headSha).slice(0, 10)}</small></div><button aria-pressed={snapshot === r.id} onClick={() => setSnapshot(Number(r.id))}>Mở #{text(r.id)}</button><button className="secondary" aria-pressed={baseline === r.id} onClick={() => setBaseline(Number(r.id))}>Chọn mốc #{text(r.id)}</button></li>)}</ul>} />
      {snapshot != null && <SnapshotView key={snapshot} id={snapshot} repo={repository.id} />}
      {snapshot != null && baseline != null && <Paged key={`${baseline}-${snapshot}`} path={`${root}/snapshot-comparison?fromSnapshotId=${baseline}&toSnapshotId=${snapshot}`} title={`So sánh #${baseline} → #${snapshot}`} render={rows => <>{rows.map(r => <details key={text(r.fileId)}><summary>{text(r.afterPath ?? r.beforePath)} · {text(r.state)}</summary><p className="muted">Chênh lệch tương đối là tỷ lệ; BASE_ZERO: mốc bằng 0 nên không tính tỷ lệ. UNAVAILABLE: thiếu số đo, không thay bằng 0.</p><MetricComparison metrics={r.metrics as Record<string, Row>} /></details>)}</>} />}
    </>}
  </section>
}

function SnapshotView({ id, repo }: { id: number; repo: number }) {
  const { data, error } = useRemote<Row>(`/api/snapshots/${id}`)
  const [file, setFile] = useState<number | null>(null)
  return <section aria-label="Chi tiết snapshot"><h3>Snapshot #{id}</h3><Failure message={error} />
    {data && <><p>Quan sát: {date(data.windowStart)} → {date(data.windowEnd)} · {text(data.configVersion)}</p><p className="muted">HEAD {text(data.headSha)} · Cảnh báo: {text(data.evaluationStatus)}</p>{(data.truncated === true || (data.summary as Row)?.coverageComplete === false) && <p className="error">Dữ liệu chưa đầy đủ, không dùng để so sánh toàn bộ hoặc kết luận không có cảnh báo.</p>}<details><summary>Nguồn gốc và chất lượng dữ liệu</summary><Json value={data.summary} /><Json value={data.evaluationNotes} /></details></>}
    <Paged path={`/api/snapshots/${id}/hotspots`} title="Điểm nóng" render={rows => <><div className="heatmap" aria-label="Bản đồ điểm nóng của trang hiện tại">{rows.map(r => <button key={text(r.fileId)} className={typeof r.hotspotScore !== 'number' ? 'heat unknown' : r.hotspotScore >= .7 ? 'heat high' : r.hotspotScore >= .4 ? 'heat medium' : 'heat low'} onClick={() => setFile(Number(r.fileId))}><span>{text(r.pathAtSnapshot)}</span><strong>{num(r.hotspotScore)}</strong></button>)}</div><div className="table-scroll"><table><thead><tr><th>File</th><th>Hotspot</th><th>Tần suất</th><th>Churn</th><th>Max CCN</th></tr></thead><tbody>{rows.map(r => <tr key={text(r.fileId)}><td><button className="text-button" onClick={() => setFile(Number(r.fileId))}>{text(r.pathAtSnapshot)}</button>{r.unavailableReason != null && <small>{text(r.unavailableReason)}</small>}</td><td>{num(r.hotspotScore)}</td><td>{num(r.changeFrequency)}</td><td>{num(r.churn)}</td><td>{num(r.maxCcn)}</td></tr>)}</tbody></table></div></>} />
    {file != null && <FileView key={file} snapshot={id} file={file} repo={repo} />}
  </section>
}

function FileView({ snapshot, file, repo }: { snapshot: number; file: number; repo: number }) {
  const root = `/api/snapshots/${snapshot}/files/${file}`
  const { data, error } = useRemote<Row>(root)
  const [related, setRelated] = useState<number | null>(null)
  return <section className="file-detail" aria-label="Chi tiết file"><h3>{data ? text(data.pathAtSnapshot) : `File #${file}`}</h3><Failure message={error} />
    {data && <><dl className="metrics">{[['Tần suất', 'changeFrequency'], ['Churn', 'churn'], ['Max CCN', 'maxCcn'], ['Hotspot', 'hotspotScore'], ['HHI', 'hhi']].map(([label, key]) => <div key={key}><dt>{label}</dt><dd>{num(data[key])}</dd></div>)}</dl><p className="muted">HHI thể hiện mức tập trung đóng góp lịch sử, không đánh giá năng lực cá nhân.</p>{data.evidenceAvailable === false && <p>Chưa có bằng chứng commit cho file này.</p>}</>}
    <Paged path={`${root}/contributions`} title="Đóng góp theo thay đổi dòng" render={rows => <ul>{rows.map(r => <li key={text(r.contributorId)}>{text(r.displayName)} · thêm {num(r.additions)}, xóa {num(r.deletions)} · {r.share == null ? 'Chưa đo được tỷ lệ' : `${num(Number(r.share) * 100)}%`}</li>)}</ul>} />
    <Paged path={`${root}/evidence`} title="Bằng chứng commit" render={rows => <Json value={rows.map(r => r.evidence)} />} />
    <Paged path={`${root}/couplings`} title="File thường thay đổi cùng" render={rows => <ul>{rows.map(r => <li key={text(r.relatedFileId)}><button className="text-button" onClick={() => setRelated(Number(r.relatedFileId))}>{text(r.relatedPath)}</button> · {num(r.sharedCommits)} commit chung / min({num(r.fileCommitCount)}, {num(r.relatedCommitCount)}) = {num(r.couplingDegree)}<small>Tương quan lịch sử, không chứng minh phụ thuộc mã nguồn.</small></li>)}</ul>} />
    {related != null && <Paged key={related} path={`${root}/couplings/${related}/evidence`} title="Bằng chứng cặp file" render={rows => <Json value={rows.map(r => r.evidence)} />} />}
    <Paged path={`/api/repositories/${repo}/files/${file}/trend`} title="Xu hướng file" render={rows => <Trend rows={rows} />} />
    <AiReview key={`${snapshot}-${file}`} root={root} />
  </section>
}

function Trend({ rows }: { rows: Row[] }) {
  // Each contiguous compatible segment gets its own line; never connect across missing coverage/configuration.
  let segment = 0
  const groups: string[] = []
  const chart = rows.map((r, i) => {
    if (i && (r.configHash !== rows[i - 1].configHash || r.configVersion !== rows[i - 1].configVersion || r.truncated || rows[i - 1].truncated || !r.coverageComplete || !rows[i - 1].coverageComplete || (Date.parse(String(r.windowEnd)) - Date.parse(String(r.windowStart))) !== (Date.parse(String(rows[i - 1].windowEnd)) - Date.parse(String(rows[i - 1].windowStart))))) segment++
    const key = `series${segment}`
    if (!groups.includes(key)) groups.push(key)
    return { name: `#${r.snapshotId}`, [key]: r.hotspotScore }
  })
  return <><p className="muted">Hotspot 0–1 theo snapshot; đường ngắt khi cấu hình, cửa sổ hoặc độ đầy đủ không tương thích. Biểu đồ chỉ gồm trang đang xem.</p><div className="chart"><ResponsiveContainer width="100%" height="100%"><LineChart data={chart}><XAxis dataKey="name" /><YAxis domain={[0, 1]} /><Tooltip />{groups.map(key => <Line key={key} dataKey={key} name="Hotspot" stroke="#126f63" connectNulls={false} isAnimationActive={false} />)}</LineChart></ResponsiveContainer></div><div className="table-scroll"><table><thead><tr><th>Snapshot / cửa sổ</th><th>File</th><th>Hotspot</th><th>Tần suất</th></tr></thead><tbody>{rows.map(r => <tr key={text(r.snapshotId)}><td>#{text(r.snapshotId)}<small>{date(r.windowEnd)}</small></td><td>{text(r.pathAtSnapshot)}</td><td>{num(r.hotspotScore)}</td><td>{num(r.changeFrequency)}</td></tr>)}</tbody></table></div></>
}

function Monitoring({ root, refresh, mutate, busy, writes, canAnalyze }: { root: string; refresh: number; mutate: (path: string, method: string, body: unknown) => Promise<void>; busy: boolean; writes: boolean; canAnalyze: boolean }) {
  const settings = useRemote<Row>(`${root}/monitoring`, refresh)
  const [interval, setInterval] = useState(1440)
  const [type, setType] = useState('HOTSPOT_INCREASE')
  const [threshold, setThreshold] = useState(.2)
  const [filter, setFilter] = useState('ALL')
  return <><h3>Lịch giám sát</h3><Failure message={settings.error} />{settings.data && <><p>{settings.data.monitoringEnabled ? 'Đang bật' : 'Đang tắt'} · chu kỳ {text(settings.data.intervalMinutes)} phút · lần kế tiếp {date(settings.data.nextScanAt)}</p><label>Chu kỳ mới (phút)<input type="number" min="60" max="10080" value={interval} onChange={e => setInterval(Number(e.target.value))} /></label><div className="toolbar"><button disabled={busy || !canAnalyze || interval < 60 || interval > 10080 || !Number.isInteger(interval)} onClick={() => void mutate(`${root}/monitoring`, 'PUT', { enabled: true, intervalMinutes: interval, version: settings.data?.monitoringVersion })}>Lưu và bật lịch</button><button className="secondary" disabled={busy || !writes || !settings.data.monitoringEnabled} onClick={() => void mutate(`${root}/monitoring`, 'PUT', { enabled: false, intervalMinutes: settings.data?.intervalMinutes, version: settings.data?.monitoringVersion })}>Tắt lịch</button></div></>}
    <h3>Quy tắc cảnh báo</h3><div className="toolbar"><label>Loại quy tắc<select value={type} onChange={e => { setType(e.target.value); setThreshold(e.target.value === 'STRONG_COUPLING' ? .5 : .2) }}><option value="HOTSPOT_INCREASE">Hotspot tăng tuyệt đối</option><option value="STRONG_COUPLING">Coupling mạnh (≥5 commit chung)</option></select></label><label>Ngưỡng<input type="number" step="0.05" min={type === 'STRONG_COUPLING' ? .5 : 0} max="1" value={threshold} onChange={e => setThreshold(Number(e.target.value))} /></label><button disabled={busy || !writes || threshold < (type === 'STRONG_COUPLING' ? .5 : 0) || threshold > 1} onClick={() => void mutate(`${root}/alert-rules`, 'POST', { type, threshold, minShared: 5, enabled: true })}>Thêm quy tắc</button></div>
    <Paged path={`${root}/alert-rules`} title="Quy tắc đã lưu" refresh={refresh} render={rows => <ul>{rows.map(r => <li key={text(r.id)}>#{text(r.id)} · {text(r.type)} · {r.enabled ? 'Bật' : 'Tắt'} · ngưỡng {num((r.parameters as Row).threshold)} <button className="secondary" disabled={busy || !writes} onClick={() => void mutate(`/api/alert-rules/${r.id}`, 'PUT', { type: r.type, ...(r.parameters as Row), enabled: !r.enabled, version: r.version })}>{r.enabled ? 'Tắt' : 'Bật'} quy tắc #{text(r.id)}</button></li>)}</ul>} />
    <label>Lọc cảnh báo<select value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">Tất cả</option><option value="OPEN">Mới</option><option value="ACKNOWLEDGED">Đã xem</option><option value="CLOSED">Đã đóng</option></select></label>
    <Paged key={filter} path={`${root}/alerts?status=${filter}`} title="Cảnh báo" refresh={refresh} render={rows => <>{rows.map(r => <article className="alert-card" key={text(r.id)}><strong>#{text(r.id)} · {text(r.status)} · snapshot #{text(r.snapshotId)}</strong><p>Giá trị {num(r.observedValue)} · ngưỡng {num(r.threshold)}</p><details><summary>Lý do và bằng chứng</summary><Json value={r.evidence} /></details>{r.status !== 'CLOSED' && <div className="toolbar"><button disabled={busy || !writes || r.status === 'ACKNOWLEDGED'} onClick={() => void mutate(`/api/alerts/${r.id}`, 'PATCH', { status: 'ACKNOWLEDGED' })}>Đã xem #{text(r.id)}</button><button className="secondary" disabled={busy || !writes} onClick={() => void mutate(`/api/alerts/${r.id}`, 'PATCH', { status: 'CLOSED' })}>Đóng #{text(r.id)}</button></div>}</article>)}</>} />
  </>
}

type Preview = { contextHash: string; context: Row; providerAvailable: boolean }
function MetricComparison({ metrics }: { metrics: Record<string, Row> }) {
  const labels: Record<string, string> = { changeFrequency: 'Tần suất', churn: 'Churn', maxCcn: 'Max CCN', avgCcn: 'CCN trung bình', functionCount: 'Số hàm', nloc: 'Dòng mã', hotspotScore: 'Hotspot', hhi: 'HHI' }
  return <div className="table-scroll"><table><thead><tr><th>Chỉ số</th><th>Trước</th><th>Sau</th><th>Chênh lệch</th><th>Tương đối</th><th>Giới hạn</th></tr></thead><tbody>{Object.entries(metrics).map(([key, m]) => <tr key={key}><td>{labels[key] ?? key}</td><td>{num(m.before)}</td><td>{num(m.after)}</td><td>{num(m.absoluteDelta)}</td><td>{m.relativeDelta == null ? 'Không tính được' : `${num(Number(m.relativeDelta) * 100)}%`}</td><td>{m.reason == null ? '—' : text(m.reason)}</td></tr>)}</tbody></table></div>
}
function ReviewResult({ value }: { value: Row }) {
  const output = value.result as { riskSummary?: string; evidence?: { evidenceId: string; explanation: string }[]; reviewChecklist?: { text: string; evidenceIds: string[] }[]; refactorSuggestions?: { text: string; evidenceIds: string[] }[]; limitations?: string[] }
  return <article><h4>Nhận xét rủi ro</h4><p>{output.riskSummary}</p><h4>Bằng chứng</h4><ul>{output.evidence?.map(e => <li key={e.evidenceId}>{e.evidenceId}: {e.explanation}</li>)}</ul><h4>Điểm cần kiểm tra</h4><ul>{output.reviewChecklist?.map((a, i) => <li key={i}>{a.text} <small>Bằng chứng: {a.evidenceIds.join(', ')}</small></li>)}</ul><h4>Gợi ý cải tiến mã</h4><ul>{output.refactorSuggestions?.map((a, i) => <li key={i}>{a.text} <small>Bằng chứng: {a.evidenceIds.join(', ')}</small></li>)}</ul><h4>Giới hạn</h4><ul>{output.limitations?.map((s, i) => <li key={i}>{s}</li>)}</ul><details><summary>Thông tin truy vết kết quả AI</summary><Json value={value.metadata} /></details></article>
}
function AiReview({ root }: { root: string }) {
  const [start, setStart] = useState(1)
  const [end, setEnd] = useState(20)
  const [preview, setPreview] = useState<Preview | null>(null)
  const [consent, setConsent] = useState(false)
  const [result, setResult] = useState<Row | null>(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function run(review: boolean) {
    setBusy(true); setError(''); setResult(null)
    try {
      if (review) setResult(await send<Row>(`${root}/ai-reviews`, 'POST', { startLine: start, endLine: end, contextHash: preview?.contextHash, consent }, 40000))
      else { setPreview(null); setConsent(false); setPreview(await send<Preview>(`${root}/ai-review-preview`, 'POST', { startLine: start, endLine: end })) }
    } catch (e) { setError(e instanceof Error ? e.message : 'AI review thất bại.') }
    finally { setBusy(false) }
  }
  const reset = () => { setPreview(null); setConsent(false); setResult(null); setError('') }
  return <section className="subsection" aria-label="Trợ lý AI review"><h3>Trợ lý AI review</h3><p>Xem trước chính xác đoạn mã và chỉ số tại snapshot trước khi đồng ý gửi. Kết quả AI là gợi ý cần kiểm chứng.</p><div className="toolbar"><label>Từ dòng<input type="number" min="1" disabled={busy} value={start} onChange={e => { setStart(Number(e.target.value)); reset() }} /></label><label>Đến dòng<input type="number" min={start} disabled={busy} value={end} onChange={e => { setEnd(Number(e.target.value)); reset() }} /></label><button disabled={busy || start < 1 || end < start || end - start >= 200 || !Number.isInteger(start) || !Number.isInteger(end)} onClick={() => void run(false)}>Xem trước dữ liệu gửi</button></div><Failure message={error} />
    {preview && <><Json value={preview.context} />{!preview.providerAvailable && <p className="notice">Máy chủ chưa cấu hình nhà cung cấp AI. Dữ liệu xem trước chưa được gửi ra ngoài.</p>}<label className="consent"><input type="checkbox" checked={consent} disabled={busy} onChange={e => setConsent(e.target.checked)} />Tôi đồng ý gửi đúng dữ liệu đang xem tới nhà cung cấp AI do nhóm cấu hình.</label><button disabled={busy || !consent || !preview.providerAvailable} onClick={() => void run(true)}>{busy ? 'Đang xử lý…' : 'Gửi yêu cầu AI review'}</button></>}
    {result && <><p className="notice">Nội dung do AI tạo; đối chiếu bằng chứng trước khi quyết định.</p><ReviewResult value={result} /></>}
  </section>
}
