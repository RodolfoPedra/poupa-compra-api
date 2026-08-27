package br.com.poupacompra.integracao.service.usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public String gerar() {
        byte[] value = new byte[32];
        secureRandom.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }

    public String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível gerar o hash do token", exception);
        }
    }
}