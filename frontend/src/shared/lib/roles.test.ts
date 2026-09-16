import { describe, it, expect } from 'vitest';
import { hasAnyRole, hasRole, isExecutor } from './roles';
import { ROLES } from '@/shared/constants/roles';

describe('roles utils', () => {
  it('hasRole: true when role present', () => {
    expect(hasRole([ROLES.ADMIN, ROLES.RESIDENT], ROLES.ADMIN)).toBe(true);
  });

  it('hasRole: false when role missing', () => {
    expect(hasRole([ROLES.RESIDENT], ROLES.ADMIN)).toBe(false);
  });

  it('hasRole: false when undefined input', () => {
    expect(hasRole(undefined, ROLES.ADMIN)).toBe(false);
  });

  it('hasAnyRole: true if any role matches', () => {
    expect(hasAnyRole([ROLES.RESIDENT], [ROLES.ADMIN, ROLES.RESIDENT])).toBe(true);
  });

  it('hasAnyRole: false when none match', () => {
    expect(hasAnyRole([ROLES.RESIDENT], [ROLES.ADMIN, ROLES.PROPERTY_MANAGER])).toBe(false);
  });

  it('isExecutor: true for executor roles', () => {
    expect(isExecutor([ROLES.EXECUTOR_ELECTRIC])).toBe(true);
    expect(isExecutor([ROLES.EXECUTOR_GAS, ROLES.RESIDENT])).toBe(true);
  });

  it('isExecutor: false for non-executor roles', () => {
    expect(isExecutor([ROLES.RESIDENT, ROLES.ADMIN])).toBe(false);
  });
});
