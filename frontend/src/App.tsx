import { useEffect } from 'react';
import { ConfigProvider, App as AntApp } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { RouterProvider } from 'react-router-dom';
import dayjs from 'dayjs';
import 'dayjs/locale/ru';
import { router } from './routes';
import { installInterceptors } from './shared/api/interceptors';
import { useAuthStore } from './features/auth/store';
import { refresh } from './features/auth/api';
import type { AxiosError } from 'axios';

dayjs.locale('ru');

installInterceptors();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000,
      retry: (failureCount, error) => {
        const status = (error as AxiosError | undefined)?.response?.status ?? 0;
        return failureCount < 2 && status >= 500;
      },
      refetchOnWindowFocus: false,
    },
  },
});

function AuthHydrator() {
  const setHydrated = useAuthStore((s) => s.setHydrated);
  const setTokens = useAuthStore((s) => s.setTokens);
  const storeLogout = useAuthStore((s) => s.logout);
  const refreshToken = useAuthStore((s) => s.refreshToken);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      if (!refreshToken) {
        setHydrated(true);
        return;
      }
      try {
        const res = await refresh(refreshToken);
        if (!cancelled) {
          setTokens(res.accessToken, res.refreshToken);
        }
      } catch {
        if (!cancelled) {
          storeLogout();
        }
      } finally {
        if (!cancelled) {
          setHydrated(true);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
    // hydrate one time on mount
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return null;
}

export function App() {
  return (
    <ConfigProvider locale={ruRU} theme={{ token: { colorPrimary: '#1677ff' } }}>
      <AntApp>
        <QueryClientProvider client={queryClient}>
          <AuthHydrator />
          <RouterProvider router={router} />
{/* {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />} */}
        </QueryClientProvider>
      </AntApp>
    </ConfigProvider>
  );
}
