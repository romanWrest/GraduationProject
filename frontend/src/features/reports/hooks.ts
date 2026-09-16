import { useQuery } from '@tanstack/react-query';
import {
  getAppliancesReport,
  getAssigneesReport,
  getConsumablesReport,
  getRequestsReport,
  getResidentsReport,
} from './api';

const KEY = ['reports'] as const;

export function useRequestsReport(params: {
  from: string;
  to: string;
  type?: string;
  status?: string;
  assigneeId?: string;
}) {
  return useQuery({
    queryKey: [...KEY, 'requests', params],
    queryFn: () => getRequestsReport(params),
    enabled: !!params.from && !!params.to,
  });
}

export function useAssigneesReport(params: { from: string; to: string }) {
  return useQuery({
    queryKey: [...KEY, 'assignees', params],
    queryFn: () => getAssigneesReport(params),
    enabled: !!params.from && !!params.to,
  });
}

export function useResidentsReport(params: { asOf?: string }) {
  return useQuery({
    queryKey: [...KEY, 'residents', params],
    queryFn: () => getResidentsReport(params),
  });
}

export function useAppliancesReport(params: { roomId?: string; status?: string }) {
  return useQuery({
    queryKey: [...KEY, 'appliances', params],
    queryFn: () => getAppliancesReport(params),
  });
}

export function useConsumablesReport(params: {
  from: string;
  to: string;
  typeId?: string;
}) {
  return useQuery({
    queryKey: [...KEY, 'consumables', params],
    queryFn: () => getConsumablesReport(params),
    enabled: !!params.from && !!params.to,
  });
}
