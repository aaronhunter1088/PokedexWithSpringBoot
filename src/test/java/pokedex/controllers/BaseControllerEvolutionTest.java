package pokedex.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pokedexapi.service.PokemonApiService;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the evolution chain helpers in {@link BaseController}.
 */
class BaseControllerEvolutionTest
{
    private PokemonApiService pokemonApiService;
    private BaseController controller;

    @BeforeEach
    void setup()
    {
        pokemonApiService = mock(PokemonApiService.class);
        controller = new BaseController(pokemonApiService, null, null, null, null);
    }

    private static Map<String, Object> link(int id, List<Map<String, Object>> evolvesTo)
    {
        return Map.of("species", Map.of("url", "https://pokeapi.co/api/v2/pokemon-species/" + id + "/"),
                "evolves_to", evolvesTo);
    }

    @Test
    @DisplayName("getEvolutionChainID reads the chain id from the species data")
    void testGetEvolutionChainID()
    {
        final PokemonSpecies species = new PokemonSpecies();
        species.setEvolutionChain(new NamedApiResource<>("EvolutionChain", "https://pokeapi.co/api/v2/evolution-chain/1/"));
        when(pokemonApiService.getPokemonSpeciesData("1")).thenReturn(species);

        assertEquals(1, controller.getEvolutionChainID("1"));

        verify(pokemonApiService, times(1)).getPokemonSpeciesData("1");
    }

    @Test
    @DisplayName("getEvolutionChainID returns 0 when species data is missing")
    void testGetEvolutionChainIDMissingSpecies()
    {
        when(pokemonApiService.getPokemonSpeciesData(anyString())).thenReturn(null);

        assertEquals(0, controller.getEvolutionChainID("0"));

        verify(pokemonApiService, times(1)).getPokemonSpeciesData("0");
    }

    @Test
    @DisplayName("getEvolutionChainID returns 0 when the lookup throws")
    void testGetEvolutionChainIDThrows()
    {
        when(pokemonApiService.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("Test Exception"));

        assertEquals(0, controller.getEvolutionChainID("1"));

        verify(pokemonApiService, times(1)).getPokemonSpeciesData("1");
    }

    @Test
    @DisplayName("getFamilyIDs builds one list per stage for a linear chain")
    void testGetFamilyIDsLinear()
    {
        final Map<String, Object> chain = Map.of("chain", link(1, List.of(link(2, List.of(link(3, List.of()))))));

        assertEquals(List.of(List.of(1), List.of(2), List.of(3)), controller.getFamilyIDs(chain));
    }

    @Test
    @DisplayName("getFamilyIDs groups branching evolutions into the same stage")
    void testGetFamilyIDsBranching()
    {
        final Map<String, Object> chain = Map.of("chain", link(133, List.of(link(134, List.of()), link(135, List.of()))));

        assertEquals(List.of(List.of(133), List.of(134, 135)), controller.getFamilyIDs(chain));
    }

    @Test
    @DisplayName("getFamilyIDs returns an empty list for a missing chain")
    void testGetFamilyIDsEmpty()
    {
        assertTrue(controller.getFamilyIDs(Map.of()).isEmpty());
        assertTrue(controller.getFamilyIDs(null).isEmpty());
    }
}
