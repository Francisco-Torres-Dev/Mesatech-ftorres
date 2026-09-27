import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AUTH_MODE } from '../auth/authConfig';
import { useAuth } from '../auth/AuthProvider';

const roles = ['CLIENTE', 'OPERADOR', 'ADMINISTRADOR'];

export default function LoginPage() {
  const [rol, setRol] = useState('CLIENTE');
  const { login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      if (AUTH_MODE === 'entra') {
        await login();
      } else {
        await login(rol);
      }
      navigate('/');
    } catch (error) {
      alert(error.message || 'No se pudo iniciar sesión');
    }
  };

  return (
    <div className="login-box">
      <h2>Iniciar sesión</h2>
      <p className="muted">{AUTH_MODE === 'entra' ? 'Microsoft Entra ID • MesaTech Cloud' : 'Modo local • MesaTech Cloud'}</p>
      <form className="form" onSubmit={onSubmit}>
        {AUTH_MODE !== 'entra' && (
          <label>
            Perfil
            <select value={rol} onChange={(e) => setRol(e.target.value)}>
              {roles.map((role) => (
                <option key={role} value={role}>{role}</option>
              ))}
            </select>
          </label>
        )}
        <button type="submit">{AUTH_MODE === 'entra' ? 'Entrar con Microsoft' : 'Entrar'}</button>
      </form>
    </div>
  );
}
