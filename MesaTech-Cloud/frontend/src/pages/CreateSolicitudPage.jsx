import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import api from '../services/api';

export default function CreateSolicitudPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [categoriaId, setCategoriaId] = useState('');
  const [prioridadId, setPrioridadId] = useState('');
  const [categorias, setCategorias] = useState([]);
  const [prioridades, setPrioridades] = useState([]);

  useEffect(() => {
    api.get('/v1/catalogo').then((res) => {
      const categoriasList = res.data.filter((item) => item.nombre && item.descripcion !== undefined && !('nivel' in item));
      const prioridadesList = res.data.filter((item) => item.nivel !== undefined);
      setCategorias(categoriasList);
      setPrioridades(prioridadesList);
    });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/v1/solicitudes', {
        titulo,
        descripcion,
        categoriaId: Number(categoriaId),
        prioridadId: Number(prioridadId),
        usuarioEmail: user.email,
      });
      navigate('/solicitudes');
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
        </nav>
      </aside>
      <main className="main">
        <h2>Crear solicitud</h2>
        <form className="form card" onSubmit={handleSubmit}>
          <label>
            Título
            <input value={titulo} onChange={(e) => setTitulo(e.target.value)} minLength="5" maxLength="150" required />
          </label>
          <label>
            Descripción
            <textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} minLength="10" maxLength="2000" rows="4" required />
          </label>
          <label>
            Categoría
            <select value={categoriaId} onChange={(e) => setCategoriaId(e.target.value)} required>
              <option value="">Seleccione</option>
              {categorias.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </label>
          <label>
            Prioridad
            <select value={prioridadId} onChange={(e) => setPrioridadId(e.target.value)} required>
              <option value="">Seleccione</option>
              {prioridades.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}
            </select>
          </label>
          <button type="submit">Guardar solicitud</button>
        </form>
      </main>
    </div>
  );
}
