import { useEffect, useState } from 'react';
import api from '../services/api';

export default function CatalogoPage() {
  const [catalogo, setCatalogo] = useState({ categorias: [], prioridades: [] });
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  
  // Estado para creación de categoría
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [creando, setCreando] = useState(false);
  const [mensajeExito, setMensajeExito] = useState('');

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

  const handleCrearCategoria = async (e) => {
    e.preventDefault();
    if (!nombre.trim()) {
      alert('Por favor ingresa un nombre para la categoría');
      return;
    }

    setCreando(true);
    setError('');
    setMensajeExito('');

    try {
      await api.post('/v1/catalogo/categorias', {
        nombre: nombre.trim(),
        descripcion: descripcion.trim() || 'Categoría de soporte técnico',
      });
      setMensajeExito(`¡Categoría "${nombre}" creada en RDS Catálogo y evento "categoria.creada" emitido a RabbitMQ!`);
      setNombre('');
      setDescripcion('');
      cargar();
    } catch (err) {
      setError(err.message || 'Error al crear la categoría');
    } finally {
      setCreando(false);
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
        <h2>Catálogo de Soporte</h2>
        {cargando && <p className="muted">Cargando catálogo...</p>}
        {error && <p className="error" style={{ color: '#fc8181', padding: '10px', background: 'rgba(254, 178, 178, 0.1)', borderRadius: '8px' }}>{error}</p>}
        {mensajeExito && <p style={{ color: '#68d391', padding: '10px', background: 'rgba(104, 211, 145, 0.1)', borderRadius: '8px' }}>{mensajeExito}</p>}

        {/* Formulario para Crear Categoría y Disparar Evento en RabbitMQ */}
        <div className="card" style={{ marginBottom: '24px' }}>
          <h3>➕ Nueva Categoría (Emite Evento a RabbitMQ)</h3>
          <p className="muted" style={{ fontSize: '14px', marginBottom: '14px' }}>
            Al crear una categoría, el microservicio Catálogo la guarda en <strong>RDS Catálogo</strong> y publica el evento <code>categoria.creada</code> al exchange <code>catalogo.exchange</code> en RabbitMQ.
          </p>
          <form className="form" onSubmit={handleCrearCategoria}>
            <label>
              Nombre de la Categoría
              <input
                type="text"
                placeholder="Ej: Servidores Cloud, Ciberseguridad 2, etc."
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                required
              />
            </label>
            <label>
              Descripción
              <input
                type="text"
                placeholder="Ej: Problemas con infraestructura y balanceadores"
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
              />
            </label>
            <button type="submit" disabled={creando}>
              {creando ? 'Creando y Publicando...' : 'Crear Categoría y Emitir Evento AMQP'}
            </button>
          </form>
        </div>

        <div className="grid">
          <div className="card">
            <h3>Categorías Maestras ({catalogo.categorias.length})</h3>
            <ul>
              {catalogo.categorias.map((c) => (
                <li key={c.id} style={{ marginBottom: '8px' }}>
                  <strong>{c.nombre}</strong> (ID: {c.id})
                  {c.descripcion && <span className="muted" style={{ display: 'block', fontSize: '13px' }}>{c.descripcion}</span>}
                </li>
              ))}
            </ul>
          </div>
          <div className="card">
            <h3>Prioridades ({catalogo.prioridades.length})</h3>
            <ul>
              {catalogo.prioridades.map((p) => (
                <li key={p.id} style={{ marginBottom: '8px' }}>
                  <strong>{p.nombre}</strong> — Nivel {p.nivel}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
