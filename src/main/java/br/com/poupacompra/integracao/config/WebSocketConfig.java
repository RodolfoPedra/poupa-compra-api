package br.com.poupacompra.integracao.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import br.com.poupacompra.integracao.service.listacompra.ListaCompraAcessoService;
import br.com.poupacompra.integracao.service.usuario.JwtService;
import br.com.poupacompra.integracao.service.usuario.UsuarioDetailsService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final String TOPICO_LISTA = "/topic/listas/";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final ListaCompraAcessoService acessoService;
    private final String allowedOrigins;

    public WebSocketConfig(JwtService jwtService, UsuarioDetailsService usuarioDetailsService,
            ListaCompraAcessoService acessoService,
            @Value("${security.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.acessoService = acessoService;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).toArray(String[]::new));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    throw new MessagingException("Frame STOMP inválido");
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    autenticar(accessor);
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    autorizarAssinatura(accessor);
                } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                    throw new MessagingException("Envio de mensagens não permitido");
                }
                return message;
            }
        });
    }

    private void autenticar(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new MessagingException("Token de acesso ausente");
        }
        String token = authorization.substring(7);
        try {
            UserDetails user = usuarioDetailsService.loadUserByUsername(jwtService.extrairEmail(token));
            if (!jwtService.isValido(token, user)) {
                throw new MessagingException("Token de acesso inválido");
            }
            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } catch (RuntimeException exception) {
            throw new MessagingException("Token de acesso inválido");
        }
    }

    private void autorizarAssinatura(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new MessagingException("Usuário não autenticado");
        }
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(TOPICO_LISTA)) {
            throw new MessagingException("Destino não permitido");
        }
        try {
            Long listaId = Long.valueOf(destination.substring(TOPICO_LISTA.length()));
            acessoService.buscarParaLeitura(accessor.getUser().getName(), listaId);
        } catch (RuntimeException exception) {
            throw new MessagingException("Assinatura não autorizada");
        }
    }
}