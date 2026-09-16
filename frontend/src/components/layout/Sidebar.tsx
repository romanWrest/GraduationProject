import { useMemo } from 'react';
import { Layout, Menu } from 'antd';
import type { MenuProps } from 'antd';
import {
  AppstoreOutlined,
  BellOutlined,
  DashboardOutlined,
  FileTextOutlined,
  HomeOutlined,
  InboxOutlined,
  ProfileOutlined,
  SafetyOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { useCurrentUser } from '@/features/auth/hooks';
import { ROLES, type Role } from '@/shared/constants/roles';
import { hasAnyRole, hasRole, isExecutor } from '@/shared/lib/roles';

const { Sider } = Layout;

type Item = NonNullable<MenuProps['items']>[number];

function buildItems(roles: Role[]): Item[] {
  const items: Item[] = [
    { key: '/dashboard', icon: <DashboardOutlined />, label: 'Дашборд' },
  ];

  if (hasAnyRole(roles, [ROLES.ADMIN, ROLES.RESIDENT, ROLES.PROPERTY_MANAGER])) {
    items.push({ key: '/requests', icon: <FileTextOutlined />, label: 'Заявки' });
  }
  if (isExecutor(roles) || hasRole(roles, ROLES.PROPERTY_MANAGER)) {
    items.push({ key: '/requests/pool', icon: <InboxOutlined />, label: 'Пул заявок' });
  }
  if (hasRole(roles, ROLES.ADMIN)) {
    items.push(
      { key: '/rooms', icon: <HomeOutlined />, label: 'Комнаты' },
      { key: '/residents', icon: <TeamOutlined />, label: 'Жильцы' },
      { key: '/users', icon: <UserOutlined />, label: 'Пользователи' },
      { key: '/appliances', icon: <ThunderboltOutlined />, label: 'Приборы' },
      { key: '/reports', icon: <SafetyOutlined />, label: 'Отчёты' },
    );
  }
  if (hasRole(roles, ROLES.RESIDENT)) {
    items.push(
      { key: '/my/appliances', icon: <ThunderboltOutlined />, label: 'Мои приборы' },
      { key: '/my/consumables', icon: <AppstoreOutlined />, label: 'Мои расходники' },
    );
  }
  if (hasRole(roles, ROLES.PROPERTY_MANAGER)) {
    items.push(
      { key: '/consumables/stock', icon: <AppstoreOutlined />, label: 'Склад' },
      { key: '/consumables/issues', icon: <ProfileOutlined />, label: 'Журнал выдач' },
      { key: '/consumables/types', icon: <ProfileOutlined />, label: 'Типы расходников' },
    );
  }
  items.push(
    { key: '/notifications', icon: <BellOutlined />, label: 'Уведомления' },
    { key: '/profile', icon: <UserOutlined />, label: 'Профиль' },
  );
  return items;
}

interface Props {
  collapsed: boolean;
  onCollapse: (collapsed: boolean) => void;
}

export function Sidebar({ collapsed, onCollapse }: Props) {
  const navigate = useNavigate();
  const location = useLocation();
  const { data: user } = useCurrentUser();

  const items = useMemo(() => buildItems(user?.roles ?? []), [user?.roles]);

  const selectedKey =
    items
      .map((i) => i?.key as string)
      .filter(Boolean)
      .find((k) => location.pathname === k || location.pathname.startsWith(`${k}/`)) ??
    '/dashboard';

  return (
    <Sider collapsible collapsed={collapsed} onCollapse={onCollapse} width={240} theme="light">
      <div
        style={{
          height: 56,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 600,
          fontSize: collapsed ? 14 : 16,
          color: '#1677ff',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        {collapsed ? 'СУО' : 'СУО ДГТУ'}
      </div>
      <Menu
        mode="inline"
        items={items}
        selectedKeys={[selectedKey]}
        onClick={(e) => navigate(e.key)}
        style={{ borderRight: 0 }}
      />
    </Sider>
  );
}
