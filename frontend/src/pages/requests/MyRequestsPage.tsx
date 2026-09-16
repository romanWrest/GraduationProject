import { useState } from 'react';
import { Button, Card, Space, Table, Tag } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyRequests } from '@/features/requests/hooks';
import type { RequestStatus, RequestSummaryDto, RequestType } from '@/features/requests/types';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_TYPE_LABELS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime} from '@/shared/lib/format';

export function MyRequestsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const { data, isLoading } = useMyRequests(page, size);

  const columns: ColumnsType<RequestSummaryDto> = [

    {
      title: 'Тип',
      dataIndex: 'type',
      width: 180,
      render: (t: RequestType) => <Tag>{REQUEST_TYPE_LABELS[t]}</Tag>,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 140,
      render: (s: RequestStatus) => <Tag color={STATUS_COLORS[s]}>{REQUEST_STATUS_LABELS[s]}</Tag>,
    },
    { title: 'Название', dataIndex: 'title', ellipsis: true },
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
  };

  return (
    <>
      <PageHeader
        title="Мои заявки"
        extra={
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate('/requests/new')}
            >
              Создать заявку
            </Button>
          </Space>
        }
      />
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
          onRow={(r) => ({
            onClick: () => navigate(`/requests/${r.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Card>
    </>
  );
}
