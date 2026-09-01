"use client";

import Link from "next/link";
import { FormEvent, Suspense, useState } from "react";
import { useSearchParams } from "next/navigation";
import { verifyEmail } from "../lib/api";

function VerificarEmailForm() {
  const searchParams = useSearchParams();
  const [token, setToken] = useState(searchParams.get("token") ?? "");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState(false);
  const [enviando, setEnviando] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      await verifyEmail(token);
      setSucesso(true);
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível verificar o e-mail.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="auth-card">
      <p className="eyebrow">Poupacompra</p>
      <h1>Verificar e-mail</h1>
      <p className="auth-subtitle">Informe o token de verificação exibido no log local do backend.</p>

      {sucesso ? (
        <>
          <p className="auth-success">E-mail verificado. Você já pode entrar na conta.</p>
          <Link className="submit-button auth-submit-link" href="/login">Ir para o login</Link>
        </>
      ) : (
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            <span>Token</span>
            <input value={token} onChange={(event) => setToken(event.target.value)} required />
          </label>

          {erro && <p className="auth-error"><span>{erro}</span></p>}

          <button className="submit-button" type="submit" disabled={enviando}>
            {enviando ? "Verificando..." : "Verificar e-mail"}
          </button>
        </form>
      )}

      <p className="auth-footer-link">
        <Link href="/login">Voltar para o login</Link>
      </p>
    </section>
  );
}

export default function VerificarEmailPage() {
  return (
    <main className="auth-shell">
      <Suspense fallback={null}>
        <VerificarEmailForm />
      </Suspense>
    </main>
  );
}
