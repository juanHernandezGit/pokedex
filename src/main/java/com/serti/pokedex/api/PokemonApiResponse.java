package com.serti.pokedex.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PokemonApiResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("species")
    private Species species;
}
