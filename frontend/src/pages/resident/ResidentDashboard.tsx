import { Button, Card, Col, List, Row, Space, Tag, Typography } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useCurrentUser } from '@/features/auth/hooks';
import { useMyRequests } from '@/features/requests/hooks';
import { useMyAppliances } from '@/features/appliances/hooks';
import { useMyIssues } from '@/features/consumables/hooks';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import {
  APPLIANCE_STATUS_COLORS,
  APPLIANCE_STATUS_LABELS,
  APPLIANCE_TYPE_LABELS,
} from '@/shared/constants/appliances';
import {
  ISSUE_STATUS_COLORS,
  ISSUE_STATUS_LABELS,
} from '@/shared/constants/consumables';
import { formatDateTime } from '@/shared/lib/format';
import type {
  RequestStatus,
  RequestType,
} from '@/features/requests/types';
import type {
  ApplianceStatus,
  ApplianceType,
} from '@/features/appliances/types';
import type { IssueStatus } from '@/features/consumables/types';

const { Text } = Typography;

export function ResidentDashboard() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: requests } = useMyRequests(0, 5);
  const { data: appliances } = useMyAppliances({ size: 5 });
  const { data: issues } = useMyIssues();

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${user?.fullName ?? user?.email}`}
        subtitle="Личный кабинет жильца"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/requests/new')}
          >
            Создать заявку
          </Button>
        }
      />
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card
            title="Мои заявки"
            extra={<a onClick={() => navigate('/my/requests')}>Все →</a>}
          >
            <List
              dataSource={requests?.content ?? []}
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
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card
            title="Мои электроприборы"
            extra={<a onClick={() => navigate('/my/appliances')}>Все →</a>}
          >
            <List
              dataSource={appliances?.content ?? []}
              locale={{ emptyText: 'Приборов пока нет' }}
              renderItem={(a) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <Text strong>
                          {APPLIANCE_TYPE_LABELS[a.type as ApplianceType]}
                          {a.brand ? ` · ${a.brand}` : ''}
                        </Text>
                        <Tag color={APPLIANCE_STATUS_COLORS[a.status as ApplianceStatus]}>
                          {APPLIANCE_STATUS_LABELS[a.status as ApplianceStatus]}
                        </Tag>
                      </Space>
                    }
                    description={<Text type="secondary">{a.powerWatts} Вт</Text>}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card
            title="Мои расходники"
            extra={<a onClick={() => navigate('/my/consumables')}>Все →</a>}
          >
            <List
              dataSource={issues ?? []}
              locale={{ emptyText: 'Расходников пока нет' }}
              renderItem={(i) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <Text strong>{i.typeName ?? '—'}</Text>
                        <Tag color={ISSUE_STATUS_COLORS[i.status as IssueStatus]}>
                          {ISSUE_STATUS_LABELS[i.status as IssueStatus]}
                        </Tag>
                      </Space>
                    }
                    description={
                      <Text type="secondary">
                        Кол-во {i.quantity} · выдано {formatDateTime(i.issuedAt)}
                      </Text>
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
