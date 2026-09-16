import { useState } from 'react';
import { App, Button, Card, Form, Input, Select, Upload } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@/components/common/PageHeader';
import { useCreateRequest } from '@/features/requests/hooks';
import { REQUEST_TYPE_OPTIONS } from '@/shared/constants/requestTypes';
import type { RequestType } from '@/features/requests/types';
import { handleApiError } from '@/shared/lib/handleApiError';

const MAX_FILES = 5;
const MAX_FILE_SIZE = 10 * 1024 * 1024;
const ALLOWED_EXT = ['jpg', 'jpeg', 'png', 'pdf', 'doc', 'docx', 'txt'];

const schema = z.object({
  type: z.enum([
    'REPAIR_ELECTRIC',
    'REPAIR_PLUMBING',
    'REPAIR_CARPENTRY',
    'REPAIR_GAS',
    'LINEN_REPLACEMENT',
    'GUEST_PASS',
    'ITEM_MOVEMENT',
    'RELOCATION',
    'COMPLAINT',
    'ELECTRICAL_APPLIANCE',
    'OTHER',
  ]) satisfies z.ZodType<RequestType>,
  title: z.string().min(1, 'Введите название').max(150),
  description: z.string().min(1, 'Введите описание').max(10000),
  reason: z.string().max(1000).optional().or(z.literal('')),
});

type FormValues = z.infer<typeof schema>;

export function CreateRequestPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const create = useCreateRequest();
  const [files, setFiles] = useState<UploadFile[]>([]);

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { type: undefined, title: '', description: '', reason: '' },
  });

  const onSubmit = async (values: FormValues) => {
    const realFiles: File[] = files
      .map((f) => f.originFileObj)
      .filter((f): f is NonNullable<typeof f> => !!f) as unknown as File[];
    try {
      const created = await create.mutateAsync({
        payload: {
          type: values.type,
          title: values.title,
          description: values.description,
          reason: values.reason || undefined,
        },
        files: realFiles,
      });
      message.success('Заявка создана');
      navigate(`/requests/${created.id}`);
    } catch (e) {
      handleApiError<FormValues>(e, { setError });
    }
  };

  return (
    <>
      <PageHeader title="Новая заявка" />
      <Card>
        <Form layout="vertical" onFinish={handleSubmit(onSubmit)} disabled={create.isPending}>
          <Form.Item
            label="Тип"
            validateStatus={errors.type ? 'error' : ''}
            help={errors.type?.message}
            required
          >
            <Controller
              name="type"
              control={control}
              render={({ field }) => (
                <Select
                  {...field}
                  placeholder="Выберите тип заявки"
                  options={REQUEST_TYPE_OPTIONS}
                  showSearch
                  optionFilterProp="label"
                />
              )}
            />
          </Form.Item>

          <Form.Item
            label="Название"
            validateStatus={errors.title ? 'error' : ''}
            help={errors.title?.message}
            required
          >
            <Controller
              name="title"
              control={control}
              render={({ field }) => <Input {...field} maxLength={150} showCount />}
            />
          </Form.Item>

          <Form.Item
            label="Описание"
            validateStatus={errors.description ? 'error' : ''}
            help={errors.description?.message}
            required
          >
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <Input.TextArea {...field} rows={5} maxLength={10000} showCount />
              )}
            />
          </Form.Item>

          <Form.Item
            label="Причина / контекст"
            validateStatus={errors.reason ? 'error' : ''}
            help={errors.reason?.message}
          >
            <Controller
              name="reason"
              control={control}
              render={({ field }) => (
                <Input.TextArea {...field} rows={3} maxLength={1000} showCount />
              )}
            />
          </Form.Item>

          <Form.Item label={`Вложения (до ${MAX_FILES}, до 10 МБ, ${ALLOWED_EXT.join('/')})`}>
            <Upload.Dragger
              multiple
              fileList={files}
              beforeUpload={(file) => {
                const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
                if (!ALLOWED_EXT.includes(ext)) {
                  message.error(`Неподдерживаемый формат: ${file.name}`);
                  return Upload.LIST_IGNORE;
                }
                if (file.size > MAX_FILE_SIZE) {
                  message.error(`Файл больше 10 МБ: ${file.name}`);
                  return Upload.LIST_IGNORE;
                }
                if (files.length >= MAX_FILES) {
                  message.error(`Максимум ${MAX_FILES} файлов`);
                  return Upload.LIST_IGNORE;
                }
                return false;
              }}
              onChange={({ fileList }) => setFiles(fileList.slice(0, MAX_FILES))}
              onRemove={(file) => {
                setFiles((prev) => prev.filter((f) => f.uid !== file.uid));
              }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">Перетащите файлы сюда или нажмите для выбора</p>
              <p className="ant-upload-hint">Изображения и документы по делу заявки</p>
            </Upload.Dragger>
          </Form.Item>

          <div style={{ display: 'flex', gap: 8 }}>
            <Button type="primary" htmlType="submit" loading={create.isPending}>
              Создать
            </Button>
            <Button onClick={() => navigate(-1)}>Отмена</Button>
          </div>
        </Form>
      </Card>
    </>
  );
}
