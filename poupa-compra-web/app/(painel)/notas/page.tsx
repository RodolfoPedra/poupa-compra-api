"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import ItensNotaModal from "../../components/ItensNotaModal";
import { listarNotas, NotaResumo } from "../../lib/api";
import { useSession } from "../../lib/session";

function formatarMoeda(valor: number) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export default function NotasPage() {
  const { accessToken } = useSession();
  const [notas, setNotas] = useState<NotaResumo[] | null>(null);
  const [erro, setErro] = useState("");
  const [notaSelecionada, setNotaSelecionada] = useState<NotaResumo | null>(null);

  useEffect(() => {
    if (!accessToken) return;
    listarNotas(accessToken)
      .then(setNotas)
      .catch((error) => setErro(error instanceof Error ? error.message : "Não foi possível carregar as notas."));
  }, [accessToken]);

  return (
    <>
      <div className="panel-toolbar">
        <div>
          <p className="section-kicker">Notas fiscais</p>
          <h2>Notas cadastradas</h2>
        </div>
        <Link className="submit-button panel-toolbar-action" href="/notas/nova">Cadastrar nota</Link>
      </div>

      {erro && <p className="auth-error"><span>{erro}</span></p>}

      {!erro && notas === null && <p className="auth-subtitle">Carregando notas...</p>}

      {!erro && notas !== null && notas.length === 0 && (
        <p className="auth-subtitle">Nenhuma nota cadastrada até o momento.</p>
      )}

      {!erro && notas !== null && notas.length > 0 && (
        <table className="notas-table notas-table-clickable">
          <thead>
            <tr>
              <th>Chave de acesso</th>
              <th>UF</th>
              <th>Itens</th>
              <th>Valor total</th>
              <th>Emissão</th>
            </tr>
          </thead>
          <tbody>
            {notas.map((nota) => (
              <tr key={nota.id} onClick={() => setNotaSelecionada(nota)}>
                <td>{nota.chaveAcesso}</td>
                <td>{nota.ufCfe}</td>
                <td>{nota.quantidadeItens}</td>
                <td>{formatarMoeda(nota.valorTotal)}</td>
                <td>{nota.dataHoraEmissao ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {notaSelecionada && accessToken && (
        <ItensNotaModal
          nota={notaSelecionada}
          accessToken={accessToken}
          onClose={() => setNotaSelecionada(null)}
        />
      )}
    </>
  );
}
