export type UserRole = 'CLIENTE' | 'OPERADOR' | 'ADMINISTRADOR';

export type LocalUser = {
  id: string;
  nombre: string;
  email: string;
  rol: UserRole;
  token: string;
  authMode: 'local';
};

const users: Record<UserRole, LocalUser> = {
  CLIENTE: {
    id: '11111111-1111-1111-1111-111111111111',
    nombre: 'Cliente Demo',
    email: 'cliente@mesatech.local',
    rol: 'CLIENTE',
    token: 'local-token-cliente',
    authMode: 'local',
  },
  OPERADOR: {
    id: '22222222-2222-2222-2222-222222222222',
    nombre: 'Operador Demo',
    email: 'operador@mesatech.local',
    rol: 'OPERADOR',
    token: 'local-token-operador',
    authMode: 'local',
  },
  ADMINISTRADOR: {
    id: '33333333-3333-3333-3333-333333333333',
    nombre: 'Admin Demo',
    email: 'admin@mesatech.local',
    rol: 'ADMINISTRADOR',
    token: 'local-token-admin',
    authMode: 'local',
  },
};

export const loginLocal = (rol: UserRole): LocalUser => {
  const user = users[rol];
  if (!user) throw new Error('Rol no válido');
  localStorage.setItem('mesatech-user', JSON.stringify(user));
  return user;
};

export const getStoredUser = (): LocalUser | null => {
  const raw = localStorage.getItem('mesatech-user');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as LocalUser;
  } catch {
    return null;
  }
};

export const logoutLocal = () => localStorage.removeItem('mesatech-user');
