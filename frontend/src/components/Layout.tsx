import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

export function Layout() {
  return (
    <div className="bg-background text-text-primary font-body-md text-body-md min-h-screen">
      <Sidebar />
      <Topbar />
      <main className="md:pl-sidebar-width pt-topbar-height min-h-screen flex flex-col">
        <Outlet />
      </main>
    </div>
  );
}
