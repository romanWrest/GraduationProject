import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuthStore } from './store';

describe('auth store', () => {
  beforeEach(() => {
    window.localStorage.clear();
    useAuthStore.setState({
      accessToken: null,
      refreshToken: null,
      user: null,
      hydrated: false,
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('setTokens persists refreshToken to localStorage', () => {
    useAuthStore.getState().setTokens('access', 'refresh');
    expect(useAuthStore.getState().accessToken).toBe('access');
    expect(useAuthStore.getState().refreshToken).toBe('refresh');
    expect(window.localStorage.getItem('auth.refreshToken')).toBe('refresh');
  });

  it('logout clears tokens and localStorage', () => {
    useAuthStore.getState().setTokens('a', 'r');
    useAuthStore.getState().logout();
    expect(useAuthStore.getState().accessToken).toBeNull();
    expect(useAuthStore.getState().refreshToken).toBeNull();
    expect(useAuthStore.getState().user).toBeNull();
    expect(window.localStorage.getItem('auth.refreshToken')).toBeNull();
  });
});
