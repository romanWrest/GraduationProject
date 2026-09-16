import { Layout, Avatar, Dropdown, Space, Badge, Button, Typography } from 'antd';
import { BellOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useCurrentUser, useLogout } from '@/features/auth/hooks';
import { useUnreadCount } from '@/features/notifications/hooks';

const { Header } = Layout;
const { Text } = Typography;

export function HeaderBar() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: unread } = useUnreadCount();
  const logout = useLogout();

  const onLogout = async () => {
    await logout.mutateAsync();
    navigate('/login', { replace: true });
  };

  return (
    <Header
      style={{
        background: '#fff',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        borderBottom: '1px solid #f0f0f0',
      }}
    >
      <Space size="middle">
        <Badge count={unread?.count ?? 0} size="small">
          <Button
            type="text"
            icon={<BellOutlined style={{ fontSize: 18 }} />}
            onClick={() => navigate('/notifications')}
          />
        </Badge>
        <Dropdown
          menu={{
            items: [
              {
                key: 'profile',
                icon: <UserOutlined />,
                label: 'Профиль',
                onClick: () => navigate('/profile'),
              },
              { type: 'divider' },
              {
                key: 'logout',
                icon: <LogoutOutlined />,
                label: 'Выйти',
                onClick: onLogout,
              },
            ],
          }}
          placement="bottomRight"
        >
          <Space style={{ cursor: 'pointer' }}>
            <Avatar icon={<UserOutlined />} />
            <Text>{user?.fullName ?? user?.email ?? '—'}</Text>
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
}
