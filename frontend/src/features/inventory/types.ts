export type InventoryType =
  | 'BED'
  | 'DESK'
  | 'CHAIR'
  | 'CABINET'
  | 'NIGHTSTAND'
  | 'REFRIGERATOR'
  | 'STOVE'
  | 'WASHING_MACHINE'
  | 'KETTLE'
  | 'OTHER';

export type InventoryState = 'NEW' | 'USED' | 'BROKEN' | 'WRITTEN_OFF';

export interface InventoryItemDto {
  id: string;
  roomId: string;
  type: InventoryType;
  serialNumber?: string;
  state: InventoryState;
  notes?: string;
  writtenOffAt?: string;
  writtenOffReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInventoryPayload {
  type: InventoryType;
  serialNumber?: string;
  state: InventoryState;
  notes?: string;
}

export interface UpdateInventoryPayload {
  state?: InventoryState;
  serialNumber?: string;
  notes?: string;
}
