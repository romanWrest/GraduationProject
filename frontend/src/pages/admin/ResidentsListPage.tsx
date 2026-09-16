import { useMemo, useState } from 'react';
import { useUsers } from '@/features/users/hooks';
import { Button, Card, Form, Input, Select, Space, Table, Tag, Tooltip } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useResidents } from '@/features/residents/hooks';
import type { ResidentDto, ResidentKind, ResidentsFilters } from '@/features/residents/types';
import {
  RESIDENT_KIND_LABELS,
  RESIDENT_KIND_OPTIONS,
} from '@/shared/constants/residents';
import { formatDate, shortId } from '@/shared/lib/format';

interface FormValues {
  kind?: ResidentKind;
  active?: 'all' | 'active' | 'inactive';
  search?: string;
  faculty?: string;
}

export function ResidentsListPage() {
  const navigate = useNavigate();
const [form] = Form.useForm<FormValues>();
const [page, setPage] = useState(0);
const [size, setSize] = useState(20);
const [filters, setFilters] = useState<ResidentsFilters>({ active: true });

const queryFilters = useMemo<ResidentsFilters>(
  () => ({ ...filters, page, size }),
  [filters, page, size],
);

const { data, isLoading } = useResidents(queryFilters);

const { data: usersPage } = useUsers({ size: 1000 });
const userMap = useMemo(() => {
  const m = new Map<string, string>();
  usersPage?.content.forEach((u) => m.set(u.id, u.fullName ?? u.email));
  return m;
}, [usersPage]);


  const onSubmit = (values: FormValues) => {
    setFilters({
      kind: values.kind,
      faculty: values.faculty,
      search: values.search || undefined,
      active:
        values.active === 'active'
          ? true
          : values.active === 'inactive'
            ? false
            : undefined,
    });
    setPage(0);
  };

  const columns: ColumnsType<ResidentDto> = [
    {
  title: 'ФИО',
  dataIndex: 'userId',
  width: 220,
  ellipsis: true,
  render: (uid: string) => (
    <Tooltip title={uid}>{userMap.get(uid) ?? shortId(uid)}</Tooltip>
  ),
},
    {
      title: 'Категория',
      dataIndex: 'kind',
      width: 150,
      render: (k: ResidentKind) => <Tag>{RESIDENT_KIND_LABELS[k]}</Tag>,
    },
    { title: 'Факультет', dataIndex: 'faculty', ellipsis: true },
    { title: 'Группа', dataIndex: 'studyGroup', width: 110 },
    {
      title: 'Комната',
      dataIndex: 'roomNumber',
      width: 110,
      render: (v?: string) => v ?? '—',
    },
    {
      title: 'Заселён',
      dataIndex: 'enrolledAt',
      width: 130,
      render: formatDate,
    },
    {
      title: 'Выселен',
      dataIndex: 'evictedAt',
      width: 130,
      render: (v?: string) => (v ? formatDate(v) : '—'),
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
        title="Жильцы"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/residents/new')}
          >
            Заселить
          </Button>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={onSubmit}
          initialValues={{ active: 'active' }}
        >
          <Form.Item name="search">
            <Input.Search placeholder="Поиск" allowClear style={{ minWidth: 240 }} />
          </Form.Item>
          <Form.Item name="kind">
            <Select
              allowClear
              placeholder="Категория"
              style={{ minWidth: 180 }}
              options={RESIDENT_KIND_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="faculty">
            <Input placeholder="Факультет" style={{ minWidth: 180 }} />
          </Form.Item>
          <Form.Item name="active">
            <Select
              style={{ minWidth: 160 }}
              options={[
                { value: 'active', label: 'Активные' },
                { value: 'inactive', label: 'Выселенные' },
                { value: 'all', label: 'Все' },
              ]}
            />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              Применить
            </Button>
            <Button
              onClick={() => {
                form.resetFields();
                setFilters({ active: true });
                setPage(0);
              }}
            >
              Сбросить
            </Button>
          </Space>
        </Form>
      </Card>
      <Card>
        <Table<ResidentDto>
          rowKey="id"
          columns={columns}
          dataSource={data?.content ?? []}
          loading={isLoading}
          pagination={pagination}
          onChange={(p) => {
            setPage((p.current ?? 1) - 1);
            setSize(p.pageSize ?? 20);
          }}
          onRow={(r) => ({
            onClick: () => navigate(`/residents/${r.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Card>
    </>
  );
}
