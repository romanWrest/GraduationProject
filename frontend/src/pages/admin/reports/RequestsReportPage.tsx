import { useMemo, useState } from 'react';
import {
  Card,
  Col,
  DatePicker,
  Form,
  Row,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import dayjs, { type Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useRequestsReport } from '@/features/reports/hooks';
import { Aggregates, CountTable } from '@/components/reports/Aggregates';
import { ExportButton } from '@/components/reports/ExportButton';
import {
  REQUEST_STATUS_LABELS,
  REQUEST_STATUS_OPTIONS,
  REQUEST_TYPE_LABELS,
  REQUEST_TYPE_OPTIONS,
} from '@/shared/constants/requestTypes';
import { STATUS_COLORS } from '@/shared/constants/statusColors';
import { formatDateTime } from '@/shared/lib/format';
import type {
  RequestStatus,
  RequestType,
} from '@/features/requests/types';
import type { RequestRowDto } from '@/features/reports/types';

interface FormValues {
  range: [Dayjs, Dayjs];
  type?: RequestType;
  status?: RequestStatus;
}

export function RequestsReportPage() {
  const [filters, setFilters] = useState<FormValues>({
    range: [dayjs().subtract(30, 'day').startOf('day'), dayjs().endOf('day')],
  });

  const params = useMemo(
    () => ({
      from: filters.range[0].toISOString(),
      to: filters.range[1].toISOString(),
      type: filters.type,
      status: filters.status,
    }),
    [filters],
  );

  const { data, isLoading } = useRequestsReport(params);

  const exportParams = useMemo(
    () => ({
      from: params.from,
      to: params.to,
    }),
    [params.from, params.to],
  );

  const columns: ColumnsType<RequestRowDto> = [
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 200,
      render: (t: string) => REQUEST_TYPE_LABELS[t as RequestType] ?? t,
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 140,
      render: (s: string) => (
        <Tag color={STATUS_COLORS[s as RequestStatus] ?? 'default'}>
          {REQUEST_STATUS_LABELS[s as RequestStatus] ?? s}
        </Tag>
      ),
    },
    { title: 'Автор', dataIndex: 'authorName', ellipsis: true, render: (v) => v ?? '—' },
    { title: 'Исполнитель', dataIndex: 'assigneeName', ellipsis: true, render: (v) => v ?? '—' },
    { title: 'Комната', dataIndex: 'roomNumber', width: 120, render: (v) => v ?? '—' },
    {
      title: 'Создана',
      dataIndex: 'createdAt',
      width: 150,
      render: formatDateTime,
    },
    {
      title: 'Закрыта',
      dataIndex: 'closedAt',
      width: 150,
      render: (v) => (v ? formatDateTime(v) : '—'),
    },
    {
      title: 'Срок (часы)',
      dataIndex: 'resolutionSeconds',
      width: 130,
      render: (v?: number) => (v ? (v / 3600).toFixed(1) : '—'),
    },
  ];

  const avgHours = data?.avgResolutionSeconds ? (data.avgResolutionSeconds / 3600).toFixed(1) : '—';

  return (
    <>
      <PageHeader
        title="Отчёт: заявки"
        extra={<ExportButton name="requests" params={exportParams} />}
      />
      <Card style={{ marginBottom: 16 }}>
        <Form
          layout="inline"
          initialValues={filters}
          onFinish={(v) => setFilters(v as FormValues)}
        >
          <Form.Item name="range" required>
            <DatePicker.RangePicker format="DD.MM.YYYY" />
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
          <Space>
            <button type="submit" className="ant-btn ant-btn-primary">
              Применить
            </button>
          </Space>
        </Form>
      </Card>

      <Aggregates
        items={[
          { label: 'Всего заявок', value: data?.totalCount ?? 0 },
          { label: 'Среднее время закрытия', value: avgHours, suffix: 'ч' },
          { label: 'Уникальных авторов', value: Object.keys(data?.byAuthor ?? {}).length },
          { label: 'Уникальных исполнителей', value: Object.keys(data?.byAssignee ?? {}).length },
        ]}
      />

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <CountTable title="По статусам" data={data?.byStatus ?? {}} />
        </Col>
        <Col xs={24} md={12}>
          <CountTable title="По типам" data={data?.byType ?? {}} />
        </Col>
      </Row>

      <Card title="Детализация" style={{ marginTop: 16 }}>
        <Table<RequestRowDto>
          rowKey="requestId"
          columns={columns}
          dataSource={data?.rows ?? []}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true, pageSizeOptions: [10, 20, 50] }}
        />
      </Card>
    </>
  );
}
