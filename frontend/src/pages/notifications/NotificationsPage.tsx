import { useMemo, useState } from 'react';
import {
  App,
  Badge,
  Button,
  Card,
  DatePicker,
  Empty,
  Form,
  List,
  Select,
  Space,
  Switch,
  Tag,
  Typography,
} from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useMarkAllRead,
  useMarkRead,
  useNotifications,
} from '@/features/notifications/hooks';
import type {
  NotificationsFilters,
  NotificationType,
} from '@/features/notifications/types';
import { NOTIFICATION_TYPE_LABELS, NOTIFICATION_TYPE_OPTIONS } from '@/shared/constants/notifications';
import { formatDateTime } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';
import { useNavigate } from 'react-router-dom';

const { Text, Paragraph } = Typography;

interface FormValues {
  unreadOnly?: boolean;
  type?: NotificationType;
  range?: [Dayjs, Dayjs];
}

export function NotificationsPage() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [form] = Form.useForm<FormValues>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<NotificationsFilters>({});
  const queryFilters = useMemo<NotificationsFilters>(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading } = useNotifications(queryFilters);
  const markRead = useMarkRead();
  const markAllRead = useMarkAllRead();

  const onFilter = (values: FormValues) => {
    setFilters({
      unreadOnly: values.unreadOnly || undefined,
      type: values.type,
      from: values.range?.[0]?.startOf('day').toISOString(),
      to: values.range?.[1]?.endOf('day').toISOString(),
    });
    setPage(0);
  };

  const onMarkAllRead = async () => {
    try {
      const res = await markAllRead.mutateAsync();
      message.success(`Отмечено как прочитанных: ${res.affected}`);
    } catch (e) {
      handleApiError(e);
    }
  };

  const handleClick = async (id: string, isRead: boolean, payload?: Record<string, unknown>) => {
    if (!isRead) {
      try {
        await markRead.mutateAsync(id);
      } catch {
        // no-op
      }
    }
    if (payload && typeof payload['requestId'] === 'string') {
      navigate(`/requests/${payload['requestId']}`);
    }
  };

  return (
    <>
      <PageHeader
        title="Уведомления"
        extra={
          <Space>
            <Button onClick={() => navigate('/notifications/settings')}>Настройки</Button>
            <Button
              icon={<CheckOutlined />}
              onClick={onMarkAllRead}
              loading={markAllRead.isPending}
            >
              Прочитать все
            </Button>
          </Space>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form form={form} layout="inline" onFinish={onFilter}>
          <Form.Item name="unreadOnly" valuePropName="checked" label="Только непрочитанные">
            <Switch />
          </Form.Item>
          <Form.Item name="type">
            <Select
              allowClear
              placeholder="Тип"
              style={{ minWidth: 240 }}
              options={NOTIFICATION_TYPE_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="range">
            <DatePicker.RangePicker format="DD.MM.YYYY" />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              Применить
            </Button>
            <Button
              onClick={() => {
                form.resetFields();
                setFilters({});
                setPage(0);
              }}
            >
              Сбросить
            </Button>
          </Space>
        </Form>
      </Card>
      <Card>
        {isLoading ? null : data?.content.length === 0 ? (
          <Empty description="Нет уведомлений" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        ) : (
          <List
            dataSource={data?.content ?? []}
            pagination={{
              current: page + 1,
              pageSize: size,
              total: data?.totalElements ?? 0,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 50],
              onChange: (p, ps) => {
                setPage(p - 1);
                setSize(ps);
              },
            }}
            renderItem={(n) => (
              <List.Item
                key={n.id}
                onClick={() => handleClick(n.id, n.read, n.payload)}
                style={{ cursor: 'pointer', background: n.read ? undefined : '#e6f4ff' }}
              >
                <List.Item.Meta
                  avatar={<Badge dot status={n.read ? 'default' : 'processing'} />}
                  title={
                    <Space>
                      <Text strong>{n.title}</Text>
                      <Tag>{NOTIFICATION_TYPE_LABELS[n.type]}</Tag>
                      {!n.read && <Tag color="blue">Новое</Tag>}
                    </Space>
                  }
                  description={
                    <div>
                      <Paragraph
                        style={{ whiteSpace: 'pre-wrap', marginBottom: 4 }}
                        ellipsis={{ rows: 3, expandable: true, symbol: 'ещё' }}
                      >
                        {n.body}
                      </Paragraph>
                      <Text type="secondary">{formatDateTime(n.createdAt)}</Text>
                      {n.readAt && (
                        <Text type="secondary"> · прочитано {formatDateTime(n.readAt)}</Text>
                      )}
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </>
  );
}
