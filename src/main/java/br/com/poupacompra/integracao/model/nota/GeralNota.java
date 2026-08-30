package br.com.poupacompra.integracao.model.nota;

import java.util.ArrayList;
import java.util.List;

import br.com.poupacompra.integracao.model.usuario.Usuario;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_cfe")
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

    public void setItensNotas(List<ItensNota> itensNotas ) {
        this.itensNotas = itensNotas;
        itensNotas.forEach(itemNota -> itemNota.setNota(this));
    }

    
}