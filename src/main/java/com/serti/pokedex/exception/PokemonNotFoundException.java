package com.serti.pokedex.exception;

public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(Long id) {
        super("No se encontro el Pokémon con el id: " + id);
    }
}
