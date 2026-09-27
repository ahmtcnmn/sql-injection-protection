import { apiClient } from './client';
import type { DashboardSummary, TimelinePoint } from '../types';

export function getDashboardSummary() {
  return apiClient.get<DashboardSummary>('/dashboard/summary').then((r) => r.data);
}

export function getEventsTimeline(hours = 24) {
  return apiClient.get<TimelinePoint[]>('/dashboard/events-timeline', { params: { hours } }).then((r) => r.data);
}
