package com.serti.pokedex.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serti.pokedex.api.Chain;
import com.serti.pokedex.api.EvolutionChainApiResponse;
import com.serti.pokedex.api.EvolvesTo;
import com.serti.pokedex.api.PokemonApiResponse;
import com.serti.pokedex.api.SpeciesApiResponse;
import com.serti.pokedex.audit.LogEntity;
import com.serti.pokedex.audit.LogRepository;
import com.serti.pokedex.dto.EvolutionChainDTO;
import com.serti.pokedex.dto.PokemonDTO;
import com.serti.pokedex.entity.EvolutionChainEntity;
import com.serti.pokedex.entity.PokemonEntity;
import com.serti.pokedex.exception.ApiRequestException;
import com.serti.pokedex.exception.EvolutionChainNotFoundException;
import com.serti.pokedex.exception.PokemonNotFoundException;
import com.serti.pokedex.mapper.PokemonMapper;
import com.serti.pokedex.repository.EvolutionChainRepository;
import com.serti.pokedex.repository.PokemonRepository;

import jakarta.transaction.Transactional;

@Service
public class PokemonService {

    private static final Logger logger = LoggerFactory.getLogger(PokemonService.class);

    @Value("${pokeapi.base-url}")
    private String pokeApiBaseUrl;

    private final PokemonRepository pokemonRepository;
    private final EvolutionChainRepository evolutionChainRepository;
    private final LogRepository logRepository;
    private final ObjectMapper objectMapper;

    public PokemonService(PokemonRepository pokemonRepository,
                          EvolutionChainRepository evolutionChainRepository,
                          LogRepository logRepository,
                          ObjectMapper objectMapper) {
        this.pokemonRepository = pokemonRepository;
        this.evolutionChainRepository = evolutionChainRepository;
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
    }

    public PokemonDTO getPokemonById(Long id) {
        logger.info("Obteniendo pokemon y cadena de evolución para ID: {}", id);
        String pokemonUrl = pokeApiBaseUrl + "/pokemon/" + id;

        try {
            // Buscamos pokemon en base de datos, si existe, regresamos los datos.
            Optional<PokemonEntity> pokemonBd = pokemonRepository.findById(id);
            if (pokemonBd.isPresent()) {
                // Cargar la cadena de evolucion desde la base de datos
                Set<EvolutionChainEntity> evolutionChainEntities = evolutionChainRepository.findByPokemon(pokemonBd.get());
                Set<EvolutionChainDTO> evolutionChains = new HashSet<>();
                for (EvolutionChainEntity evolutionChainEntity : evolutionChainEntities) {
                    EvolutionChainDTO evolutionChainDTO = new EvolutionChainDTO();
                    evolutionChainDTO.setId(evolutionChainEntity.getId());
                    evolutionChainDTO.setName(evolutionChainEntity.getName());
                    evolutionChains.add(evolutionChainDTO);
                }
                // Mapear la entidad Pokemon a DTO y devolver
                return mapearAPokemonDTO(pokemonBd.get(), evolutionChains);
            }else {//Si no existe lo buscamos desde la api y continuamos el proceso.

            // Obtener y procesar datos de Pokemon
            PokemonApiResponse pokemonApiResponse = obtenerYProcesarPokemon(pokemonUrl);
            PokemonEntity pokemon = mapearAPokemonEntity(pokemonApiResponse);

            // Obtener y procesar datos de la especie para obtener la URL de la cadena de evolucion
            String speciesUrl = pokemonApiResponse.getSpecies().getUrl();
            SpeciesApiResponse pokemonSpeciesApiResponse = obtenerYProcesarPokemonSpecies(speciesUrl);
            String evolutionChainUrl = pokemonSpeciesApiResponse.getEvolutionChain().getUrl();

            // Obtener y procesar datos de la cadena de evolucion
            EvolutionChainApiResponse evolutionChainApiResponse = obtenerYProcesarCadenaEvolucion(evolutionChainUrl);
            Set<EvolutionChainDTO> evolutionChains = procesarCadenaEvolucion(evolutionChainApiResponse, pokemon);

            // Guardar Pokemon y cadenas de evolucion en la base de datos
            guardarPokemonYCadenasEvolucion(pokemon, evolutionChains);

            // Registrar el acceso a la API
            registrarAccesoApi(pokemonUrl, pokemonApiResponse);

            // Mapear la entidad Pokemon a DTO y devolver
            return mapearAPokemonDTO(pokemon, evolutionChains);
            }
        } catch (URISyntaxException | IOException e) {
            manejarExcepcion(e, pokemonUrl, id);
            return null;
        }
    }

    private PokemonApiResponse obtenerYProcesarPokemon(String pokemonUrl) throws URISyntaxException, IOException {
        String response = obtenerRespuestaDeApi(pokemonUrl);
        return objectMapper.readValue(response, PokemonApiResponse.class);
    }

    private SpeciesApiResponse obtenerYProcesarPokemonSpecies(String speciesUrl) throws URISyntaxException, IOException {
        String response = obtenerRespuestaDeApi(speciesUrl);
        return objectMapper.readValue(response, SpeciesApiResponse.class);
    }

