import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getAgent } from '../api/agents';
import { getEventTypeBreakdown } from '../api/events';
import type { AgentResponse, EventTypeCount } from '../types';

const tabs = ['Overview', 'Events', 'Command History', 'Config'] as const;
type Tab = (typeof tabs)[number];

export function AgentDetailPage() {
  const { agentId } = useParams();
  const numericId = Number(agentId);
  const [activeTab, setActiveTab] = useState<Tab>('Overview');
  const [agent, setAgent] = useState<AgentResponse | null>(null);
  const [breakdown, setBreakdown] = useState<EventTypeCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (Number.isNaN(numericId)) {
      setError('Invalid agent id');
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    Promise.all([getAgent(numericId), getEventTypeBreakdown(numericId)])
      .then(([a, b]) => {
        if (cancelled) return;
        setAgent(a);
        setBreakdown(b);
        setError(null);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load agent');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [numericId]);

  if (loading) {
    return <div className="p-container-padding text-on-surface-variant font-body-sm text-body-sm">Loading...</div>;
  }
  if (error || !agent) {
    return <div className="p-container-padding text-error font-body-sm text-body-sm">Failed to load data.</div>;
  }

  const isOnline = agent.isOnline;

  return (
    <div className="p-container-padding flex flex-col gap-element-gap">
      {/* Page Header: Agent Identity */}
      <header className="flex flex-col md:flex-row md:items-start justify-between gap-4 border-b border-border pb-6">
        <div className="flex flex-col gap-2">
          <div className="flex items-center gap-3">
            <span
              className={`w-2.5 h-2.5 rounded-full ${
                isOnline ? 'bg-success animate-pulse shadow-[0_0_8px_rgba(34,197,94,0.4)]' : 'bg-info'
              }`}
            />
            <h1 className="font-data-mono text-headline-lg font-semibold tracking-tight text-text-primary">{agent.hostname}</h1>
          </div>
          <div className="flex items-center gap-4 font-body-sm text-body-sm text-on-surface-variant">
            <span className="flex items-center gap-1">
              <span className="material-symbols-outlined text-[14px]">cell_tower</span> {isOnline ? 'Online' : 'Offline'}
            </span>
            <span className="w-1 h-1 rounded-full bg-border" />
            <span className="flex items-center gap-1">
              <span className="material-symbols-outlined text-[14px]">schedule</span> Last seen:{' '}
              {agent.lastSeen ? new Date(agent.lastSeen).toLocaleString() : 'Never'}
            </span>
            <span className="w-1 h-1 rounded-full bg-border" />
            <span className="flex items-center gap-1 font-data-mono text-data-mono">ID: {agent.id}</span>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button className="px-4 py-2 bg-surface text-text-primary font-headline-sm text-headline-sm border border-border hover:bg-hover hover:border-outline transition-colors rounded flex items-center gap-2">
            <span className="material-symbols-outlined text-[18px]">restart_alt</span>
            Restart Agent
          </button>
          <button className="px-4 py-2 bg-primary-container text-on-primary-container font-headline-sm text-headline-sm font-semibold hover:bg-primary transition-colors rounded flex items-center gap-2 shadow-[0_0_15px_rgba(34,211,238,0.15)]">
            <span className="material-symbols-outlined text-[18px]">terminal</span>
            Send Command
          </button>
        </div>
      </header>

      {/* In-Page Tabs */}
      <div className="flex items-center gap-8 border-b border-border">
        {tabs.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`pb-3 border-b-2 font-body-md text-body-md transition-colors ${
              activeTab === tab
                ? 'border-primary text-primary font-semibold'
                : 'border-transparent text-on-surface-variant hover:text-text-primary'
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {activeTab === 'Overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-element-gap mt-4">
          <div className="col-span-1 lg:col-span-2 bg-surface border border-border rounded p-6 flex flex-col gap-6">
            <h2 className="font-headline-sm text-headline-sm text-text-primary flex items-center gap-2">
              <span className="material-symbols-outlined text-on-surface-variant text-[20px]">memory</span>
              Agent Info
            </h2>
            <div className="grid grid-cols-2 gap-y-6 gap-x-8">
              <div className="flex flex-col gap-1">
                <span className="font-label-caps text-label-caps text-on-surface-variant">ID</span>
                <span className="font-data-mono text-data-mono text-text-primary">{agent.id}</span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="font-label-caps text-label-caps text-on-surface-variant">Hostname</span>
                <span className="font-data-mono text-data-mono text-text-primary">{agent.hostname}</span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="font-label-caps text-label-caps text-on-surface-variant">Status</span>
                <span className="font-data-mono text-data-mono text-text-primary">{isOnline ? 'Online' : 'Offline'}</span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="font-label-caps text-label-caps text-on-surface-variant">Last Seen</span>
                <span className="font-data-mono text-data-mono text-text-primary">
                  {agent.lastSeen ? new Date(agent.lastSeen).toLocaleString() : 'Never'}
                </span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="font-label-caps text-label-caps text-on-surface-variant">Registered At</span>
                <span className="font-data-mono text-data-mono text-text-primary">{new Date(agent.createdAt).toLocaleString()}</span>
              </div>
            </div>
          </div>

          <div className="col-span-1 bg-surface border border-border rounded p-6 flex flex-col gap-4">
            <h2 className="font-headline-sm text-headline-sm text-text-primary flex items-center justify-between">
              <span className="flex items-center gap-2">
                <span className="material-symbols-outlined text-on-surface-variant text-[20px]">pie_chart</span> Event Type Breakdown
              </span>
            </h2>
            <div className="flex-grow flex flex-col justify-center gap-5 pt-2">
              {breakdown.length === 0 && (
                <p className="font-body-sm text-body-sm text-on-surface-variant">No events recorded.</p>
              )}
              {breakdown.map((item) => (
                <div key={item.type} className="flex flex-col gap-2">
                  <div className="flex justify-between items-end font-body-sm text-body-sm">
                    <span className="text-text-primary flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full bg-primary" /> {item.type}
                    </span>
                    <span className="font-data-mono text-data-mono text-on-surface-variant">
                      {item.percentage.toFixed(1)}% ({item.count})
                    </span>
                  </div>
                  <div className="w-full h-1.5 bg-surface-variant rounded overflow-hidden">
                    <div className="h-full bg-primary" style={{ width: `${item.percentage}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {activeTab !== 'Overview' && (
        <div className="mt-4 bg-surface border border-border rounded p-12 flex items-center justify-center text-on-surface-variant font-body-sm text-body-sm">
          {activeTab} content coming soon.
        </div>
      )}
    </div>
  );
}
