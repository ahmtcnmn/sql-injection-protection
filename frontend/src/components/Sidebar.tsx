import { NavLink } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Dashboard', icon: 'dashboard', end: true },
  { to: '/agents', label: 'Agents', icon: 'security' },
  { to: '/events', label: 'Events', icon: 'notifications' },
  { to: '/settings', label: 'Settings', icon: 'settings' },
];

export function Sidebar() {
  return (
    <nav className="fixed left-0 top-0 h-full w-sidebar-width bg-surface border-r border-border flex flex-col py-inner-padding z-40 hidden md:flex">
      <div className="px-container-padding mb-8 flex items-center gap-3">
        <div className="w-10 h-10 rounded bg-primary/10 flex items-center justify-center border border-primary/20">
          <span className="material-symbols-outlined text-primary text-xl">security</span>
        </div>
        <div>
          <h1 className="font-headline-sm text-headline-sm font-bold text-primary leading-tight">Obsidian Sentinel</h1>
          <p className="font-body-sm text-body-sm text-on-surface-variant">Vigilant Monitoring</p>
        </div>
      </div>

      <div className="flex-1 px-inner-padding space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded transition-colors active:scale-95 duration-150 ${
                isActive
                  ? 'text-primary bg-hover font-semibold'
                  : 'text-on-surface-variant hover:bg-hover hover:text-primary'
              }`
            }
          >
            <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
            <span className="font-body-md text-body-md">{item.label}</span>
          </NavLink>
        ))}
      </div>

      <div className="px-inner-padding mt-auto">
        <a
          className="flex items-center gap-3 px-3 py-2 rounded text-on-surface-variant hover:bg-hover hover:text-primary transition-colors active:scale-95 duration-150"
          href="#"
        >
          <span className="material-symbols-outlined text-[20px]">account_circle</span>
          <span className="font-body-md text-body-md">User Profile</span>
        </a>
      </div>
    </nav>
  );
}
