import { Card, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import { useMyIssues } from '@/features/consumables/hooks';
import type {
  ConsumableIssueDto,
  IssueStatus,
  ReturnCondition,
} from '@/features/consumables/types';
import {
  ISSUE_STATUS_COLORS,
  ISSUE_STATUS_LABELS,
  RETURN_CONDITION_COLORS,
  RETURN_CONDITION_LABELS,
} from '@/shared/constants/consumables';
import { formatDateTime } from '@/shared/lib/format';

const { Paragraph } = Typography;

export function MyConsumablesPage() {
  const { data, isLoading } = useMyIssues();

  const columns: ColumnsType<ConsumableIssueDto> = [
    { title: 'Расходник', dataIndex: 'typeName', ellipsis: true },
    { title: 'Кол-во', dataIndex: 'quantity', width: 100 },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 140,
      render: (s: IssueStatus) => <Tag color={ISSUE_STATUS_COLORS[s]}>{ISSUE_STATUS_LABELS[s]}</Tag>,
    },
    { title: 'Выдано', dataIndex: 'issuedAt', width: 160, render: formatDateTime },
    {
      title: 'Возвращено',
      dataIndex: 'returnedAt',
      width: 160,
      render: (v) => (v ? formatDateTime(v) : '—'),
    },
    {
      title: 'Состояние',
      dataIndex: 'returnCondition',
      width: 140,
      render: (c?: ReturnCondition) =>
        c ? <Tag color={RETURN_CONDITION_COLORS[c]}>{RETURN_CONDITION_LABELS[c]}</Tag> : '—',
    },
    { title: 'Заметки', dataIndex: 'notes', ellipsis: true, render: (v) => v ?? '—' },
  ];

  return (
    <>
      <PageHeader
        title="Мои расходники"
        subtitle={
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            Полученные комплекты белья, покрывал и др.
          </Paragraph>
        }
      />
      <Card>
        <Table<ConsumableIssueDto>
          rowKey="id"
          columns={columns}
          dataSource={data ?? []}
          loading={isLoading}
          pagination={false}
        />
      </Card>
    </>
  );
}
