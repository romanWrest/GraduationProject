export type RequestType =
  | 'REPAIR_ELECTRIC'
  | 'REPAIR_PLUMBING'
  | 'REPAIR_CARPENTRY'
  | 'REPAIR_GAS'
  | 'LINEN_REPLACEMENT'
  | 'GUEST_PASS'
  | 'ITEM_MOVEMENT'
  | 'RELOCATION'
  | 'COMPLAINT'
  | 'ELECTRICAL_APPLIANCE'
  | 'OTHER';

export type RequestStatus =
  | 'NEW'
  | 'IN_REVIEW'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'DONE'
  | 'CLOSED'
  | 'REJECTED'
  | 'CANCELLED';

export type TargetPool =
  | 'EXECUTOR_ELECTRIC'
  | 'EXECUTOR_PLUMBING'
  | 'EXECUTOR_CARPENTRY'
  | 'EXECUTOR_GAS'
  | 'PROPERTY_MANAGER'
  | 'ADMIN';

export type PatchAction = 'ASSIGN' | 'START' | 'DONE' | 'CONFIRM' | 'REJECT' | 'REVIEW';

export interface RequestSummaryDto {
  id: string;
  authorId: string;
  roomId?: string;
  type: RequestType;
  title: string;
  status: RequestStatus;
  targetPool: TargetPool;
  assigneeId?: string;
  scheduledAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RequestDto {
  id: string;
  authorId: string;
  authorResidentId?: string;
  roomId?: string;
  type: RequestType;
  title: string;
  description: string;
  reason?: string;
  status: RequestStatus;
  targetPool: TargetPool;
  assigneeId?: string;
  scheduledAt?: string;
  resolutionComment?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  reopenReason?: string;
  autoClosed: boolean;
  createdAt: string;
  updatedAt: string;
  closedAt?: string;
}

export interface CommentDto {
  id: string;
  requestId: string;
  authorId: string;
  body: string;
  createdAt: string;
}

export interface AttachmentDto {
  id: string;
  requestId: string;
  originalName: string;
  sizeBytes: number;
  contentType: string;
  uploadedBy: string;
  uploadedAt: string;
}

export interface StatusHistoryDto {
  id: string;
  requestId: string;
  fromStatus?: RequestStatus;
  toStatus: RequestStatus;
  actorId: string;
  comment?: string;
  changedAt: string;
}

export interface RequestDetailDto {
  request: RequestDto;
  comments: CommentDto[];
  attachments: AttachmentDto[];
}

export interface CreateRequestPayload {
  type: RequestType;
  title: string;
  description: string;
  reason?: string;
}

export interface PatchActionPayload {
  action: PatchAction;
  assigneeId?: string;
  scheduledAt?: string;
  resolutionComment?: string;
  rejectionReason?: string;
}

export interface RequestFilters {
  type?: RequestType;
  status?: RequestStatus;
  assigneeId?: string;
  authorId?: string;
  roomId?: string;
  from?: string;
  to?: string;
  search?: string;
  page?: number;
  size?: number;
}
