import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createRoom,
  deleteRoom,
  getRoom,
  listRoomHistory,
  listRoomResidents,
  listRooms,
  updateRoom,
} from './api';
import type { CreateRoomPayload, RoomsFilters, UpdateRoomPayload } from './types';

const ROOMS_KEY = ['rooms'] as const;

export function useRooms(filters: RoomsFilters) {
  return useQuery({
    queryKey: [...ROOMS_KEY, 'list', filters],
    queryFn: () => listRooms(filters),
    placeholderData: (prev) => prev,
  });
}

export function useRoom(id: string | undefined) {
  return useQuery({
    queryKey: [...ROOMS_KEY, 'detail', id],
    queryFn: () => getRoom(id!),
    enabled: !!id,
  });
}

export function useRoomResidents(id: string | undefined) {
  return useQuery({
    queryKey: [...ROOMS_KEY, 'residents', id],
    queryFn: () => listRoomResidents(id!),
    enabled: !!id,
  });
}

export function useRoomHistory(id: string | undefined) {
  return useQuery({
    queryKey: [...ROOMS_KEY, 'history', id],
    queryFn: () => listRoomHistory(id!),
    enabled: !!id,
  });
}

export function useCreateRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateRoomPayload) => createRoom(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ROOMS_KEY }),
  });
}

export function useUpdateRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateRoomPayload }) =>
      updateRoom(id, payload),
    onSuccess: (_d, { id }) => {
      qc.invalidateQueries({ queryKey: [...ROOMS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: [...ROOMS_KEY, 'list'] });
    },
  });
}

export function useDeleteRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteRoom(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ROOMS_KEY }),
  });
}
