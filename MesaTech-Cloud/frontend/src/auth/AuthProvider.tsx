import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { PublicClientApplication } from '@azure/msal-browser';
import { AUTH_MODE, loginRequest, msalConfig } from './authConfig';
import { getStoredUser, loginLocal, logoutLocal, UserRole } from './localAuth';

const AuthContext = createContext<any>(null);
export const msalInstance = new PublicClientApplication(msalConfig);

export const getAccessToken = async () => {
  if (AUTH_MODE !== 'entra') {
    const storedUser = getStoredUser();
    if (!storedUser) throw new Error('No autenticado');
    return storedUser.token;
  }

  await msalInstance.initialize();
  const account = msalInstance.getAllAccounts()[0];
  if (!account) {
    await msalInstance.loginRedirect(loginRequest);
    throw new Error('Redirigiendo a Microsoft Entra ID');
  }

  try {
    const result = await msalInstance.acquireTokenSilent({
      account,
      scopes: loginRequest.scopes,
    });

    return result.accessToken;
  } catch (error) {
    await msalInstance.acquireTokenRedirect({
      account,
      scopes: loginRequest.scopes,
    });
    throw error;
  }
};

const mapMsalUser = (account: any, accessToken?: string) => {
  if (!account) return null;

  return {
    id: account.localAccountId || account.homeAccountId || account.username,
    nombre: account.name || account.username,
    email: account.username,
    rol: 'CLIENTE',
    token: accessToken || 'entra-access-token',
    authMode: 'entra',
  };
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => AUTH_MODE === 'entra' ? null : getStoredUser());
  const [authError, setAuthError] = useState('');
  const redirectStarted = useRef(false);

  useEffect(() => {
    if (!user) {
      logoutLocal();
    }
  }, [user]);

  useEffect(() => {
    if (AUTH_MODE !== 'entra') return;

    const initializeAuthentication = async () => {
      try {
        await msalInstance.initialize();
        const redirectResult = await msalInstance.handleRedirectPromise();
        const account = redirectResult?.account || msalInstance.getAllAccounts()[0];

        if (account) {
          const result = redirectResult || await msalInstance.acquireTokenSilent({
            account,
            scopes: loginRequest.scopes,
            forceRefresh: true,
          });
          const mapped = mapMsalUser(result.account, result.accessToken);
          if (mapped) {
            localStorage.setItem('mesatech-user', JSON.stringify(mapped));
            setUser(mapped);
          }
          return;
        }

        if (!redirectStarted.current) {
          redirectStarted.current = true;
          await msalInstance.loginRedirect(loginRequest);
        }
      } catch (error) {
        console.error('No se pudo iniciar Microsoft Entra ID', error);
        if (AUTH_MODE === 'entra' && !redirectStarted.current) {
          redirectStarted.current = true;
          await msalInstance.loginRedirect(loginRequest);
          return;
        }
        setAuthError('No se pudo iniciar sesión con Microsoft Entra ID. Usa el botón para reintentar.');
      }
    };

    initializeAuthentication();
  }, []);

  const login = async (rol?: UserRole) => {
    if (AUTH_MODE === 'entra') {
      const result = await msalInstance.loginPopup(loginRequest);
      const tokenResult = await msalInstance.acquireTokenSilent({
        account: result.account,
        scopes: loginRequest.scopes,
        forceRefresh: true,
      }).catch(() => msalInstance.acquireTokenPopup({
        account: result.account,
        scopes: loginRequest.scopes,
      }));

      const mappedUser = mapMsalUser(tokenResult?.account || result.account, tokenResult?.accessToken || result.accessToken);
      if (!mappedUser) {
        throw new Error('No se pudo iniciar sesión con Microsoft Entra ID');
      }

      localStorage.setItem('mesatech-user', JSON.stringify(mappedUser));
      setUser(mappedUser);
      return mappedUser;
    }

    const loggedUser = loginLocal(rol || 'CLIENTE');
    setUser(loggedUser);
    return loggedUser;
  };

  const logout = async () => {
    if (AUTH_MODE === 'entra') {
      try {
        await msalInstance.logoutPopup();
      } catch {
        // Ignore popup logout failures and continue with local cleanup.
      }
    }

    logoutLocal();
    setUser(null);
  };

  const value = useMemo(() => ({ user, login, logout }), [user]);

  return (
    <AuthContext.Provider value={value}>
      {AUTH_MODE === 'entra' && !user && !authError
        ? <div className="login-box"><p>Redirigiendo a Microsoft Entra ID...</p></div>
        : children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
