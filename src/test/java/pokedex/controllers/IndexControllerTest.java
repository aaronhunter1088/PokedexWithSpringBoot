package pokedex.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import pokedex.services.DarkmodeService;
import pokedex.services.GifService;
import pokedexapi.service.PokemonApiService;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.pokemon.Pokemon;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("IndexController")
class IndexControllerTest
{
    private PokemonApiService service;
    private DarkmodeService darkmodeService;
    private GifService gifService;
    private MockEnvironment environment;
    private MockHttpSession session;
    private IndexController controller;

    @BeforeEach
    void setup()
    {
        service = mock(PokemonApiService.class);
        darkmodeService = new DarkmodeService();
        gifService = new GifService();
        environment = new MockEnvironment();
        session = new MockHttpSession();
        controller = new IndexController(service, TestFixtures.JSON_MAPPER, environment, darkmodeService, gifService);

        final Pokemon ditto = TestFixtures.ditto();
        final NamedApiResource<Pokemon> resource = new NamedApiResource<>("ditto", "https://pokeapi.co/api/v2/pokemon/132/");
        when(service.getAllPokemons(anyInt(), anyInt()))
                .thenReturn(new NamedApiResourceList<>(95, null, null, List.of(resource)));
        when(service.getPokemonByIdOrName("ditto")).thenReturn(ditto);
        when(service.getPokemonSpeciesData("132")).thenReturn(TestFixtures.species());
        when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());
        when(service.getAllTypes()).thenReturn(List.of("fire", "water"));
    }

    private ModelAndView homepage(String darkmode, String tileColor, Integer pageNumber)
    {
        return controller.homepage(new ModelAndView(), session, darkmode, tileColor, pageNumber, null);
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("homepage renders the index view with the pokemon")
        void testHomepage()
        {
            final ModelAndView mav = homepage(null, null, null);

            assertEquals("index", mav.getViewName());
            assertEquals(1, ((java.util.Map<?, ?>) mav.getModel().get("pokemonMap")).size());
            assertEquals(false, mav.getModel().get("loadFailed"));
            assertEquals(95, mav.getModel().get("totalPokemon"));
            assertEquals(10, mav.getModel().get("totalPages"));
            assertEquals(10, mav.getModel().get("pkmnPerPage"));
            assertEquals(1, mav.getModel().get("page"));
            assertEquals(List.of("fire", "water"), mav.getModel().get("uniqueTypes"));
            assertEquals("#4CAF50", mav.getModel().get("tileColorParam"));
        }

        @Test
        @DisplayName("homepage reports the production env when the profile is active")
        void testHomepageProductionEnv()
        {
            environment.setActiveProfiles("production");

            assertEquals("production", homepage(null, null, null).getModel().get("env"));
        }

        @Test
        @DisplayName("homepage reports the dev env otherwise")
        void testHomepageDevEnv()
        {
            environment.setActiveProfiles("local");

            assertEquals("dev", homepage(null, null, null).getModel().get("env"));
        }

        @ParameterizedTest(name = "darkmode={0}")
        @ValueSource(strings = {"true", "false"})
        @DisplayName("homepage stores the requested darkmode")
        void testHomepageDarkmode(String darkmode)
        {
            final ModelAndView mav = homepage(darkmode, null, null);

            final boolean expected = Boolean.parseBoolean(darkmode);
            assertEquals(expected, darkmodeService.isDarkmode());
            assertEquals(expected, session.getAttribute("isDarkMode"));
            assertEquals(expected, mav.getModel().get("isDarkMode"));
        }

        @Test
        @DisplayName("homepage keeps a valid tile color in the session")
        void testHomepageTileColor()
        {
            homepage(null, "#AABBCC", null);

            assertEquals("#AABBCC", homepage(null, null, null).getModel().get("tileColorParam"));
        }

        @Test
        @DisplayName("homepage remembers the page in the session")
        void testHomepagePageRemembered()
        {
            homepage(null, null, 3);

            assertEquals(3, session.getAttribute("page"));
            assertEquals(3, homepage(null, null, null).getModel().get("page"));
        }

        @Test
        @DisplayName("homepage reflects the gif preference")
        void testHomepageShowGifs()
        {
            gifService.setShowGifs(true);

            assertEquals(true, homepage(null, null, null).getModel().get("showGifs"));
        }

        @Test
        @DisplayName("page renders the requested page")
        void testPage()
        {
            final ModelAndView mav = controller.page(2, new ModelAndView(), session);

            assertEquals("index", mav.getViewName());
            assertEquals(2, mav.getModel().get("page"));
        }

        @Test
        @DisplayName("toggleGifs flips the gif preference")
        void testToggleGifs()
        {
            assertTrue(controller.toggleGifs());
            assertFalse(controller.toggleGifs());
        }

        @Test
        @DisplayName("toggleDarkmode flips the preference when none is requested")
        void testToggleDarkmode()
        {
            assertTrue(controller.toggleDarkmode(null, session));
            assertEquals(true, session.getAttribute("isDarkMode"));
            assertFalse(controller.toggleDarkmode(null, session));
        }

        @ParameterizedTest(name = "requested darkmode={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("toggleDarkmode sets the requested preference")
        void testToggleDarkmodeRequested(boolean requested)
        {
            assertEquals(requested, controller.toggleDarkmode(requested, session));
            assertEquals(requested, session.getAttribute("isDarkMode"));
        }

        @ParameterizedTest(name = "{0} per page from page {1} lands on page {2}")
        @MethodSource("perPageScenarios")
        @DisplayName("getPokemonPerPage keeps the first visible pokemon on screen")
        void testGetPokemonPerPage(int perPage, int currentPage, int expectedPage)
        {
            final ResponseEntity<String> response = controller.getPokemonPerPage(perPage, currentPage, session);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(String.valueOf(expectedPage), response.getBody());
            assertEquals(expectedPage, session.getAttribute("page"));
        }

        static Stream<Arguments> perPageScenarios()
        {
            return Stream.of(
                    Arguments.of(20, 1, 1),
                    Arguments.of(20, 5, 3),
                    Arguments.of(5, 3, 5),
                    Arguments.of(500, 1, 1));
        }

        @Test
        @DisplayName("getPokemonPerPage caps the value at 50")
        void testGetPokemonPerPageCapped()
        {
            controller.getPokemonPerPage(500, 1, session);

            assertEquals(50, session.getAttribute("pkmnPerPage"));
        }

        @Test
        @DisplayName("getPokemonByType acknowledges the chosen type")
        void testGetPokemonByType()
        {
            final ResponseEntity<String> response = controller.getPokemonByType("fire");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("chosenType set", response.getBody());
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @ParameterizedTest(name = "tileColor={0}")
        @ValueSource(strings = {"red", "#12345", "#GGGGGG", "#1234567", "<script>"})
        @DisplayName("homepage falls back to the default tile color for an invalid color")
        void testHomepageInvalidTileColor(String tileColor)
        {
            homepage(null, tileColor, null);

            assertEquals("#4CAF50", homepage(null, null, null).getModel().get("tileColorParam"));
        }

        @Test
        @DisplayName("homepage ignores a blank darkmode value")
        void testHomepageBlankDarkmode()
        {
            homepage("  ", null, null);

            assertFalse(darkmodeService.isDarkmode());
            assertNull(session.getAttribute("isDarkMode"));
        }

        @Test
        @DisplayName("homepage clamps a page beyond the last page")
        void testHomepagePageBeyondLast()
        {
            final ModelAndView mav = homepage(null, null, 9999);

            assertEquals(10, mav.getModel().get("page"));
        }

        @Test
        @DisplayName("homepage clamps a page below 1")
        void testHomepagePageBelowOne()
        {
            assertEquals(1, homepage(null, null, -4).getModel().get("page"));
        }

        @Test
        @DisplayName("homepage flags a load failure when no pokemon come back")
        void testHomepageLoadFailed()
        {
            when(service.getAllPokemons(anyInt(), anyInt())).thenReturn(null);

            final ModelAndView mav = homepage(null, null, null);

            assertEquals(true, mav.getModel().get("loadFailed"));
            assertEquals(1, mav.getModel().get("totalPages"));
        }

        @Test
        @DisplayName("homepage has no types when the service fails")
        void testHomepageTypesFail()
        {
            when(service.getAllTypes()).thenThrow(new RuntimeException("types down"));

            assertEquals(List.of(), homepage(null, null, null).getModel().get("uniqueTypes"));
        }

        @ParameterizedTest(name = "pkmnPerPage={0}")
        @ValueSource(ints = {0, -1, -50})
        @DisplayName("getPokemonPerPage rejects a non positive value")
        void testGetPokemonPerPageInvalid(int perPage)
        {
            final ResponseEntity<String> response = controller.getPokemonPerPage(perPage, 1, session);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid number of Pokemon per page", response.getBody());
            assertNull(session.getAttribute("pkmnPerPage"));
        }

        @Test
        @DisplayName("getPokemonPerPage treats a current page below 1 as page 1")
        void testGetPokemonPerPageLowCurrentPage()
        {
            assertEquals("1", controller.getPokemonPerPage(20, -3, session).getBody());
        }
    }
}
