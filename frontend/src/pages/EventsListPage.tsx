import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listEvents } from '../api/events';
import { SeverityBadge } from '../components/SeverityBadge';
import { severityToLevel } from '../lib/mappers';
import type { EventResponse, Page } from '../types';

function formatRawData(rawData: string) {
  try {
    return JSON.stringify(JSON.parse(rawData), null, 2);
  } catch {
    return rawData;
  }
}

export function EventsListPage() {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<Page<EventResponse> | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<EventResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    listEvents({ page, size: 20, sort: 'timestamp,desc' })
      .then((data) => {
        if (cancelled) return;
        setPageData(data);
        setError(null);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load events');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [page]);

  const events = pageData?.content ?? [];

  return (
    <div className="flex-1 flex flex-col relative overflow-hidden">
      {/* Header & Filter Bar */}
      <div className="flex-none p-container-padding pb-0 border-b border-border bg-surface">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-4 gap-4">
          <div>
            <h2 className="font-headline-lg text-headline-lg font-bold text-text-primary tracking-tight">Events</h2>
            <p className="font-body-sm text-body-sm text-on-surface-variant mt-1">
              Real-time system telemetry and security alerts.
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button className="flex items-center gap-2 px-3 py-1.5 border border-border text-text-primary font-body-sm text-body-sm rounded hover:bg-hover transition-colors">
              <span className="material-symbols-outlined text-[16px]">download</span>
              Export
            </button>
            <button className="flex items-center gap-2 px-3 py-1.5 bg-primary-container text-on-primary-container font-body-sm text-body-sm rounded hover:opacity-90 transition-opacity font-semibold">
              <span className="material-symbols-outlined text-[16px]">pause</span>
              Pause Stream
            </button>
          </div>
        </div>

        <div className="flex flex-wrap gap-element-gap mb-4">
          <div className="flex flex-col gap-1 min-w-[150px]">
            <label className="font-label-caps text-label-caps text-on-surface-variant">Agent</label>
            <select className="bg-background border border-border text-on-surface font-body-sm text-body-sm rounded px-3 py-1.5 focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container h-8">
              <option>All Agents</option>
            </select>
          </div>
          <div className="flex flex-col gap-1 min-w-[200px]">
            <label className="font-label-caps text-label-caps text-on-surface-variant">Event Type</label>
            <button className="w-full bg-background border border-border text-on-surface font-body-sm text-body-sm rounded px-3 py-1.5 flex justify-between items-center focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container h-8 text-left">
              <span className="truncate">All Types</span>
              <span className="material-symbols-outlined text-[16px] text-on-surface-variant">arrow_drop_down</span>
            </button>
          </div>
          <div className="flex flex-col gap-1">
            <label className="font-label-caps text-label-caps text-on-surface-variant">Timeframe</label>
            <div className="flex items-center gap-2">
              <input
                className="bg-background border border-border text-on-surface font-body-sm text-body-sm rounded px-2 py-1.5 focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container h-8 w-[170px]"
                type="datetime-local"
              />
              <span className="text-on-surface-variant">-</span>
              <input
                className="bg-background border border-border text-on-surface font-body-sm text-body-sm rounded px-2 py-1.5 focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container h-8 w-[170px]"
                type="datetime-local"
              />
            </div>
          </div>
          <div className="flex flex-col gap-1">
            <label className="font-label-caps text-label-caps text-on-surface-variant">Min Severity</label>
            <div className="flex bg-background border border-border rounded overflow-hidden h-8">
              <button className="px-3 text-body-sm font-body-sm text-on-surface-variant hover:bg-hover border-r border-border">
                Info
              </button>
              <button className="px-3 text-body-sm font-body-sm text-on-surface-variant hover:bg-hover border-r border-border">
                Warn
              </button>
              <button className="px-3 text-body-sm font-body-sm text-on-surface-variant hover:bg-hover">Crit</button>
            </div>
          </div>
        </div>
      </div>

      {/* Data Table */}
      <div className="flex-1 overflow-auto bg-background p-container-padding">
        {loading && <div className="text-on-surface-variant font-body-sm text-body-sm">Loading...</div>}
        {!loading && error && <div className="text-error font-body-sm text-body-sm">Failed to load data.</div>}
        {!loading && !error && (
          <>
            <div className="w-full border border-border rounded bg-surface">
              <table className="w-full text-left border-collapse">
                <thead className="sticky top-0 bg-surface z-10 shadow-[0_1px_0_0_#2A333D]">
                  <tr>
                    <th className="py-3 px-4 font-label-caps text-label-caps text-on-surface-variant w-[180px]">Timestamp</th>
                    <th className="py-3 px-4 font-label-caps text-label-caps text-on-surface-variant w-[140px]">Severity</th>
                    <th className="py-3 px-4 font-label-caps text-label-caps text-on-surface-variant w-[150px]">Agent</th>
                    <th className="py-3 px-4 font-label-caps text-label-caps text-on-surface-variant w-[120px]">Type</th>
                    <th className="py-3 px-4 font-label-caps text-label-caps text-on-surface-variant">Raw Data (Truncated)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border font-body-sm text-body-sm">
                  {events.map((event) => (
                    <tr
                      key={event.id}
                      className="hover:bg-hover transition-colors cursor-pointer"
                      onClick={() => setSelectedEvent(event)}
                    >
                      <td className="py-2 px-4 text-on-surface-variant whitespace-nowrap">
                        {new Date(event.timestamp).toLocaleString()}
                      </td>
                      <td className="py-2 px-4">
                        <SeverityBadge severity={severityToLevel(event.severity)} />
                      </td>
                      <td className="py-2 px-4">
                        <Link
                          className="text-primary hover:underline"
                          to={`/agents/${event.agentId}`}
                          onClick={(e) => e.stopPropagation()}
                        >
                          {event.agentHostname}
                        </Link>
                      </td>
                      <td className="py-2 px-4">
                        <span className="border border-border text-on-surface-variant px-1.5 py-0.5 rounded text-[10px] uppercase tracking-wider">
                          {event.eventType}
                        </span>
                      </td>
                      <td className="py-2 px-4">
                        <div className="font-data-mono text-data-mono text-on-surface-variant truncate max-w-[400px]">
                          {event.rawData}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="mt-4 flex justify-between items-center text-on-surface-variant font-body-sm text-body-sm">
              <span>
                {pageData ? `Showing ${pageData.numberOfElements} of ${pageData.totalElements} events` : ''}
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={!pageData || pageData.first}
                  className="px-2 py-1 border border-border rounded hover:bg-hover disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Prev
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={!pageData || pageData.last}
                  className="px-2 py-1 border border-border rounded hover:bg-hover disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Next
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Event Detail Drawer */}
      <div
        className={`absolute top-0 right-0 h-full w-full sm:w-[80%] md:w-[40%] bg-surface border-l border-border shadow-[-10px_0_30px_rgba(0,0,0,0.5)] transform transition-transform duration-300 ease-in-out z-20 flex flex-col ${
          selectedEvent ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        {selectedEvent && (
          <>
            <div className="flex items-center justify-between p-4 border-b border-border">
              <div className="flex items-center gap-3">
                <SeverityBadge severity={severityToLevel(selectedEvent.severity)} />
                <h3 className="font-headline-sm text-headline-sm font-bold text-text-primary">Event Details</h3>
              </div>
              <button
                className="text-on-surface-variant hover:text-text-primary transition-colors"
                onClick={() => setSelectedEvent(null)}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            <div className="flex-1 overflow-auto p-4 flex flex-col gap-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="font-label-caps text-label-caps text-on-surface-variant mb-1">Timestamp</p>
                  <p className="font-data-mono text-data-mono text-on-surface">
                    {new Date(selectedEvent.timestamp).toLocaleString()}
                  </p>
                </div>
                <div>
                  <p className="font-label-caps text-label-caps text-on-surface-variant mb-1">Agent</p>
                  <Link className="font-data-mono text-data-mono text-primary hover:underline" to={`/agents/${selectedEvent.agentId}`}>
                    {selectedEvent.agentHostname}
                  </Link>
                </div>
                <div>
                  <p className="font-label-caps text-label-caps text-on-surface-variant mb-1">Event Type</p>
                  <p className="font-body-sm text-body-sm text-on-surface">{selectedEvent.eventType}</p>
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <div className="flex justify-between items-end">
                  <p className="font-label-caps text-label-caps text-on-surface-variant">Raw Payload</p>
                  <button className="text-primary font-body-sm text-[11px] hover:underline flex items-center gap-1">
                    <span className="material-symbols-outlined text-[14px]">content_copy</span> Copy
                  </button>
                </div>
                <div className="bg-background border border-border rounded p-4 overflow-x-auto">
                  <pre className="font-data-mono text-data-mono text-text-primary whitespace-pre-wrap break-words">
                    {formatRawData(selectedEvent.rawData)}
                  </pre>
                </div>
              </div>

              <div className="mt-auto pt-4 border-t border-border flex gap-3">
                <button className="flex-1 bg-primary-container text-on-primary-container font-body-sm text-body-sm py-2 rounded font-semibold hover:opacity-90 transition-opacity">
                  Acknowledge Alert
                </button>
                <button className="flex-1 border border-border text-text-primary font-body-sm text-body-sm py-2 rounded hover:bg-hover transition-colors">
                  Create Ticket
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