    private EvolutionChainApiResponse obtenerYProcesarCadenaEvolucion(String evolutionChainUrl) throws URISyntaxException, IOException {
        String response = obtenerRespuestaDeApi(evolutionChainUrl);
        return objectMapper.readValue(response, EvolutionChainApiResponse.class);
    }

    private Set<EvolutionChainDTO> procesarCadenaEvolucion(EvolutionChainApiResponse evolutionChainApiResponse, PokemonEntity pokemon) {
        Set<EvolutionChainDTO> evolutionChains = new HashSet<>();
        if (evolutionChainApiResponse != null && evolutionChainApiResponse.getChain() != null) {
            recorrerCadenaEvolucion(evolutionChainApiResponse.getChain(), evolutionChains);
        }
        return evolutionChains;
    }

    @Transactional
    private void guardarPokemonYCadenasEvolucion(PokemonEntity pokemon, Set<EvolutionChainDTO> evolutionChains) {
        // Guarda el PokemonEntity
        PokemonEntity savedPokemon = pokemonRepository.save(pokemon);
        logger.info("Pokemon guardado: {}", savedPokemon);

        // Guarda cada EvolutionChainEntity asociado con el PokemonEntity guardado
        for (EvolutionChainDTO evolutionChainDTO : evolutionChains) {
            EvolutionChainEntity evolutionChain = new EvolutionChainEntity();
            evolutionChain.setId(evolutionChainDTO.getId());
            evolutionChain.setName(evolutionChainDTO.getName());
            evolutionChain.setPokemon(savedPokemon);
            EvolutionChainEntity savedEvolutionChain = evolutionChainRepository.save(evolutionChain);
            logger.info("Cadena de evolucion guardada: {}", savedEvolutionChain);
        }
    }


    private void registrarAccesoApi(String pokemonUrl, PokemonApiResponse pokemonApiResponse) {
        LogEntity log = new LogEntity();
        log.setUrl(pokemonUrl);
        log.setTimestamp(LocalDateTime.now());
        log.setAction("save");
        try {
            log.setResponse(objectMapper.writeValueAsString(pokemonApiResponse));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logRepository.save(log);
    }

    private PokemonDTO mapearAPokemonDTO(PokemonEntity pokemon, Set<EvolutionChainDTO> evolutionChains) {
        PokemonDTO pokemonDTO = PokemonMapper.INSTANCE.pokemonToPokemonDTO(pokemon);
        pokemonDTO.setEvolutionChains(evolutionChains);
        return pokemonDTO;
    }

    private void manejarExcepcion(Exception e, String url, Long id) {
        logger.error("Error obteniendo datos desde URL: {}", url, e);
        if (url.contains("pokemon")) {
            throw new PokemonNotFoundException(id);
        } else if (url.contains("evolution-chain")) {
            throw new EvolutionChainNotFoundException(id);
        } else {
            throw new ApiRequestException(url, e.getMessage());
        }
    }

    private PokemonEntity mapearAPokemonEntity(PokemonApiResponse pokemonApiResponse) {
        PokemonEntity pokemon = new PokemonEntity();
        pokemon.setId(pokemonApiResponse.getId());
        pokemon.setName(pokemonApiResponse.getName());
        return pokemon;
    }

    private String obtenerRespuestaDeApi(String url) throws URISyntaxException, IOException {
        try {
            URI uri = new URI(url);
            Response response = Request.get(uri).execute();
            return response.returnContent().asString(StandardCharsets.UTF_8);
        } catch (HttpResponseException e) {
            throw new ApiRequestException(url, "Error al obtener respuesta de la API: " + e.getMessage());
        }
    }

    private void recorrerCadenaEvolucion(Chain chain, Set<EvolutionChainDTO> evolutionChains) {
        if (chain != null) {
            EvolutionChainDTO evolutionChainDTO = new EvolutionChainDTO();
            evolutionChainDTO.setId(Long.parseLong(chain.getSpecies().getUrl().split("/")[6])); 
            evolutionChainDTO.setName(chain.getSpecies().getName());
            evolutionChains.add(evolutionChainDTO);
            if (chain.getEvolvesTo() != null) {
                for (EvolvesTo evolvesTo : chain.getEvolvesTo()) {
                    recorrerCadenaEvolucion(evolvesTo, evolutionChains);
                }
            }
        }
    }

    private void recorrerCadenaEvolucion(EvolvesTo evolvesTo, Set<EvolutionChainDTO> evolutionChains) {
        if (evolvesTo != null) {
            EvolutionChainDTO evolutionChainDTO = new EvolutionChainDTO();
            evolutionChainDTO.setId(Long.parseLong(evolvesTo.getSpecies().getUrl().split("/")[6])); 
            evolutionChainDTO.setName(evolvesTo.getSpecies().getName());
            evolutionChains.add(evolutionChainDTO);
            if (evolvesTo.getEvolvesTo() != null) {
                for (EvolvesTo futureEvolvesTo : evolvesTo.getEvolvesTo()) {
                    recorrerCadenaEvolucion(futureEvolvesTo, evolutionChains);
                }
            }
        }
    }
}
