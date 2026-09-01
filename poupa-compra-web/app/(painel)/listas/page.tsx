"use client";

import { FormEvent, startTransition, useDeferredValue, useEffect, useState } from "react";
import {
  atualizarVinculoNota,
  buscarLista,
  cadastrarNotaVinculada,
  CategoriaProduto,
  criarLista,
  EstabelecimentoNota,
  excluirLista,
  gerarRascunhoListaPorNotas,
  ItemListaCompra,
  ListaCompra,
  ListaCompraResumo,
  listarCategorias,
  listarEstabelecimentosDasNotas,
  listarListas,
  listarNotasDisponiveisParaVinculo,
  listarNotasParaLista,
  NotaOrigemLista,
  PaginaResponse,
  pesquisarProdutos,
  ProdutoCatalogo,
  RascunhoListaNotas,
  salvarLista,
  SalvarListaPayload,
  UnidadeMedida,
} from "../../lib/api";
import { useSession } from "../../lib/session";

type ItemRascunho = Omit<ItemListaCompra, "id"> & { id: number };
type ListaRascunho = Omit<ListaCompra, "id" | "itens"> & { id: number | null; itens: ItemRascunho[] };
type AcaoPendente = { tipo: "selecionar"; id: number }
  | { tipo: "criar"; nome: string }
  | { tipo: "importar"; rascunho: RascunhoListaNotas };

const paginaVazia: PaginaResponse<ProdutoCatalogo> = {
  conteudo: [], pagina: 0, tamanho: 12, totalElementos: 0, totalPaginas: 0,
};

const paginaNotasVazia: PaginaResponse<NotaOrigemLista> = {
  conteudo: [], pagina: 0, tamanho: 10, totalElementos: 0, totalPaginas: 0,
};

const exemploNota = `{
  "estabelecimento": {},
  "itensNota": [],
  "nota": {}
}`;

let proximoIdTemporario = -1;

function mensagemErro(error: unknown) {
  return error instanceof Error ? error.message : "Não foi possível concluir a operação.";
}

function serializarRascunho(lista: ListaRascunho, nome: string) {
  return JSON.stringify({
    nome,
    itens: lista.itens.map(({ id, produtoId, descricao, quantidade, unidade, selecionado, ordem }) => ({
      id, produtoId, descricao, quantidade, unidade, selecionado, ordem,
    })),
  });
}

function criarRascunho(detalhe: ListaCompra): ListaRascunho {
  return { ...detalhe, itens: detalhe.itens.map((item) => ({ ...item })) };
}

function criarRascunhoDeNotas(detalhe: RascunhoListaNotas): ListaRascunho {
  return {
    id: null,
    nome: detalhe.nome,
    createdAt: "",
    updatedAt: "",
    nota: null,
    itens: detalhe.itens.map((item) => ({ ...item, id: proximoIdTemporario-- })),
  };
}

function formatarDataNota(data: string | null) {
  if (!data) return "Data não informada";
  const dataValida = new Date(data);
  return Number.isNaN(dataValida.getTime()) ? data : dataValida.toLocaleString("pt-BR");
}

function formatarQuantidade(item: ItemRascunho) {
  const quantidadeFormatada = item.quantidade?.toLocaleString("pt-BR", { maximumFractionDigits: 3 });
  if (quantidadeFormatada && item.unidade === "QUILOGRAMA") return `${quantidadeFormatada} kg`;
  if (quantidadeFormatada && item.unidade === "UNIDADE") return `${quantidadeFormatada} un.`;
  if (quantidadeFormatada) return quantidadeFormatada;
  if (item.unidade === "QUILOGRAMA") return "kg";
  if (item.unidade === "UNIDADE") return "un.";
  return "Sem quantidade";
}

