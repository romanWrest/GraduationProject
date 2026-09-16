import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useCurrentUser } from '@/features/auth/hooks';
import { hasAnyRole } from '@/shared/lib/roles';
import type { Role } from '@/shared/constants/roles';

interface Props {
  allowedRoles: Role[];
  children: ReactNode;
}

export function RoleRoute({ allowedRoles, children }: Props) {
  const { data: user } = useCurrentUser();
  if (!user) return null;
  if (!hasAnyRole(user.roles, allowedRoles)) {
    return <Navigate to="/forbidden" replace />;
  }
  return <>{children}</>;
}
