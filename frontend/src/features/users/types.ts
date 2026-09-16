import type { Role } from '@/shared/constants/roles';

export interface UserDto {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  active: boolean;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface CreatedUserDto {
  user: UserDto;
  temporaryPassword: string;
}

export interface CreateUserPayload {
  email: string;
  fullName: string;
  phone?: string;
  roles: Role[];
}

export interface UpdateUserPayload {
  fullName?: string;
  phone?: string;
  email?: string;
}

export interface UsersFilters {
  role?: Role;
  active?: boolean;
  q?: string;
  page?: number;
  size?: number;
}
