import { Card, Col, List, Row, Space, Tag, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useCurrentUser } from '@/features/auth/hooks';
import { usePool, useRequests } from '@/features/requests/hooks';
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

export function ExecutorDashboard() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: pool } = usePool(0, 5);
  const { data: assigned } = useRequests({
    assigneeId: user?.id,
    size: 5,
  });

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${user?.fullName ?? user?.email}`}
        subtitle="Заявки в вашем пуле и назначенные вам"
      />
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Мой пул" extra={<a onClick={() => navigate('/requests/pool')}>Все →</a>}>
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
        <Col xs={24} lg={12}>
          <Card title="Назначены мне" extra={<a onClick={() => navigate('/requests')}>Все →</a>}>
            <List
              dataSource={assigned?.content ?? []}
              locale={{ emptyText: 'Назначений пока нет' }}
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
