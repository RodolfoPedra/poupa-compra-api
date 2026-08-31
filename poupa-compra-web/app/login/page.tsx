"use client";

import Link from "next/link";
import Script from "next/script";
import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import GoogleLoginButton from "../components/GoogleLoginButton";
import { login, resendVerification } from "../lib/api";
import { useSession } from "../lib/session";

export default function LoginPage() {
  const router = useRouter();
  const { usuario, iniciarSessao } = useSession();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [googleCarregado, setGoogleCarregado] = useState(false);
  const [reenviando, setReenviando] = useState(false);

  const precisaVerificarEmail = erro.toLowerCase().includes("não verificado");

  useEffect(() => {
    if (usuario) {
      router.replace("/notas");
    }
  }, [usuario, router]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      iniciarSessao(await login(email, senha));
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível entrar.");
    } finally {
      setEnviando(false);
    }
  }

  async function handleReenviarVerificacao() {
    setReenviando(true);
    try {
      await resendVerification(email);
      setErro("Um novo link de verificação foi gerado. Confira o log local do backend.");
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível reenviar a verificação.");
    } finally {
      setReenviando(false);
    }
  }

  return (
    <main className="auth-shell">
      <Script
        src="https://accounts.google.com/gsi/client"
        strategy="afterInteractive"
        onLoad={() => setGoogleCarregado(true)}
      />
      <section className="auth-card">
        <p className="eyebrow">Poupacompra</p>
        <h1>Entrar na conta</h1>
        <p className="auth-subtitle">Acesse o painel para administrar notas e listas de compras.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            <span>E-mail</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </label>
          <label>
            <span>Senha</span>
            <input
              type="password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>

          <div className="auth-links">
            <Link href="/esqueci-senha">Esqueci minha senha</Link>
          </div>

          {erro && (
            <p className="auth-error">
              <span>{erro}</span>
              {precisaVerificarEmail && (
                <button
                  type="button"
                  className="text-button"
                  onClick={handleReenviarVerificacao}
                  disabled={reenviando}
                >
                  {reenviando ? "Reenviando..." : "Reenviar verificação"}
                </button>
              )}
            </p>
          )}

          <button className="submit-button" type="submit" disabled={enviando}>
            {enviando ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <div className="auth-divider"><span>ou</span></div>

        <GoogleLoginButton carregado={googleCarregado} onErro={setErro} />

        <p className="auth-footer-link">
          Não tem uma conta? <Link href="/cadastro">Criar conta</Link>
        </p>
      </section>
    </main>
  );
}
