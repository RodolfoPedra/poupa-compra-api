"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { register } from "../lib/api";

export default function CadastroPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState(false);
  const [enviando, setEnviando] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");

    if (senha !== confirmarSenha) {
      setErro("As senhas informadas não coincidem.");
      return;
    }

    setEnviando(true);
    try {
      await register(nome, email, senha);
      setSucesso(true);
    } catch (error) {
      setErro(error instanceof Error ? error.message : "Não foi possível concluir o cadastro.");
    } finally {
      setEnviando(false);
    }
  }

  if (sucesso) {
    return (
      <main className="auth-shell">
        <section className="auth-card">
          <p className="eyebrow">Poupacompra</p>
          <h1>Confirme seu e-mail</h1>
          <p className="auth-subtitle">
            Cadastro realizado. Durante o desenvolvimento local, o token de verificação é exibido no log do backend.
          </p>
          <Link className="submit-button auth-submit-link" href="/verificar-email">Já tenho um token</Link>
          <p className="auth-footer-link">
            <Link href="/login">Voltar para o login</Link>
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">Poupacompra</p>
        <h1>Criar conta</h1>
        <p className="auth-subtitle">Cadastre-se com e-mail e senha para acessar o painel.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            <span>Nome</span>
            <input value={nome} onChange={(event) => setNome(event.target.value)} required />
          </label>
          <label>
            <span>E-mail</span>
            <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </label>
          <label>
            <span>Senha</span>
            <input
              type="password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              minLength={8}
              required
            />
          </label>
          <label>
            <span>Confirmar senha</span>
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
            {enviando ? "Criando conta..." : "Criar conta"}
          </button>
        </form>

        <p className="auth-footer-link">
          Já tem uma conta? <Link href="/login">Entrar</Link>
        </p>
      </section>
    </main>
  );
}
