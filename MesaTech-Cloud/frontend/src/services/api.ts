import axios from 'axios';
import { AUTH_MODE, apiConfig } from '../auth/authConfig';
import { getAccessToken } from '../auth/AuthProvider';

export const api = axios.create({
  baseURL: apiConfig.baseUrl,
  timeout: 15000,
});

api.interceptors.request.use(async (config) => {
  if (AUTH_MODE === 'entra') {
    config.headers.Authorization = `Bearer ${await getAccessToken()}`;
    return config;
  }

  const user = localStorage.getItem('mesatech-user');
  if (user) {
    const parsed = JSON.parse(user);
    config.headers.Authorization = `Bearer ${parsed.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || 'Error en la solicitud';
    const status = error?.response?.status;

    if (status === 401) {
      throw new Error('No autenticado');
    }
    if (status === 403) {
      throw new Error('No tienes permisos para realizar esta acción');
    }
    if (status === 404) {
      throw new Error('Solicitud no encontrada');
    }
    if (status === 409) {
      throw new Error('Transición de estado no permitida');
    }
    throw new Error(message);
  }
);

export default api;
