package br.com.poupacompra.integracao.model.nota;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "estabelecimento")
public class Estabelecimento {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 150)
    @NotNull
    @Column(name = "nome_estabelecimento", nullable = false, length = 150)
    private String nomeEstabelecimento;

    @Size(max = 14)
    @NotNull
    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Size(max = 200)
    @NotNull
    @Column(name = "endereco", nullable = false, length = 200)
    private String endereco;

    @JsonIgnore
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GeralNota> notas = new ArrayList<>();

}