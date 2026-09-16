import { Card, Col, Row, Space, Typography } from 'antd';
import {
  AppstoreOutlined,
  FileTextOutlined,
  HomeOutlined,
  TeamOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';

const { Title, Paragraph } = Typography;

const reports = [
  {
    path: '/reports/requests',
    title: 'Заявки',
    description: 'Количество, типы, среднее время закрытия за период',
    icon: <FileTextOutlined style={{ fontSize: 28 }} />,
  },
  {
    path: '/reports/assignees',
    title: 'Загрузка исполнителей',
    description: 'Сколько назначено, выполнено, средний срок по каждому',
    icon: <TeamOutlined style={{ fontSize: 28 }} />,
  },
  {
    path: '/reports/residents',
    title: 'Проживающие',
    description: 'Текущие жильцы на дату с группировкой',
    icon: <HomeOutlined style={{ fontSize: 28 }} />,
  },
  {
    path: '/reports/appliances',
    title: 'Электроприборы',
    description: 'Зарегистрированные приборы и суммарная мощность',
    icon: <ThunderboltOutlined style={{ fontSize: 28 }} />,
  },
  {
    path: '/reports/consumables',
    title: 'Расходники',
    description: 'Выдачи и возвраты за период',
    icon: <AppstoreOutlined style={{ fontSize: 28 }} />,
  },
];

export function ReportsPage() {
  return (
    <>
      <PageHeader
        title="Отчёты"
        subtitle="Аналитика по операциям общежития. Каждый отчёт можно выгрузить в CSV / XLSX / PDF."
      />
      <Row gutter={[16, 16]}>
        {reports.map((r) => (
          <Col xs={24} md={12} lg={8} key={r.path}>
            <Link to={r.path}>
              <Card hoverable>
                <Space size={16} align="start">
                  <div style={{ color: '#1677ff' }}>{r.icon}</div>
                  <div>
                    <Title level={4} style={{ marginTop: 0 }}>
                      {r.title}
                    </Title>
                    <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                      {r.description}
                    </Paragraph>
                  </div>
                </Space>
              </Card>
            </Link>
          </Col>
        ))}
      </Row>
    </>
  );
}
