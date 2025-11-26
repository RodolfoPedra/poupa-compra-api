package br.com.poupacompra.integracao.model.nota;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "geral_nota")
public class GeralNota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "quantidade_itens", nullable = false)
    private Integer quantidadeItens;

    @NotNull
    @Column(name = "valor_total", nullable = false)
    private Float valorTotal;

    @NotNull
    @Column(name = "usuario_id", nullable = false)
    private Long usuario;

    @NotNull
    @Column(name = "numero_cfe", nullable = false)
    private Integer numeroCfe;

    @Size(max = 2)
    @NotNull
    @Column(name = "uf_cfe", nullable = false, length = 2)
    private String ufCfe;

    @Column(name = "data_hora_emissao")
    private String dataHoraEmissao;

    @NotNull
    @Column(name = "url_cfe", nullable = false)
    private String urlCfe;

    @NotNull
    @Column(name = "chave_acesso", nullable = false)
    private String chaveAcesso;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "id", nullable = false)
    private Estabelecimento estabelecimento;

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItensNota> itensNotas = new ArrayList<>();

    // @Version
    // @Column(name = "version")
    // private Long version;

    public void setItensNotas(List<ItensNota> itensNotas ) {
        this.itensNotas = itensNotas;
        itensNotas.forEach(itemNota -> itemNota.setNota(this));
    }

    
}