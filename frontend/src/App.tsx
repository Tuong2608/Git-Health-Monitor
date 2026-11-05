import { lazy, Suspense, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { listRepositories, registerRepository } from './api'
import type { TrackedRepository } from './api'
import './App.css'
const AnalysisDashboard = lazy(() => import('./components/AnalysisDashboard').then((module) => ({ default: module.AnalysisDashboard })))

function App() {
  const [repositories, setRepositories] = useState<TrackedRepository[]>([])
  const [selected, setSelected] = useState<TrackedRepository | null>(null)
  const [url, setUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [listError, setListError] = useState('')
  const [notice, setNotice] = useState('')
  const [page, setPage] = useState(0)
  const [refresh, setRefresh] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    listRepositories(page, controller.signal).then(setRepositories)
      .catch((reason: Error) => { if (!controller.signal.aborted) setListError(reason.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [page, refresh])
  function reload(nextPage = page) {
    setError(''); setListError(''); setLoading(true); setPage(nextPage); setRefresh((value) => value + 1)
  }
  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(''); setNotice('')
    try {
      const repository = await registerRepository(url)
      setUrl(''); setNotice(`Đã lưu ${repository.owner}/${repository.name}. Chưa phân tích lịch sử Git.`)
      reload(0)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Không thể lưu repository.') }
    finally { setSaving(false) }
  }
  return <main>
    <header className="masthead"><a className="brand" href="/">Git Health Monitor</a><span className="badge">Phân tích & giám sát</span></header>
    <section className="intro"><p className="eyebrow">KHÔNG GIAN DỰ ÁN</p><h1>Bắt đầu từ repository của bạn.</h1><p>Theo dõi lịch sử thay đổi, tìm file cần review và kiểm chứng bằng dữ liệu.</p></section>
    <div className="workspace">
      <section className="panel" aria-labelledby="register-title">
        <h2 id="register-title">Thêm repository</h2><p className="muted">Bản thử chỉ lưu địa chỉ GitHub. Chưa kiểm tra kho có tồn tại hoặc công khai.</p>
        <form onSubmit={submit}><label htmlFor="repository-url">URL repository GitHub</label>
          <input id="repository-url" type="url" required maxLength={512} value={url} placeholder="https://github.com/owner/repository" onChange={(event) => setUrl(event.target.value)} />
          <button disabled={saving} type="submit">{saving ? 'Đang lưu…' : 'Lưu repository'}</button></form>
        {notice && <p className="notice" role="status">{notice}</p>}
        {(error || listError) && <div className="error" role="alert"><p>{error || listError}</p><button className="secondary" onClick={() => reload()}>Tải lại danh sách</button></div>}
      </section>
      <section className="panel" aria-labelledby="repositories-title">
        <div className="section-head"><h2 id="repositories-title">Repository đã lưu</h2><button className="secondary" disabled={loading} onClick={() => reload()}>Làm mới</button></div>
        {loading ? <p role="status" className="empty">Đang tải danh sách…</p> : listError ? <p className="empty">Chưa thể xác nhận danh sách mới nhất. Hãy tải lại.</p> : repositories.length === 0 ? <p className="empty">Chưa có repository ở trang này.</p> :
          <ul className="repository-list">{repositories.map((repository) => <li key={repository.id}><div>
            <a href={repository.url} target="_blank" rel="noreferrer">{repository.owner}/{repository.name}</a>
            <p className="muted">Đã lưu {new Date(repository.createdAt).toLocaleString('vi-VN')}</p></div><button className="secondary" onClick={() => setSelected(repository)}>Mở phân tích {repository.name}</button></li>)}</ul>}
        <nav className="pagination" aria-label="Trang repository"><button className="secondary" disabled={page === 0 || loading} onClick={() => reload(page - 1)}>Trước</button><span>Trang {page + 1}</span><button className="secondary" disabled={repositories.length < 20 || loading} onClick={() => reload(page + 1)}>Sau</button></nav>
      </section>
    </div>
    {selected && <Suspense fallback={<p role="status">Đang tải bảng phân tích…</p>}><AnalysisDashboard key={selected.id} repository={selected} /></Suspense>}
    <footer>Chọn một repository để mở dữ liệu phân tích. Các chỉ số là tín hiệu hỗ trợ review và cần đối chiếu bằng chứng.</footer>
  </main>
}
export default App
