import axios from 'axios';

export interface RootEntity<T> {
  result: boolean;
  messages: string[] | null;
  data: T;
}

export const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});
