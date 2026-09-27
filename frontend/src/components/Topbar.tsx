export function Topbar() {
  return (
    <header className="fixed top-0 right-0 w-full md:w-[calc(100%-240px)] h-topbar-height bg-background border-b border-border flex items-center justify-between px-container-padding z-30">
      <div className="flex items-center gap-4 w-1/3">
        <button className="md:hidden text-text-primary hover:text-primary transition-opacity">
          <span className="material-symbols-outlined">menu</span>
        </button>
        <div className="relative w-full max-w-md hidden md:block">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            className="w-full bg-background border border-border rounded pl-9 pr-3 py-1.5 font-body-sm text-body-sm text-text-primary focus:outline-none focus:border-primary-container transition-colors placeholder:text-on-surface-variant"
            placeholder="Global search..."
            type="text"
          />
        </div>
      </div>
      <div className="flex items-center gap-2">
        <button
          className="p-2 text-on-surface-variant hover:text-primary transition-opacity cursor-pointer active:opacity-80 rounded-full hover:bg-hover"
          title="notifications"
        >
          <span className="material-symbols-outlined">notifications</span>
        </button>
        <button
          className="p-2 text-on-surface-variant hover:text-primary transition-opacity cursor-pointer active:opacity-80 rounded-full hover:bg-hover"
          title="apps"
        >
          <span className="material-symbols-outlined">apps</span>
        </button>
        <button
          className="p-2 text-on-surface-variant hover:text-primary transition-opacity cursor-pointer active:opacity-80 rounded-full hover:bg-hover"
          title="account_circle"
        >
          <span className="material-symbols-outlined">account_circle</span>
        </button>
      </div>
    </header>
  );
}
