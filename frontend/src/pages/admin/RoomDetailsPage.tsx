import { useState } from 'react';
import { useUsers } from '@/features/users/hooks';
import { useResidents } from '@/features/residents/hooks';
import { useMemo } from 'react';
import {
  App,
  Button,
  Card,
  Col,
  Descriptions,
  Empty,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Timeline,
  Tooltip,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useDeleteRoom,
  useRoom,
  useRoomHistory,
  useUpdateRoom,
} from '@/features/rooms/hooks';
import {
  useAddInventory,
  useDeleteInventory,
  useUpdateInventory,
  useWriteOffInventory,
} from '@/features/inventory/hooks';
import type {
  InventoryItemDto,
  InventoryState,
  InventoryType,
} from '@/features/inventory/types';
import {
  INVENTORY_STATE_COLORS,
  INVENTORY_STATE_LABELS,
  INVENTORY_STATE_OPTIONS,
  INVENTORY_TYPE_LABELS,
  INVENTORY_TYPE_OPTIONS,
} from '@/shared/constants/inventory';
import { RESIDENT_KIND_LABELS } from '@/shared/constants/residents';
import { formatDate, formatDateTime, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';
import type { ResidentDto } from '@/features/residents/types';

const { Text } = Typography;

export function RoomDetailsPage() {
  const { id = '' } = useParams<{ id: string }>();
  const { data: usersPage } = useUsers({ size: 1000 });
const { data: residentsPage } = useResidents({ size: 1000 });
  const navigate = useNavigate();
  const { message, modal } = App.useApp();
  const { data: room, isLoading } = useRoom(id);
  const { data: history } = useRoomHistory(id);

  const update = useUpdateRoom();
  const remove = useDeleteRoom();
  const addInv = useAddInventory();
  const updInv = useUpdateInventory();
  const writeOff = useWriteOffInventory();
  const delInv = useDeleteInventory();

  const [editOpen, setEditOpen] = useState(false);
  const [editForm, setEditForm] = useState({
    number: '',
    floor: 1,
    capacity: 2,
    notes: '',
  });

  const [addInvOpen, setAddInvOpen] = useState(false);
  const [newInv, setNewInv] = useState<{
    type: InventoryType;
    state: InventoryState;
    serialNumber: string;
    notes: string;
  }>({ type: 'BED', state: 'NEW', serialNumber: '', notes: '' });

  const [editInv, setEditInv] = useState<InventoryItemDto | null>(null);
  const [editInvForm, setEditInvForm] = useState({
    state: 'USED' as InventoryState,
    serialNumber: '',
    notes: '',
  });
  const residentNameMap = useMemo(() => {
  const userNameById = new Map<string, string>();
  usersPage?.content.forEach((u) => userNameById.set(u.id, u.fullName ?? u.email));

  const result = new Map<string, string>();
  residentsPage?.content.forEach((r) => {
    const name = userNameById.get(r.userId);
    if (name) result.set(r.id, name);
  });
  return result;
}, [usersPage, residentsPage]);

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }
  if (!room) {
    return <Empty description="Комната не найдена" />;
  }

  const onEditOpen = () => {
    setEditForm({
      number: room.number,
      floor: room.floor,
      capacity: room.capacity,
      notes: room.notes ?? '',
    });
    setEditOpen(true);
  };

  const onEditSave = async () => {
    try {
      await update.mutateAsync({
        id,
        payload: {
          number: editForm.number,
          floor: editForm.floor,
          capacity: editForm.capacity,
          notes: editForm.notes || undefined,
        },
      });
      message.success('Комната обновлена');
      setEditOpen(false);
    } catch (e) {
      handleApiError(e);
    }
  };

  const onDelete = () => {
    modal.confirm({
      title: 'Удалить комнату?',
      content: 'Удалить можно только пустую комнату',
      okText: 'Удалить',
      okButtonProps: { danger: true },
      cancelText: 'Отмена',
      onOk: async () => {
        try {
          await remove.mutateAsync(id);
          message.success('Комната удалена');
          navigate('/rooms');
        } catch (e) {
          handleApiError(e);
        }
      },
    });
  };

  const onAddInventory = async () => {
    try {
      await addInv.mutateAsync({
        roomId: id,
        payload: {
          type: newInv.type,
          state: newInv.state,
          serialNumber: newInv.serialNumber || undefined,
          notes: newInv.notes || undefined,
        },
      });
      message.success('Позиция добавлена');
      setAddInvOpen(false);
      setNewInv({ type: 'BED', state: 'NEW', serialNumber: '', notes: '' });
    } catch (e) {
      handleApiError(e);
    }
  };

  const onSaveInvEdit = async () => {
    if (!editInv) return;
    try {
      await updInv.mutateAsync({
        id: editInv.id,
        payload: {
          state: editInvForm.state,
          serialNumber: editInvForm.serialNumber || undefined,
          notes: editInvForm.notes || undefined,
        },
      });
      message.success('Позиция обновлена');
      setEditInv(null);
    } catch (e) {
      handleApiError(e);
    }
  };

  const onWriteOff = (item: InventoryItemDto) => {
    let reason = '';
    modal.confirm({
      title: `Списать «${INVENTORY_TYPE_LABELS[item.type]}»?`,
      content: (
        <Input.TextArea
          rows={3}
          maxLength={512}
          showCount
          placeholder="Причина списания"
          onChange={(e) => {
            reason = e.target.value;
          }}
        />
      ),
      okText: 'Списать',
      okButtonProps: { danger: true },
      cancelText: 'Отмена',
      onOk: async () => {
        if (!reason.trim()) {
          message.error('Введите причину');
          throw new Error('reason required');
        }
        try {
          await writeOff.mutateAsync({ id: item.id, reason: reason.trim() });
          message.success('Позиция списана');
        } catch (e) {
          handleApiError(e);
        }
      },
    });
  };

  const onDeleteInv = (item: InventoryItemDto) => {
    modal.confirm({
      title: 'Удалить позицию?',
      content: 'Удалять можно только списанные позиции',
      okText: 'Удалить',
      okButtonProps: { danger: true },
      cancelText: 'Отмена',
      onOk: async () => {
        try {
          await delInv.mutateAsync(item.id);
          message.success('Позиция удалена');
        } catch (e) {
          handleApiError(e);
        }
      },
    });
  };

  const residentsCols: ColumnsType<ResidentDto> = [
    {
  title: 'ФИО',
  dataIndex: 'id',
  width: 220,
  ellipsis: true,
  render: (residentId: string) => (
    <Tooltip title={residentId}>
      {residentNameMap.get(residentId) ?? shortId(residentId)}
    </Tooltip>
  ),
},

    {
      title: 'Категория',
      dataIndex: 'kind',
      width: 140,
      render: (k) => <Tag>{RESIDENT_KIND_LABELS[k as keyof typeof RESIDENT_KIND_LABELS]}</Tag>,
    },
    { title: 'Факультет', dataIndex: 'faculty', ellipsis: true },
    { title: 'Группа', dataIndex: 'studyGroup', width: 110 },
    {
      title: 'Заселён',
      dataIndex: 'enrolledAt',
      width: 130,
      render: formatDate,
    },
  ];

  const inventoryCols: ColumnsType<InventoryItemDto> = [
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 200,
      render: (v: InventoryType) => INVENTORY_TYPE_LABELS[v],
    },
    { title: 'Серийный №', dataIndex: 'serialNumber', width: 180, render: (v) => v ?? '—' },
    {
      title: 'Состояние',
      dataIndex: 'state',
      width: 130,
      render: (v: InventoryState) => (
        <Tag color={INVENTORY_STATE_COLORS[v]}>{INVENTORY_STATE_LABELS[v]}</Tag>
      ),
    },
    { title: 'Заметки', dataIndex: 'notes', ellipsis: true },
    {
      title: '',
      key: 'actions',
      width: 220,
      render: (_, item) => (
        <Space>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditInv(item);
              setEditInvForm({
                state: item.state,
                serialNumber: item.serialNumber ?? '',
                notes: item.notes ?? '',
              });
            }}
          />
          {item.state !== 'WRITTEN_OFF' && (
            <Button size="small" danger onClick={() => onWriteOff(item)}>
              Списать
            </Button>
          )}
          {item.state === 'WRITTEN_OFF' && (
            <Button
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={() => onDeleteInv(item)}
            />
          )}
        </Space>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title={`Комната ${room.number}`}
        subtitle={
          <Space>
            <Text type="secondary">Этаж {room.floor}</Text>
            <Text type="secondary">·</Text>
            <Text type="secondary">
              {room.occupied} / {room.capacity} мест
            </Text>
          </Space>
        }
        extra={
          <Space>
            <Button icon={<EditOutlined />} onClick={onEditOpen}>
              Редактировать
            </Button>
            <Button icon={<DeleteOutlined />} danger onClick={onDelete}>
              Удалить
            </Button>
            <Button onClick={() => navigate(-1)}>Назад</Button>
          </Space>
        }
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card title="Информация">
            <Descriptions column={1} size="small" labelStyle={{ width: 140 }}>

              <Descriptions.Item label="Номер">{room.number}</Descriptions.Item>
              <Descriptions.Item label="Этаж">{room.floor}</Descriptions.Item>
              <Descriptions.Item label="Вместимость">{room.capacity}</Descriptions.Item>
              <Descriptions.Item label="Заполнено">
                {room.occupied} / {room.capacity}
              </Descriptions.Item>
              <Descriptions.Item label="Заметки">
                <span style={{ whiteSpace: 'pre-wrap' }}>{room.notes ?? '—'}</span>
              </Descriptions.Item>
              <Descriptions.Item label="Создана">
                {formatDateTime(room.createdAt)}
              </Descriptions.Item>
              <Descriptions.Item label="Обновлена">
                {formatDateTime(room.updatedAt)}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={16}>
          <Card>
            <Tabs
              defaultActiveKey="residents"
              items={[
                {
                  key: 'residents',
                  label: `Жильцы (${room.residents.length})`,
                  children: (
                    <Table<ResidentDto>
                      rowKey="id"
                      size="small"
                      columns={residentsCols}
                      dataSource={room.residents}
                      pagination={false}
                      onRow={(r) => ({
                        onClick: () => navigate(`/residents/${r.id}`),
                        style: { cursor: 'pointer' },
                      })}
                    />
                  ),
                },
                {
                  key: 'inventory',
                  label: `Инвентарь (${room.inventory.length})`,
                  children: (
                    <>
                      <div style={{ marginBottom: 8 }}>
                        <Button
                          icon={<PlusOutlined />}
                          onClick={() => setAddInvOpen(true)}
                        >
                          Добавить
                        </Button>
                      </div>
                      <Table<InventoryItemDto>
                        rowKey="id"
                        size="small"
                        columns={inventoryCols}
                        dataSource={room.inventory}
                        pagination={false}
                      />
                    </>
                  ),
                },
                {
                  key: 'history',
                  label: 'История',
                  children:
                    history && history.length > 0 ? (
                      <Timeline
                        items={history.map((h) => ({
                          children: (
                            <div>
                              <Text strong>
  {residentNameMap.get(h.residentId) ?? shortId(h.residentId)}: {formatDate(h.movedInAt)}
  {' — '}
  {h.movedOutAt ? formatDate(h.movedOutAt) : 'сейчас'}
</Text>
                              {h.reason && (
                                <div style={{ color: 'rgba(0,0,0,0.45)' }}>{h.reason}</div>
                              )}
                            </div>
                          ),
                        }))}
                      />
                    ) : (
                      <Empty description="История пуста" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                    ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Modal
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        title="Редактировать комнату"
        okText="Сохранить"
        cancelText="Отмена"
        confirmLoading={update.isPending}
        onOk={onEditSave}
      >
        <Form layout="vertical">
          <Form.Item label="Номер">
            <Input
              maxLength={32}
              value={editForm.number}
              onChange={(e) => setEditForm({ ...editForm, number: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Этаж">
            <Input
              type="number"
              value={editForm.floor}
              onChange={(e) => setEditForm({ ...editForm, floor: Number(e.target.value) })}
            />
          </Form.Item>
          <Form.Item label="Вместимость">
            <Input
              type="number"
              value={editForm.capacity}
              onChange={(e) => setEditForm({ ...editForm, capacity: Number(e.target.value) })}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={2}
              maxLength={512}
              value={editForm.notes}
              onChange={(e) => setEditForm({ ...editForm, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={addInvOpen}
        onCancel={() => setAddInvOpen(false)}
        title="Добавить позицию инвентаря"
        okText="Добавить"
        cancelText="Отмена"
        confirmLoading={addInv.isPending}
        onOk={onAddInventory}
      >
        <Form layout="vertical">
          <Form.Item label="Тип">
            <Select
              value={newInv.type}
              onChange={(v) => setNewInv({ ...newInv, type: v })}
              options={INVENTORY_TYPE_OPTIONS}
            />
          </Form.Item>
          <Form.Item label="Состояние">
            <Select
              value={newInv.state}
              onChange={(v) => setNewInv({ ...newInv, state: v })}
              options={INVENTORY_STATE_OPTIONS.filter((o) => o.value !== 'WRITTEN_OFF')}
            />
          </Form.Item>
          <Form.Item label="Серийный №">
            <Input
              maxLength={128}
              value={newInv.serialNumber}
              onChange={(e) => setNewInv({ ...newInv, serialNumber: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={2}
              maxLength={512}
              value={newInv.notes}
              onChange={(e) => setNewInv({ ...newInv, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={!!editInv}
        onCancel={() => setEditInv(null)}
        title="Редактировать позицию"
        okText="Сохранить"
        cancelText="Отмена"
        confirmLoading={updInv.isPending}
        onOk={onSaveInvEdit}
      >
        <Form layout="vertical">
          <Form.Item label="Состояние">
            <Select
              value={editInvForm.state}
              onChange={(v) => setEditInvForm({ ...editInvForm, state: v })}
              options={INVENTORY_STATE_OPTIONS}
            />
          </Form.Item>
          <Form.Item label="Серийный №">
            <Input
              maxLength={128}
              value={editInvForm.serialNumber}
              onChange={(e) =>
                setEditInvForm({ ...editInvForm, serialNumber: e.target.value })
              }
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={2}
              maxLength={512}
              value={editInvForm.notes}
              onChange={(e) => setEditInvForm({ ...editInvForm, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
