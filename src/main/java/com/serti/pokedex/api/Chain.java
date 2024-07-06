package com.serti.pokedex.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Chain {
	 private Species species;
	 @JsonProperty("evolves_to")
     private List<EvolvesTo> evolvesTo;
}