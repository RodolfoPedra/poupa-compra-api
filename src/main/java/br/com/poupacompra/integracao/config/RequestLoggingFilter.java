package br.com.poupacompra.integracao.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final String REQUEST_ID = "requestId";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod()) || request.getRequestURI().endsWith("/actuator/health");
    }

    @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long inicio = System.nanoTime();
        MDC.put(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
            String usuario = usuarioAutenticado();
            if (status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                log.error("http method={} path={} status={} durationMs={} user={}", request.getMethod(),
                        request.getRequestURI(), status, duracaoMs, usuario);
            } else if (status >= HttpServletResponse.SC_BAD_REQUEST) {
                log.warn("http method={} path={} status={} durationMs={} user={}", request.getMethod(),
                        request.getRequestURI(), status, duracaoMs, usuario);
            } else {
                log.info("http method={} path={} status={} durationMs={} user={}", request.getMethod(),
                        request.getRequestURI(), status, duracaoMs, usuario);
            }
            MDC.remove(REQUEST_ID);
        }
    }

    private String usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? "authenticated" : "anonymous";
    }
}