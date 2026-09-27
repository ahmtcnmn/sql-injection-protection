import type { AgentStatus, EventSeverityLevel } from '../types';

export function isOnlineToStatus(isOnline: boolean): AgentStatus {
  return isOnline ? 'online' : 'offline';
}

// DashboardService.getEventsTimeline ile tutarlı: low=(1,2), medium=3, high=4, critical=5
export function severityToLevel(severity: number): EventSeverityLevel {
  if (severity <= 2) return 'low';
  if (severity === 3) return 'medium';
  if (severity === 4) return 'high';
  return 'critical';
}
