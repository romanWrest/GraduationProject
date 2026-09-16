import { useEffect } from 'react';
import { Button, Card, Form, Input, Typography } from 'antd';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useLogin } from '@/features/auth/hooks';
import { useAuthStore } from '@/features/auth/store';
import { handleApiError } from '@/shared/lib/handleApiError';

const { Title, Text } = Typography;

const schema = z.object({
  email: z.string().min(1, 'Введите email').email('Некорректный email'),
  password: z.string().min(1, 'Введите пароль'),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const accessToken = useAuthStore((s) => s.accessToken);
  const login = useLogin();

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', password: '' },
  });

  const from = (location.state as { from?: { pathname: string } } | null)?.from?.pathname ?? '/';

  useEffect(() => {
    if (accessToken) navigate(from, { replace: true });
  }, [accessToken, from, navigate]);

  if (accessToken) {
    return <Navigate to={from} replace />;
  }

  const onSubmit = async (values: FormValues) => {
    try {
      await login.mutateAsync(values);
      navigate(from, { replace: true });
    } catch (error) {
      handleApiError<FormValues>(error, { setError });
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f0f2f5',
      }}
    >
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ marginBottom: 4 }}>
            СУО ДГТУ
          </Title>
          <Text type="secondary">Система управления общежитием</Text>
        </div>
        <Form layout="vertical" onFinish={handleSubmit(onSubmit)} disabled={isSubmitting}>
          <Form.Item
            label="Email"
            validateStatus={errors.email ? 'error' : ''}
            help={errors.email?.message}
          >
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <Input {...field} autoComplete="username" placeholder="user@example.com" />
              )}
            />
          </Form.Item>
          <Form.Item
            label="Пароль"
            validateStatus={errors.password ? 'error' : ''}
            help={errors.password?.message}
          >
            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <Input.Password {...field} autoComplete="current-password" />
              )}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={login.isPending} block>
            Войти
          </Button>
        </Form>
      </Card>
    </div>
  );
}
