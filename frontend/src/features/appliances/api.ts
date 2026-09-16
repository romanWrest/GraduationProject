import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  ApplianceDto,
  AppliancesFilters,
  RegisterAppliancePayload,
  RoomAppliancesDto,
} from './types';

export async function listAppliances(filters: AppliancesFilters): Promise<Page<ApplianceDto>> {
  const { data } = await apiClient.get<Page<ApplianceDto>>('/appliances', { params: filters });
  return data;
}

export async function listMyAppliances(filters: AppliancesFilters): Promise<Page<ApplianceDto>> {
  const { data } = await apiClient.get<Page<ApplianceDto>>('/appliances/my', { params: filters });
  return data;
}

export async function getAppliance(id: string): Promise<ApplianceDto> {
  const { data } = await apiClient.get<ApplianceDto>(`/appliances/${id}`);
  return data;
}

export async function registerAppliance(
  payload: RegisterAppliancePayload,
): Promise<ApplianceDto> {
  const { data } = await apiClient.post<ApplianceDto>('/appliances', payload);
  return data;
}

export async function approveAppliance(id: string, comment?: string): Promise<ApplianceDto> {
  const { data } = await apiClient.patch<ApplianceDto>(`/appliances/${id}/approve`, { comment });
  return data;
}

export async function rejectAppliance(id: string, reason: string): Promise<ApplianceDto> {
  const { data } = await apiClient.patch<ApplianceDto>(`/appliances/${id}/reject`, { reason });
  return data;
}

export async function revokeAppliance(id: string, reason: string): Promise<ApplianceDto> {
  const { data } = await apiClient.patch<ApplianceDto>(`/appliances/${id}/revoke`, { reason });
  return data;
}

export async function getRoomAppliances(roomId: string): Promise<RoomAppliancesDto> {
  const { data } = await apiClient.get<RoomAppliancesDto>(`/appliances/by-room/${roomId}`);
  return data;
}
