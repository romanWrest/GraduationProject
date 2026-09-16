import type { ReactNode } from 'react';
import { Space, Typography } from 'antd';

const { Title } = Typography;

interface Props {
  title: ReactNode;
  subtitle?: ReactNode;
  extra?: ReactNode;
}

export function PageHeader({ title, subtitle, extra }: Props) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        marginBottom: 16,
        gap: 16,
      }}
    >
      <div>
        <Title level={3} style={{ margin: 0 }}>
          {title}
        </Title>
        {subtitle && <div style={{ color: 'rgba(0,0,0,0.45)', marginTop: 4 }}>{subtitle}</div>}
      </div>
      {extra && <Space>{extra}</Space>}
    </div>
  );
}
