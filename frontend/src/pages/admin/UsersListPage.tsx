import { useMemo, useState } from 'react';
import {
  App,
  Button,
  Card,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table/interface';
import { PageHeader } from '@/components/common/PageHeader';
import {
  useCreateUser,
  useDeactivateUser,
  useSetUserRoles,
  useSetUserStatus,
  useUsers,
} from '@/features/users/hooks';
import type { UsersFilters } from '@/features/users/types';
import { ROLE_LABELS, ROLES, type Role } from '@/shared/constants/roles';
import { formatDate, shortId } from '@/shared/lib/format';
import { handleApiError } from '@/shared/lib/handleApiError';
import { useCurrentUser } from '@/features/auth/hooks';
import type { UserDto } from '@/features/auth/types';

const { Paragraph, Text } = Typography;

const ROLE_OPTIONS = (Object.keys(ROLE_LABELS) as Role[]).map((value) => ({
  value,
  label: ROLE_LABELS[value],
}));

interface FormValues {
  q?: string;
  role?: Role;
  active?: 'all' | 'active' | 'inactive';
}

export function UsersListPage() {
  const { message } = App.useApp();
  const { data: me } = useCurrentUser();
  const [form] = Form.useForm<FormValues>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<UsersFilters>({});

  const queryFilters = useMemo<UsersFilters>(
    () => ({ ...filters, page, size }),
    [filters, page, size],
  );
  const { data, isLoading } = useUsers(queryFilters);

  const create = useCreateUser();
  const setStatus = useSetUserStatus();
  const setRoles = useSetUserRoles();
  const deactivate = useDeactivateUser();

  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState<{
    email: string;
    fullName: string;
    phone: string;
    roles: Role[];
  }>({ email: '', fullName: '', phone: '', roles: [ROLES.RESIDENT] });
  const [createdInfo, setCreatedInfo] = useState<{ email: string; password: string } | null>(null);

  const [rolesEdit, setRolesEdit] = useState<UserDto | null>(null);
  const [editRoles, setEditRoles] = useState<Role[]>([]);

  const onSearch = (values: FormValues) => {
    setFilters({
      q: values.q || undefined,
      role: values.role,
      active:
        values.active === 'active'
          ? true
          : values.active === 'inactive'
            ? false
            : undefined,
    });
    setPage(0);
  };

  const onCreate = async () => {
    try {
      const created = await create.mutateAsync({
        email: createForm.email,
        fullName: createForm.fullName,
        phone: createForm.phone || undefined,
        roles: createForm.roles,
      });
      setCreateOpen(false);
      setCreatedInfo({ email: created.user.email, password: created.temporaryPassword });
      setCreateForm({ email: '', fullName: '', phone: '', roles: [ROLES.RESIDENT] });
    } catch (e) {
      handleApiError(e);
    }
  };

  const onSaveRoles = async () => {
    if (!rolesEdit) return;
    try {
      await setRoles.mutateAsync({ id: rolesEdit.id, roles: editRoles });
      message.success('Роли обновлены');
      setRolesEdit(null);
    } catch (e) {
      handleApiError(e);
    }
  };

  const onToggleStatus = async (u: UserDto) => {
    try {
      await setStatus.mutateAsync({ id: u.id, active: !u.active });
      message.success(u.active ? 'Деактивирован' : 'Активирован');
    } catch (e) {
      handleApiError(e);
    }
  };

  const onDeactivate = async (u: UserDto) => {
    try {
      await deactivate.mutateAsync(u.id);
      message.success('Деактивирован');
    } catch (e) {
      handleApiError(e);
    }
  };

  const columns: ColumnsType<UserDto> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
      render: (v: string) => <Tooltip title={v}>{shortId(v)}</Tooltip>,
    },
    { title: 'Email', dataIndex: 'email', ellipsis: true },
    { title: 'ФИО', dataIndex: 'fullName', ellipsis: true },
    { title: 'Телефон', dataIndex: 'phone', width: 160, render: (v) => v ?? '—' },
    {
      title: 'Роли',
      dataIndex: 'roles',
      render: (roles: Role[]) => (
        <Space wrap size={[4, 4]}>
          {roles.map((r) => (
            <Tag key={r}>{ROLE_LABELS[r]}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Активен',
      dataIndex: 'active',
      width: 100,
      render: (active: boolean, u: UserDto) => (
        <Switch
          checked={active}
          onChange={() => onToggleStatus(u)}
          disabled={me?.id === u.id}
        />
      ),
    },
    {
      title: 'Создан',
      dataIndex: 'createdAt',
      width: 130,
      render: formatDate,
    },
    {
      title: '',
      key: 'actions',
      width: 220,
      render: (_, u) => (
        <Space>
          <Button
            size="small"
            onClick={() => {
              setRolesEdit(u);
              setEditRoles(u.roles);
            }}
          >
            Роли
          </Button>
          <Popconfirm
            title="Деактивировать пользователя?"
            okText="Да"
            cancelText="Отмена"
            onConfirm={() => onDeactivate(u)}
            disabled={me?.id === u.id}
          >
            <Button size="small" danger disabled={me?.id === u.id}>
              Удалить
            </Button>
          </Popconfirm>
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
    showTotal: (t) => `Всего: ${t}`,
  };

  return (
    <>
      <PageHeader
        title="Пользователи"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            Создать
          </Button>
        }
      />
      <Card style={{ marginBottom: 16 }}>
        <Form form={form} layout="inline" onFinish={onSearch} initialValues={{ active: 'all' }}>
          <Form.Item name="q">
            <Input.Search placeholder="Email или ФИО" allowClear style={{ minWidth: 240 }} />
          </Form.Item>
          <Form.Item name="role">
            <Select allowClear placeholder="Роль" style={{ minWidth: 220 }} options={ROLE_OPTIONS} />
          </Form.Item>
          <Form.Item name="active">
            <Select
              style={{ minWidth: 160 }}
              options={[
                { value: 'all', label: 'Все' },
                { value: 'active', label: 'Активные' },
                { value: 'inactive', label: 'Неактивные' },
              ]}
            />
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
        <Table<UserDto>
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
        open={createOpen}
        title="Создать пользователя"
        okText="Создать"
        cancelText="Отмена"
        confirmLoading={create.isPending}
        onCancel={() => setCreateOpen(false)}
        onOk={onCreate}
        okButtonProps={{
          disabled:
            !createForm.email.trim() || !createForm.fullName.trim() || createForm.roles.length === 0,
        }}
      >
        <Form layout="vertical">
          <Form.Item label="Email" required>
            <Input
              value={createForm.email}
              onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="ФИО" required>
            <Input
              value={createForm.fullName}
              onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Телефон">
            <Input
              value={createForm.phone}
              onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
            />
          </Form.Item>
          <Form.Item label="Роли" required>
            <Select
              mode="multiple"
              value={createForm.roles}
              onChange={(v) => setCreateForm({ ...createForm, roles: v })}
              options={ROLE_OPTIONS}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={!!createdInfo}
        title="Пользователь создан"
        onCancel={() => setCreatedInfo(null)}
        onOk={() => setCreatedInfo(null)}
        okText="Закрыть"
        cancelButtonProps={{ style: { display: 'none' } }}
      >
        <Paragraph>
          Учётная запись <Text strong>{createdInfo?.email}</Text> создана. Передайте пользователю
          временный пароль:
        </Paragraph>
        <Paragraph
          copyable
          style={{
            background: '#f5f5f5',
            padding: 12,
            fontFamily: 'monospace',
            fontSize: 16,
          }}
        >
          {createdInfo?.password}
        </Paragraph>
        <Text type="secondary">Сохраните его — после закрытия окна пароль увидеть нельзя.</Text>
      </Modal>

      <Modal
        open={!!rolesEdit}
        title="Изменить роли"
        onCancel={() => setRolesEdit(null)}
        onOk={onSaveRoles}
        okText="Сохранить"
        cancelText="Отмена"
        confirmLoading={setRoles.isPending}
        okButtonProps={{ disabled: editRoles.length === 0 }}
      >
        <Paragraph>
          Пользователь: <Text strong>{rolesEdit?.fullName}</Text> ({rolesEdit?.email})
        </Paragraph>
        <Select
          mode="multiple"
          value={editRoles}
          onChange={setEditRoles}
          options={ROLE_OPTIONS}
          style={{ width: '100%' }}
        />
      </Modal>
    </>
  );
}
