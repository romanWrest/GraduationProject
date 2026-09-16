import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  EnrollResidentPayload,
  EvictResidentPayload,
  MoveResidentPayload,
  ResidencyHistoryDto,
  ResidentDto,
  ResidentsFilters,
  UpdateResidentPayload,
} from './types';

export async function listResidents(filters: ResidentsFilters): Promise<Page<ResidentDto>> {
  const { data } = await apiClient.get<Page<ResidentDto>>('/residents', { params: filters });
  return data;
}

export async function getResident(id: string): Promise<ResidentDto> {
  const { data } = await apiClient.get<ResidentDto>(`/residents/${id}`);
  return data;
}

export async function getResidentByUser(userId: string): Promise<ResidentDto> {
  const { data } = await apiClient.get<ResidentDto>(`/residents/by-user/${userId}`);
  return data;
}

export async function enrollResident(payload: EnrollResidentPayload): Promise<ResidentDto> {
  const { data } = await apiClient.post<ResidentDto>('/residents', payload);
  return data;
}

export async function updateResident(
  id: string,
  payload: UpdateResidentPayload,
): Promise<ResidentDto> {
  const { data } = await apiClient.patch<ResidentDto>(`/residents/${id}`, payload);
  return data;
}

export async function evictResident(
  id: string,
  payload: EvictResidentPayload,
): Promise<ResidentDto> {
  const { data } = await apiClient.post<ResidentDto>(`/residents/${id}/evict`, payload);
  return data;
}

export async function moveResident(
  id: string,
  payload: MoveResidentPayload,
): Promise<ResidentDto> {
  const { data } = await apiClient.post<ResidentDto>(`/residents/${id}/move`, payload);
  return data;
}

export async function getResidentHistory(id: string): Promise<ResidencyHistoryDto[]> {
  const { data } = await apiClient.get<ResidencyHistoryDto[]>(`/residents/${id}/history`);
  return data;
}
