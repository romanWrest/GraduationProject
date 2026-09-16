import type { Role } from '@/shared/constants/roles';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

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
