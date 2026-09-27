import { useEffect, useState } from 'react';
import api from '../services/api';

export default function CatalogoPage() {
  const [catalogo, setCatalogo] = useState({ categorias: [], prioridades: [] });
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const cargar = () => {
    setCargando(true);
    setError('');
    api.get('/v1/catalogo')
      .then((res) => {
        setCatalogo({
          categorias: res.data.filter((item) => item.descripcion !== undefined && item.nivel === undefined),
          prioridades: res.data.filter((item) => item.nivel !== undefined),
        });
      })
      .catch((requestError) => {
        setError(requestError.message || 'No se pudo cargar el catálogo');
      })
      .finally(() => setCargando(false));
  };

  useEffect(() => { cargar(); }, []);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">MesaTech</div>
        <nav className="nav">
          <a href="/">Dashboard</a>
          <a href="/solicitudes">Solicitudes</a>
          <a href="/catalogo">Catálogo</a>
        </nav>
      </aside>
      <main className="main">
        <h2>Catálogo</h2>
        {cargando && <p className="muted">Cargando catálogo...</p>}
        {error && <p className="error">{error}</p>}
        <div className="grid">
          <div className="card">
            <h3>Categorías</h3>
            <ul>
              {catalogo.categorias.map((c) => <li key={c.id}>{c.nombre}</li>)}
            </ul>
          </div>
          <div className="card">
            <h3>Prioridades</h3>
            <ul>
              {catalogo.prioridades.map((p) => <li key={p.id}>{p.nombre} (Nivel {p.nivel})</li>)}
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
