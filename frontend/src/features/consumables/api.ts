import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  ConsumableIssueDto,
  ConsumableTypeDto,
  CreateTypePayload,
  IssuePayload,
  IssuesFilters,
  ReturnPayload,
  StockItemDto,
  StockOperationPayload,
  UpdateTypePayload,
} from './types';

export async function listTypes(): Promise<ConsumableTypeDto[]> {
  const { data } = await apiClient.get<ConsumableTypeDto[]>('/consumables/types');
  return data;
}

export async function createType(payload: CreateTypePayload): Promise<ConsumableTypeDto> {
  const { data } = await apiClient.post<ConsumableTypeDto>('/consumables/types', payload);
  return data;
}

export async function updateType(
  id: string,
  payload: UpdateTypePayload,
): Promise<ConsumableTypeDto> {
  const { data } = await apiClient.patch<ConsumableTypeDto>(`/consumables/types/${id}`, payload);
  return data;
}

export async function adjustStock(
  id: string,
  payload: StockOperationPayload,
): Promise<ConsumableTypeDto> {
  const { data } = await apiClient.post<ConsumableTypeDto>(
    `/consumables/types/${id}/stock`,
    payload,
  );
  return data;
}

export async function listStock(): Promise<StockItemDto[]> {
  const { data } = await apiClient.get<StockItemDto[]>('/consumables/stock');
  return data;
}

export async function listIssues(filters: IssuesFilters): Promise<Page<ConsumableIssueDto>> {
  const { data } = await apiClient.get<Page<ConsumableIssueDto>>('/consumables/issues', {
    params: filters,
  });
  return data;
}

export async function issueConsumable(payload: IssuePayload): Promise<ConsumableIssueDto> {
  const { data } = await apiClient.post<ConsumableIssueDto>('/consumables/issues', payload);
  return data;
}

export async function returnIssue(
  id: string,
  payload: ReturnPayload,
): Promise<ConsumableIssueDto> {
  const { data } = await apiClient.patch<ConsumableIssueDto>(
    `/consumables/issues/${id}/return`,
    payload,
  );
  return data;
}

export async function listMyIssues(): Promise<ConsumableIssueDto[]> {
  const { data } = await apiClient.get<ConsumableIssueDto[]>('/consumables/my');
  return data;
}

export async function listIssuesByResident(
  residentId: string,
): Promise<ConsumableIssueDto[]> {
  const { data } = await apiClient.get<ConsumableIssueDto[]>(
    `/consumables/by-resident/${residentId}`,
  );
  return data;
}
