import { apiClient } from '@/shared/api/client';
import type {
  AppliancesReportDto,
  AssigneeLoadReportDto,
  ConsumablesReportDto,
  ExportFormat,
  ReportName,
  RequestsReportDto,
  ResidentsReportDto,
} from './types';

export async function getRequestsReport(params: {
  from: string;
  to: string;
  type?: string;
  status?: string;
  assigneeId?: string;
}): Promise<RequestsReportDto> {
  const { data } = await apiClient.get<RequestsReportDto>('/reports/requests', { params });
  return data;
}

export async function getAssigneesReport(params: {
  from: string;
  to: string;
}): Promise<AssigneeLoadReportDto> {
  const { data } = await apiClient.get<AssigneeLoadReportDto>('/reports/assignees', { params });
  return data;
}

export async function getResidentsReport(params: {
  asOf?: string;
}): Promise<ResidentsReportDto> {
  const { data } = await apiClient.get<ResidentsReportDto>('/reports/residents', { params });
  return data;
}

export async function getAppliancesReport(params: {
  roomId?: string;
  status?: string;
}): Promise<AppliancesReportDto> {
  const { data } = await apiClient.get<AppliancesReportDto>('/reports/appliances', { params });
  return data;
}

export async function getConsumablesReport(params: {
  from: string;
  to: string;
  typeId?: string;
}): Promise<ConsumablesReportDto> {
  const { data } = await apiClient.get<ConsumablesReportDto>('/reports/consumables', { params });
  return data;
}

export async function exportReport(
  name: ReportName,
  format: ExportFormat,
  params: Record<string, string | undefined>,
): Promise<{ blob: Blob; filename: string }> {
  const response = await apiClient.get<Blob>(`/reports/${name}/export`, {
    params: { format, ...params },
    responseType: 'blob',
  });
  const disposition = response.headers['content-disposition'] as string | undefined;
  let filename = `report-${name}.${format}`;
  if (disposition) {
    const match = /filename\*?=(?:UTF-8''|")?([^";]+)/i.exec(disposition);
    if (match?.[1]) {
      try {
        filename = decodeURIComponent(match[1].replace(/^"|"$/g, ''));
      } catch {
        filename = match[1].replace(/^"|"$/g, '');
      }
    }
  }
  return { blob: response.data, filename };
}

export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
