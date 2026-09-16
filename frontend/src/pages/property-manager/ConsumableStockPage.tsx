import { Card, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import { useStock } from '@/features/consumables/hooks';
import type { ConsumableUnit, StockItemDto } from '@/features/consumables/types';
import { UNIT_LABELS } from '@/shared/constants/consumables';

export function ConsumableStockPage() {
  const { data, isLoading } = useStock();

  const columns: ColumnsType<StockItemDto> = [
    { title: 'Название', dataIndex: 'name', ellipsis: true },
    {
      title: 'Ед. изм.',
      dataIndex: 'unit',
      width: 120,
      render: (u: ConsumableUnit) => UNIT_LABELS[u],
    },
    { title: 'Остаток', dataIndex: 'stock', width: 120 },
    { title: 'Порог', dataIndex: 'lowStockThreshold', width: 120 },
    {
      title: 'Состояние',
      dataIndex: 'low',
      width: 160,
      render: (low: boolean) =>
        low ? <Tag color="red">Ниже порога</Tag> : <Tag color="green">Норма</Tag>,
    },
  ];

  return (
    <>
      <PageHeader title="Склад расходников" subtitle="Текущие остатки по типам" />
      <Card>
        <Table<StockItemDto>
          rowKey="typeId"
          columns={columns}
          dataSource={data ?? []}
          loading={isLoading}
          pagination={false}
        />
      </Card>
    </>
  );
}
