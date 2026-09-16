import { Spin } from 'antd';
import { useCurrentUser } from '@/features/auth/hooks';
import { hasRole, isExecutor } from '@/shared/lib/roles';
import { ROLES } from '@/shared/constants/roles';
import { AdminDashboard } from '@/pages/admin/AdminDashboard';
import { ResidentDashboard } from '@/pages/resident/ResidentDashboard';
import { ExecutorDashboard } from '@/pages/executor/ExecutorDashboard';
import { PropertyManagerDashboard } from '@/pages/property-manager/PropertyManagerDashboard';

export function DashboardPage() {
  const { data: user, isLoading } = useCurrentUser();

  if (isLoading || !user) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (hasRole(user.roles, ROLES.ADMIN)) return <AdminDashboard />;
  if (hasRole(user.roles, ROLES.PROPERTY_MANAGER)) return <PropertyManagerDashboard />;
  if (isExecutor(user.roles)) return <ExecutorDashboard />;
  return <ResidentDashboard />;
}
