const apiRoot = "/api/integracao-poupa-compra/api/v1";
const basePath = `${apiRoot}/auth`;

export type UserResponse = {
  id: number;
  nome: string;
  email: string;
  role: "USER" | "ADMIN";
  emailVerificado: boolean;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  usuario: UserResponse;
};

async function parseErrorMessage(response: Response): Promise<string> {
  const text = await response.text();
  return text || `Erro inesperado (HTTP ${response.status}).`;
}

async function postJson<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${basePath}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response));
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function login(email: string, senha: string) {
  return postJson<AuthResponse>("/login", { email, senha });
}

export function register(nome: string, email: string, senha: string) {
  return postJson<UserResponse>("/register", { nome, email, senha });
}

export function loginGoogle(authorizationCode: string, redirectUri: string) {
  return postJson<AuthResponse>("/oauth/google", { authorizationCode, redirectUri });
}

export function verifyEmail(token: string) {
  return postJson<void>("/verify-email", { token });
}

export function resendVerification(email: string) {
  return postJson<void>("/resend-verification", { email });
}

export function forgotPassword(email: string) {
  return postJson<void>("/forgot-password", { email });
}

export function resetPassword(token: string, novaSenha: string) {
  return postJson<void>("/reset-password", { token, novaSenha });
}

export function refreshSession(refreshToken: string) {
  return postJson<AuthResponse>("/refresh", { refreshToken });
}

export function logout(refreshToken: string) {
  return postJson<void>("/logout", { refreshToken });
}

