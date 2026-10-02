export interface TrackedRepository {
  id: number; url: string; owner: string; name: string; createdAt: string
}
const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const timeout = AbortSignal.timeout(15000)
  const signal = options.signal ? AbortSignal.any([options.signal, timeout]) : timeout
  let response: Response
  try { response = await fetch(`${baseUrl}${path}`, { ...options, signal }) }
  catch (error) {
    if (options.signal?.aborted) throw error
    throw new Error('Không kết nối được máy chủ hoặc đã quá 15 giây. Hãy thử lại.', { cause: error })
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as { detail?: string }
    const fallback = response.status === 403 ? 'Máy chủ từ chối yêu cầu. Kiểm tra quyền đăng ký và cấu hình origin.' : `Yêu cầu thất bại (${response.status}).`
    throw new Error(problem.detail ?? fallback)
  }
  return response.json() as Promise<T>
}
export function listRepositories(page = 0, signal?: AbortSignal) {
  return request<TrackedRepository[]>(`/api/repositories?page=${page}&size=20`, { signal })
}
export function registerRepository(url: string) {
  return request<TrackedRepository>('/api/repositories', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ url }) })
}
