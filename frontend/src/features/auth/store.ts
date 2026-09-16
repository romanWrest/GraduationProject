import { create } from 'zustand';
import type { UserDto } from './types';

const REFRESH_TOKEN_KEY = 'auth.refreshToken';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserDto | null;
  hydrated: boolean;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: UserDto | null) => void;
  setHydrated: (hydrated: boolean) => void;
  logout: () => void;
}

function readRefreshToken(): string | null {
  if (typeof window === 'undefined') return null;
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

function writeRefreshToken(token: string | null): void {
  if (typeof window === 'undefined') return;
  if (token) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
  } else {
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: readRefreshToken(),
  user: null,
  hydrated: false,
  setTokens: (accessToken, refreshToken) => {
    writeRefreshToken(refreshToken);
    set({ accessToken, refreshToken });
  },
  setUser: (user) => set({ user }),
  setHydrated: (hydrated) => set({ hydrated }),
  logout: () => {
    writeRefreshToken(null);
    set({ accessToken: null, refreshToken: null, user: null });
  },
}));
