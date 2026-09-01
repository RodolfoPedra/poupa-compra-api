package br.com.poupacompra.integracao.service.listacompra;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.poupacompra.integracao.dto.listacompra.EventoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ItemListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.TipoEventoLista;
import br.com.poupacompra.integracao.model.listacompra.ItemListaCompra;
import br.com.poupacompra.integracao.model.listacompra.ListaCompra;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ListaCompraEventoPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public ListaCompraEventoPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publicar(TipoEventoLista tipo, ListaCompra lista, ItemListaCompra item) {
        agendar(new EventoListaResponse(tipo, lista.getId(),
                lista.getUpdatedAt(), lista.getNome(), item == null ? null : ItemListaCompraResponse.from(item),
            item == null ? null : item.getId()));
    }

    public void publicarRemocao(ListaCompra lista, Long itemId) {
        agendar(new EventoListaResponse(TipoEventoLista.ITEM_REMOVIDO,
            lista.getId(), lista.getUpdatedAt(), lista.getNome(), null, itemId));
    }

    public void publicar(TipoEventoLista tipo, Long listaId) {
        agendar(new EventoListaResponse(tipo, listaId, null, null, null, null));
    }

    private void agendar(EventoListaResponse evento) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Eventos de lista devem ser publicados dentro de uma transação");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend("/topic/listas/" + evento.listaId(), evento);
                log.info("listEvent type={} listaId={}", evento.tipo(), evento.listaId());
            }
        });
    }
}