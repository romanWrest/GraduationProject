import { useState } from 'react';
import {
  App,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useMyAppliances,
  useRegisterAppliance,
} from '@/features/appliances/hooks';
import type { ApplianceDto, ApplianceStatus, ApplianceType } from '@/features/appliances/types';
import {
  APPLIANCE_STATUS_COLORS,
  APPLIANCE_STATUS_LABELS,
  APPLIANCE_TYPE_LABELS,
  APPLIANCE_TYPE_OPTIONS,
} from '@/shared/constants/appliances';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';

export function MyAppliancesPage() {
  const { message } = App.useApp();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const { data, isLoading } = useMyAppliances({ page, size });

  const register = useRegisterAppliance();
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState<{
    type: ApplianceType;
    brand: string;
    model: string;
    powerWatts: number;
    notes: string;
  }>({ type: 'KETTLE', brand: '', model: '', powerWatts: 1000, notes: '' });

  const onRegister = async () => {
    try {
      await register.mutateAsync({
        type: formData.type,
        brand: formData.brand || undefined,
        model: formData.model || undefined,
        powerWatts: formData.powerWatts,
        notes: formData.notes || undefined,
      });
      message.success('Заявка на регистрацию прибора отправлена');
      setOpen(false);
      setFormData({ type: 'KETTLE', brand: '', model: '', powerWatts: 1000, notes: '' });
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<ApplianceDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (v: string) => <Tooltip title={v}>{shortId(v)}</Tooltip>,
    },
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 200,
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
      title: 'Решение',
      dataIndex: 'decisionReason',
      ellipsis: true,
      render: (v) => v ?? '—',
    },
    {
      title: 'Создан',
      dataIndex: 'createdAt',
      width: 140,
      render: formatDateTime,
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
      <PageHeader
        title="Мои электроприборы"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setOpen(true)}>
            Зарегистрировать
          </Button>
        }
      />
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
        open={open}
        onCancel={() => setOpen(false)}
        title="Зарегистрировать прибор"
        okText="Отправить"
        cancelText="Отмена"
        confirmLoading={register.isPending}
        onOk={onRegister}
      >
        <Form layout="vertical">
          <Form.Item label="Тип" required>
            <Select
              value={formData.type}
              onChange={(v) => setFormData({ ...formData, type: v })}
              options={APPLIANCE_TYPE_OPTIONS}
            />
          </Form.Item>
          <Form.Item label="Бренд">
            <Input
              value={formData.brand}
              onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
              maxLength={128}
            />
          </Form.Item>
          <Form.Item label="Модель">
            <Input
              value={formData.model}
              onChange={(e) => setFormData({ ...formData, model: e.target.value })}
              maxLength={128}
            />
          </Form.Item>
          <Form.Item label="Мощность, Вт" required>
            <InputNumber
              min={1}
              max={5000}
              value={formData.powerWatts}
              onChange={(v) => setFormData({ ...formData, powerWatts: Number(v ?? 0) })}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={3}
              maxLength={512}
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
