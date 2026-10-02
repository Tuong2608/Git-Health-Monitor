import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
// Learning fixture only, never mixed into repository/API results.
const sample = [{ snapshot: 'Mẫu 1', score: 0.42 }, { snapshot: 'Mẫu 2', score: 0.56 }, { snapshot: 'Mẫu 3', score: 0.48 }, { snapshot: 'Mẫu 4', score: 0.63 }]
export function DemoTrend() {
  return <section className="panel trend" aria-labelledby="trend-title">
    <div className="section-head"><h2 id="trend-title">Cách đọc xu hướng hotspot</h2><span className="badge demo">Dữ liệu minh họa</span></div>
    <p className="muted">Bài thực hành Recharts, không phải kết quả phân tích repository đã lưu. Điểm mẫu nằm trong khoảng 0–1.</p>
    <div className="chart" role="img" aria-label="Điểm hotspot minh họa: 0,42; 0,56; 0,48; 0,63">
      <ResponsiveContainer width="100%" height="100%"><LineChart data={sample} margin={{ top: 20, right: 24, bottom: 4, left: 0 }}>
        <CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey="snapshot" /><YAxis domain={[0, 1]} width={40} /><Tooltip />
        <Line type="linear" dataKey="score" name="Điểm mẫu" stroke="#147d70" strokeWidth={3} isAnimationActive={false} />
      </LineChart></ResponsiveContainer>
    </div>
    <details><summary>Xem số liệu minh họa dạng bảng</summary><table><thead><tr><th>Snapshot mẫu</th><th>Điểm</th></tr></thead><tbody>{sample.map((point) => <tr key={point.snapshot}><td>{point.snapshot}</td><td>{point.score}</td></tr>)}</tbody></table></details>
  </section>
}
