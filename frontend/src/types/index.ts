export type AgentStatus = 'online' | 'offline' | 'high-load' | 'unresponsive';

export type EventSeverityLevel = 'info' | 'low' | 'medium' | 'high' | 'critical';

export interface AgentResponse {
  id: number;
  hostname: string;
  isOnline: boolean;
  lastSeen: string | null;
  createdAt: string;
}

export interface EventResponse {
  id: number;
  agentId: number;
  agentHostname: string;
  eventType: string;
  timestamp: string;
  severity: number;
  rawData: string;
}

export interface DashboardSummary {
  totalAgents: number;
  onlineAgents: number;
  eventsTotal: number;
  criticalEventsToday: number;
}

export interface TimelinePoint {
  hour: string;
  low: number;
  medium: number;
  high: number;
  critical: number;
}

export interface EventTypeCount {
  type: string;
  count: number;
  percentage: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
