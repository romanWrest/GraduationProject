export type ApplianceStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVOKED';

export type ApplianceType =
  | 'KETTLE'
  | 'MICROWAVE'
  | 'FRIDGE'
  | 'LAPTOP_CHARGER'
  | 'PHONE_CHARGER'
  | 'IRON'
  | 'HAIR_DRYER'
  | 'TOASTER'
  | 'HEATER'
  | 'FAN'
  | 'LAMP'
  | 'TV'
  | 'SPEAKER'
  | 'OTHER';

export interface ApplianceDto {
  id: string;
  residentId: string;
  userId: string;
  roomId?: string;
  type: ApplianceType;
  brand?: string;
  model?: string;
  powerWatts: number;
  photoUrl?: string;
  notes?: string;
  status: ApplianceStatus;
  decisionBy?: string;
  decisionAt?: string;
  decisionReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RoomAppliancesDto {
  roomId: string;
  totalPowerWatts: number;
  powerLimitWatts: number;
  appliances: ApplianceDto[];
}

export interface RegisterAppliancePayload {
  type: ApplianceType;
  brand?: string;
  model?: string;
  powerWatts: number;
  photoUrl?: string;
  notes?: string;
}

export interface AppliancesFilters {
  status?: ApplianceStatus;
  residentId?: string;
  roomId?: string;
  type?: ApplianceType;
  search?: string;
  page?: number;
  size?: number;
}
