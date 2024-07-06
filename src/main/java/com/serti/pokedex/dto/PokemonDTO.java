package com.serti.pokedex.dto;

import java.util.Set;

import lombok.Data;

@Data
public class PokemonDTO {
    private Long id;
    private String name;
    private Set<EvolutionChainDTO> evolutionChains;
}
