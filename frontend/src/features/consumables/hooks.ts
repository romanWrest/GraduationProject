import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  adjustStock,
  createType,
  issueConsumable,
  listIssues,
  listMyIssues,
  listIssuesByResident,
  listStock,
  listTypes,
  returnIssue,
  updateType,
} from './api';
import type {
  CreateTypePayload,
  IssuePayload,
  IssuesFilters,
  ReturnPayload,
  StockOperationPayload,
  UpdateTypePayload,
} from './types';

const KEY = ['consumables'] as const;

export function useTypes() {
  return useQuery({
    queryKey: [...KEY, 'types'],
    queryFn: listTypes,
  });
}

export function useStock() {
  return useQuery({
    queryKey: [...KEY, 'stock'],
    queryFn: listStock,
  });
}

export function useIssues(filters: IssuesFilters) {
  return useQuery({
    queryKey: [...KEY, 'issues', filters],
    queryFn: () => listIssues(filters),
    placeholderData: (prev) => prev,
  });
}

export function useMyIssues() {
  return useQuery({
    queryKey: [...KEY, 'my'],
    queryFn: listMyIssues,
  });
}

export function useResidentIssues(residentId: string | undefined) {
  return useQuery({
    queryKey: [...KEY, 'by-resident', residentId],
    queryFn: () => listIssuesByResident(residentId!),
    enabled: !!residentId,
  });
}

export function useCreateType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateTypePayload) => createType(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useUpdateType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateTypePayload }) =>
      updateType(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useAdjustStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: StockOperationPayload }) =>
      adjustStock(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useIssue() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: IssuePayload) => issueConsumable(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}

export function useReturnIssue() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ReturnPayload }) =>
      returnIssue(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  });
}
