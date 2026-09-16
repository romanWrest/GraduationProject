import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  addComment,
  cancelRequest,
  createRequest,
  deleteAttachment,
  getRequest,
  listComments,
  listHistory,
  listMyRequests,
  listPool,
  listRequests,
  patchRequest,
  reopenRequest,
  uploadAttachment,
} from './api';
import type {
  CreateRequestPayload,
  PatchActionPayload,
  RequestFilters,
} from './types';

const REQUESTS_KEY = ['requests'] as const;

export function useRequests(filters: RequestFilters) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'list', filters],
    queryFn: () => listRequests(filters),
    placeholderData: (prev) => prev,
  });
}

export function useMyRequests(page: number, size: number) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'my', page, size],
    queryFn: () => listMyRequests(page, size),
    placeholderData: (prev) => prev,
  });
}

export function usePool(page: number, size: number) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'pool', page, size],
    queryFn: () => listPool(page, size),
    placeholderData: (prev) => prev,
  });
}

export function useRequest(id: string | undefined) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'detail', id],
    queryFn: () => getRequest(id!),
    enabled: !!id,
  });
}

export function useCreateRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ payload, files }: { payload: CreateRequestPayload; files: File[] }) =>
      createRequest(payload, files),
    onSuccess: () => qc.invalidateQueries({ queryKey: REQUESTS_KEY }),
  });
}

export function usePatchRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: PatchActionPayload }) =>
      patchRequest(id, payload),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'list'] });
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'my'] });
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'pool'] });
    },
  });
}

export function useCancelRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) => cancelRequest(id, reason),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: REQUESTS_KEY });
    },
  });
}

export function useReopenRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => reopenRequest(id, reason),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] });
      qc.invalidateQueries({ queryKey: REQUESTS_KEY });
    },
  });
}

export function useComments(id: string | undefined) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'comments', id],
    queryFn: () => listComments(id!),
    enabled: !!id,
  });
}

export function useAddComment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: string }) => addComment(id, body),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'comments', id] });
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] });
    },
  });
}

export function useUploadAttachment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, file }: { id: string; file: File }) => uploadAttachment(id, file),
    onSuccess: (_data, { id }) =>
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] }),
  });
}

export function useDeleteAttachment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, attId }: { id: string; attId: string }) => deleteAttachment(id, attId),
    onSuccess: (_data, { id }) =>
      qc.invalidateQueries({ queryKey: [...REQUESTS_KEY, 'detail', id] }),
  });
}

export function useHistory(id: string | undefined) {
  return useQuery({
    queryKey: [...REQUESTS_KEY, 'history', id],
    queryFn: () => listHistory(id!),
    enabled: !!id,
  });
}
