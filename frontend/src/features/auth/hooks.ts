import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { changePassword, fetchMe, login, logout, updateMe } from './api';
import type { UpdateProfilePayload } from './api';
import { useAuthStore } from './store';
import type { LoginCredentials } from './types';

export const ME_QUERY_KEY = ['auth', 'me'] as const;

export function useCurrentUser() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const setUser = useAuthStore((s) => s.setUser);

  const query = useQuery({
    queryKey: ME_QUERY_KEY,
    queryFn: async () => {
      const user = await fetchMe();
      setUser(user);
      return user;
    },
    enabled: !!accessToken,
    staleTime: 5 * 60 * 1000,
  });

  return query;
}

export function useLogin() {
  const setTokens = useAuthStore((s) => s.setTokens);
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (credentials: LoginCredentials) => login(credentials),
    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken);
      qc.invalidateQueries({ queryKey: ME_QUERY_KEY });
    },
  });
}

export function useUpdateMe() {
  const setUser = useAuthStore((s) => s.setUser);
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateProfilePayload) => updateMe(payload),
    onSuccess: (user) => {
      setUser(user);
      qc.setQueryData(ME_QUERY_KEY, user);
    },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (payload: { oldPassword: string; newPassword: string }) => changePassword(payload),
  });
}

export function useLogout() {
  const refreshToken = useAuthStore((s) => s.refreshToken);
  const storeLogout = useAuthStore((s) => s.logout);
  const qc = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      if (refreshToken) {
        try {
          await logout(refreshToken);
        } catch {
          // ignore: даже если revoke не прошёл, выходим локально
        }
      }
    },
    onSettled: () => {
      storeLogout();
      qc.clear();
    },
  });
}
