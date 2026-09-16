import { useMemo, useState } from 'react';
import {
  App,
  Button,
  Card,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useAppliances,
  useApproveAppliance,
  useRejectAppliance,
  useRevokeAppliance,
} from '@/features/appliances/hooks';
import type {
  ApplianceDto,
  ApplianceStatus,
  ApplianceType,
  AppliancesFilters,
} from '@/features/appliances/types';
import {
  APPLIANCE_STATUS_COLORS,
  APPLIANCE_STATUS_LABELS,
  APPLIANCE_STATUS_OPTIONS,
  APPLIANCE_TYPE_LABELS,
  APPLIANCE_TYPE_OPTIONS,
} from '@/shared/constants/appliances';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';

interface FormValues {
  status?: ApplianceStatus;
  type?: ApplianceType;
  search?: string;
}

type Action = 'APPROVE' | 'REJECT' | 'REVOKE';

export function AppliancesModerationPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<FormValues>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<AppliancesFilters>({ status: 'PENDING' });
  const queryFilters = useMemo<AppliancesFilters>(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading } = useAppliances(queryFilters);

  const approve = useApproveAppliance();
  const reject = useRejectAppliance();
  const revoke = useRevokeAppliance();

  const [acting, setActing] = useState<{ appliance: ApplianceDto; action: Action } | null>(null);
  const [text, setText] = useState('');

  const onSearch = (values: FormValues) => {
    setFilters({
      status: values.status,
      type: values.type,
      search: values.search || undefined,
    });
    setPage(0);
  };

  const onConfirm = async () => {
    if (!acting) return;
    try {
      if (acting.action === 'APPROVE') {
        await approve.mutateAsync({ id: acting.appliance.id, comment: text || undefined });
        message.success('Прибор одобрен');
      } else if (acting.action === 'REJECT') {
        await reject.mutateAsync({ id: acting.appliance.id, reason: text });
        message.success('Прибор отклонён');
      } else {
        await revoke.mutateAsync({ id: acting.appliance.id, reason: text });
        message.success('Прибор снят с учёта');
      }
      setActing(null);
      setText('');
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<ApplianceDto> = [
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 180,
      render: (t: ApplianceType) => <Tag>{APPLIANCE_TYPE_LABELS[t]}</Tag>,
    },
    { title: 'Бренд', dataIndex: 'brand', width: 140, render: (v) => v ?? '—' },
    { title: 'Модель', dataIndex: 'model', ellipsis: true, render: (v) => v ?? '—' },
    {
      title: 'Мощность',
      dataIndex: 'powerWatts',
      width: 110,
      render: (v: number) => `${v} Вт`,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 150,
      render: (s: ApplianceStatus) => (
        <Tag color={APPLIANCE_STATUS_COLORS[s]}>{APPLIANCE_STATUS_LABELS[s]}</Tag>
      ),
    },
    {
      title: 'Жилец',
      dataIndex: 'residentId',
      width: 110,
      render: (v) => <Tooltip title={v}>{shortId(v)}</Tooltip>,
    },
    {
      title: 'Создан',
      dataIndex: 'createdAt',
      width: 140,
      render: formatDateTime,
    },
    {
      title: '',
      key: 'actions',
      width: 280,
      render: (_, item) => (
        <Space>
          {item.status === 'PENDING' && (
            <>
              <Button
                size="small"
                type="primary"
                onClick={() => {
                  setActing({ appliance: item, action: 'APPROVE' });
                  setText('');
                }}
              >
                Одобрить
              </Button>
              <Button
                size="small"
                danger
                onClick={() => {
                  setActing({ appliance: item, action: 'REJECT' });
                  setText('');
                }}
              >
                Отклонить
              </Button>
            </>
          )}
          {item.status === 'APPROVED' && (
            <Button
              size="small"
              danger
              onClick={() => {
                setActing({ appliance: item, action: 'REVOKE' });
                setText('');
              }}
            >
              Снять
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const pagination: TablePaginationConfig = {
    current: page + 1,
    pageSize: size,
    total: data?.totalElements ?? 0,
    showSizeChanger: true,
    pageSizeOptions: [10, 20, 50],
    showTotal: (t) => `Всего: ${t}`,
  };

  return (
    <>
      <PageHeader title="Электроприборы — модерация" />
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={form}
          layout="inline"
          onFinish={onSearch}
          initialValues={{ status: 'PENDING' }}
        >
          <Form.Item name="search">
            <Input.Search placeholder="Бренд, модель" allowClear style={{ minWidth: 220 }} />
          </Form.Item>
          <Form.Item name="status">
            <Select
              allowClear
              placeholder="Статус"
              style={{ minWidth: 200 }}
              options={APPLIANCE_STATUS_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="type">
            <Select
              allowClear
              placeholder="Тип"
              style={{ minWidth: 200 }}
              options={APPLIANCE_TYPE_OPTIONS}
            />
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
        <Table<ApplianceDto>
          rowKey="id"
          columns={columns}
          dataSource={data?.content ?? []}
          loading={isLoading}
          pagination={pagination}
          onChange={(p) => {
            setPage((p.current ?? 1) - 1);
            setSize(p.pageSize ?? 20);
          }}
        />
      </Card>

      <Modal
        open={!!acting}
        onCancel={() => {
          setActing(null);
          setText('');
        }}
        title={
          acting?.action === 'APPROVE'
            ? 'Одобрить прибор'
            : acting?.action === 'REJECT'
              ? 'Отклонить прибор'
              : 'Снять прибор с учёта'
        }
        okText={
          acting?.action === 'APPROVE'
            ? 'Одобрить'
            : acting?.action === 'REJECT'
              ? 'Отклонить'
              : 'Снять'
        }
        okButtonProps={{
          danger: acting?.action !== 'APPROVE',
          disabled: acting?.action !== 'APPROVE' && !text.trim(),
        }}
        cancelText="Отмена"
        confirmLoading={approve.isPending || reject.isPending || revoke.isPending}
        onOk={onConfirm}
      >
        <Input.TextArea
          rows={4}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder={acting?.action === 'APPROVE' ? 'Комментарий (необязательно)' : 'Причина'}
          maxLength={1024}
          showCount
        />
      </Modal>
    </>
  );
}