export async function me(accessToken: string): Promise<UserResponse> {
  const response = await fetch(`${basePath}/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response));
  }
  return response.json();
}

export type NotaResumo = {
  id: number;
  quantidadeItens: number;
  valorTotal: number;
  usuario: number;
  numeroCfe: number | null;
  ufCfe: string;
  dataHoraEmissao: string | null;
  urlCfe: string;
  chaveAcesso: string;
};

export async function listarNotas(accessToken: string): Promise<NotaResumo[]> {
  const response = await fetch(`${apiRoot}/notas`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response));
  }
  return response.json();
}

export type ItemNotaResumo = {
  descricao: string;
  quantidade: number;
  tipoUnidade: string | null;
  codigoItem: number | null;
  valorUnitario: number;
  valorTotal: number;
};

export async function listarItensDaNota(accessToken: string, notaId: number): Promise<ItemNotaResumo[]> {
  const response = await fetch(`${apiRoot}/notas/${notaId}/itens`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response));
  }
  return response.json();
}

export type CategoriaProduto = {
  id: number;
  nome: string;
};

export type ProdutoCatalogo = {
  id: number;
  nome: string;
  categoriaId: number;
  categoriaNome: string;
};

export type PaginaResponse<T> = {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
};

export type UnidadeMedida = "UNIDADE" | "QUILOGRAMA";

export type ItemListaCompra = {
  id: number;
  produtoId: number | null;
  descricao: string;
  quantidade: number | null;
  unidade: UnidadeMedida | null;
  selecionado: boolean;
  ordem: number;
};

export type ListaCompraResumo = {
  id: number;
  nome: string;
  quantidadeItens: number;
  quantidadeSelecionados: number;
  createdAt: string;
  updatedAt: string;
};

export type ListaCompra = {
  id: number;
  nome: string;
  createdAt: string;
  updatedAt: string;
  nota: NotaOrigemLista | null;
  itens: ItemListaCompra[];
};

export type ItemListaPayload = {
  id?: number;
  produtoId?: number | null;
  descricao: string;
  quantidade: number | null;
  unidade: UnidadeMedida | null;
  selecionado: boolean;
  ordem: number;
};

export type SalvarListaPayload = {
  nome: string;
  updatedAt?: string;
  itens: ItemListaPayload[];
};

export type EstabelecimentoNota = {
  id: number;
  nome: string;
  cpfCnpj: string;
};

export type NotaOrigemLista = {
  id: number;
  numeroCfe: number | null;
  dataHoraEmissao: string | null;
  valorTotal: number;
  quantidadeItens: number;
};

export type RascunhoListaNotas = {
  nome: string;
  itens: Omit<ItemListaCompra, "id">[];
};

async function authenticatedJson<T>(accessToken: string, path: string, init?: RequestInit): Promise<T> {
  const timeoutSignal = AbortSignal.timeout(15_000);
  const signal = init?.signal ? AbortSignal.any([init.signal, timeoutSignal]) : timeoutSignal;
  try {
    const response = await fetch(`${apiRoot}${path}`, {
      ...init,
      signal,
      headers: {
        Authorization: `Bearer ${accessToken}`,
        ...(init?.body ? { "Content-Type": "application/json" } : {}),
        ...init?.headers,
      },
    });
    if (!response.ok) {
      throw new Error(await parseErrorMessage(response));
    }
    const text = await response.text();
    return (text ? JSON.parse(text) : undefined) as T;
  } catch (error) {
    if (timeoutSignal.aborted) {
      throw new Error("A API demorou para responder. Tente novamente.");
    }
    throw error;
  }
}

export function listarCategorias(accessToken: string) {
  return authenticatedJson<CategoriaProduto[]>(accessToken, "/categorias");
}

export function pesquisarProdutos(
  accessToken: string,
  filtros: { busca?: string; categoriaId?: number; pagina?: number; tamanho?: number; signal?: AbortSignal },
) {
  const params = new URLSearchParams({
    pagina: String(filtros.pagina ?? 0),
    tamanho: String(filtros.tamanho ?? 20),
  });
  if (filtros.busca?.trim()) params.set("busca", filtros.busca.trim());
  if (filtros.categoriaId) params.set("categoriaId", String(filtros.categoriaId));
  return authenticatedJson<PaginaResponse<ProdutoCatalogo>>(accessToken, `/produtos?${params}`, {
    signal: filtros.signal,
  });
}

export function listarListas(accessToken: string) {
  return authenticatedJson<ListaCompraResumo[]>(accessToken, "/listas");
}

export function buscarLista(accessToken: string, listaId: number) {
  return authenticatedJson<ListaCompra>(accessToken, `/listas/${listaId}`);
}

export function criarLista(accessToken: string, payload: SalvarListaPayload) {
  return authenticatedJson<ListaCompra>(accessToken, "/listas", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function salvarLista(accessToken: string, listaId: number, payload: SalvarListaPayload) {
  return authenticatedJson<ListaCompra>(accessToken, `/listas/${listaId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function excluirLista(accessToken: string, listaId: number) {
  return authenticatedJson<void>(accessToken, `/listas/${listaId}`, { method: "DELETE" });
}

export function listarEstabelecimentosDasNotas(accessToken: string) {
  return authenticatedJson<EstabelecimentoNota[]>(accessToken, "/listas/origem-notas/estabelecimentos");
}

export function listarNotasParaLista(
  accessToken: string,
  estabelecimentoId: number,
  filtros: { pagina?: number; tamanho?: number; signal?: AbortSignal },
) {
  const params = new URLSearchParams({
    pagina: String(filtros.pagina ?? 0),
    tamanho: String(filtros.tamanho ?? 10),
  });
  return authenticatedJson<PaginaResponse<NotaOrigemLista>>(
    accessToken,
    `/listas/origem-notas/estabelecimentos/${estabelecimentoId}/notas?${params}`,
    { signal: filtros.signal },
  );
}

export function gerarRascunhoListaPorNotas(accessToken: string, notaIds: number[]) {
  return authenticatedJson<RascunhoListaNotas>(accessToken, "/listas/origem-notas/rascunho", {
    method: "POST",
    body: JSON.stringify({ notaIds }),
  });
}

export function listarNotasDisponiveisParaVinculo(
  accessToken: string,
  listaId: number,
  filtros: { pagina?: number; tamanho?: number; signal?: AbortSignal },
) {
  const params = new URLSearchParams({
    pagina: String(filtros.pagina ?? 0),
    tamanho: String(filtros.tamanho ?? 10),
  });
  return authenticatedJson<PaginaResponse<NotaOrigemLista>>(
    accessToken,
    `/listas/${listaId}/notas-disponiveis?${params}`,
    { signal: filtros.signal },
  );
}

export function atualizarVinculoNota(
  accessToken: string,
  listaId: number,
  notaId: number | null,
  updatedAt: string,
) {
  return authenticatedJson<ListaCompra>(accessToken, `/listas/${listaId}/nota`, {
    method: "PATCH",
    body: JSON.stringify({ notaId, updatedAt }),
  });
}

export function cadastrarNotaVinculada(
  accessToken: string,
  listaId: number,
  nota: unknown,
  updatedAt: string,
) {
  return authenticatedJson<ListaCompra>(accessToken, `/listas/${listaId}/nota`, {
    method: "POST",
    body: JSON.stringify({ nota, updatedAt }),
  });
}

