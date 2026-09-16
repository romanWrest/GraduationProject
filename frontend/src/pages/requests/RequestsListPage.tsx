import { useMemo, useState } from 'react';
import { useUsers } from '@/features/users/hooks';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs, { type Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useRequests } from '@/features/requests/hooks';
import type {
  RequestFilters,
  RequestStatus,
  RequestSummaryDto,
  RequestType,
} from '@/features/requests/types';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_STATUS_OPTIONS,
  REQUEST_TYPE_LABELS,
  REQUEST_TYPE_OPTIONS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { useCurrentUser } from '@/features/auth/hooks';
import { hasAnyRole } from '@/shared/lib/roles';
import { ROLES } from '@/shared/constants/roles';

interface FormValues {
  type?: RequestType;
  status?: RequestStatus;
  search?: string;
  range?: [Dayjs, Dayjs];
}

export function RequestsListPage() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const canCreate = hasAnyRole(user?.roles, [ROLES.ADMIN, ROLES.RESIDENT]);

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [form] = Form.useForm<FormValues>();
  const [filters, setFilters] = useState<RequestFilters>({});

  const queryFilters: RequestFilters = useMemo(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading, refetch, isFetching } = useRequests(queryFilters);

const { data: usersPage } = useUsers({ size: 1000 });
const userMap = useMemo(() => {
  const m = new Map<string, string>();
  usersPage?.content.forEach((u) => m.set(u.id, u.fullName ?? u.email));
  return m;
}, [usersPage]);

  const onSearch = (values: FormValues) => {
    setFilters({
      type: values.type,
      status: values.status,
      search: values.search || undefined,
      from: values.range?.[0]?.startOf('day').toISOString(),
      to: values.range?.[1]?.endOf('day').toISOString(),
    });
    setPage(0);
  };

  const onReset = () => {
    form.resetFields();
    setFilters({});
    setPage(0);
  };

  const columns: ColumnsType<RequestSummaryDto> = [
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 180,
      render: (type: RequestType) => <Tag>{REQUEST_TYPE_LABELS[type]}</Tag>,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 140,
      render: (status: RequestStatus) => (
        <Tag color={STATUS_COLORS[status]}>{REQUEST_STATUS_LABELS[status]}</Tag>
      ),
    },
    { title: 'Название', dataIndex: 'title', ellipsis: true },
    {
  title: 'Автор',
  dataIndex: 'authorId',
  width: 200,
  ellipsis: true,
  render: (id?: string) =>
    id ? <Tooltip title={id}>{userMap.get(id) ?? shortId(id)}</Tooltip> : '—',
},
{
  title: 'Исполнитель',
  dataIndex: 'assigneeId',
  width: 200,
  ellipsis: true,
  render: (id?: string) =>
    id ? <Tooltip title={id}>{userMap.get(id) ?? shortId(id)}</Tooltip> : '—',
},
    {
      title: 'Создана',
      dataIndex: 'createdAt',
      width: 150,
      render: (v: string) => formatDateTime(v),
    },
  ];

  const pagination: TablePaginationConfig = {
    current: page + 1,
    pageSize: size,
    total: data?.totalElements ?? 0,
    showSizeChanger: true,
    pageSizeOptions: [10, 20, 50],
    showTotal: (total) => `Всего: ${total}`,
  };

  return (
    <>
      <PageHeader
        title="Заявки"
        extra={
          canCreate && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate('/requests/new')}
            >
              Создать заявку
            </Button>
          )
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={onSearch}
          style={{ rowGap: 8, columnGap: 8, flexWrap: 'wrap' }}
        >
          <Form.Item name="search" style={{ minWidth: 220 }}>
            <Input.Search placeholder="Поиск по названию/описанию" allowClear />
          </Form.Item>
          <Form.Item name="type">
            <Select
              allowClear
              placeholder="Тип"
              style={{ minWidth: 200 }}
              options={REQUEST_TYPE_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="status">
            <Select
              allowClear
              placeholder="Статус"
              style={{ minWidth: 180 }}
              options={REQUEST_STATUS_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="range">
            <DatePicker.RangePicker
              format="DD.MM.YYYY"
              presets={[
                { label: 'Сегодня', value: [dayjs().startOf('day'), dayjs().endOf('day')] },
                {
                  label: 'Последние 7 дней',
                  value: [dayjs().subtract(7, 'day'), dayjs()],
                },
                {
                  label: 'Последние 30 дней',
                  value: [dayjs().subtract(30, 'day'), dayjs()],
                },
              ]}
            />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              Применить
            </Button>
            <Button onClick={onReset}>Сбросить</Button>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()} loading={isFetching} />
          </Space>
        </Form>
      </Card>
      <Card>
        <Table<RequestSummaryDto>
          rowKey="id"
          dataSource={data?.content ?? []}
          columns={columns}
          loading={isLoading}
          pagination={pagination}
          onChange={(p) => {
            setPage((p.current ?? 1) - 1);
            setSize(p.pageSize ?? 20);
          }}
          onRow={(record) => ({
            onClick: () => navigate(`/requests/${record.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Card>
    </>
  );
}
