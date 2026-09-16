import { useMemo, useState } from 'react';
import {
  App,
  Button,
  Card,
  Form,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tooltip,
  Input,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useCreateRoom, useRooms } from '@/features/rooms/hooks';
import type { RoomDto, RoomsFilters } from '@/features/rooms/types';
import { handleApiError } from '@/shared/lib/handleApiError';
import { shortId } from '@/shared/lib/format';

interface FormValues {
  floor?: number;
  capacity?: number;
  hasFreeBeds?: 'all' | 'free';
}

export function RoomsListPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [form] = Form.useForm<FormValues>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<RoomsFilters>({});
  const queryFilters = useMemo<RoomsFilters>(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading } = useRooms(queryFilters);

  const create = useCreateRoom();
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState<{
    number: string;
    floor: number;
    capacity: number;
    notes: string;
  }>({ number: '', floor: 1, capacity: 2, notes: '' });

  const onSubmit = (values: FormValues) => {
    setFilters({
      floor: values.floor,
      capacity: values.capacity,
      hasFreeBeds: values.hasFreeBeds === 'free' ? true : undefined,
    });
    setPage(0);
  };

  const onCreate = async () => {
    try {
      const created = await create.mutateAsync({
        number: createForm.number,
        floor: createForm.floor,
        capacity: createForm.capacity,
        notes: createForm.notes || undefined,
      });
      message.success('Комната создана');
      setCreateOpen(false);
      navigate(`/rooms/${created.id}`);
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<RoomDto> = [
    { title: 'Номер', dataIndex: 'number', width: 120 },
    { title: 'Этаж', dataIndex: 'floor', width: 80 },
    { title: 'Вместимость', dataIndex: 'capacity', width: 130 },
    {
      title: 'Заполнено',
      key: 'occupancy',
      width: 130,
      render: (_, r) => `${r.occupied} / ${r.capacity}`,
    },
    { title: 'Заметки', dataIndex: 'notes', ellipsis: true },
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
        title="Комнаты"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            Создать
          </Button>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form form={form} layout="inline" onFinish={onSubmit}>
          <Form.Item name="floor">
            <InputNumber placeholder="Этаж" min={-5} max={100} style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="capacity">
            <InputNumber placeholder="Мест" min={1} max={20} style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="hasFreeBeds">
            <Select
              placeholder="Свободные"
              allowClear
              style={{ width: 180 }}
              options={[
                { value: 'all', label: 'Все' },
                { value: 'free', label: 'Со свободными местами' },
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
        <Table<RoomDto>
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
            onClick: () => navigate(`/rooms/${r.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Card>

      <Modal
        open={createOpen}
        title="Создать комнату"
        okText="Создать"
        cancelText="Отмена"
        confirmLoading={create.isPending}
        onCancel={() => setCreateOpen(false)}
        onOk={onCreate}
        okButtonProps={{ disabled: !createForm.number.trim() }}
      >
        <Form layout="vertical">
          <Form.Item label="Номер" required>
            <Input
              maxLength={32}
              value={createForm.number}
              onChange={(e) => setCreateForm({ ...createForm, number: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Этаж" required>
            <InputNumber
              min={-5}
              max={100}
              value={createForm.floor}
              onChange={(v) => setCreateForm({ ...createForm, floor: Number(v ?? 1) })}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item label="Вместимость" required>
            <InputNumber
              min={1}
              max={20}
              value={createForm.capacity}
              onChange={(v) => setCreateForm({ ...createForm, capacity: Number(v ?? 1) })}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={2}
              maxLength={512}
              value={createForm.notes}
              onChange={(e) => setCreateForm({ ...createForm, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
