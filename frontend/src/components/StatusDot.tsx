import type { AgentStatus } from '../types';

const statusColor: Record<AgentStatus, string> = {
  online: 'bg-success shadow-[0_0_8px_rgba(34,197,94,0.5)]',
  offline: 'bg-info',
  'high-load': 'bg-warning shadow-[0_0_8px_rgba(245,158,11,0.5)]',
  unresponsive: 'bg-error shadow-[0_0_8px_rgba(239,68,68,0.5)] animate-pulse',
};

export function StatusDot({ status, className = '' }: { status: AgentStatus; className?: string }) {
  return <span className={`w-2 h-2 rounded-full inline-block ${statusColor[status]} ${className}`} />;
}
