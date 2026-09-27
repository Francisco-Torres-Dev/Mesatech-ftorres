import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import api from '../services/api';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const [solicitudes, setSolicitudes] = useState([]);

  useEffect(() => {
    if (!user) return;
    api.get('/v1/solicitudes').then((res) => setSolicitudes(res.data)).catch(() => setSolicitudes([]));
  }, [user]);

  const pending = solicitudes.filter((s) => s.estado !== 'CERRADA' && s.estado !== 'CANCELADA').length;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">MesaTech</div>
        <nav className="nav">
          <Link to="/">Dashboard</Link>
          <Link to="/solicitudes">Solicitudes</Link>
          <Link to="/solicitudes/nueva">Crear solicitud</Link>
          <Link to="/catalogo">Catálogo</Link>
          <Link to="/perfil">Perfil</Link>
          <button className="secondary" onClick={logout}>Cerrar sesión</button>
        </nav>
      </aside>
      <main className="main">
        <div className="user-topbar">
          <div>
            <h2>Dashboard</h2>
            <div className="muted">{user?.nombre} · {user?.rol}</div>
          </div>
          <div className="muted">{user?.email}</div>
        </div>

        <div className="grid">
          <div className="card metric">
            <div className="muted">Solicitudes</div>
            <h3>{solicitudes.length}</h3>
          </div>
          <div className="card metric">
            <div className="muted">Pendientes</div>
            <h3>{pending}</h3>
          </div>
          <div className="card metric">
            <div className="muted">Rol</div>
            <h3>{user?.rol}</h3>
          </div>
        </div>
      </main>
    </div>
  );
}
