import { useState } from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { HeaderBar } from './HeaderBar';

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sidebar collapsed={collapsed} onCollapse={setCollapsed} />
      <Layout>
        <HeaderBar />
        <Layout.Content style={{ padding: 24, background: '#f5f5f5' }}>
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  );
}
