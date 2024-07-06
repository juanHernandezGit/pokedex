package com.serti.pokedex.controller;

import com.serti.pokedex.dto.PokemonDTO;
import com.serti.pokedex.service.PokemonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{id}")
    public PokemonDTO getPokemonById(@PathVariable Long id) {
        return pokemonService.getPokemonById(id);
    }
}

