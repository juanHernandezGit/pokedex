package com.serti.pokedex.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SpeciesApiResponse {

	@JsonProperty("evolution_chain")
    private EvolutionChain evolutionChain;
}
