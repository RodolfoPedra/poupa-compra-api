"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useSession } from "../lib/session";

const links = [
  { href: "/notas/nova", label: "Cadastrar nota" },
  { href: "/notas", label: "Notas" },
  { href: "/listas", label: "Listas de compras" },
];

export default function PainelShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { usuario, encerrarSessao } = useSession();

  async function handleLogout() {
    await encerrarSessao();
    router.push("/login");
  }

  return (
    <div className="painel-shell">
      <aside className="painel-sidebar">
        <div>
          <p className="eyebrow">Poupacompra</p>
          <h1 className="painel-title">Painel</h1>
        </div>

        <nav className="painel-nav">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className={pathname === link.href ? "active" : ""}>
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="painel-user">
          <p>{usuario?.nome}</p>
          <p>{usuario?.email}</p>
          {usuario?.role === "ADMIN" && <span className="painel-badge">Administrador</span>}
          <button className="text-button" type="button" onClick={handleLogout}>Sair</button>
        </div>
      </aside>

      <main className="painel-main">{children}</main>
    </div>
  );
}
