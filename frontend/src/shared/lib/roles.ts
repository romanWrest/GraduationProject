import type { Role } from '@/shared/constants/roles';
import { EXECUTOR_ROLES } from '@/shared/constants/roles';

export function hasRole(userRoles: Role[] | undefined, role: Role): boolean {
  if (!userRoles) return false;
  return userRoles.includes(role);
}

export function hasAnyRole(userRoles: Role[] | undefined, roles: Role[]): boolean {
  if (!userRoles) return false;
  return roles.some((r) => userRoles.includes(r));
}

export function isExecutor(userRoles: Role[] | undefined): boolean {
  return hasAnyRole(userRoles, EXECUTOR_ROLES);
}
