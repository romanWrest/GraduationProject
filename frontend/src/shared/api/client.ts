import axios from 'axios';

const rawBase = import.meta.env.VITE_API_BASE_URL ?? '';
const trimmed = rawBase.replace(/\/$/, '');
// При пустом VITE_API_BASE_URL запросы идут same-origin (через Vite proxy в dev,
// либо через nginx-проксирование в проде).
const baseURL = `${trimmed}/api/v1`;

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});
