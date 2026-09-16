import { apiClient } from '@/shared/api/client';
import type { Page } from '@/shared/api/types';
import type {
  CreateRoomPayload,
  RoomDetailDto,
  RoomDto,
  RoomsFilters,
  UpdateRoomPayload,
} from './types';
import type { ResidentDto, ResidencyHistoryDto } from '@/features/residents/types';

export async function listRooms(filters: RoomsFilters): Promise<Page<RoomDto>> {
  const { data } = await apiClient.get<Page<RoomDto>>('/rooms', { params: filters });
  return data;
}

export async function getRoom(id: string): Promise<RoomDetailDto> {
  const { data } = await apiClient.get<RoomDetailDto>(`/rooms/${id}`);
  return data;
}

export async function createRoom(payload: CreateRoomPayload): Promise<RoomDto> {
  const { data } = await apiClient.post<RoomDto>('/rooms', payload);
  return data;
}

export async function updateRoom(id: string, payload: UpdateRoomPayload): Promise<RoomDto> {
  const { data } = await apiClient.patch<RoomDto>(`/rooms/${id}`, payload);
  return data;
}

export async function deleteRoom(id: string): Promise<void> {
  await apiClient.delete(`/rooms/${id}`);
}

export async function listRoomResidents(id: string): Promise<ResidentDto[]> {
  const { data } = await apiClient.get<ResidentDto[]>(`/rooms/${id}/residents`);
  return data;
}

export async function listRoomHistory(id: string): Promise<ResidencyHistoryDto[]> {
  const { data } = await apiClient.get<ResidencyHistoryDto[]>(`/rooms/${id}/history`);
  return data;
}
