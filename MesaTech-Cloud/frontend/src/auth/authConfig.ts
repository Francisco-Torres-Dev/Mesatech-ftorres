export const AUTH_MODE = (import.meta.env.VITE_AUTH_MODE || 'entra').toLowerCase();

const tenantId = import.meta.env.VITE_ENTRA_TENANT_ID || '2b28c657-3169-48ee-91b9-d36cede4fe70';
const clientId = import.meta.env.VITE_ENTRA_CLIENT_ID || 'bf7f454a-0b2d-4faa-abcb-5c5d4a315ac6';
const redirectUri = import.meta.env.PROD
  ? window.location.origin
  : import.meta.env.VITE_ENTRA_REDIRECT_URI || 'http://localhost:5173';

export const apiConfig = {
  baseUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
};

export const msalConfig = {
  auth: {
    clientId,
    authority: `https://login.microsoftonline.com/${tenantId}`,
    redirectUri,
    knownAuthorities: [`login.microsoftonline.com`, `login.windows.net`],
    navigateToLoginRequestUrl: false,
  },
  cache: {
    cacheLocation: 'sessionStorage',
    storeAuthStateInCookie: false,
  },
};

export const loginRequest = {
  scopes: [import.meta.env.VITE_ENTRA_API_SCOPE],
};
