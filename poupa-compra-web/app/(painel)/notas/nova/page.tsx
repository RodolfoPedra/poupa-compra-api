"use client";

import { FormEvent, useState } from "react";
import { useSession } from "../../../lib/session";

const exemploNota = `{
  "estabelecimento": {},
  "itensNota": [],
  "nota": {}
}`;

export default function NovaNotaPage() {
  const { accessToken } = useSession();
  const [payload, setPayload] = useState(exemploNota);
  const [resposta, setResposta] = useState("");
  const [status, setStatus] = useState<number | null>(null);
  const [enviando, setEnviando] = useState(false);

  function formatarJson() {
    try {
      setPayload(JSON.stringify(JSON.parse(payload), null, 2));
      setResposta("JSON formatado.");
      setStatus(null);
    } catch {
      setResposta("O conteúdo atual não é um JSON válido.");
      setStatus(null);
    }
  }

  async function enviarNota(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setEnviando(true);
    setResposta("");
    setStatus(null);

    try {
      JSON.parse(payload);
      const response = await fetch("/api/integracao-poupa-compra/api/v1/notas", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        },
        body: payload,
      });
      const body = await response.text();
      setStatus(response.status);
      setResposta(body || "Resposta sem conteúdo.");
    } catch (error) {
      setResposta(
        error instanceof SyntaxError
          ? "O conteúdo informado não é um JSON válido."
          : "Não foi possível alcançar a API. Verifique se o backend está em execução."
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="panel-toolbar">
        <div>
          <p className="section-kicker">Simulador da API</p>
          <h2>Cadastrar nota</h2>
          <p className="intro-copy">
            Cole o mesmo JSON utilizado pelo aplicativo mobile e acompanhe a resposta do endpoint em tempo real.
          </p>
        </div>
        <div className="endpoint"><span>POST</span> /api/v1/notas</div>
      </div>

      <section className="workspace">
        <form className="editor-panel" onSubmit={enviarNota}>
          <div className="panel-heading">
            <div>
              <h3>Payload da requisição</h3>
              <p>application/json</p>
            </div>
            <button className="text-button" type="button" onClick={formatarJson}>Formatar JSON</button>
          </div>
          <textarea
            aria-label="Payload JSON da nota"
            value={payload}
            onChange={(event) => setPayload(event.target.value)}
            spellCheck={false}
          />
          <div className="editor-footer">
            <span>{payload.length.toLocaleString("pt-BR")} caracteres</span>
            <button className="clear-button" type="button" onClick={() => setPayload("")}>Limpar</button>
          </div>
          <button className="submit-button" type="submit" disabled={enviando}>
            {enviando ? "Enviando..." : "Enviar nota"}
            <span aria-hidden="true">-&gt;</span>
          </button>
        </form>

        <aside className="response-panel">
          <div className="panel-heading">
            <div>
              <h3>Resposta da API</h3>
              <p>{status ? `HTTP ${status}` : "Aguardando uma requisição"}</p>
            </div>
            <span className={`status-dot ${status && status < 400 ? "success" : ""}`} />
          </div>
          <pre>{resposta || "A resposta do endpoint aparecerá aqui depois do envio."}</pre>
        </aside>
      </section>
    </>
  );
}