export default function ListasPage() {
  const { accessToken } = useSession();
  const [listas, setListas] = useState<ListaCompraResumo[] | null>(null);
  const [lista, setLista] = useState<ListaRascunho | null>(null);
  const [listaId, setListaId] = useState<number | null>(null);
  const [baseline, setBaseline] = useState<string | null>(null);
  const [acaoPendente, setAcaoPendente] = useState<AcaoPendente | null>(null);
  const [nomeNovaLista, setNomeNovaLista] = useState("");
  const [nomeLista, setNomeLista] = useState("");
  const [categorias, setCategorias] = useState<CategoriaProduto[]>([]);
  const [produtos, setProdutos] = useState(paginaVazia);
  const [busca, setBusca] = useState("");
  const buscaAdiada = useDeferredValue(busca);
  const [categoriaId, setCategoriaId] = useState<number | undefined>();
  const [pagina, setPagina] = useState(0);
  const [produtoSelecionado, setProdutoSelecionado] = useState<ProdutoCatalogo | null>(null);
  const [modoInclusao, setModoInclusao] = useState<"catalogo" | "personalizado">("catalogo");
  const [descricao, setDescricao] = useState("");
  const [quantidade, setQuantidade] = useState("");
  const [unidade, setUnidade] = useState<UnidadeMedida | "">("");
  const [salvando, setSalvando] = useState(false);
  const [carregandoDetalhe, setCarregandoDetalhe] = useState(false);
  const [seletorNotasAberto, setSeletorNotasAberto] = useState(false);
  const [estabelecimentos, setEstabelecimentos] = useState<EstabelecimentoNota[]>([]);
  const [estabelecimentoId, setEstabelecimentoId] = useState<number | null>(null);
  const [notasOrigem, setNotasOrigem] = useState(paginaNotasVazia);
  const [paginaNotas, setPaginaNotas] = useState(0);
  const [notasSelecionadas, setNotasSelecionadas] = useState<number[]>([]);
  const [carregandoEstabelecimentos, setCarregandoEstabelecimentos] = useState(false);
  const [carregandoNotas, setCarregandoNotas] = useState(false);
  const [gerandoRascunho, setGerandoRascunho] = useState(false);
  const [vinculoNotaAberto, setVinculoNotaAberto] = useState(false);
  const [modoVinculo, setModoVinculo] = useState<"existente" | "nova">("existente");
  const [notasDisponiveis, setNotasDisponiveis] = useState(paginaNotasVazia);
  const [paginaVinculo, setPaginaVinculo] = useState(0);
  const [notaVinculoId, setNotaVinculoId] = useState<number | null>(null);
  const [payloadNovaNota, setPayloadNovaNota] = useState(exemploNota);
  const [carregandoVinculo, setCarregandoVinculo] = useState(false);
  const [salvandoVinculo, setSalvandoVinculo] = useState(false);
  const [erro, setErro] = useState("");

  const sujo = lista !== null && (baseline === null || serializarRascunho(lista, nomeLista) !== baseline);

  async function recarregarListas(token: string) {
    const resultado = await listarListas(token);
    setListas(resultado);
    return resultado;
  }

  function aplicarDetalhe(detalhe: ListaCompra) {
    const rascunho = criarRascunho(detalhe);
    setLista(rascunho);
    setListaId(detalhe.id);
    setNomeLista(detalhe.nome);
    setBaseline(serializarRascunho(rascunho, detalhe.nome));
  }

  useEffect(() => {
    if (!accessToken) return;
    let ativo = true;
    void (async () => {
      try {
        const [listasCarregadas, categoriasCarregadas] = await Promise.all([
          listarListas(accessToken), listarCategorias(accessToken),
        ]);
        if (!ativo) return;
        setListas(listasCarregadas);
        setCategorias(categoriasCarregadas);
        if (listasCarregadas.length > 0) {
          const detalhe = await buscarLista(accessToken, listasCarregadas[0].id);
          if (ativo) aplicarDetalhe(detalhe);
        }
      } catch (error) {
        if (ativo) setErro(mensagemErro(error));
      }
    })();
    return () => { ativo = false; };
  }, [accessToken]);

  useEffect(() => {
    if (!accessToken) return;
    const controller = new AbortController();
    const timer = window.setTimeout(() => {
      pesquisarProdutos(accessToken, {
        busca: buscaAdiada, categoriaId, pagina, tamanho: 12, signal: controller.signal,
      })
        .then((resultado) => startTransition(() => setProdutos(resultado)))
        .catch((error) => {
          if (error instanceof DOMException && error.name === "AbortError") return;
          setErro(mensagemErro(error));
        });
    }, 250);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [accessToken, buscaAdiada, categoriaId, pagina]);

  useEffect(() => {
    if (!accessToken || !seletorNotasAberto || estabelecimentoId === null) return;
    const controller = new AbortController();
    listarNotasParaLista(accessToken, estabelecimentoId, {
      pagina: paginaNotas, tamanho: 10, signal: controller.signal,
    })
      .then(setNotasOrigem)
      .catch((error) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setErro(mensagemErro(error));
      })
      .finally(() => {
        if (!controller.signal.aborted) setCarregandoNotas(false);
      });
    return () => controller.abort();
  }, [accessToken, seletorNotasAberto, estabelecimentoId, paginaNotas]);

  useEffect(() => {
    if (!accessToken || !vinculoNotaAberto || modoVinculo !== "existente" || lista?.id == null) return;
    const controller = new AbortController();
    listarNotasDisponiveisParaVinculo(accessToken, lista.id, {
      pagina: paginaVinculo, tamanho: 10, signal: controller.signal,
    })
      .then(setNotasDisponiveis)
      .catch((error) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setErro(mensagemErro(error));
      })
      .finally(() => {
        if (!controller.signal.aborted) setCarregandoVinculo(false);
      });
    return () => controller.abort();
  }, [accessToken, vinculoNotaAberto, modoVinculo, lista?.id, paginaVinculo]);

  useEffect(() => {
    if (!sujo) return;
    const avisar = (event: BeforeUnloadEvent) => event.preventDefault();
    window.addEventListener("beforeunload", avisar);
    return () => window.removeEventListener("beforeunload", avisar);
  }, [sujo]);

  async function executarAcao(acao: AcaoPendente) {
    if (!accessToken) return;
    setErro("");
    if (acao.tipo === "importar") {
      const novo = criarRascunhoDeNotas(acao.rascunho);
      setLista(novo);
      setListaId(null);
      setNomeLista(novo.nome);
      setBaseline(null);
      return;
    }
    if (acao.tipo === "criar") {
      const novo: ListaRascunho = {
        id: null, nome: acao.nome, createdAt: "", updatedAt: "", nota: null, itens: [],
      };
      setLista(novo);
      setListaId(null);
      setNomeLista(acao.nome);
      setBaseline(null);
      setNomeNovaLista("");
      return;
    }
    setCarregandoDetalhe(true);
    setListaId(acao.id);
    setLista(null);
    try {
      aplicarDetalhe(await buscarLista(accessToken, acao.id));
    } catch (error) {
      setErro(mensagemErro(error));
    } finally {
      setCarregandoDetalhe(false);
    }
  }

  function solicitarAcao(acao: AcaoPendente) {
    if (acao.tipo === "selecionar" && listaId === acao.id && lista) return;
    if (sujo) setAcaoPendente(acao);
    else void executarAcao(acao);
  }

  function iniciarCriacao(event: FormEvent) {
    event.preventDefault();
    const nome = nomeNovaLista.trim();
    if (nome) solicitarAcao({ tipo: "criar", nome });
  }

  async function abrirSeletorNotas() {
    if (!accessToken) return;
    setErro("");
    setCarregandoEstabelecimentos(true);
    try {
      const opcoes = await listarEstabelecimentosDasNotas(accessToken);
      setEstabelecimentos(opcoes);
      setEstabelecimentoId(opcoes[0]?.id ?? null);
      setNotasOrigem(paginaNotasVazia);
      setPaginaNotas(0);
      setNotasSelecionadas([]);
      setCarregandoNotas(opcoes.length > 0);
      setSeletorNotasAberto(true);
    } catch (error) {
      setErro(mensagemErro(error));
    } finally {
      setCarregandoEstabelecimentos(false);
    }
  }

  function alternarNota(notaId: number) {
    setNotasSelecionadas((atuais) => atuais.includes(notaId)
      ? atuais.filter((id) => id !== notaId)
      : atuais.length < 5 ? [...atuais, notaId] : atuais);
  }

  async function gerarListaDasNotas() {
    if (!accessToken || notasSelecionadas.length === 0) return;
    setGerandoRascunho(true);
    setErro("");
    try {
      const rascunho = await gerarRascunhoListaPorNotas(accessToken, notasSelecionadas);
      setSeletorNotasAberto(false);
      solicitarAcao({ tipo: "importar", rascunho });
    } catch (error) {
      setErro(mensagemErro(error));
    } finally {
      setGerandoRascunho(false);
    }
  }

  function abrirVinculoNota() {
    if (!lista || lista.id === null || sujo) return;
    setModoVinculo("existente");
    setPaginaVinculo(0);
    setNotaVinculoId(lista.nota?.id ?? null);
    setNotasDisponiveis(paginaNotasVazia);
    setPayloadNovaNota(exemploNota);
    setCarregandoVinculo(true);
    setVinculoNotaAberto(true);
    setErro("");
  }

  async function concluirVinculo(notaId: number | null) {
    if (!accessToken || !lista || lista.id === null) return;
    setSalvandoVinculo(true);
    setErro("");
    try {
      const atualizada = await atualizarVinculoNota(accessToken, lista.id, notaId, lista.updatedAt);
      aplicarDetalhe(atualizada);
      await recarregarListas(accessToken);
      setVinculoNotaAberto(false);
    } catch (error) {
      setErro(mensagemErro(error));
    } finally {
      setSalvandoVinculo(false);
    }
  }

  async function cadastrarEVincularNota() {
    if (!accessToken || !lista || lista.id === null) return;
    setSalvandoVinculo(true);
    setErro("");
    try {
      const nota = JSON.parse(payloadNovaNota) as unknown;
      const atualizada = await cadastrarNotaVinculada(accessToken, lista.id, nota, lista.updatedAt);
      aplicarDetalhe(atualizada);
      await recarregarListas(accessToken);
      setVinculoNotaAberto(false);
    } catch (error) {
      setErro(error instanceof SyntaxError ? "O conteúdo informado não é um JSON válido." : mensagemErro(error));
    } finally {
      setSalvandoVinculo(false);
    }
  }

  function montarPayload(): SalvarListaPayload | null {
    if (!lista || !nomeLista.trim()) {
      setErro("Informe o nome da lista.");
      return null;
    }
    for (const item of lista.itens) {
      if (item.quantidade !== null
          && (item.quantidade <= 0 || (item.unidade === "UNIDADE" && !Number.isInteger(item.quantidade)))) {
        setErro(`Revise a quantidade de ${item.descricao}.`);
        return null;
      }
    }
    return {
      nome: nomeLista.trim(),
      ...(lista.id !== null ? { updatedAt: lista.updatedAt } : {}),
      itens: lista.itens.map((item, ordem) => ({
        ...(item.id > 0 ? { id: item.id } : {}),
        produtoId: item.produtoId,
        descricao: item.descricao,
        quantidade: item.quantidade,
        unidade: item.unidade,
        selecionado: item.selecionado,
        ordem,
      })),
    };
  }

  async function persistir(): Promise<boolean> {
    if (!accessToken || !lista) return false;
    const payload = montarPayload();
    if (!payload) return false;
    setSalvando(true);
    setErro("");
    try {
      const salva = lista.id === null
        ? await criarLista(accessToken, payload)
        : await salvarLista(accessToken, lista.id, payload);
      aplicarDetalhe(salva);
      await recarregarListas(accessToken);
      return true;
    } catch (error) {
      setErro(mensagemErro(error));
      return false;
    } finally {
      setSalvando(false);
    }
  }

  async function salvarEContinuar() {
    if (!acaoPendente || !(await persistir())) return;
    const acao = acaoPendente;
    setAcaoPendente(null);
    await executarAcao(acao);
  }

  async function descartarEContinuar() {
    if (!acaoPendente) return;
    const acao = acaoPendente;
    setAcaoPendente(null);
    await executarAcao(acao);
  }

  async function removerLista() {
    if (!accessToken || !lista) return;
    if (lista.id === null) {
      setLista(null); setListaId(null); setBaseline(null);
      return;
    }
    if (!window.confirm(`Excluir a lista "${lista.nome}"?`)) return;
    setSalvando(true);
    setErro("");
    try {
      await excluirLista(accessToken, lista.id);
      const restantes = await recarregarListas(accessToken);
      setLista(null); setListaId(null); setBaseline(null);
      if (restantes[0]) await executarAcao({ tipo: "selecionar", id: restantes[0].id });
    } catch (error) {
      setErro(mensagemErro(error));
    } finally {
      setSalvando(false);
    }
  }

  function adicionarItem(event: FormEvent) {
    event.preventDefault();
    if (!lista) return;
    const valor = quantidade.trim() ? Number(quantidade.replace(",", ".")) : null;
    if (valor !== null
        && (!Number.isFinite(valor) || valor <= 0 || (unidade === "UNIDADE" && !Number.isInteger(valor)))) {
      setErro("Informe uma quantidade válida para a unidade escolhida.");
      return;
    }
    if (modoInclusao === "catalogo" && produtoSelecionado
        && lista.itens.some((item) => item.produtoId === produtoSelecionado.id)) {
      setErro("Este produto já está na lista.");
      return;
    }
    const descricaoItem = modoInclusao === "catalogo" ? produtoSelecionado?.nome : descricao.trim();
    if (!descricaoItem) return;
    const item: ItemRascunho = {
      id: proximoIdTemporario--,
      produtoId: modoInclusao === "catalogo" ? produtoSelecionado?.id ?? null : null,
      descricao: descricaoItem,
      quantidade: valor,
      unidade: unidade || null,
      selecionado: false,
      ordem: lista.itens.length,
    };
    setLista({ ...lista, itens: [...lista.itens, item] });
    setProdutoSelecionado(null); setDescricao(""); setQuantidade(""); setUnidade(""); setErro("");
  }

  function atualizarItem(itemId: number, alteracao: Partial<ItemRascunho>) {
    if (!lista) return;
    setLista({ ...lista, itens: lista.itens.map((item) => item.id === itemId ? { ...item, ...alteracao } : item) });
  }

  function removerItem(itemId: number) {
    if (!lista) return;
    setLista({ ...lista, itens: lista.itens.filter((item) => item.id !== itemId) });
  }

  const progresso = lista?.itens.length
    ? Math.round((lista.itens.filter((item) => item.selecionado).length / lista.itens.length) * 100)
    : 0;

  return (
    <>
      <div className="panel-toolbar lista-toolbar">
        <div><p className="section-kicker">Planejamento de compras</p><h2>Listas de compras</h2></div>
        <div className="lista-create-actions">
          <form className="lista-create-form" onSubmit={iniciarCriacao}>
            <label htmlFor="nome-nova-lista">Nova lista manual</label>
            <div>
              <input id="nome-nova-lista" value={nomeNovaLista} maxLength={120} required
                onChange={(event) => setNomeNovaLista(event.target.value)} placeholder="Ex.: Compras do mês" />
              <button className="submit-button" type="submit" disabled={salvando}>Criar</button>
            </div>
          </form>
          <button className="lista-from-notes-button" type="button" onClick={() => void abrirSeletorNotas()}
            disabled={carregandoEstabelecimentos}>
            {carregandoEstabelecimentos ? "Carregando..." : "Criar de notas"}
          </button>
        </div>
      </div>

      {erro && <p className="lista-error" role="alert">{erro}</p>}

      <div className="listas-workspace">
        <aside className="listas-index" aria-label="Suas listas de compras">
          <div className="listas-index-heading"><span>Suas listas</span><strong>{listas?.length ?? 0}</strong></div>
          {listas === null && <p className="lista-muted">Carregando...</p>}
          {listas?.length === 0 && <p className="lista-muted">Crie sua primeira lista para começar.</p>}
          {listas?.map((resumo) => (
            <button key={resumo.id} type="button"
              className={`lista-index-item ${listaId === resumo.id ? "active" : ""}`}
              onClick={() => solicitarAcao({ tipo: "selecionar", id: resumo.id })}>
              <span>{resumo.nome}</span><small>{resumo.quantidadeSelecionados}/{resumo.quantidadeItens} escolhidos</small>
            </button>
          ))}
        </aside>

        <section className="lista-detail">
          {!lista && !carregandoDetalhe && <div className="lista-empty"><h3>Nenhuma lista selecionada</h3><p>Crie uma lista para organizar sua próxima compra.</p></div>}
          {carregandoDetalhe && <p className="lista-muted">Carregando lista...</p>}
          {lista && (
            <>
              <header className="lista-detail-header">
                <div className="lista-title-edit">
                  <input aria-label="Nome da lista" value={nomeLista} maxLength={120}
                    onChange={(event) => setNomeLista(event.target.value)} />
                  {sujo && <span className="lista-unsaved">Alterações não salvas</span>}
                </div>
                <div className="lista-header-actions">
                  <button type="button" className="lista-link-note-button" onClick={abrirVinculoNota}
                    disabled={lista.id === null || sujo || salvando}
                    title={lista.id === null || sujo ? "Salve a lista antes de vincular uma NFC-e" : undefined}>
                    {lista.nota ? "Trocar NFC-e" : "Vincular NFC-e"}
                  </button>
                  <button type="button" className="lista-save-button" onClick={() => void persistir()}
                    disabled={salvando || !sujo}>{salvando ? "Salvando..." : "Salvar lista"}</button>
                  <button type="button" className="lista-delete-button" onClick={() => void removerLista()}
                    disabled={salvando}>{lista.id === null ? "Descartar lista" : "Excluir lista"}</button>
                </div>
                <div className="lista-progress" aria-label={`${progresso}% concluído`}><span style={{ width: `${progresso}%` }} /></div>
              </header>

              {lista.nota && (
                <div className="lista-linked-note">
                  <span>NFC-e vinculada</span>
                  <strong>{lista.nota.numeroCfe ? `Nº ${lista.nota.numeroCfe}` : `Nota ${lista.nota.id}`}</strong>
                  <small>{formatarDataNota(lista.nota.dataHoraEmissao)} · {lista.nota.quantidadeItens} itens · {lista.nota.valorTotal.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })}</small>
                </div>
              )}

              <div className="lista-items">
                {lista.itens.length === 0 && <p className="lista-muted">Nenhum item adicionado.</p>}
                {lista.itens.map((item) => (
                  <div className={`lista-item ${item.selecionado ? "checked" : ""}`} key={item.id}>
                    <input className="lista-check" type="checkbox" checked={item.selecionado}
                      aria-label={`Marcar ${item.descricao}`}
                      onChange={(event) => atualizarItem(item.id, { selecionado: event.target.checked })} />
                    <div className="lista-item-name"><strong>{item.descricao}</strong><small>{item.produtoId ? "Catálogo" : "Personalizado"} · {formatarQuantidade(item)}</small></div>
                    <input className="lista-quantity" type="number" min="0.001"
                      step={item.unidade === "UNIDADE" ? "1" : "0.001"} aria-label={`Quantidade de ${item.descricao}`}
                      value={item.quantidade ?? ""} onChange={(event) => atualizarItem(item.id, {
                        quantidade: event.target.value ? Number(event.target.value) : null,
                      })} />
                    <select className="lista-unit" value={item.unidade ?? ""} aria-label={`Unidade de ${item.descricao}`}
                      onChange={(event) => atualizarItem(item.id, {
                        unidade: event.target.value ? event.target.value as UnidadeMedida : null,
                      })}>
                      <option value="">Sem unidade</option><option value="UNIDADE">un.</option><option value="QUILOGRAMA">kg</option>
                    </select>
                    <button className="lista-remove-item" type="button" title="Remover item"
                      aria-label={`Remover ${item.descricao}`} onClick={() => removerItem(item.id)}>×</button>
                  </div>
                ))}
              </div>

              <section className="item-composer">
                <div className="composer-tabs" role="tablist" aria-label="Tipo de item">
                  <button type="button" role="tab" aria-selected={modoInclusao === "catalogo"}
                    className={modoInclusao === "catalogo" ? "active" : ""} onClick={() => setModoInclusao("catalogo")}>Catálogo</button>
                  <button type="button" role="tab" aria-selected={modoInclusao === "personalizado"}
                    className={modoInclusao === "personalizado" ? "active" : ""} onClick={() => setModoInclusao("personalizado")}>Item personalizado</button>
                </div>

                {modoInclusao === "catalogo" && (
                  <div className="catalog-search">
                    <div className="catalog-filters">
                      <input type="search" value={busca} placeholder="Buscar produto ou categoria"
                        onChange={(event) => { setBusca(event.target.value); setPagina(0); }} />
                      <select value={categoriaId ?? ""} onChange={(event) => { setCategoriaId(event.target.value ? Number(event.target.value) : undefined); setPagina(0); }}>
                        <option value="">Todas as categorias</option>
                        {categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}
                      </select>
                    </div>
                    <div className="catalog-results">
                      {produtos.conteudo.map((produto) => (
                        <button type="button" key={produto.id} className={produtoSelecionado?.id === produto.id ? "active" : ""}
                          onClick={() => setProdutoSelecionado(produto)}>
                          <strong>{produto.nome}</strong><small>{produto.categoriaNome}</small>
                        </button>
                      ))}
                    </div>
                    {produtos.totalPaginas > 1 && <div className="catalog-pagination">
                      <button type="button" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>Anterior</button>
                      <span>{pagina + 1} de {produtos.totalPaginas}</span>
                      <button type="button" disabled={pagina + 1 >= produtos.totalPaginas} onClick={() => setPagina(pagina + 1)}>Próxima</button>
                    </div>}
                  </div>
                )}

                <form className="item-add-form" onSubmit={adicionarItem}>
                  {modoInclusao === "catalogo" ? (
                    <div className="selected-product"><span>Produto</span><strong>{produtoSelecionado?.nome ?? "Selecione no catálogo"}</strong></div>
                  ) : (
                    <label className="item-description">Descrição<input value={descricao} maxLength={160}
                      placeholder="Nome do item" onChange={(event) => setDescricao(event.target.value)} /></label>
                  )}
                  <label>Quantidade<input type="number" min="0.001" step={unidade === "UNIDADE" ? "1" : "0.001"}
                    value={quantidade} placeholder="Opcional" onChange={(event) => setQuantidade(event.target.value)} /></label>
                  <label>Unidade<select value={unidade}
                    onChange={(event) => setUnidade(event.target.value as UnidadeMedida | "")}>
                    <option value="">Sem unidade</option><option value="UNIDADE">Unidade</option><option value="QUILOGRAMA">Quilograma</option>
                  </select></label>
                  <button className="submit-button item-add-button" type="submit"
                    disabled={modoInclusao === "catalogo" ? !produtoSelecionado : !descricao.trim()}>Adicionar item</button>
                </form>
              </section>
            </>
          )}
        </section>
      </div>

      {vinculoNotaAberto && lista?.id != null && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setVinculoNotaAberto(false)}>
          <section className="modal-card nota-source-dialog nota-link-dialog" role="dialog" aria-modal="true"
            aria-labelledby="nota-link-title" onMouseDown={(event) => event.stopPropagation()}>
            <header>
              <div><p className="section-kicker">Compra finalizada</p><h3 id="nota-link-title">Vincular NFC-e</h3></div>
              <button type="button" className="nota-dialog-close" title="Fechar" aria-label="Fechar"
                onClick={() => setVinculoNotaAberto(false)}>×</button>
            </header>

            <div className="composer-tabs nota-link-tabs" role="tablist" aria-label="Origem da NFC-e">
              <button type="button" role="tab" aria-selected={modoVinculo === "existente"}
                className={modoVinculo === "existente" ? "active" : ""} onClick={() => {
                  setModoVinculo("existente"); setCarregandoVinculo(true);
                }}>Nota existente</button>
              <button type="button" role="tab" aria-selected={modoVinculo === "nova"}
                className={modoVinculo === "nova" ? "active" : ""}
                onClick={() => setModoVinculo("nova")}>Cadastrar nova</button>
            </div>

            {modoVinculo === "existente" ? (
              <>
                <div className="nota-source-list nota-link-list" aria-busy={carregandoVinculo}>
                  {carregandoVinculo && <p className="lista-muted">Carregando notas...</p>}
                  {!carregandoVinculo && notasDisponiveis.conteudo.length === 0 &&
                    <p className="lista-muted">Nenhuma nota disponível para vínculo.</p>}
                  {!carregandoVinculo && notasDisponiveis.conteudo.map((nota) => (
                    <label key={nota.id} className={`nota-source-item ${notaVinculoId === nota.id ? "selected" : ""}`}>
                      <input type="radio" name="nota-vinculo" checked={notaVinculoId === nota.id}
                        onChange={() => setNotaVinculoId(nota.id)} />
                      <span><strong>{nota.numeroCfe ? `NFC-e ${nota.numeroCfe}` : `Nota ${nota.id}`}</strong>
                        <small>{formatarDataNota(nota.dataHoraEmissao)}</small></span>
                      <span><strong>{nota.quantidadeItens} itens</strong>
                        <small>{nota.valorTotal.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })}</small></span>
                    </label>
                  ))}
                </div>
                {notasDisponiveis.totalPaginas > 1 && <div className="catalog-pagination nota-pagination">
                  <button type="button" disabled={paginaVinculo === 0 || carregandoVinculo}
                    onClick={() => { setCarregandoVinculo(true); setPaginaVinculo(paginaVinculo - 1); }}>Anterior</button>
                  <span>{paginaVinculo + 1} de {notasDisponiveis.totalPaginas}</span>
                  <button type="button" disabled={paginaVinculo + 1 >= notasDisponiveis.totalPaginas || carregandoVinculo}
                    onClick={() => { setCarregandoVinculo(true); setPaginaVinculo(paginaVinculo + 1); }}>Próxima</button>
                </div>}
              </>
            ) : (
              <label className="nota-json-field">Payload da nova NFC-e
                <textarea value={payloadNovaNota} spellCheck={false}
                  onChange={(event) => setPayloadNovaNota(event.target.value)} />
              </label>
            )}

            <div className="lista-dialog-actions nota-link-actions">
              {lista.nota && <button type="button" className="lista-discard-button" disabled={salvandoVinculo}
                onClick={() => void concluirVinculo(null)}>Desvincular</button>}
              <button type="button" className="clear-button" onClick={() => setVinculoNotaAberto(false)}>Cancelar</button>
              {modoVinculo === "existente" ? (
                <button type="button" className="lista-save-button"
                  disabled={notaVinculoId === null || notaVinculoId === lista.nota?.id || salvandoVinculo}
                  onClick={() => void concluirVinculo(notaVinculoId)}>
                  {salvandoVinculo ? "Vinculando..." : "Vincular nota"}
                </button>
              ) : (
                <button type="button" className="lista-save-button" disabled={!payloadNovaNota.trim() || salvandoVinculo}
                  onClick={() => void cadastrarEVincularNota()}>
                  {salvandoVinculo ? "Cadastrando..." : "Cadastrar e vincular"}
                </button>
              )}
            </div>
          </section>
        </div>
      )}

      {seletorNotasAberto && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setSeletorNotasAberto(false)}>
          <section className="modal-card nota-source-dialog" role="dialog" aria-modal="true"
            aria-labelledby="nota-source-title" onMouseDown={(event) => event.stopPropagation()}>
            <header>
              <div><p className="section-kicker">Origem da lista</p><h3 id="nota-source-title">Selecionar notas fiscais</h3></div>
              <button type="button" className="nota-dialog-close" title="Fechar" aria-label="Fechar"
                onClick={() => setSeletorNotasAberto(false)}>×</button>
            </header>

            {estabelecimentos.length === 0 ? (
              <p className="lista-muted">Nenhuma nota fiscal disponível.</p>
            ) : (
              <>
                <label className="nota-establishment">Estabelecimento
                  <select value={estabelecimentoId ?? ""} onChange={(event) => {
                    setCarregandoNotas(true);
                    setEstabelecimentoId(Number(event.target.value));
                    setPaginaNotas(0);
                    setNotasSelecionadas([]);
                  }}>
                    {estabelecimentos.map((estabelecimento) => (
                      <option key={estabelecimento.id} value={estabelecimento.id}>
                        {estabelecimento.nome} · {estabelecimento.cpfCnpj}
                      </option>
                    ))}
                  </select>
                </label>

                <div className="nota-selection-heading">
                  <span>Notas mais recentes</span><strong>{notasSelecionadas.length} de 5 selecionadas</strong>
                </div>
                <div className="nota-source-list" aria-busy={carregandoNotas}>
                  {carregandoNotas && <p className="lista-muted">Carregando notas...</p>}
                  {!carregandoNotas && notasOrigem.conteudo.length === 0 &&
                    <p className="lista-muted">Nenhuma nota encontrada.</p>}
                  {!carregandoNotas && notasOrigem.conteudo.map((nota) => {
                    const selecionada = notasSelecionadas.includes(nota.id);
                    const limiteAtingido = notasSelecionadas.length === 5 && !selecionada;
                    return (
                      <label key={nota.id} className={`nota-source-item ${selecionada ? "selected" : ""} ${limiteAtingido ? "disabled" : ""}`}>
                        <input type="checkbox" checked={selecionada} disabled={limiteAtingido}
                          onChange={() => alternarNota(nota.id)} />
                        <span><strong>{nota.numeroCfe ? `NFC-e ${nota.numeroCfe}` : `Nota ${nota.id}`}</strong>
                          <small>{formatarDataNota(nota.dataHoraEmissao)}</small></span>
                        <span><strong>{nota.quantidadeItens} itens</strong>
                          <small>{nota.valorTotal.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })}</small></span>
                      </label>
                    );
                  })}
                </div>

                {notasOrigem.totalPaginas > 1 && <div className="catalog-pagination nota-pagination">
                  <button type="button" disabled={paginaNotas === 0 || carregandoNotas}
                    onClick={() => { setCarregandoNotas(true); setPaginaNotas(paginaNotas - 1); }}>Anterior</button>
                  <span>{paginaNotas + 1} de {notasOrigem.totalPaginas}</span>
                  <button type="button" disabled={paginaNotas + 1 >= notasOrigem.totalPaginas || carregandoNotas}
                    onClick={() => { setCarregandoNotas(true); setPaginaNotas(paginaNotas + 1); }}>Próxima</button>
                </div>}
              </>
            )}

            <div className="lista-dialog-actions">
              <button type="button" className="clear-button" onClick={() => setSeletorNotasAberto(false)}>Cancelar</button>
              <button type="button" className="lista-save-button" disabled={notasSelecionadas.length === 0 || gerandoRascunho}
                onClick={() => void gerarListaDasNotas()}>{gerandoRascunho ? "Gerando..." : "Gerar rascunho"}</button>
            </div>
          </section>
        </div>
      )}

      {acaoPendente && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setAcaoPendente(null)}>
          <section className="modal-card lista-dirty-dialog" role="dialog" aria-modal="true"
            aria-labelledby="dirty-dialog-title" onMouseDown={(event) => event.stopPropagation()}>
            <p className="section-kicker">Alterações pendentes</p>
            <h3 id="dirty-dialog-title">Salvar antes de continuar?</h3>
            <p>As mudanças desta lista ainda não foram enviadas para o banco de dados.</p>
            <div className="lista-dialog-actions">
              <button type="button" className="clear-button" onClick={() => setAcaoPendente(null)}>Cancelar</button>
              <button type="button" className="lista-discard-button" onClick={() => void descartarEContinuar()}>Descartar</button>
              <button type="button" className="lista-save-button" disabled={salvando}
                onClick={() => void salvarEContinuar()}>{salvando ? "Salvando..." : "Salvar e continuar"}</button>
            </div>
          </section>
        </div>
      )}
    </>
  );
}