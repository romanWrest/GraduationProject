export type ConsumableUnit = 'PIECE' | 'SET';
export type IssueStatus = 'ISSUED' | 'RETURNED';
export type ReturnCondition = 'OK' | 'DAMAGED' | 'LOST';

export interface ConsumableTypeDto {
  id: string;
  name: string;
  unit: ConsumableUnit;
  stock: number;
  lowStockThreshold: number;
  createdAt: string;
  updatedAt: string;
}

export interface StockItemDto {
  typeId: string;
  name: string;
  unit: ConsumableUnit;
  stock: number;
  lowStockThreshold: number;
  low: boolean;
}

export interface ConsumableIssueDto {
  id: string;
  residentId: string;
  userId: string;
  typeId: string;
  typeName?: string;
  quantity: number;
  status: IssueStatus;
  issuedAt: string;
  issuedBy: string;
  returnedAt?: string;
  returnedBy?: string;
  returnCondition?: ReturnCondition;
  notes?: string;
}

export interface CreateTypePayload {
  name: string;
  unit: ConsumableUnit;
  stock: number;
  lowStockThreshold: number;
}

export interface UpdateTypePayload {
  name?: string;
  lowStockThreshold?: number;
}

export interface StockOperationPayload {
  delta: number;
  reason: string;
}

export interface IssuePayload {
  residentId: string;
  typeId: string;
  quantity: number;
  notes?: string;
}

export interface ReturnPayload {
  condition: ReturnCondition;
  notes?: string;
}

export interface IssuesFilters {
  residentId?: string;
  typeId?: string;
  status?: IssueStatus;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}
