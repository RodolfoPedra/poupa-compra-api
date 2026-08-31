"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { forgotPassword } from "../lib/api";

export default function EsqueciSenhaPage() {
  const [email, setEmail] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);
  const [erro, setErro] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      await forgotPassword(email);
      setEnviado(true);
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível processar a solicitação.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">Poupacompra</p>
        <h1>Recuperar senha</h1>
        <p className="auth-subtitle">
          Informe o e-mail da conta. Se ele existir, um token de redefinição será gerado.
        </p>

        {enviado ? (
          <p className="auth-success">
            Solicitação registrada. Durante o desenvolvimento local, o token aparece no log do backend.
          </p>
        ) : (
          <form className="auth-form" onSubmit={handleSubmit}>
            <label>
              <span>E-mail</span>
              <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
            </label>

            {erro && <p className="auth-error"><span>{erro}</span></p>}

            <button className="submit-button" type="submit" disabled={enviando}>
              {enviando ? "Enviando..." : "Enviar solicitação"}
            </button>
          </form>
        )}

        <p className="auth-footer-link">
          <Link href="/redefinir-senha">Já tenho um token</Link>
        </p>
        <p className="auth-footer-link">
          <Link href="/login">Voltar para o login</Link>
        </p>
      </section>
    </main>
  );
}
