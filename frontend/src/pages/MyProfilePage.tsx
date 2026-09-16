import { useEffect } from 'react';
import { Button, Card, Col, Form, Input, Row, Space, Tag, Typography, App } from 'antd';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useChangePassword, useCurrentUser, useUpdateMe } from '@/features/auth/hooks';
import { ROLE_LABELS } from '@/shared/constants/roles';
import { handleApiError } from '@/shared/lib/handleApiError';

const { Title, Text } = Typography;

const profileSchema = z.object({
  fullName: z.string().min(1, 'Введите имя').max(255),
  phone: z.string().max(64).optional().or(z.literal('')),
  email: z.string().email('Некорректный email'),
});
type ProfileForm = z.infer<typeof profileSchema>;

const passwordSchema = z
  .object({
    oldPassword: z.string().min(1, 'Введите текущий пароль'),
    newPassword: z.string().min(8, 'Минимум 8 символов'),
    confirm: z.string(),
  })
  .refine((d) => d.newPassword === d.confirm, {
    path: ['confirm'],
    message: 'Пароли не совпадают',
  });
type PasswordForm = z.infer<typeof passwordSchema>;

export function MyProfilePage() {
  const { message } = App.useApp();
  const { data: user } = useCurrentUser();
  const updateMe = useUpdateMe();
  const changePassword = useChangePassword();

  const profileForm = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { fullName: '', phone: '', email: '' },
  });

  useEffect(() => {
    if (user) {
      profileForm.reset({
        fullName: user.fullName ?? '',
        phone: user.phone ?? '',
        email: user.email ?? '',
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const passwordForm = useForm<PasswordForm>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { oldPassword: '', newPassword: '', confirm: '' },
  });

  const onSubmitProfile = async (values: ProfileForm) => {
    try {
      await updateMe.mutateAsync({
        fullName: values.fullName,
        phone: values.phone || undefined,
        email: values.email,
      });
      message.success('Профиль обновлён');
    } catch (e) {
      handleApiError<ProfileForm>(e, { setError: profileForm.setError });
    }
  };

  const onSubmitPassword = async (values: PasswordForm) => {
    try {
      await changePassword.mutateAsync({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      message.success('Пароль изменён');
      passwordForm.reset();
    } catch (e) {
      handleApiError<PasswordForm>(e, { setError: passwordForm.setError });
    }
  };

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} lg={14}>
        <Card title="Личные данные">
          <Form layout="vertical" onFinish={profileForm.handleSubmit(onSubmitProfile)}>
            <Form.Item
              label="ФИО"
              validateStatus={profileForm.formState.errors.fullName ? 'error' : ''}
              help={profileForm.formState.errors.fullName?.message}
            >
              <Controller
                name="fullName"
                control={profileForm.control}
                render={({ field }) => <Input {...field} />}
              />
            </Form.Item>
            <Form.Item
              label="Email"
              validateStatus={profileForm.formState.errors.email ? 'error' : ''}
              help={profileForm.formState.errors.email?.message}
            >
              <Controller
                name="email"
                control={profileForm.control}
                render={({ field }) => <Input {...field} />}
              />
            </Form.Item>
            <Form.Item
              label="Телефон"
              validateStatus={profileForm.formState.errors.phone ? 'error' : ''}
              help={profileForm.formState.errors.phone?.message}
            >
              <Controller
                name="phone"
                control={profileForm.control}
                render={({ field }) => <Input {...field} placeholder="+7 (___) ___-__-__" />}
              />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={updateMe.isPending}>
              Сохранить
            </Button>
          </Form>
        </Card>
      </Col>

      <Col xs={24} lg={10}>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Card title="Учётная запись">
            <Title level={5} style={{ marginTop: 0 }}>
              {user?.fullName ?? '—'}
            </Title>
            <Text type="secondary">{user?.email}</Text>
            <div style={{ marginTop: 12 }}>
              <Text>Роли:</Text>
              <div style={{ marginTop: 4 }}>
                <Space wrap>
                  {(user?.roles ?? []).map((r) => (
                    <Tag key={r} color="blue">
                      {ROLE_LABELS[r] ?? r}
                    </Tag>
                  ))}
                </Space>
              </div>
            </div>
          </Card>

          <Card title="Сменить пароль">
            <Form layout="vertical" onFinish={passwordForm.handleSubmit(onSubmitPassword)}>
              <Form.Item
                label="Текущий пароль"
                validateStatus={passwordForm.formState.errors.oldPassword ? 'error' : ''}
                help={passwordForm.formState.errors.oldPassword?.message}
              >
                <Controller
                  name="oldPassword"
                  control={passwordForm.control}
                  render={({ field }) => <Input.Password {...field} />}
                />
              </Form.Item>
              <Form.Item
                label="Новый пароль"
                validateStatus={passwordForm.formState.errors.newPassword ? 'error' : ''}
                help={passwordForm.formState.errors.newPassword?.message}
              >
                <Controller
                  name="newPassword"
                  control={passwordForm.control}
                  render={({ field }) => <Input.Password {...field} />}
                />
              </Form.Item>
              <Form.Item
                label="Повторите новый пароль"
                validateStatus={passwordForm.formState.errors.confirm ? 'error' : ''}
                help={passwordForm.formState.errors.confirm?.message}
              >
                <Controller
                  name="confirm"
                  control={passwordForm.control}
                  render={({ field }) => <Input.Password {...field} />}
                />
              </Form.Item>
              <Button type="primary" htmlType="submit" loading={changePassword.isPending}>
                Изменить пароль
              </Button>
            </Form>
          </Card>
        </Space>
      </Col>
    </Row>
  );
}
