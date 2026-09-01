"use client";

import { useEffect, useState } from "react";
import { ItemNotaResumo, listarItensDaNota, NotaResumo } from "../lib/api";

function formatarMoeda(valor: number) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

type ItensNotaModalProps = {
  nota: NotaResumo;
  accessToken: string;
  onClose: () => void;
};

export default function ItensNotaModal({ nota, accessToken, onClose }: ItensNotaModalProps) {
  const [itens, setItens] = useState<ItemNotaResumo[] | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    listarItensDaNota(accessToken, nota.id)
      .then(setItens)
      .catch((error) => setErro(error instanceof Error ? error.message : "Não foi possível carregar os itens."));
  }, [accessToken, nota.id]);

  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(event) => event.stopPropagation()}>
        <div className="panel-heading">
          <div>
            <h3>Itens da nota</h3>
            <p>{nota.chaveAcesso}</p>
          </div>
          <button className="text-button" type="button" onClick={onClose}>Fechar</button>
        </div>

        {erro && <p className="auth-error"><span>{erro}</span></p>}

        {!erro && itens === null && <p className="auth-subtitle">Carregando itens...</p>}

        {!erro && itens !== null && itens.length === 0 && (
          <p className="auth-subtitle">Nenhum item vinculado a esta nota.</p>
        )}

        {!erro && itens !== null && itens.length > 0 && (
          <table className="notas-table">
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Qtd.</th>
                <th>Unid.</th>
                <th>Valor unitário</th>
                <th>Valor total</th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item, index) => (
                <tr key={`${item.codigoItem ?? "item"}-${index}`}>
                  <td>{item.descricao}</td>
                  <td>{item.quantidade}</td>
                  <td>{item.tipoUnidade ?? "—"}</td>
                  <td>{formatarMoeda(item.valorUnitario)}</td>
                  <td>{formatarMoeda(item.valorTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
