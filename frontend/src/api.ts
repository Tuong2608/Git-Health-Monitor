export interface TrackedRepository {
  id: number; url: string; owner: string; name: string; createdAt: string
}
const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')
export async function request<T>(path: string, options: RequestInit = {}, timeoutMs = 15000): Promise<T> {
  const timeout = AbortSignal.timeout(timeoutMs)
  const signal = options.signal ? AbortSignal.any([options.signal, timeout]) : timeout
  let response: Response
  try { response = await fetch(`${baseUrl}${path}`, { ...options, signal }) }
  catch (error) {
    if (options.signal?.aborted) throw error
    throw new Error(`Không kết nối được máy chủ hoặc đã quá ${timeoutMs / 1000} giây. Hãy thử lại.`, { cause: error })
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as { detail?: string; code?: string }
    const messages: Record<string, string> = {
      INVALID_SELECTION: 'Đoạn dòng không hợp lệ hoặc vượt quá độ dài file. Hãy chọn đoạn ngắn hơn.',
      AI_REVIEW_DISABLED: 'Máy chủ chưa bật chức năng AI review.',
      AI_PROVIDER_UNAVAILABLE: 'Máy chủ chưa cấu hình nhà cung cấp AI.',
      SNAPSHOT_SOURCE_UNAVAILABLE: 'Mã nguồn tại snapshot chưa có trong bộ nhớ lưu Git của máy chủ.',
      PUBLIC_CHECK_FAILED: 'Không xác nhận được repository còn công khai. Yêu cầu đã dừng.',
      PUBLIC_CHECK_UNAVAILABLE: 'Chưa kiểm tra được trạng thái công khai của repository. Hãy thử lại sau.',
      CONSENT_REQUIRED: 'Bạn cần xem trước và đồng ý gửi dữ liệu.',
      CONTEXT_CHANGED_PREVIEW_AGAIN: 'Dữ liệu khác với bản đã xác nhận. Hãy xem trước và đồng ý lại.',
      SECRET_DETECTED: 'Đoạn mã có dấu hiệu chứa thông tin bí mật, chưa được gửi tới AI.',
      CONTEXT_TOO_LARGE: 'Dữ liệu vượt giới hạn. Hãy chọn ít dòng hơn.',
      RATE_LIMITED: 'Máy chủ đang bận hoặc đã đạt giới hạn yêu cầu. Hãy thử lại sau.',
      DEADLINE_EXCEEDED: 'AI chưa trả kết quả trong thời gian cho phép. Dữ liệu phân tích vẫn được giữ nguyên.',
      INVALID_OUTPUT: 'Đầu ra AI không hợp lệ nên chưa được hiển thị.',
      INVALID_EVIDENCE: 'AI dẫn bằng chứng ngoài dữ liệu được cung cấp nên kết quả bị từ chối.',
      PROVIDER_FAILED: 'Nhà cung cấp AI chưa trả kết quả hợp lệ.',
      SNAPSHOTS_NOT_COMPARABLE_CONFIG: 'Hai snapshot khác cấu hình hoặc độ dài cửa sổ, không thể tính chênh lệch.',
      SNAPSHOTS_NOT_COMPARABLE_COVERAGE: 'Dữ liệu chưa đầy đủ để so sánh hai snapshot.',
    }
    const fallback = response.status === 403 ? 'Máy chủ từ chối yêu cầu. Kiểm tra quyền đăng ký và cấu hình origin.' : `Yêu cầu thất bại (${response.status}).`
    const code = problem.code ?? problem.detail ?? ''
    throw new Error(messages[code] ? `${messages[code]} (${code})` : problem.detail ?? fallback)
  }
  return response.json() as Promise<T>
}
export function send<T>(path: string, method: string, body: unknown, timeoutMs = 15000) {
  return request<T>(path, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }, timeoutMs)
}
export function listRepositories(page = 0, signal?: AbortSignal) {
  return request<TrackedRepository[]>(`/api/repositories?page=${page}&size=20`, { signal })
}
export function registerRepository(url: string) {
  return request<TrackedRepository>('/api/repositories', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ url }) })
}
