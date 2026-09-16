import { useMemo, useState } from 'react';
import { Button, Card, DatePicker, Form, Table, Tooltip } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import dayjs, { type Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useAssigneesReport } from '@/features/reports/hooks';
import { Aggregates } from '@/components/reports/Aggregates';
import { ExportButton } from '@/components/reports/ExportButton';
import { shortId } from '@/shared/lib/format';
import type { AssigneeLoadRowDto } from '@/features/reports/types';

interface FormValues {
  range: [Dayjs, Dayjs];
}

export function AssigneesReportPage() {
  const [filters, setFilters] = useState<FormValues>({
    range: [dayjs().subtract(30, 'day').startOf('day'), dayjs().endOf('day')],
  });
  const params = useMemo(
    () => ({
      from: filters.range[0].toISOString(),
      to: filters.range[1].toISOString(),
    }),
    [filters],
  );
  const { data, isLoading } = useAssigneesReport(params);

  const columns: ColumnsType<AssigneeLoadRowDto> = [
    {
      title: 'Исполнитель',
      key: 'assignee',
      render: (_, r) =>
        r.assigneeName ?? (
          <Tooltip title={r.assigneeId}>
            <span>{shortId(r.assigneeId)}</span>
          </Tooltip>
        ),
    },
    { title: 'Назначено', dataIndex: 'assignedCount', width: 130 },
    { title: 'Выполнено', dataIndex: 'completedCount', width: 130 },
    {
      title: 'Среднее (ч)',
      dataIndex: 'avgResolutionSeconds',
      width: 140,
      render: (v?: number) => (v ? (v / 3600).toFixed(1) : '—'),
    },
  ];

  return (
    <>
      <PageHeader
        title="Отчёт: загрузка исполнителей"
        extra={<ExportButton name="assignees" params={params} />}
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
          <Button type="primary" htmlType="submit">
            Применить
          </Button>
        </Form>
      </Card>

      <Aggregates
        items={[
          { label: 'Всего исполнителей', value: data?.totalAssignees ?? 0 },
          {
            label: 'Всего назначено',
            value: (data?.rows ?? []).reduce((s, r) => s + r.assignedCount, 0),
          },
          {
            label: 'Всего выполнено',
            value: (data?.rows ?? []).reduce((s, r) => s + r.completedCount, 0),
          },
        ]}
      />

      <Card title="Детализация" style={{ marginTop: 16 }}>
        <Table<AssigneeLoadRowDto>
          rowKey="assigneeId"
          columns={columns}
          dataSource={data?.rows ?? []}
          loading={isLoading}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </>
  );
}
