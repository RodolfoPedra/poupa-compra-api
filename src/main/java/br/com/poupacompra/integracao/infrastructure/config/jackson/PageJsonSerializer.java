package br.com.poupacompra.integracao.infrastructure.config.jackson;

import java.io.IOException;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PageJsonSerializer extends JsonSerializer<Page<?>> {

  @Override
  public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeStartObject();

    gen.writeObjectFieldStart("metadata");
    gen.writeNumberField("size", page.getSize());
    gen.writeNumberField("totalElements", page.getTotalElements());
    gen.writeNumberField("totalPages", page.getTotalPages());
    gen.writeNumberField("pageNumber", page.getNumber());
    gen.writeNumberField("pageElements", page.getNumberOfElements());
    gen.writeEndObject();

    gen.writeObjectField("data", page.getContent());

    gen.writeEndObject();
  }

}
