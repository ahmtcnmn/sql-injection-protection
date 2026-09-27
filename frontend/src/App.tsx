import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { AgentsListPage } from './pages/AgentsListPage';
import { AgentDetailPage } from './pages/AgentDetailPage';
import { EventsListPage } from './pages/EventsListPage';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/agents" element={<AgentsListPage />} />
        <Route path="/agents/:agentId" element={<AgentDetailPage />} />
        <Route path="/events" element={<EventsListPage />} />
      </Route>
    </Routes>
  );
}

export default App;
