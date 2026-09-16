export interface Page<T> {
  content: T[];
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
  empty?: boolean;
}

export interface ErrorResponseDTO {
  timestamp: string;
  error?: string;
  message: string;
  description?: string;
  path?: string;
  details?: string;
  validationErrors?: Record<string, string>;
}
