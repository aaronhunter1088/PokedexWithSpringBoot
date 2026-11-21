package pokedex.controllers;

import org.springframework.core.io.ClassPathResource;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Loads the Pokemon and species fixtures used across the controller tests.
 */
final class TestFixtures
{
    static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private TestFixtures() {}

    static Pokemon ditto()
    {
        return read("fixtures/ditto.json", Pokemon.class);
    }


    static PokemonSpecies species()
    {
        return read("fixtures/species1.json", PokemonSpecies.class);
    }

    static String speciesJson()
    {
        try {
            return new String(new ClassPathResource("fixtures/species1.json").getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> T read(String path, Class<T> type)
    {
        try {
            return JSON_MAPPER.readValue(new ClassPathResource(path).getFile(), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
