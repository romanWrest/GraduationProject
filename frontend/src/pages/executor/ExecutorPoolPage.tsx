import { useState } from 'react';
import { App, Button, Card, DatePicker, Modal, Space, Table, Tag, Tooltip } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { usePatchRequest, usePool } from '@/features/requests/hooks';
import type { RequestStatus, RequestSummaryDto, RequestType } from '@/features/requests/types';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { useCurrentUser } from '@/features/auth/hooks';
import { handleApiError } from '@/shared/lib/handleApiError';

export function ExecutorPoolPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { data: user } = useCurrentUser();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const { data, isLoading, isFetching } = usePool(page, size);
  const patch = usePatchRequest();

  const [takeFor, setTakeFor] = useState<RequestSummaryDto | null>(null);
  const [scheduledAt, setScheduledAt] = useState<string | undefined>();

  const onTake = async () => {
    if (!takeFor || !user) return;
    try {
      await patch.mutateAsync({
        id: takeFor.id,
        payload: { action: 'ASSIGN', assigneeId: user.id, scheduledAt },
      });
      message.success('Заявка взята в работу');
      setTakeFor(null);
      setScheduledAt(undefined);
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<RequestSummaryDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (id: string) => <Tooltip title={id}>{shortId(id)}</Tooltip>,
    },
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 180,
      render: (t: RequestType) => <Tag>{REQUEST_TYPE_LABELS[t]}</Tag>,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 130,
      render: (s: RequestStatus) => <Tag color={STATUS_COLORS[s]}>{REQUEST_STATUS_LABELS[s]}</Tag>,
    },
    { title: 'Название', dataIndex: 'title', ellipsis: true },
    {
      title: 'Создана',
      dataIndex: 'createdAt',
      width: 150,
      render: (v: string) => formatDateTime(v),
    },
    {
      title: '',
      key: 'actions',
      width: 220,
      render: (_, record) => (
        <Space>
          <Button onClick={() => navigate(`/requests/${record.id}`)}>Открыть</Button>
          {record.status !== 'CANCELLED' && record.status !== 'CLOSED' && (
            <Button type="primary" onClick={() => setTakeFor(record)}>
              Взять в работу
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
    showTotal: (total) => `Всего: ${total}`,
  };

  return (
    <>
      <PageHeader title="Пул заявок" subtitle="Заявки, доступные вам по пулу" />
      <Card>
        <Table<RequestSummaryDto>
          rowKey="id"
          dataSource={data?.content ?? []}
          columns={columns}
          loading={isLoading || isFetching}
          pagination={pagination}
          onChange={(p) => {
            setPage((p.current ?? 1) - 1);
            setSize(p.pageSize ?? 20);
          }}
        />
      </Card>

      <Modal
        open={!!takeFor}
        title="Взять заявку в работу"
        onOk={onTake}
        onCancel={() => {
          setTakeFor(null);
          setScheduledAt(undefined);
        }}
        okText="Назначить себе"
        cancelText="Отмена"
        confirmLoading={patch.isPending}
      >
        <div style={{ marginBottom: 8 }}>Выберите дату и время визита (опционально):</div>
        <DatePicker
          showTime={{ format: 'HH:mm' }}
          format="DD.MM.YYYY HH:mm"
          style={{ width: '100%' }}
          onChange={(d) => setScheduledAt(d ? d.toISOString() : undefined)}
        />
      </Modal>
    </>
  );
}
