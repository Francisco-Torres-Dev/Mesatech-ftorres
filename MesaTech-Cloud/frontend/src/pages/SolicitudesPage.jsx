import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthProvider';
import api from '../services/api';

export default function SolicitudesPage() {
  const { user } = useAuth();
  const [solicitudes, setSolicitudes] = useState([]);

  useEffect(() => {
    if (!user) return;
    const endpoint = user.rol === 'CLIENTE' ? `/v1/solicitudes/mias?email=${encodeURIComponent(user.email)}` : '/v1/solicitudes';
    api.get(endpoint).then((res) => setSolicitudes(res.data)).catch((err) => alert(err.message));
  }, [user]);

  const updateEstado = async (id, estado) => {
    try {
      await api.put(`/v1/solicitudes/${id}/estado`, { estado, observacion: 'Actualización desde frontend local' });
      const res = await api.get(user.rol === 'CLIENTE' ? `/v1/solicitudes/mias?email=${encodeURIComponent(user.email)}` : '/v1/solicitudes');
      setSolicitudes(res.data);
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">MesaTech</div>
        <nav className="nav">
          <a href="/">Dashboard</a>
          <a href="/solicitudes">Solicitudes</a>
          <a href="/solicitudes/nueva">Crear solicitud</a>
          <a href="/catalogo">Catálogo</a>
          <a href="/perfil">Perfil</a>
        </nav>
      </aside>
      <main className="main">
        <h2>Solicitudes</h2>
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Título</th>
                <th>Estado</th>
                <th>Prioridad</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {solicitudes.map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td>{s.titulo}</td>
                  <td><span className="status-pill">{s.estado}</span></td>
                  <td>{s.prioridadNombre}</td>
                  <td>
                    {user?.rol !== 'CLIENTE' && (
                      <>
                        <button type="button" onClick={() => updateEstado(s.id, 'ASIGNADA')}>Asignar</button>
                        <button type="button" className="secondary" onClick={() => updateEstado(s.id, 'EN_PROCESO')}>En proceso</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
