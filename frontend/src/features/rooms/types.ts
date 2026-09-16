import type { ResidentDto } from '@/features/residents/types';
import type { InventoryItemDto } from '@/features/inventory/types';

export interface RoomDto {
  id: string;
  number: string;
  floor: number;
  capacity: number;
  occupied: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RoomDetailDto {
  id: string;
  number: string;
  floor: number;
  capacity: number;
  occupied: number;
  notes?: string;
  residents: ResidentDto[];
  inventory: InventoryItemDto[];
  createdAt: string;
  updatedAt: string;
}

export interface RoomsFilters {
  floor?: number;
  capacity?: number;
  hasFreeBeds?: boolean;
  page?: number;
  size?: number;
}

export interface CreateRoomPayload {
  number: string;
  floor: number;
  capacity: number;
  notes?: string;
}

export interface UpdateRoomPayload {
  number?: string;
  floor?: number;
  capacity?: number;
  notes?: string;
}
