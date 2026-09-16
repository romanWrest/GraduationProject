import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  addInventory,
  deleteInventory,
  listInventory,
  updateInventory,
  writeOffInventory,
} from './api';
import type { CreateInventoryPayload, UpdateInventoryPayload } from './types';

const INVENTORY_KEY = ['inventory'] as const;

export function useInventory(roomId: string | undefined) {
  return useQuery({
    queryKey: [...INVENTORY_KEY, 'list', roomId],
    queryFn: () => listInventory(roomId!),
    enabled: !!roomId,
  });
}

export function useAddInventory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ roomId, payload }: { roomId: string; payload: CreateInventoryPayload }) =>
      addInventory(roomId, payload),
    onSuccess: (_d, { roomId }) => {
      qc.invalidateQueries({ queryKey: [...INVENTORY_KEY, 'list', roomId] });
      qc.invalidateQueries({ queryKey: ['rooms', 'detail', roomId] });
    },
  });
}

export function useUpdateInventory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateInventoryPayload }) =>
      updateInventory(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: INVENTORY_KEY });
      qc.invalidateQueries({ queryKey: ['rooms'] });
    },
  });
}

export function useWriteOffInventory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => writeOffInventory(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: INVENTORY_KEY });
      qc.invalidateQueries({ queryKey: ['rooms'] });
    },
  });
}

export function useDeleteInventory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteInventory(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: INVENTORY_KEY });
      qc.invalidateQueries({ queryKey: ['rooms'] });
    },
  });
}
