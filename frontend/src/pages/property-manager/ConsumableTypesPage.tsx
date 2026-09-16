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
  Space,
  Table,
} from 'antd';
import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useAdjustStock,
  useCreateType,
  useTypes,
  useUpdateType,
} from '@/features/consumables/hooks';
import type {
  ConsumableTypeDto,
  ConsumableUnit,
} from '@/features/consumables/types';
import { UNIT_LABELS, UNIT_OPTIONS } from '@/shared/constants/consumables';
import { handleApiError } from '@/shared/lib/handleApiError';

export function ConsumableTypesPage() {
  const { message } = App.useApp();
  const { data, isLoading } = useTypes();
  const create = useCreateType();
  const update = useUpdateType();
  const stockMut = useAdjustStock();

  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState<{
    name: string;
    unit: ConsumableUnit;
    stock: number;
    lowStockThreshold: number;
  }>({ name: '', unit: 'PIECE', stock: 0, lowStockThreshold: 10 });

  const [editing, setEditing] = useState<ConsumableTypeDto | null>(null);
  const [editForm, setEditForm] = useState({ name: '', lowStockThreshold: 10 });

  const [stockFor, setStockFor] = useState<ConsumableTypeDto | null>(null);
  const [stockDelta, setStockDelta] = useState(0);
  const [stockReason, setStockReason] = useState('');

  const onCreate = async () => {
    try {
      await create.mutateAsync(createForm);
      message.success('Тип создан');
      setCreateOpen(false);
      setCreateForm({ name: '', unit: 'PIECE', stock: 0, lowStockThreshold: 10 });
    } catch (e) {
      handleApiError(e);
    }
  };
  const onEdit = async () => {
    if (!editing) return;
    try {
      await update.mutateAsync({ id: editing.id, payload: editForm });
      message.success('Сохранено');
      setEditing(null);
    } catch (e) {
      handleApiError(e);
    }
  };
  const onAdjust = async () => {
    if (!stockFor) return;
    if (!stockDelta || !stockReason.trim()) return;
    try {
      await stockMut.mutateAsync({
        id: stockFor.id,
        payload: { delta: stockDelta, reason: stockReason.trim() },
      });
      message.success('Остаток изменён');
      setStockFor(null);
      setStockDelta(0);
      setStockReason('');
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<ConsumableTypeDto> = [
    { title: 'Название', dataIndex: 'name', ellipsis: true },
    {
      title: 'Ед. изм.',
      dataIndex: 'unit',
      width: 120,
      render: (u: ConsumableUnit) => UNIT_LABELS[u],
    },
    { title: 'Остаток', dataIndex: 'stock', width: 100 },
    { title: 'Порог', dataIndex: 'lowStockThreshold', width: 100 },
    {
      title: '',
      key: 'actions',
      width: 280,
      render: (_, t) => (
        <Space>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditing(t);
              setEditForm({ name: t.name, lowStockThreshold: t.lowStockThreshold });
            }}
          >
            Редактировать
          </Button>
          <Button
            size="small"
            onClick={() => {
              setStockFor(t);
              setStockDelta(0);
              setStockReason('');
            }}
          >
            Остаток
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Типы расходников"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            Создать
          </Button>
        }
      />
      <Card>
        <Table<ConsumableTypeDto>
          rowKey="id"
          columns={columns}
          dataSource={data ?? []}
          loading={isLoading}
          pagination={false}
        />
      </Card>

      <Modal
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        title="Новый тип"
        okText="Создать"
        cancelText="Отмена"
        confirmLoading={create.isPending}
        onOk={onCreate}
        okButtonProps={{ disabled: !createForm.name.trim() }}
      >
        <Form layout="vertical">
          <Form.Item label="Название" required>
            <Input
              maxLength={128}
              value={createForm.name}
              onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Единица измерения" required>
            <Select
              value={createForm.unit}
              onChange={(v) => setCreateForm({ ...createForm, unit: v })}
              options={UNIT_OPTIONS}
            />
          </Form.Item>
          <Form.Item label="Стартовый остаток">
            <InputNumber
              min={0}
              style={{ width: '100%' }}
              value={createForm.stock}
              onChange={(v) => setCreateForm({ ...createForm, stock: Number(v ?? 0) })}
            />
          </Form.Item>
          <Form.Item label="Порог низкого остатка">
            <InputNumber
              min={0}
              style={{ width: '100%' }}
              value={createForm.lowStockThreshold}
              onChange={(v) =>
                setCreateForm({ ...createForm, lowStockThreshold: Number(v ?? 0) })
              }
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={!!editing}
        onCancel={() => setEditing(null)}
        title={`Редактировать «${editing?.name}»`}
        okText="Сохранить"
        cancelText="Отмена"
        confirmLoading={update.isPending}
        onOk={onEdit}
      >
        <Form layout="vertical">
          <Form.Item label="Название">
            <Input
              maxLength={128}
              value={editForm.name}
              onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Порог низкого остатка">
            <InputNumber
              min={0}
              style={{ width: '100%' }}
              value={editForm.lowStockThreshold}
              onChange={(v) =>
                setEditForm({ ...editForm, lowStockThreshold: Number(v ?? 0) })
              }
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={!!stockFor}
        onCancel={() => setStockFor(null)}
        title={`Изменить остаток «${stockFor?.name}»`}
        okText="Применить"
        cancelText="Отмена"
        confirmLoading={stockMut.isPending}
        onOk={onAdjust}
        okButtonProps={{ disabled: !stockDelta || !stockReason.trim() }}
      >
        <Form layout="vertical">
          <Form.Item label="Дельта (положительное — пополнение, отрицательное — списание)" required>
            <InputNumber
              style={{ width: '100%' }}
              value={stockDelta}
              onChange={(v) => setStockDelta(Number(v ?? 0))}
            />
          </Form.Item>
          <Form.Item label="Причина" required>
            <Input
              maxLength={255}
              value={stockReason}
              onChange={(e) => setStockReason(e.target.value)}
            />
          </Form.Item>
          <div style={{ color: 'rgba(0,0,0,0.45)' }}>
            Текущий остаток: {stockFor?.stock} {stockFor && UNIT_LABELS[stockFor.unit]}
          </div>
        </Form>
      </Modal>
    </>
  );
}
