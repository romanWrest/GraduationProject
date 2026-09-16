import { useMemo, useState } from 'react';
import {
  App,
  AutoComplete,
  Button,
  Card,
  DatePicker,
  Form,
  InputNumber,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import type { Dayjs } from 'dayjs';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useIssue,
  useIssues,
  useReturnIssue,
  useTypes,
} from '@/features/consumables/hooks';
import type {
  ConsumableIssueDto,
  IssueStatus,
  IssuesFilters,
  ReturnCondition,
} from '@/features/consumables/types';
import {
  ISSUE_STATUS_COLORS,
  ISSUE_STATUS_LABELS,
  ISSUE_STATUS_OPTIONS,
  RETURN_CONDITION_COLORS,
  RETURN_CONDITION_LABELS,
  RETURN_CONDITION_OPTIONS,
} from '@/shared/constants/consumables';
import { useResidents } from '@/features/residents/hooks';
import { useDebounce } from '@/shared/hooks/useDebounce';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';

interface FormValues {
  status?: IssueStatus;
  range?: [Dayjs, Dayjs];
  typeId?: string;
}

export function IssuanceJournalPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<FormValues>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<IssuesFilters>({});
  const queryFilters = useMemo<IssuesFilters>(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading } = useIssues(queryFilters);
  const { data: types } = useTypes();

  const issue = useIssue();
  const returnMut = useReturnIssue();

  const [issueOpen, setIssueOpen] = useState(false);
  const [residentSearch, setResidentSearch] = useState('');
  const debouncedResidentSearch = useDebounce(residentSearch, 300);
  const { data: residents } = useResidents({
    search: debouncedResidentSearch || undefined,
    active: true,
    size: 20,
  });

  const [issueForm, setIssueForm] = useState<{
    residentId: string;
    typeId: string;
    quantity: number;
    notes: string;
  }>({ residentId: '', typeId: '', quantity: 1, notes: '' });

  const [returnFor, setReturnFor] = useState<ConsumableIssueDto | null>(null);
  const [returnForm, setReturnForm] = useState<{
    condition: ReturnCondition;
    notes: string;
  }>({ condition: 'OK', notes: '' });

  const onFilter = (values: FormValues) => {
    setFilters({
      status: values.status,
      typeId: values.typeId,
      from: values.range?.[0]?.startOf('day').toISOString(),
      to: values.range?.[1]?.endOf('day').toISOString(),
    });
    setPage(0);
  };

  const onIssue = async () => {
    try {
      await issue.mutateAsync({
        residentId: issueForm.residentId,
        typeId: issueForm.typeId,
        quantity: issueForm.quantity,
        notes: issueForm.notes || undefined,
      });
      message.success('Выдача зарегистрирована');
      setIssueOpen(false);
      setIssueForm({ residentId: '', typeId: '', quantity: 1, notes: '' });
    } catch (e) {
      handleApiError(e);
    }
  };

  const onReturn = async () => {
    if (!returnFor) return;
    try {
      await returnMut.mutateAsync({
        id: returnFor.id,
        payload: {
          condition: returnForm.condition,
          notes: returnForm.notes || undefined,
        },
      });
      message.success('Возврат принят');
      setReturnFor(null);
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<ConsumableIssueDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (v: string) => <Tooltip title={v}>{shortId(v)}</Tooltip>,
    },
    {
      title: 'Жилец',
      dataIndex: 'residentId',
      width: 110,
      render: (v: string) => <Tooltip title={v}>{shortId(v)}</Tooltip>,
    },
    { title: 'Расходник', dataIndex: 'typeName', ellipsis: true },
    { title: 'Кол-во', dataIndex: 'quantity', width: 100 },
    {
      title: 'Статус',
      dataIndex: 'status',
      width: 130,
      render: (s: IssueStatus) => <Tag color={ISSUE_STATUS_COLORS[s]}>{ISSUE_STATUS_LABELS[s]}</Tag>,
    },
    { title: 'Выдано', dataIndex: 'issuedAt', width: 150, render: formatDateTime },
    { title: 'Возвращено', dataIndex: 'returnedAt', width: 150, render: (v) => (v ? formatDateTime(v) : '—') },
    {
      title: 'Состояние',
      dataIndex: 'returnCondition',
      width: 140,
      render: (c?: ReturnCondition) =>
        c ? <Tag color={RETURN_CONDITION_COLORS[c]}>{RETURN_CONDITION_LABELS[c]}</Tag> : '—',
    },
    {
      title: '',
      key: 'actions',
      width: 160,
      render: (_, item) =>
        item.status === 'ISSUED' ? (
          <Button
            size="small"
            onClick={() => {
              setReturnFor(item);
              setReturnForm({ condition: 'OK', notes: '' });
            }}
          >
            Принять возврат
          </Button>
        ) : null,
    },
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
        title="Журнал выдач"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setIssueOpen(true)}>
            Новая выдача
          </Button>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form form={form} layout="inline" onFinish={onFilter}>
          <Form.Item name="status">
            <Select
              allowClear
              placeholder="Статус"
              style={{ minWidth: 180 }}
              options={ISSUE_STATUS_OPTIONS}
            />
          </Form.Item>
          <Form.Item name="typeId">
            <Select
              allowClear
              placeholder="Тип расходника"
              style={{ minWidth: 220 }}
              showSearch
              optionFilterProp="label"
              options={(types ?? []).map((t) => ({ value: t.id, label: t.name }))}
            />
          </Form.Item>
          <Form.Item name="range">
            <DatePicker.RangePicker format="DD.MM.YYYY" />
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
        <Table<ConsumableIssueDto>
          rowKey="id"
          columns={columns}
          dataSource={data?.content ?? []}
          loading={isLoading}
          pagination={pagination}
          onChange={(p) => {
            setPage((p.current ?? 1) - 1);
            setSize(p.pageSize ?? 20);
          }}
        />
      </Card>

      <Modal
        open={issueOpen}
        onCancel={() => setIssueOpen(false)}
        title="Новая выдача"
        okText="Выдать"
        cancelText="Отмена"
        confirmLoading={issue.isPending}
        onOk={onIssue}
        okButtonProps={{
          disabled:
            !issueForm.residentId || !issueForm.typeId || issueForm.quantity < 1,
        }}
      >
        <Form layout="vertical">
          <Form.Item label="Жилец" required>
            <AutoComplete
              value={issueForm.residentId}
              showSearch
              placeholder="Поиск жильца"
              onSearch={setResidentSearch}
              filterOption={false}
              options={(residents?.content ?? []).map((r) => ({
                value: r.id,
                label: `${r.faculty ?? ''} ${r.studyGroup ?? ''} (${r.roomNumber ?? '—'})`.trim() || r.id,
              }))}
              onSelect={(v) => setIssueForm({ ...issueForm, residentId: v })}
              onChange={(v) => setIssueForm({ ...issueForm, residentId: v })}
            />
          </Form.Item>
          <Form.Item label="Тип расходника" required>
            <Select
              value={issueForm.typeId || undefined}
              onChange={(v) => setIssueForm({ ...issueForm, typeId: v })}
              showSearch
              optionFilterProp="label"
              options={(types ?? []).map((t) => ({
                value: t.id,
                label: `${t.name} (остаток ${t.stock})`,
              }))}
            />
          </Form.Item>
          <Form.Item label="Количество" required>
            <InputNumber
              min={1}
              style={{ width: '100%' }}
              value={issueForm.quantity}
              onChange={(v) => setIssueForm({ ...issueForm, quantity: Number(v ?? 1) })}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={2}
              maxLength={512}
              value={issueForm.notes}
              onChange={(e) => setIssueForm({ ...issueForm, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={!!returnFor}
        onCancel={() => setReturnFor(null)}
        title="Принять возврат"
        okText="Принять"
        cancelText="Отмена"
        confirmLoading={returnMut.isPending}
        onOk={onReturn}
      >
        <Form layout="vertical">
          <Form.Item label="Состояние" required>
            <Select
              value={returnForm.condition}
              onChange={(v) => setReturnForm({ ...returnForm, condition: v })}
              options={RETURN_CONDITION_OPTIONS}
            />
          </Form.Item>
          <Form.Item label="Заметки">
            <Input.TextArea
              rows={3}
              maxLength={512}
              value={returnForm.notes}
              onChange={(e) => setReturnForm({ ...returnForm, notes: e.target.value })}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
