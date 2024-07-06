package com.serti.pokedex.exception;

public class EvolutionChainNotFoundException extends RuntimeException {
    public EvolutionChainNotFoundException(Long id) {
        super("No se encontro la cadena de evolución con el id: " + id);
    }
}