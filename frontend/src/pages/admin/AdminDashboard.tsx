import { Card, Col, List, Row, Space, Statistic, Tag, Tooltip, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useRequests } from '@/features/requests/hooks';
import { useRequestsReport } from '@/features/reports/hooks';
import { useStock } from '@/features/consumables/hooks';
import { useCurrentUser } from '@/features/auth/hooks';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime, shortId } from '@/shared/lib/format';
import type { RequestStatus, RequestType } from '@/features/requests/types';

const { Text } = Typography;

export function AdminDashboard() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const since = dayjs().subtract(30, 'day').toISOString();
  const now = dayjs().toISOString();
  const today = dayjs().startOf('day').toISOString();

  const { data: report } = useRequestsReport({ from: since, to: now });
  const { data: todayReport } = useRequestsReport({ from: today, to: now });
  const { data: latest } = useRequests({ size: 5 });
  const { data: stock } = useStock();

  const openCount =
    (report?.byStatus?.NEW ?? 0) +
    (report?.byStatus?.IN_REVIEW ?? 0) +
    (report?.byStatus?.ASSIGNED ?? 0) +
    (report?.byStatus?.IN_PROGRESS ?? 0);
  const avgHours = report?.avgResolutionSeconds
    ? (report.avgResolutionSeconds / 3600).toFixed(1)
    : '—';
  const lowStock = (stock ?? []).filter((s) => s.low);

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${user?.fullName ?? user?.email}`}
        subtitle="Сводка по системе за последние 30 дней"
      />
      <Row gutter={[16, 16]}>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="Всего заявок" value={report?.totalCount ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="Открытых" value={openCount} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="За сегодня" value={todayReport?.totalCount ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card>
            <Statistic title="Средний срок закрытия, ч" value={avgHours} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={14}>
          <Card title="Последние заявки" extra={<a onClick={() => navigate('/requests')}>Все →</a>}>
            <List
              dataSource={latest?.content ?? []}
              locale={{ emptyText: 'Заявок пока нет' }}
              renderItem={(r) => (
                <List.Item
                  onClick={() => navigate(`/requests/${r.id}`)}
                  style={{ cursor: 'pointer' }}
                >
                  <List.Item.Meta
                    title={
                      <Space>
                        <Text strong>{r.title}</Text>
                        <Tag color={STATUS_COLORS[r.status as RequestStatus]}>
                          {REQUEST_STATUS_LABELS[r.status as RequestStatus]}
                        </Tag>
                      </Space>
                    }
                    description={
                      <Space>
                        <Tag>{REQUEST_TYPE_LABELS[r.type as RequestType]}</Tag>
                        <Text type="secondary">{formatDateTime(r.createdAt)}</Text>
                        <Tooltip title={r.authorId}>
                          <Text type="secondary">от {shortId(r.authorId)}</Text>
                        </Tooltip>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card
            title="Низкий остаток на складе"
            extra={<a onClick={() => navigate('/consumables/stock')}>Склад →</a>}
          >
            {lowStock.length === 0 ? (
              <Text type="secondary">Все позиции в норме</Text>
            ) : (
              <List
                dataSource={lowStock}
                renderItem={(s) => (
                  <List.Item>
                    <List.Item.Meta
                      title={s.name}
                      description={
                        <Text type="secondary">
                          Остаток {s.stock}, порог {s.lowStockThreshold}
                        </Text>
                      }
                    />
                    <Tag color="red">Низкий</Tag>
                  </List.Item>
                )}
              />
            )}
          </Card>
        </Col>
      </Row>
    </>
  );
}
