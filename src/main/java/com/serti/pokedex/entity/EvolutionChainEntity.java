package com.serti.pokedex.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "evolution_chain")
@Data
public class EvolutionChainEntity {

    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "id_pokemon")
    private PokemonEntity pokemon;
}
