import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  approveAppliance,
  getAppliance,
  getRoomAppliances,
  listAppliances,
  listMyAppliances,
  registerAppliance,
  rejectAppliance,
  revokeAppliance,
} from './api';
import type { AppliancesFilters, RegisterAppliancePayload } from './types';

const APPLIANCES_KEY = ['appliances'] as const;

export function useAppliances(filters: AppliancesFilters) {
  return useQuery({
    queryKey: [...APPLIANCES_KEY, 'list', filters],
    queryFn: () => listAppliances(filters),
    placeholderData: (prev) => prev,
  });
}

export function useMyAppliances(filters: AppliancesFilters) {
  return useQuery({
    queryKey: [...APPLIANCES_KEY, 'my', filters],
    queryFn: () => listMyAppliances(filters),
    placeholderData: (prev) => prev,
  });
}

export function useAppliance(id: string | undefined) {
  return useQuery({
    queryKey: [...APPLIANCES_KEY, 'detail', id],
    queryFn: () => getAppliance(id!),
    enabled: !!id,
  });
}

export function useRoomAppliances(roomId: string | undefined) {
  return useQuery({
    queryKey: [...APPLIANCES_KEY, 'by-room', roomId],
    queryFn: () => getRoomAppliances(roomId!),
    enabled: !!roomId,
  });
}

export function useRegisterAppliance() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: RegisterAppliancePayload) => registerAppliance(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: APPLIANCES_KEY }),
  });
}

export function useApproveAppliance() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, comment }: { id: string; comment?: string }) =>
      approveAppliance(id, comment),
    onSuccess: () => qc.invalidateQueries({ queryKey: APPLIANCES_KEY }),
  });
}

export function useRejectAppliance() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectAppliance(id, reason),
    onSuccess: () => qc.invalidateQueries({ queryKey: APPLIANCES_KEY }),
  });
}

export function useRevokeAppliance() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => revokeAppliance(id, reason),
    onSuccess: () => qc.invalidateQueries({ queryKey: APPLIANCES_KEY }),
  });
}
