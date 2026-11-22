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
import skaro.pokeapi.resource.pokemon.Pokemon;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("EvolutionsController")
class EvolutionsControllerTest
{
    private PokemonApiService service;
    private EvolutionsController controller;

    @BeforeEach
    void setup()
    {
        service = mock(PokemonApiService.class);
        controller = new EvolutionsController(service, TestFixtures.JSON_MAPPER, new MockEnvironment(),
                new DarkmodeService(), new GifService());

        final Pokemon ditto = TestFixtures.ditto();
        when(service.getPokemonByIdOrName(anyString())).thenReturn(ditto);
        when(service.getPokemonSpeciesData(anyString())).thenReturn(TestFixtures.species());
        when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("getEvolutions renders every stage of the chain")
        void testGetEvolutions()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());

            final ModelAndView mav = controller.getEvolutions("1", new ModelAndView(), "false");

            assertEquals("evolutions", mav.getViewName());
            assertEquals("Ditto", mav.getModel().get("pokemonName"));
            assertEquals("1", mav.getModel().get("pokemonId"));
            assertEquals(List.of(1, 2, 3), mav.getModel().get("stages"));
            assertEquals(List.of(1, 2, 3), mav.getModel().get("allIDs"));
            assertEquals(3, ((List<?>) mav.getModel().get("pokemonFamily")).size());
            assertEquals(false, mav.getModel().get("isDarkMode"));
        }

        @Test
        @DisplayName("getEvolutions passes darkmode through when it is true")
        void testGetEvolutionsDarkmode()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());

            assertEquals(true, controller.getEvolutions("1", new ModelAndView(), "true").getModel().get("isDarkMode"));
        }

        @Test
        @DisplayName("getEvolutions sorts branching ids and keeps them in one stage")
        void testGetEvolutionsBranching()
        {
            final Map<String, Object> chain = new java.util.HashMap<>();
            chain.put("chain", ChainFixtures.link("eevee", 133, null,
                    ChainFixtures.link("vaporeon", 134, ChainFixtures.details(1, "use-item")),
                    ChainFixtures.link("jolteon", 135, ChainFixtures.details(1, "use-item"))));
            when(service.getPokemonChainData("1")).thenReturn(chain);

            final ModelAndView mav = controller.getEvolutions("133", new ModelAndView(), "false");

            assertEquals(List.of(133, 134, 135), mav.getModel().get("allIDs"));
            assertEquals(List.of(1, 2), mav.getModel().get("stages"));
        }

        @Test
        @DisplayName("createListOfPokemonForIDList adds a stage with the pokemon")
        void testCreateListOfPokemonForIDList()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());
            controller.getEvolutions("1", new ModelAndView(), "false");
            final int stagesBefore = controller.pokemonFamily.size();

            controller.createListOfPokemonForIDList(List.of(132));

            assertEquals(stagesBefore + 1, controller.pokemonFamily.size());
            assertEquals("ditto", controller.pokemonFamily.getLast().getFirst().name());
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("getEvolutions reports an empty family when the species has no chain")
        void testGetEvolutionsNoChain()
        {
            when(service.getPokemonSpeciesData(anyString())).thenReturn(null);

            final ModelAndView mav = controller.getEvolutions("1", new ModelAndView(), "false");

            assertEquals("evolutions", mav.getViewName());
            assertEquals(0, mav.getModel().get("pokemonFamilySize"));
            assertFalse(mav.getModel().containsKey("stages"));
            verify(service, never()).getPokemonChainData(anyString());
        }

        @Test
        @DisplayName("getEvolutions reports an empty family when the pokemon does not evolve")
        void testGetEvolutionsSingleStage()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.singleChain());

            final ModelAndView mav = controller.getEvolutions("128", new ModelAndView(), "false");

            assertEquals(0, mav.getModel().get("pokemonFamilySize"));
            assertNull(controller.pokemonFamily);
            assertNull(controller.allIDs);
        }

        @Test
        @DisplayName("getEvolutions reports an empty family when the chain data is empty")
        void testGetEvolutionsEmptyChain()
        {
            when(service.getPokemonChainData("1")).thenReturn(Map.of());

            assertEquals(0, controller.getEvolutions("1", new ModelAndView(), "false")
                    .getModel().get("pokemonFamilySize"));
        }

        @Test
        @DisplayName("createListOfPokemonForIDList skips a pokemon without species data")
        void testCreateListOfPokemonSkipsMissingSpecies()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());
            controller.getEvolutions("1", new ModelAndView(), "false");
            when(service.getPokemonSpeciesData(anyString())).thenReturn(null);

            controller.createListOfPokemonForIDList(List.of(132));

            assertTrue(controller.pokemonFamily.getLast().isEmpty());
        }

        @Test
        @DisplayName("createListOfPokemonForIDList skips a pokemon when every species lookup fails")
        void testCreateListOfPokemonSpeciesThrows()
        {
            when(service.getPokemonChainData("1")).thenReturn(ChainFixtures.bulbasaurChain());
            controller.getEvolutions("1", new ModelAndView(), "false");
            when(service.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("species down"));

            controller.createListOfPokemonForIDList(List.of(132));

            assertTrue(controller.pokemonFamily.getLast().isEmpty());
        }
    }
}
