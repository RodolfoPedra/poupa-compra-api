"use client";

import Link from "next/link";
import { FormEvent, Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { resetPassword } from "../lib/api";

function RedefinirSenhaForm() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [token, setToken] = useState(searchParams.get("token") ?? "");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [sucesso, setSucesso] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");

    if (novaSenha !== confirmarSenha) {
      setErro("As senhas informadas não coincidem.");
      return;
    }

    setEnviando(true);
    try {
      await resetPassword(token, novaSenha);
      setSucesso(true);
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível redefinir a senha.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="auth-card">
      <p className="eyebrow">Poupacompra</p>
      <h1>Redefinir senha</h1>
      <p className="auth-subtitle">Informe o token recebido e a nova senha.</p>

      {sucesso ? (
        <>
          <p className="auth-success">Senha redefinida. Todas as sessões ativas foram encerradas.</p>
          <button className="submit-button auth-submit-link" type="button" onClick={() => router.push("/login")}>
            Ir para o login
          </button>
        </>
      ) : (
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            <span>Token</span>
            <input value={token} onChange={(event) => setToken(event.target.value)} required />
          </label>
          <label>
            <span>Nova senha</span>
            <input
              type="password"
              value={novaSenha}
              onChange={(event) => setNovaSenha(event.target.value)}
              minLength={8}
              required
            />
          </label>
          <label>
            <span>Confirmar nova senha</span>
            <input
              type="password"
              value={confirmarSenha}
              onChange={(event) => setConfirmarSenha(event.target.value)}
              minLength={8}
              required
            />
          </label>

          {erro && <p className="auth-error"><span>{erro}</span></p>}

          <button className="submit-button" type="submit" disabled={enviando}>
            {enviando ? "Redefinindo..." : "Redefinir senha"}
          </button>
        </form>
      )}

      <p className="auth-footer-link">
        <Link href="/login">Voltar para o login</Link>
      </p>
    </section>
  );
}

export default function RedefinirSenhaPage() {
  return (
    <main className="auth-shell">
      <Suspense fallback={null}>
        <RedefinirSenhaForm />
      </Suspense>
    </main>
  );
}
