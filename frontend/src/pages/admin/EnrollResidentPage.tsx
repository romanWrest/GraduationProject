import { useState } from 'react';
import {
  App,
  AutoComplete,
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Select,
} from 'antd';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import dayjs from 'dayjs';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useEnrollResident } from '@/features/residents/hooks';
import { useUsers } from '@/features/users/hooks';
import { useRooms } from '@/features/rooms/hooks';
import {
  RESIDENT_KIND_OPTIONS,
} from '@/shared/constants/residents';
import { handleApiError } from '@/shared/lib/handleApiError';
import { useDebounce } from '@/shared/hooks/useDebounce';
import type { ResidentKind } from '@/features/residents/types';

const schema = z.object({
  userId: z.string().uuid('Выберите пользователя'),
  kind: z.enum(['STUDENT', 'TEACHER', 'STAFF_LIVING']) satisfies z.ZodType<ResidentKind>,
  roomId: z.string().uuid('Выберите комнату'),
  enrolledAt: z.string().min(1, 'Дата обязательна'),
  faculty: z.string().max(128).optional().or(z.literal('')),
  studyGroup: z.string().max(64).optional().or(z.literal('')),
  department: z.string().max(128).optional().or(z.literal('')),
  phone: z.string().max(32).optional().or(z.literal('')),
  contactInfo: z.string().max(512).optional().or(z.literal('')),
});

type FormValues = z.infer<typeof schema>;

export function EnrollResidentPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const enroll = useEnrollResident();

  const [userSearch, setUserSearch] = useState('');
  const debouncedUserSearch = useDebounce(userSearch, 300);
  const { data: users } = useUsers({
    q: debouncedUserSearch || undefined,
    active: true,
    size: 20,
  });

  const { data: rooms } = useRooms({ hasFreeBeds: true, size: 200 });

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      userId: '',
      kind: 'STUDENT',
      roomId: '',
      enrolledAt: dayjs().format('YYYY-MM-DD'),
      faculty: '',
      studyGroup: '',
      department: '',
      phone: '',
      contactInfo: '',
    },
  });

  const onSubmit = async (values: FormValues) => {
    try {
      const created = await enroll.mutateAsync({
        userId: values.userId,
        kind: values.kind,
        roomId: values.roomId,
        enrolledAt: values.enrolledAt,
        faculty: values.faculty || undefined,
        studyGroup: values.studyGroup || undefined,
        department: values.department || undefined,
        phone: values.phone || undefined,
        contactInfo: values.contactInfo || undefined,
      });
      message.success('Жилец заселён');
      navigate(`/residents/${created.id}`);
    } catch (e) {
      handleApiError<FormValues>(e, { setError });
    }
  };

  return (
    <>
      <PageHeader title="Заселение жильца" />
      <Card>
        <Form layout="vertical" onFinish={handleSubmit(onSubmit)} disabled={enroll.isPending}>
          <Form.Item
            label="Пользователь"
            required
            validateStatus={errors.userId ? 'error' : ''}
            help={errors.userId?.message}
          >
            <Controller
              name="userId"
              control={control}
              render={({ field }) => (
                <AutoComplete
                  {...field}
                  showSearch
                  placeholder="Email или имя"
                  onSearch={setUserSearch}
                  filterOption={false}
                  options={(users?.content ?? []).map((u) => ({
                    value: u.id,
                    label: `${u.fullName} (${u.email})`,
                  }))}
                />
              )}
            />
          </Form.Item>

          <Form.Item
            label="Категория"
            required
            validateStatus={errors.kind ? 'error' : ''}
            help={errors.kind?.message}
          >
            <Controller
              name="kind"
              control={control}
              render={({ field }) => <Select {...field} options={RESIDENT_KIND_OPTIONS} />}
            />
          </Form.Item>

          <Form.Item
            label="Комната"
            required
            validateStatus={errors.roomId ? 'error' : ''}
            help={errors.roomId?.message}
          >
            <Controller
              name="roomId"
              control={control}
              render={({ field }) => (
                <Select
                  {...field}
                  showSearch
                  optionFilterProp="label"
                  placeholder="Выберите свободную комнату"
                  options={(rooms?.content ?? []).map((r) => ({
                    value: r.id,
                    label: `${r.number} (этаж ${r.floor}, мест ${r.capacity - r.occupied}/${r.capacity})`,
                  }))}
                />
              )}
            />
          </Form.Item>

          <Form.Item
            label="Дата заселения"
            required
            validateStatus={errors.enrolledAt ? 'error' : ''}
            help={errors.enrolledAt?.message}
          >
            <Controller
              name="enrolledAt"
              control={control}
              render={({ field }) => (
                <DatePicker
                  format="DD.MM.YYYY"
                  style={{ width: '100%' }}
                  value={field.value ? dayjs(field.value) : undefined}
                  onChange={(d) => field.onChange(d ? d.format('YYYY-MM-DD') : '')}
                />
              )}
            />
          </Form.Item>

          <Form.Item label="Факультет">
            <Controller name="faculty" control={control} render={({ field }) => <Input {...field} />} />
          </Form.Item>
          <Form.Item label="Группа">
            <Controller name="studyGroup" control={control} render={({ field }) => <Input {...field} />} />
          </Form.Item>
          <Form.Item label="Подразделение">
            <Controller
              name="department"
              control={control}
              render={({ field }) => <Input {...field} />}
            />
          </Form.Item>
          <Form.Item label="Телефон">
            <Controller name="phone" control={control} render={({ field }) => <Input {...field} />} />
          </Form.Item>
          <Form.Item label="Контактная информация">
            <Controller
              name="contactInfo"
              control={control}
              render={({ field }) => <Input.TextArea {...field} rows={2} />}
            />
          </Form.Item>

          <div style={{ display: 'flex', gap: 8 }}>
            <Button type="primary" htmlType="submit" loading={enroll.isPending}>
              Заселить
            </Button>
            <Button onClick={() => navigate(-1)}>Отмена</Button>
          </div>
        </Form>
      </Card>
    </>
  );
}
