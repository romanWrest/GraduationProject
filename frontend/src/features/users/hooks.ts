import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createUser,
  deactivateUser,
  getUser,
  listUsers,
  setUserRoles,
  setUserStatus,
  updateUser,
} from './api';
import type { CreateUserPayload, UpdateUserPayload, UsersFilters } from './types';
import type { Role } from '@/shared/constants/roles';

const USERS_KEY = ['users'] as const;

export function useUsers(filters: UsersFilters) {
  return useQuery({
    queryKey: [...USERS_KEY, 'list', filters],
    queryFn: () => listUsers(filters),
    placeholderData: (prev) => prev,
  });
}

export function useUser(id: string | undefined) {
  return useQuery({
    queryKey: [...USERS_KEY, 'detail', id],
    queryFn: () => getUser(id!),
    enabled: !!id,
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateUserPayload) => createUser(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateUserPayload }) =>
      updateUser(id, payload),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: [...USERS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: [...USERS_KEY, 'list'] });
    },
  });
}

export function useSetUserStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) => setUserStatus(id, active),
    onSuccess: () => qc.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useSetUserRoles() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, roles }: { id: string; roles: Role[] }) => setUserRoles(id, roles),
    onSuccess: () => qc.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useDeactivateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deactivateUser(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: USERS_KEY }),
  });
}
