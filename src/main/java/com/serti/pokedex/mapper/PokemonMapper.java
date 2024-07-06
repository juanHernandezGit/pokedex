package com.serti.pokedex.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.serti.pokedex.dto.PokemonDTO;
import com.serti.pokedex.entity.PokemonEntity;

@Mapper
public interface PokemonMapper {
    PokemonMapper INSTANCE = Mappers.getMapper(PokemonMapper.class);

    PokemonDTO pokemonToPokemonDTO(PokemonEntity pokemon);
    PokemonEntity pokemonDTOToPokemon(PokemonDTO pokemonDTO);
}

