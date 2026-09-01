"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import PainelShell from "../components/PainelShell";
import { useSession } from "../lib/session";

export default function PainelLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { usuario, carregando } = useSession();

  useEffect(() => {
    if (!carregando && !usuario) {
      router.replace("/login");
    }
  }, [carregando, usuario, router]);

  if (carregando || !usuario) {
    return (
      <main className="page-shell">
        <p className="auth-subtitle">Carregando sessão...</p>
      </main>
    );
  }

  return <PainelShell>{children}</PainelShell>;
}
