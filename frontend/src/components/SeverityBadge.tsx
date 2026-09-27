import type { EventSeverityLevel } from '../types';

const severityConfig: Record<EventSeverityLevel, { label: string; classes: string; dot: string }> = {
  info: { label: 'INFO', classes: 'bg-primary/15 text-primary border-primary/30', dot: 'bg-primary' },
  low: { label: 'LOW', classes: 'bg-success/15 text-success border-success/30', dot: 'bg-success' },
  medium: { label: 'WARNING', classes: 'bg-warning/15 text-warning border-warning/30', dot: 'bg-warning' },
  high: { label: 'HIGH', classes: 'bg-high-severity/15 text-high-severity border-high-severity/30', dot: 'bg-high-severity' },
  critical: { label: 'CRITICAL', classes: 'bg-error/15 text-error border-error/30', dot: 'bg-error' },
};

export function SeverityBadge({ severity }: { severity: EventSeverityLevel }) {
  const config = severityConfig[severity];
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-sm font-label-caps text-label-caps font-semibold border ${config.classes}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
}
