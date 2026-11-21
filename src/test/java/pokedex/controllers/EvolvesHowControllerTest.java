package pokedex.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.servlet.ModelAndView;
import pokedex.services.DarkmodeService;
import pokedex.services.GifService;
import pokedexapi.service.PokemonApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EvolvesHowController")
class EvolvesHowControllerTest
{
    private PokemonApiService service;
    private EvolvesHowController controller;

    @BeforeEach
    void setup()
    {
        service = mock(PokemonApiService.class);
        controller = new EvolvesHowController(service, TestFixtures.JSON_MAPPER, new MockEnvironment(),
                new DarkmodeService(), new GifService());
        when(service.getPokemonSpeciesData(anyString())).thenReturn(TestFixtures.species());
    }

    private ModelAndView evolvesHow(int pokemonId)
    {
        return controller.evolvesHow(pokemonId, new ModelAndView());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> attributes(ModelAndView mav)
    {
        return (Map<String, Object>) mav.getModel().get("attributesMap");
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("evolvesHow reports the level a pokemon evolves at")
        void testEvolvesHowByLevel()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());

            final ModelAndView mav = evolvesHow(1);

            assertEquals("evolves-how", mav.getViewName());
            assertEquals(1, mav.getModel().get("pokemonId"));
            assertEquals(1, mav.getModel().get("pokemonChainId"));
            assertEquals(false, mav.getModel().get("emptyChain"));
            assertEquals(true, mav.getModel().get("doesPokemonEvolve"));
            assertEquals(true, mav.getModel().get("hasMinimumLevel"));
            assertEquals(16, attributes(mav).get("minLevel"));
        }

        @Test
        @DisplayName("evolvesHow reports each stage with its own level")
        void testEvolvesHowSecondStage()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());

            assertEquals(32, attributes(evolvesHow(2)).get("minLevel"));
        }

        @Test
        @DisplayName("evolvesHow reports a final stage as not evolving")
        void testEvolvesHowFinalStage()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());

            final ModelAndView mav = evolvesHow(3);

            assertEquals(false, mav.getModel().get("emptyChain"));
            assertEquals(false, mav.getModel().get("doesPokemonEvolve"));
            assertEquals(false, mav.getModel().get("hasMinimumLevel"));
        }

        @Test
        @DisplayName("evolvesHow does not leak state between requests")
        void testEvolvesHowResetsState()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());
            assertEquals(true, evolvesHow(1).getModel().get("hasMinimumLevel"));

            assertEquals(false, evolvesHow(3).getModel().get("hasMinimumLevel"));
        }

        @Test
        @DisplayName("setAttributesMap maps the evolution details")
        void testSetAttributesMap()
        {
            controller.pokemonIdAndAttributesMap = new HashMap<>();
            final Map<String, Object> details = ChainFixtures.details(20, "trade");
            details.put("id", "64");
            details.put("name", "kadabra");
            details.put("held_item", Map.of("name", "metal-coat"));
            details.put("is_baby", false);

            controller.setAttributesMap(details);

            final Map<String, Object> result = controller.pokemonIdAndAttributesMap.get(64);
            assertEquals("kadabra", result.get("name"));
            assertEquals("metal-coat", result.get("heldItem"));
            assertEquals(20, result.get("minLevel"));
            assertEquals(true, result.get("tradeSpecies"));
            assertNull(result.get("timeOfDay"));
        }

        @Test
        @DisplayName("updateAttributesMap collects repeated details into a list")
        void testUpdateAttributesMap()
        {
            controller.pokemonIdAndAttributesMap = new HashMap<>();
            final Map<String, Object> existing = new HashMap<>();
            existing.put("minLevel", 16);
            final Map<String, Object> details = ChainFixtures.details(20, "level-up");
            details.put("id", "1");
            details.put("item", Map.of("name", "thunder-stone"));

            controller.updateAttributesMap(details, existing);

            assertEquals(List.of(16, 20), existing.get("minLevel"));
            assertEquals("thunder-stone", existing.get("useItem"));
            assertEquals(existing, controller.pokemonIdAndAttributesMap.get(1));
        }

        @Test
        @DisplayName("updateAttributesMap flags trading evolutions")
        void testUpdateAttributesMapTrade()
        {
            controller.pokemonIdAndAttributesMap = new HashMap<>();
            final Map<String, Object> existing = new HashMap<>();
            final Map<String, Object> details = ChainFixtures.details(1, "trade");
            details.put("id", "93");

            controller.updateAttributesMap(details, existing);

            assertEquals(true, existing.get("tradeSpecies"));
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("evolvesHow reports an empty chain when the species has no chain")
        void testEvolvesHowNoChain()
        {
            when(service.getPokemonSpeciesData(anyString())).thenReturn(null);

            final ModelAndView mav = evolvesHow(1);

            assertEquals(0, mav.getModel().get("pokemonChainId"));
            assertEquals(true, mav.getModel().get("emptyChain"));
            assertEquals(false, mav.getModel().get("doesPokemonEvolve"));
            assertNull(mav.getModel().get("attributesMap"));
        }

        @Test
        @DisplayName("evolvesHow reports an empty chain when the species lookup throws")
        void testEvolvesHowSpeciesThrows()
        {
            when(service.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("species down"));

            assertEquals(true, evolvesHow(1).getModel().get("emptyChain"));
        }

        @Test
        @DisplayName("evolvesHow reports an empty chain when the chain data is empty")
        void testEvolvesHowEmptyChainData()
        {
            when(service.getPokemonChainData("1")).thenReturn(Map.of());

            final ModelAndView mav = evolvesHow(1);

            assertEquals(true, mav.getModel().get("emptyChain"));
            assertEquals(false, mav.getModel().get("doesPokemonEvolve"));
        }

        @Test
        @DisplayName("evolvesHow reports a pokemon without evolutions as not evolving")
        void testEvolvesHowSingleStage()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.singleChain());

            final ModelAndView mav = evolvesHow(128);

            assertEquals(false, mav.getModel().get("doesPokemonEvolve"));
            assertTrue(attributes(mav).values().stream().noneMatch(Boolean.TRUE::equals));
        }
    }
}
