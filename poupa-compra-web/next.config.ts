import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/integracao-poupa-compra/:path*",
        destination: `${process.env.API_BASE_URL || "http://localhost:8182/integracao-poupa-compra"}/:path*`,
      },
    ];
  },
};

export default nextConfig;
