package com.serti.pokedex.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serti.pokedex.entity.EvolutionChainEntity;
import com.serti.pokedex.entity.PokemonEntity;

public interface EvolutionChainRepository extends JpaRepository<EvolutionChainEntity, Long> {
    Set<EvolutionChainEntity> findByPokemon(PokemonEntity pokemon);
}
