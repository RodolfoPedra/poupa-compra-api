"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { AuthResponse, UserResponse, me as fetchMe, logout as logoutRequest, refreshSession } from "./api";

const ACCESS_TOKEN_KEY = "poupa-compra.access-token";
const REFRESH_TOKEN_KEY = "poupa-compra.refresh-token";

type SessionContextValue = {
  usuario: UserResponse | null;
  accessToken: string | null;
  carregando: boolean;
  iniciarSessao: (auth: AuthResponse) => void;
  encerrarSessao: () => Promise<void>;
};

const SessionContext = createContext<SessionContextValue | null>(null);

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [usuario, setUsuario] = useState<UserResponse | null>(null);
  const [carregando, setCarregando] = useState(true);

  const iniciarSessao = useCallback((auth: AuthResponse) => {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
    setAccessToken(auth.accessToken);
    setUsuario(auth.usuario);
  }, []);

  const limparSessao = useCallback(() => {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    setAccessToken(null);
    setUsuario(null);
  }, []);

  const encerrarSessao = useCallback(async () => {
    const refreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY);
    if (refreshToken) {
      await logoutRequest(refreshToken).catch(() => undefined);
    }
    limparSessao();
  }, [limparSessao]);

  // Ao carregar a página, tenta restaurar a sessão a partir dos tokens salvos.
  useEffect(() => {
    async function restaurarSessao() {
      const storedAccessToken = sessionStorage.getItem(ACCESS_TOKEN_KEY);
      const storedRefreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY);

      if (storedAccessToken) {
        try {
          const usuarioAtual = await fetchMe(storedAccessToken);
          setAccessToken(storedAccessToken);
          setUsuario(usuarioAtual);
          setCarregando(false);
          return;
        } catch {
          // access token expirado ou inválido; tenta renovar pelo refresh token abaixo.
        }
      }

      if (storedRefreshToken) {
        try {
          iniciarSessao(await refreshSession(storedRefreshToken));
        } catch {
          limparSessao();
        }
      }
      setCarregando(false);
    }

    restaurarSessao();
  }, [iniciarSessao, limparSessao]);

  const value = useMemo(
    () => ({ usuario, accessToken, carregando, iniciarSessao, encerrarSessao }),
    [usuario, accessToken, carregando, iniciarSessao, encerrarSessao]
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error("useSession deve ser usado dentro de SessionProvider");
  }
  return context;
}
