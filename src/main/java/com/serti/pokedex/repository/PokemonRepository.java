package com.serti.pokedex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serti.pokedex.entity.PokemonEntity;

@Repository
public interface PokemonRepository extends JpaRepository<PokemonEntity, Long> {
}
