import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import SolicitudesPage from './pages/SolicitudesPage';
import CreateSolicitudPage from './pages/CreateSolicitudPage';
import CatalogoPage from './pages/CatalogoPage';
import PerfilPage from './pages/PerfilPage';
import { AuthProvider, useAuth } from './auth/AuthProvider';

function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

function AppRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/solicitudes" element={<ProtectedRoute><SolicitudesPage /></ProtectedRoute>} />
      <Route path="/solicitudes/nueva" element={<ProtectedRoute><CreateSolicitudPage /></ProtectedRoute>} />
      <Route path="/catalogo" element={<ProtectedRoute><CatalogoPage /></ProtectedRoute>} />
      <Route path="/perfil" element={<ProtectedRoute><PerfilPage /></ProtectedRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
