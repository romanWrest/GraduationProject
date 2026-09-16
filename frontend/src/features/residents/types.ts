export type ResidentKind = 'STUDENT' | 'TEACHER' | 'STAFF_LIVING';

export interface ResidentDto {
  id: string;
  userId: string;
  kind: ResidentKind;
  faculty?: string;
  studyGroup?: string;
  department?: string;
  phone?: string;
  contactInfo?: string;
  roomId?: string;
  roomNumber?: string;
  enrolledAt: string;
  evictedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ResidencyHistoryDto {
  id: string;
  residentId: string;
  roomId: string;
  roomNumber?: string;
  movedInAt: string;
  movedOutAt?: string;
  reason?: string;
  createdAt: string;
}

export interface ResidentsFilters {
  kind?: ResidentKind;
  roomId?: string;
  faculty?: string;
  search?: string;
  active?: boolean;
  page?: number;
  size?: number;
}

export interface EnrollResidentPayload {
  userId: string;
  kind: ResidentKind;
  faculty?: string;
  studyGroup?: string;
  department?: string;
  phone?: string;
  contactInfo?: string;
  roomId: string;
  enrolledAt: string;
}

export interface UpdateResidentPayload {
  faculty?: string;
  studyGroup?: string;
  department?: string;
  phone?: string;
  contactInfo?: string;
}

export interface EvictResidentPayload {
  evictedAt: string;
  reason?: string;
}

export interface MoveResidentPayload {
  newRoomId: string;
  movedAt: string;
  reason?: string;
}
