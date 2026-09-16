export interface RequestRowDto {
  requestId: string;
  type: string;
  status: string;
  authorName?: string;
  assigneeName?: string;
  roomNumber?: string;
  createdAt: string;
  closedAt?: string;
  resolutionSeconds?: number;
  autoClosed?: boolean;
}

export interface RequestsReportDto {
  from: string;
  to: string;
  totalCount: number;
  byStatus: Record<string, number>;
  byType: Record<string, number>;
  byAuthor: Record<string, number>;
  byAssignee: Record<string, number>;
  avgResolutionSeconds?: number;
  rows: RequestRowDto[];
}

export interface AssigneeLoadRowDto {
  assigneeId: string;
  assigneeName?: string;
  assignedCount: number;
  completedCount: number;
  avgResolutionSeconds?: number;
}

export interface AssigneeLoadReportDto {
  from: string;
  to: string;
  totalAssignees: number;
  rows: AssigneeLoadRowDto[];
}

export interface ResidentRowDto {
  residentId: string;
  userId: string;
  fullName?: string;
  kind: string;
  faculty?: string;
  studyGroup?: string;
  department?: string;
  roomNumber?: string;
  enrolledAt: string;
}

export interface ResidentsReportDto {
  asOf: string;
  totalCount: number;
  byKind: Record<string, number>;
  byFaculty: Record<string, number>;
  byRoom: Record<string, number>;
  rows: ResidentRowDto[];
}

export interface ApplianceRowDto {
  applianceId: string;
  residentName?: string;
  roomNumber?: string;
  type: string;
  brand?: string;
  model?: string;
  powerWatts?: number;
  status: string;
}

export interface AppliancesReportDto {
  totalCount: number;
  totalPowerWatts: number;
  totalPowerByRoom: Record<string, number>;
  countByStatus: Record<string, number>;
  rows: ApplianceRowDto[];
}

export interface ConsumableRowDto {
  issueId: string;
  residentName?: string;
  typeName?: string;
  quantity?: number;
  status: string;
  issuedAt: string;
  returnedAt?: string;
  returnCondition?: string;
}

export interface ConsumablesReportDto {
  from: string;
  to: string;
  totalIssues: number;
  issuesByType: Record<string, number>;
  issuesByResident: Record<string, number>;
  returnedCount: number;
  damagedCount: number;
  lostCount: number;
  returnedPct: number;
  damagedPct: number;
  lostPct: number;
  rows: ConsumableRowDto[];
}

export type ReportName =
  | 'requests'
  | 'assignees'
  | 'residents'
  | 'appliances'
  | 'consumables';

export type ExportFormat = 'csv' | 'xlsx' | 'pdf';

export interface PeriodFilter {
  from?: string;
  to?: string;
}
