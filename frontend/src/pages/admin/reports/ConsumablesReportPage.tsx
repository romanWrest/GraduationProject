import { useMemo, useState } from 'react';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Row,
  Select,
  Table,
  Tag,
} from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import dayjs, { type Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useConsumablesReport } from '@/features/reports/hooks';
import { useTypes } from '@/features/consumables/hooks';
import { Aggregates, CountTable } from '@/components/reports/Aggregates';
import { ExportButton } from '@/components/reports/ExportButton';
import {
  ISSUE_STATUS_COLORS,
  ISSUE_STATUS_LABELS,
  RETURN_CONDITION_COLORS,
  RETURN_CONDITION_LABELS,
} from '@/shared/constants/consumables';
import type {
  IssueStatus,
  ReturnCondition,
} from '@/features/consumables/types';
import type { ConsumableRowDto } from '@/features/reports/types';
import { formatDateTime } from '@/shared/lib/format';

interface FormValues {
  range: [Dayjs, Dayjs];
  typeId?: string;
}

export function ConsumablesReportPage() {
  const [filters, setFilters] = useState<FormValues>({
    range: [dayjs().subtract(30, 'day').startOf('day'), dayjs().endOf('day')],
  });
  const { data: types } = useTypes();
  const params = useMemo(
    () => ({
      from: filters.range[0].toISOString(),
      to: filters.range[1].toISOString(),
      typeId: filters.typeId,
    }),
    [filters],
  );
  const { data, isLoading } = useConsumablesReport(params);

  const columns: ColumnsType<ConsumableRowDto> = [
    { title: 'Жилец', dataIndex: 'residentName', ellipsis: true, render: (v) => v ?? '—' },
    { title: 'Расходник', dataIndex: 'typeName', ellipsis: true, render: (v) => v ?? '—' },
    { title: 'Кол-во', dataIndex: 'quantity', width: 100 },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 130,
      render: (s: string) => (
        <Tag color={ISSUE_STATUS_COLORS[s as IssueStatus] ?? 'default'}>
          {ISSUE_STATUS_LABELS[s as IssueStatus] ?? s}
        </Tag>
      ),
    },
    { title: 'Выдано', dataIndex: 'issuedAt', width: 150, render: formatDateTime },
    {
      title: 'Возвращено',
      dataIndex: 'returnedAt',
      width: 150,
      render: (v) => (v ? formatDateTime(v) : '—'),
    },
    {
      title: 'Состояние',
      dataIndex: 'returnCondition',
      width: 140,
      render: (v?: string) =>
        v ? (
          <Tag color={RETURN_CONDITION_COLORS[v as ReturnCondition] ?? 'default'}>
            {RETURN_CONDITION_LABELS[v as ReturnCondition] ?? v}
          </Tag>
        ) : (
          '—'
        ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Отчёт: расходники"
        extra={<ExportButton name="consumables" params={{ from: params.from, to: params.to, typeId: params.typeId }} />}
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
          <Form.Item name="typeId">
            <Select
              allowClear
              placeholder="Тип расходника"
              style={{ minWidth: 240 }}
              showSearch
              optionFilterProp="label"
              options={(types ?? []).map((t) => ({ value: t.id, label: t.name }))}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            Применить
          </Button>
        </Form>
      </Card>

      <Aggregates
        items={[
          { label: 'Всего выдач', value: data?.totalIssues ?? 0 },
          { label: 'Возвращено', value: data?.returnedCount ?? 0 },
          { label: 'Повреждено', value: data?.damagedCount ?? 0 },
          { label: 'Утеряно', value: data?.lostCount ?? 0 },
        ]}
      />

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <CountTable title="По типам" data={data?.issuesByType ?? {}} />
        </Col>
        <Col xs={24} md={12}>
          <CountTable title="По жильцам" data={data?.issuesByResident ?? {}} />
        </Col>
      </Row>

      <Card title="Детализация" style={{ marginTop: 16 }}>
        <Table<ConsumableRowDto>
          rowKey="issueId"
          columns={columns}
          dataSource={data?.rows ?? []}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true, pageSizeOptions: [10, 20, 50] }}
        />
      </Card>
    </>
  );
}
