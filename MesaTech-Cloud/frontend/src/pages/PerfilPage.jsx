import { useAuth } from '../auth/AuthProvider';

export default function PerfilPage() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">MesaTech</div>
        <nav className="nav">
          <a href="/">Dashboard</a>
          <a href="/solicitudes">Solicitudes</a>
          <a href="/perfil">Perfil</a>
        </nav>
      </aside>
      <main className="main">
        <h2>Perfil</h2>
        <div className="card">
          <p><strong>Nombre:</strong> {user?.nombre}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          <p><strong>Rol:</strong> {user?.rol}</p>
          <button className="danger" onClick={logout}>Cerrar sesión</button>
        </div>
      </main>
    </div>
  );
}
