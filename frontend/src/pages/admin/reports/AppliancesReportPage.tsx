import { useMemo, useState } from 'react';
import { Button, Card, Col, Form, Input, Row, Select, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import { useAppliancesReport } from '@/features/reports/hooks';
import { Aggregates, CountTable } from '@/components/reports/Aggregates';
import { ExportButton } from '@/components/reports/ExportButton';
import {
  APPLIANCE_STATUS_COLORS,
  APPLIANCE_STATUS_LABELS,
  APPLIANCE_STATUS_OPTIONS,
  APPLIANCE_TYPE_LABELS,
} from '@/shared/constants/appliances';
import type { ApplianceStatus, ApplianceType } from '@/features/appliances/types';
import type { ApplianceRowDto } from '@/features/reports/types';

interface FormValues {
  roomId?: string;
  status?: ApplianceStatus;
}

export function AppliancesReportPage() {
  const [filters, setFilters] = useState<FormValues>({});
  const params = useMemo(
    () => ({ roomId: filters.roomId, status: filters.status }),
    [filters],
  );
  const { data, isLoading } = useAppliancesReport(params);

  const exportParams = useMemo(
    () => ({ roomId: params.roomId, status: params.status }),
    [params],
  );

  const columns: ColumnsType<ApplianceRowDto> = [
    { title: 'Жилец', dataIndex: 'residentName', ellipsis: true, render: (v) => v ?? '—' },
    { title: 'Комната', dataIndex: 'roomNumber', width: 110, render: (v) => v ?? '—' },
    {
      title: 'Тип',
      dataIndex: 'type',
      width: 200,
      render: (t: string) => APPLIANCE_TYPE_LABELS[t as ApplianceType] ?? t,
    },
    { title: 'Бренд', dataIndex: 'brand', width: 140, render: (v) => v ?? '—' },
    { title: 'Модель', dataIndex: 'model', ellipsis: true, render: (v) => v ?? '—' },
    {
      title: 'Мощность',
      dataIndex: 'powerWatts',
      width: 110,
      render: (v?: number) => (v ? `${v} Вт` : '—'),
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 150,
      render: (s: string) => (
        <Tag color={APPLIANCE_STATUS_COLORS[s as ApplianceStatus] ?? 'default'}>
          {APPLIANCE_STATUS_LABELS[s as ApplianceStatus] ?? s}
        </Tag>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Отчёт: электроприборы"
        extra={<ExportButton name="appliances" params={exportParams} />}
      />
      <Card style={{ marginBottom: 16 }}>
        <Form
          layout="inline"
          initialValues={filters}
          onFinish={(v) => setFilters(v as FormValues)}
        >
          <Form.Item name="status">
            <Select
              allowClear
              placeholder="Статус"
              style={{ minWidth: 200 }}
              options={APPLIANCE_STATUS_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="roomId">
            <Input placeholder="ID комнаты (UUID)" style={{ minWidth: 280 }} allowClear />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            Применить
          </Button>
        </Form>
      </Card>

      <Aggregates
        items={[
          { label: 'Всего приборов', value: data?.totalCount ?? 0 },
          { label: 'Суммарная мощность', value: data?.totalPowerWatts ?? 0, suffix: 'Вт' },
          { label: 'Комнат с приборами', value: Object.keys(data?.totalPowerByRoom ?? {}).length },
        ]}
      />

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <CountTable title="По статусу" data={data?.countByStatus ?? {}} />
        </Col>
        <Col xs={24} md={12}>
          <CountTable title="Мощность по комнатам, Вт" data={data?.totalPowerByRoom ?? {}} />
        </Col>
      </Row>

      <Card title="Детализация" style={{ marginTop: 16 }}>
        <Table<ApplianceRowDto>
          rowKey="applianceId"
          columns={columns}
          dataSource={data?.rows ?? []}
          loading={isLoading}
          pagination={{ pageSize: 20, showSizeChanger: true, pageSizeOptions: [10, 20, 50] }}
        />
      </Card>
    </>
  );
}
