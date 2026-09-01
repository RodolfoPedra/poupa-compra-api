"use client";

import { useState } from "react";
import { loginGoogle } from "../lib/api";
import { useSession } from "../lib/session";

declare global {
  interface Window {
    google?: {
      accounts: {
        oauth2: {
          initCodeClient: (options: {
            client_id: string;
            scope: string;
            ux_mode: "popup";
            callback: (response: { code?: string; error?: string }) => void;
          }) => { requestCode: () => void };
        };
      };
    };
  }
}

type GoogleLoginButtonProps = {
  carregado?: boolean;
  onErro?: (mensagem: string) => void;
};

export default function GoogleLoginButton({ carregado = true, onErro }: GoogleLoginButtonProps) {
  const { iniciarSessao } = useSession();
  const [processando, setProcessando] = useState(false);

  function iniciarLogin() {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    if (!clientId) {
      onErro?.("Configure o client ID do Google no frontend.");
      return;
    }
    if (!carregado || !window.google) {
      onErro?.("O serviço de login Google ainda está carregando. Tente novamente em instantes.");
      return;
    }

    const client = window.google.accounts.oauth2.initCodeClient({
      client_id: clientId,
      scope: "openid email profile",
      ux_mode: "popup",
      callback: async (response) => {
        if (!response.code) {
          onErro?.("Não foi possível concluir o login Google.");
          return;
        }

        setProcessando(true);
        try {
          iniciarSessao(
            await loginGoogle(response.code, process.env.NEXT_PUBLIC_GOOGLE_REDIRECT_URI || "postmessage")
          );
        } catch (error) {
          onErro?.(error instanceof Error ? error.message : "Não foi possível validar a conta Google.");
        } finally {
          setProcessando(false);
        }
      },
    });
    client.requestCode();
  }

  return (
    <button className="google-button" type="button" onClick={iniciarLogin} disabled={processando}>
      {processando ? "Validando conta Google..." : "Entrar com Google"}
    </button>
  );
}
