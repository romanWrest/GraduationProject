import { apiClient } from '@/shared/api/client';
import type {
  CreateInventoryPayload,
  InventoryItemDto,
  UpdateInventoryPayload,
} from './types';

export async function listInventory(roomId: string): Promise<InventoryItemDto[]> {
  const { data } = await apiClient.get<InventoryItemDto[]>(`/rooms/${roomId}/inventory`);
  return data;
}

export async function addInventory(
  roomId: string,
  payload: CreateInventoryPayload,
): Promise<InventoryItemDto> {
  const { data } = await apiClient.post<InventoryItemDto>(`/rooms/${roomId}/inventory`, payload);
  return data;
}

export async function updateInventory(
  id: string,
  payload: UpdateInventoryPayload,
): Promise<InventoryItemDto> {
  const { data } = await apiClient.patch<InventoryItemDto>(`/inventory/${id}`, payload);
  return data;
}

export async function writeOffInventory(id: string, reason: string): Promise<InventoryItemDto> {
  const { data } = await apiClient.post<InventoryItemDto>(`/inventory/${id}/write-off`, {
    reason,
  });
  return data;
}

export async function deleteInventory(id: string): Promise<void> {
  await apiClient.delete(`/inventory/${id}`);
}
