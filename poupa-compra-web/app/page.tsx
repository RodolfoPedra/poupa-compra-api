"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "./lib/session";

export default function HomeRedirect() {
  const router = useRouter();
  const { usuario, carregando } = useSession();

  useEffect(() => {
    if (carregando) return;
    router.replace(usuario ? "/notas" : "/login");
  }, [carregando, usuario, router]);

  return null;
}
