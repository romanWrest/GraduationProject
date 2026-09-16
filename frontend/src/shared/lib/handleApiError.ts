import axios from 'axios';
import { notification } from 'antd';
import type { FieldValues, UseFormSetError } from 'react-hook-form';
import type { ErrorResponseDTO } from '@/shared/api/types';

interface Options<T extends FieldValues> {
  setError?: UseFormSetError<T>;
  silent401?: boolean;
}

export function handleApiError<T extends FieldValues>(error: unknown, options: Options<T> = {}) {
  if (!axios.isAxiosError<ErrorResponseDTO>(error)) {
    notification.error({ message: 'Ошибка сети' });
    return;
  }
  const status = error.response?.status;
  const data = error.response?.data;

  if (status === 401) {
    if (!options.silent401) {
      notification.error({ message: data?.message ?? 'Неверный логин или пароль' });
    }
    return;
  }

  if (status === 400) {
    if (data?.validationErrors && options.setError) {
      Object.entries(data.validationErrors).forEach(([field, message]) => {
        options.setError?.(field as never, { type: 'server', message });
      });
      return;
    }
    notification.error({ message: data?.message ?? 'Некорректные данные' });
    return;
  }

  if (status === 403) {
    notification.error({ message: 'Нет доступа' });
    return;
  }
  if (status === 404) {
    notification.error({ message: data?.message ?? 'Не найдено' });
    return;
  }
  if (status === 409) {
    notification.error({ message: data?.message ?? 'Конфликт состояния' });
    return;
  }
  if (status === 429) {
    notification.warning({
      message: 'Слишком много запросов, попробуйте через несколько секунд',
    });
    return;
  }
  if (status && status >= 500) {
    notification.error({ message: 'Сервис временно недоступен' });
    return;
  }
  notification.error({ message: data?.message ?? 'Неизвестная ошибка' });
}
