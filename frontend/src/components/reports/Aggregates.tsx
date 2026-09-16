import { Card, Col, Row, Statistic } from 'antd';

interface Item {
  label: string;
  value: number | string;
  suffix?: string;
}

export function Aggregates({ items }: { items: Item[] }) {
  return (
    <Row gutter={[16, 16]}>
      {items.map((it, i) => (
        <Col xs={12} md={8} lg={6} key={i}>
          <Card>
            <Statistic title={it.label} value={it.value} suffix={it.suffix} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}

export function CountTable({
  title,
  data,
}: {
  title: string;
  data: Record<string, number>;
}) {
  const entries = Object.entries(data ?? {}).sort((a, b) => b[1] - a[1]);
  return (
    <Card title={title} size="small">
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <tbody>
          {entries.length === 0 && (
            <tr>
              <td colSpan={2} style={{ color: 'rgba(0,0,0,0.45)' }}>
                Нет данных
              </td>
            </tr>
          )}
          {entries.map(([k, v]) => (
            <tr key={k}>
              <td style={{ padding: '4px 0' }}>{k}</td>
              <td style={{ textAlign: 'right', fontWeight: 600 }}>{v}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Card>
  );
}
