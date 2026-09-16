import { Card, Col, List, Row, Space, Statistic, Tag, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useCurrentUser } from '@/features/auth/hooks';
import { useStock, useTypes } from '@/features/consumables/hooks';
import { usePool } from '@/features/requests/hooks';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime } from '@/shared/lib/format';
import type {
  RequestStatus,
  RequestType,
} from '@/features/requests/types';

const { Text } = Typography;

export function PropertyManagerDashboard() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: types } = useTypes();
  const { data: stock } = useStock();
  const { data: pool } = usePool(0, 5);

  const totalStock = (types ?? []).reduce((s, t) => s + t.stock, 0);
  const lowStock = (stock ?? []).filter((s) => s.low);

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${user?.fullName ?? user?.email}`}
        subtitle="Расходники и связанные заявки"
      />
      <Row gutter={[16, 16]}>
        <Col xs={12} md={8}>
          <Card>
            <Statistic title="Типов расходников" value={types?.length ?? 0} />
          </Card>
        </Col>
        <Col xs={12} md={8}>
          <Card>
            <Statistic title="Общий остаток (шт.+компл.)" value={totalStock} />
          </Card>
        </Col>
        <Col xs={12} md={8}>
          <Card>
            <Statistic title="Низкий остаток" value={lowStock.length} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card
            title="Низкий остаток"
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
        <Col xs={24} lg={12}>
          <Card
            title="Заявки в пуле"
            extra={<a onClick={() => navigate('/requests/pool')}>Все →</a>}
          >
            <List
              dataSource={pool?.content ?? []}
              locale={{ emptyText: 'Пул пуст' }}
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
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </>
  );
}
