import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  enrollResident,
  evictResident,
  getResident,
  getResidentByUser,
  getResidentHistory,
  listResidents,
  moveResident,
  updateResident,
} from './api';
import type {
  EnrollResidentPayload,
  EvictResidentPayload,
  MoveResidentPayload,
  ResidentsFilters,
  UpdateResidentPayload,
} from './types';

const RESIDENTS_KEY = ['residents'] as const;

export function useResidents(filters: ResidentsFilters) {
  return useQuery({
    queryKey: [...RESIDENTS_KEY, 'list', filters],
    queryFn: () => listResidents(filters),
    placeholderData: (prev) => prev,
  });
}

export function useResident(id: string | undefined) {
  return useQuery({
    queryKey: [...RESIDENTS_KEY, 'detail', id],
    queryFn: () => getResident(id!),
    enabled: !!id,
  });
}

export function useResidentByUser(userId: string | undefined) {
  return useQuery({
    queryKey: [...RESIDENTS_KEY, 'by-user', userId],
    queryFn: () => getResidentByUser(userId!),
    enabled: !!userId,
    retry: false,
  });
}

export function useResidentHistory(id: string | undefined) {
  return useQuery({
    queryKey: [...RESIDENTS_KEY, 'history', id],
    queryFn: () => getResidentHistory(id!),
    enabled: !!id,
  });
}

export function useEnrollResident() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: EnrollResidentPayload) => enrollResident(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: RESIDENTS_KEY }),
  });
}

export function useUpdateResident() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateResidentPayload }) =>
      updateResident(id, payload),
    onSuccess: (_d, { id }) => {
      qc.invalidateQueries({ queryKey: [...RESIDENTS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: [...RESIDENTS_KEY, 'list'] });
    },
  });
}

export function useEvictResident() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: EvictResidentPayload }) =>
      evictResident(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: RESIDENTS_KEY }),
  });
}

export function useMoveResident() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: MoveResidentPayload }) =>
      moveResident(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: RESIDENTS_KEY }),
  });
}
