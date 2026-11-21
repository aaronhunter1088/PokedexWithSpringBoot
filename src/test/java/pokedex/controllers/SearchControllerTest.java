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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("SearchController")
class SearchControllerTest
{
    private DarkmodeService darkmodeService;
    private GifService gifService;
    private SearchController controller;

    @BeforeEach
    void setup()
    {
        darkmodeService = new DarkmodeService();
        gifService = new GifService();
        controller = new SearchController(mock(PokemonApiService.class), TestFixtures.JSON_MAPPER,
                new MockEnvironment(), darkmodeService, gifService);
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("searchPage renders the search view")
        void testSearchPage()
        {
            assertEquals("search", controller.searchPage(new ModelAndView()).getViewName());
        }

        @Test
        @DisplayName("searchPage reflects the darkmode and gif preferences")
        void testSearchPagePreferences()
        {
            darkmodeService.setDarkmode(true);
            gifService.setShowGifs(true);

            final ModelAndView mav = controller.searchPage(new ModelAndView());

            assertEquals(true, mav.getModel().get("isDarkMode"));
            assertEquals(true, mav.getModel().get("showGifs"));
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("searchPage defaults both preferences to off")
        void testSearchPageDefaults()
        {
            final ModelAndView mav = controller.searchPage(new ModelAndView());

            assertEquals(false, mav.getModel().get("isDarkMode"));
            assertEquals(false, mav.getModel().get("showGifs"));
        }

        @Test
        @DisplayName("searchPage does not add a pokemon")
        void testSearchPageNoPokemon()
        {
            final ModelAndView mav = controller.searchPage(new ModelAndView());

            assertFalse(mav.getModel().containsKey("pokemon"));
            assertNull(mav.getModel().get("pokemonId"));
        }
    }
}
