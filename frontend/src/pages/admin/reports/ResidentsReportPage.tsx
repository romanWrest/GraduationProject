import { useMemo, useState } from 'react';
import { Button, Card, Col, DatePicker, Form, Row, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import dayjs, { type Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import { useResidentsReport } from '@/features/reports/hooks';
import { Aggregates, CountTable } from '@/components/reports/Aggregates';
import { ExportButton } from '@/components/reports/ExportButton';
import { RESIDENT_KIND_LABELS } from '@/shared/constants/residents';
import { formatDate } from '@/shared/lib/format';
import type { ResidentRowDto } from '@/features/reports/types';
import type { ResidentKind } from '@/features/residents/types';

interface FormValues {
  asOf: Dayjs;
}

export function ResidentsReportPage() {
  const [filters, setFilters] = useState<FormValues>({ asOf: dayjs() });
  const params = useMemo(
    () => ({ asOf: filters.asOf.format('YYYY-MM-DD') }),
    [filters],
  );
  const { data, isLoading } = useResidentsReport(params);

  const columns: ColumnsType<ResidentRowDto> = [
    { title: 'ФИО', dataIndex: 'fullName', ellipsis: true, render: (v) => v ?? '—' },
    {
      title: 'Категория',
      dataIndex: 'kind',
      width: 150,
      render: (k: string) => (
        <Tag>{RESIDENT_KIND_LABELS[k as ResidentKind] ?? k}</Tag>
      ),
    },
    { title: 'Факультет', dataIndex: 'faculty', ellipsis: true },
    { title: 'Группа', dataIndex: 'studyGroup', width: 110 },
    { title: 'Подразделение', dataIndex: 'department', ellipsis: true },
    { title: 'Комната', dataIndex: 'roomNumber', width: 110, render: (v) => v ?? '—' },
    { title: 'Заселён', dataIndex: 'enrolledAt', width: 130, render: formatDate },
  ];

  return (
    <>
      <PageHeader
        title="Отчёт: проживающие"
        extra={<ExportButton name="residents" params={params} />}
      />
      <Card style={{ marginBottom: 16 }}>
        <Form
          layout="inline"
          initialValues={filters}
          onFinish={(v) => setFilters(v as FormValues)}
        >
          <Form.Item name="asOf" label="На дату" required>
            <DatePicker format="DD.MM.YYYY" />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            Применить
          </Button>
        </Form>
      </Card>

      <Aggregates
        items={[
          { label: 'Всего проживающих', value: data?.totalCount ?? 0 },
          { label: 'Категорий', value: Object.keys(data?.byKind ?? {}).length },
          { label: 'Факультетов', value: Object.keys(data?.byFaculty ?? {}).length },
          { label: 'Комнат', value: Object.keys(data?.byRoom ?? {}).length },
        ]}
      />

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={8}>
          <CountTable title="По категориям" data={data?.byKind ?? {}} />
        </Col>
        <Col xs={24} md={8}>
          <CountTable title="По факультетам" data={data?.byFaculty ?? {}} />
        </Col>
        <Col xs={24} md={8}>
          <CountTable title="По комнатам" data={data?.byRoom ?? {}} />
        </Col>
      </Row>

      <Card title="Список" style={{ marginTop: 16 }}>
        <Table<ResidentRowDto>
          rowKey="residentId"
          columns={columns}
          dataSource={data?.rows ?? []}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true, pageSizeOptions: [10, 20, 50] }}
        />
      </Card>
    </>
  );
}
