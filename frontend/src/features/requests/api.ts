import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  AttachmentDto,
  CommentDto,
  CreateRequestPayload,
  PatchActionPayload,
  RequestDetailDto,
  RequestDto,
  RequestFilters,
  RequestSummaryDto,
  StatusHistoryDto,
} from './types';

export async function listRequests(filters: RequestFilters): Promise<Page<RequestSummaryDto>> {
  const { data } = await apiClient.get<Page<RequestSummaryDto>>('/requests', { params: filters });
  return data;
}

export async function listMyRequests(
  page = 0,
  size = 20,
): Promise<Page<RequestSummaryDto>> {
  const { data } = await apiClient.get<Page<RequestSummaryDto>>('/requests/my', {
    params: { page, size },
  });
  return data;
}

export async function listPool(page = 0, size = 20): Promise<Page<RequestSummaryDto>> {
  const { data } = await apiClient.get<Page<RequestSummaryDto>>('/requests/pool', {
    params: { page, size },
  });
  return data;
}

export async function getRequest(id: string): Promise<RequestDetailDto> {
  const { data } = await apiClient.get<RequestDetailDto>(`/requests/${id}`);
  return data;
}

export async function createRequest(
  payload: CreateRequestPayload,
  files: File[],
): Promise<RequestDto> {
  const form = new FormData();
  form.append(
    'request',
    new Blob([JSON.stringify(payload)], { type: 'application/json' }),
  );
  files.forEach((file) => form.append('files', file));
  const { data } = await apiClient.post<RequestDto>('/requests', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function patchRequest(
  id: string,
  payload: PatchActionPayload,
): Promise<RequestDto> {
  const { data } = await apiClient.patch<RequestDto>(`/requests/${id}`, payload);
  return data;
}

export async function cancelRequest(id: string, reason?: string): Promise<RequestDto> {
  const { data } = await apiClient.post<RequestDto>(`/requests/${id}/cancel`, { reason });
  return data;
}

export async function reopenRequest(id: string, reason: string): Promise<RequestDto> {
  const { data } = await apiClient.post<RequestDto>(`/requests/${id}/reopen`, { reason });
  return data;
}

export async function listComments(id: string): Promise<CommentDto[]> {
  const { data } = await apiClient.get<CommentDto[]>(`/requests/${id}/comments`);
  return data;
}

export async function addComment(id: string, body: string): Promise<CommentDto> {
  const { data } = await apiClient.post<CommentDto>(`/requests/${id}/comments`, { body });
  return data;
}

export async function uploadAttachment(id: string, file: File): Promise<AttachmentDto> {
  const form = new FormData();
  form.append('file', file);
  const { data } = await apiClient.post<AttachmentDto>(`/requests/${id}/attachments`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function deleteAttachment(id: string, attId: string): Promise<void> {
  await apiClient.delete(`/requests/${id}/attachments/${attId}`);
}

export function attachmentDownloadUrl(id: string, attId: string): string {
  return `${apiClient.defaults.baseURL}/requests/${id}/attachments/${attId}`;
}

export async function downloadAttachment(id: string, attId: string): Promise<Blob> {
  const { data } = await apiClient.get<Blob>(`/requests/${id}/attachments/${attId}`, {
    responseType: 'blob',
  });
  return data;
}

export async function listHistory(id: string): Promise<StatusHistoryDto[]> {
  const { data } = await apiClient.get<StatusHistoryDto[]>(`/requests/${id}/history`);
  return data;
}
