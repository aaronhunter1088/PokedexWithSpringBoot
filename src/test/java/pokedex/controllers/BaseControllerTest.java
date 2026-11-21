package pokedex.controllers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pokedexapi.service.PokemonApiService;
import skaro.pokeapi.resource.FlavorText;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("BaseController")
class BaseControllerTest
{
    private PokemonApiService service;
    private BaseController controller;
    private Pokemon ditto;
    private PokemonSpecies species;

    @BeforeEach
    void setup()
    {
        service = mock(PokemonApiService.class);
        controller = new BaseController(service, TestFixtures.JSON_MAPPER, null, null, null);
        ditto = TestFixtures.ditto();
        species = TestFixtures.species();
    }

    @AfterEach
    void tearDown()
    {
        controller.cleanup();
    }

    private static NamedApiResourceList<Pokemon> pokemonList(int total, String... names)
    {
        final List<NamedApiResource<Pokemon>> results = Stream.of(names)
                .map(name -> new NamedApiResource<Pokemon>(name, "https://pokeapi.co/api/v2/pokemon/" + name + "/"))
                .toList();
        return new NamedApiResourceList<>(total, null, null, results);
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("createPokemon adds the application specific fields")
        void testCreatePokemon()
        {
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of("viridian-forest"));

            final Pokemon result = controller.createPokemon(ditto, species);

            assertEquals("Normal", result.type());
            assertEquals("green", result.color());
            assertEquals(List.of("viridian-forest"), result.locations());
            assertFalse(result.moveNames().isEmpty());
            assertEquals(result.moveNames().stream().sorted().toList(), result.moveNames());
            assertFalse(result.descriptions().isEmpty());
            assertNotNull(result.description());
            assertNotNull(result.defaultImage());
            assertNotNull(result.officialImage());
            assertNotNull(result.gifImage());
            assertNotNull(result.shinyImage());
        }

        @Test
        @DisplayName("createPokemon only keeps english descriptions")
        void testCreatePokemonEnglishOnly()
        {
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            final Pokemon result = controller.createPokemon(ditto, species);

            assertTrue(result.descriptions().stream().allMatch(text -> "en".equals(text.getLanguage().name())));
        }

        @Test
        @DisplayName("retrievePokemon returns the pokemon from the service")
        void testRetrievePokemon()
        {
            when(service.getPokemonByIdOrName("ditto")).thenReturn(ditto);

            assertEquals(ditto, controller.retrievePokemon("ditto"));
        }

        @ParameterizedTest(name = "{0} pokemon over {1} per page is {2} pages")
        @MethodSource("totalPageScenarios")
        @DisplayName("calculateTotalPages rounds up")
        void testCalculateTotalPages(int total, int perPage, int expected)
        {
            assertEquals(expected, BaseController.calculateTotalPages(total, perPage));
        }

        static Stream<Arguments> totalPageScenarios()
        {
            return Stream.of(
                    Arguments.of(1302, 10, 131),
                    Arguments.of(100, 50, 2),
                    Arguments.of(101, 50, 3),
                    Arguments.of(1, 50, 1));
        }

        @Test
        @DisplayName("getUniqueTypes returns the types from the service")
        void testGetUniqueTypes()
        {
            when(service.getAllTypes()).thenReturn(List.of("fire", "water"));

            assertEquals(List.of("fire", "water"), controller.getUniqueTypes());
        }

        @Test
        @DisplayName("getPokemonSprites maps each pokemon name to its sprites")
        void testGetPokemonSprites()
        {
            final Map<String, Map<String, String>> sprites = controller.getPokemonSprites(Map.of(132, ditto));

            assertEquals(1, sprites.size());
            final Map<String, String> dittoSprites = sprites.get("ditto");
            assertNotNull(dittoSprites);
            assertEquals(ditto.sprites().getFrontDefault(), dittoSprites.get("front"));
            assertEquals("true", dittoSprites.get("defaultImagePresent"));
            assertTrue(dittoSprites.containsKey("gifImagePresent"));
        }

        @Test
        @DisplayName("generateDefaultAttributesMap has every key set to null")
        void testGenerateDefaultAttributesMap()
        {
            final Map<String, Object> attributes = controller.generateDefaultAttributesMap();

            assertTrue(attributes.containsKey("minLevel"));
            assertTrue(attributes.containsKey("turnUpsideDown"));
            assertTrue(attributes.values().stream().allMatch(value -> value == null));
        }

        @Test
        @DisplayName("getPokemonPage returns the pokemon and the total")
        void testGetPokemonPage()
        {
            when(service.getAllPokemons(10, 0)).thenReturn(pokemonList(1302, "ditto"));
            when(service.getPokemonByIdOrName("ditto")).thenReturn(ditto);
            when(service.getPokemonSpeciesData("132")).thenReturn(species);
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            final BaseController.PokemonPage page = controller.getPokemonPage(1, 10);

            assertEquals(1302, page.totalPokemon());
            assertEquals(List.of(132), List.copyOf(page.pokemon().keySet()));
            assertEquals("Normal", page.pokemon().get(132).type());
        }

        @Test
        @DisplayName("getPokemonPage falls back to calling the species url")
        void testGetPokemonPageSpeciesFallback() throws Exception
        {
            @SuppressWarnings("unchecked")
            final HttpResponse<String> response = mock(HttpResponse.class);
            when(response.body()).thenReturn(TestFixtures.speciesJson());
            when(service.getAllPokemons(10, 0)).thenReturn(pokemonList(1, "ditto"));
            when(service.getPokemonByIdOrName("ditto")).thenReturn(ditto);
            when(service.getPokemonSpeciesData("132")).thenThrow(new RuntimeException("species down"));
            when(service.callUrl(ditto.species().url())).thenReturn(response);
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            final BaseController.PokemonPage page = controller.getPokemonPage(1, 10);

            assertTrue(page.pokemon().containsKey(132));
        }

        @Test
        @DisplayName("cleanup can be called more than once")
        void testCleanupTwice()
        {
            controller.cleanup();
            controller.cleanup();
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("createPokemon uses a default description when there are no flavor texts")
        void testCreatePokemonNoDescriptions()
        {
            species.setFlavorTextEntries(null);
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            final Pokemon result = controller.createPokemon(ditto, species);

            assertEquals("No description available.", result.description());
        }

        @Test
        @DisplayName("createPokemon uses a default description when no flavor text is english")
        void testCreatePokemonNoEnglishDescriptions()
        {
            final List<FlavorText> english = species.getFlavorTextEntries();
            species.setFlavorTextEntries(english.stream()
                    .filter(text -> !"en".equals(text.getLanguage().name()))
                    .toList());
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            final Pokemon result = controller.createPokemon(ditto, species);

            assertEquals("No description available.", result.description());
        }

        @Test
        @DisplayName("createPokemon defaults the color to white without a species color")
        void testCreatePokemonNoColor()
        {
            species.setColor(null);
            when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of());

            assertEquals("white", controller.createPokemon(ditto, species).color());
        }

        @Test
        @DisplayName("retrievePokemon returns null when the service throws")
        void testRetrievePokemonThrows()
        {
            when(service.getPokemonByIdOrName(anyString())).thenThrow(new RuntimeException("not found"));

            assertNull(controller.retrievePokemon("missingno"));
        }

        @Test
        @DisplayName("getUniqueTypes returns an empty list when the service throws")
        void testGetUniqueTypesThrows()
        {
            when(service.getAllTypes()).thenThrow(new RuntimeException("types down"));

            assertTrue(controller.getUniqueTypes().isEmpty());
        }

        @Test
        @DisplayName("getPokemonSprites returns an empty map for no pokemon")
        void testGetPokemonSpritesEmpty()
        {
            assertTrue(controller.getPokemonSprites(Map.of()).isEmpty());
        }

        @Test
        @DisplayName("calculateTotalPages is at least 1 when there are no pokemon")
        void testCalculateTotalPagesNone()
        {
            assertEquals(1, BaseController.calculateTotalPages(0, 10));
        }

        @Test
        @DisplayName("getPokemonPage is empty when the service returns no list")
        void testGetPokemonPageNullList()
        {
            when(service.getAllPokemons(anyInt(), anyInt())).thenReturn(null);

            final BaseController.PokemonPage page = controller.getPokemonPage(1, 10);

            assertTrue(page.pokemon().isEmpty());
            assertEquals(0, page.totalPokemon());
        }

        @Test
        @DisplayName("getPokemonPage is empty when the list has no results")
        void testGetPokemonPageNoResults()
        {
            when(service.getAllPokemons(anyInt(), anyInt())).thenReturn(pokemonList(50));

            final BaseController.PokemonPage page = controller.getPokemonPage(1, 10);

            assertTrue(page.pokemon().isEmpty());
            assertEquals(50, page.totalPokemon());
            verify(service, never()).getPokemonByIdOrName(anyString());
        }

        @Test
        @DisplayName("getPokemonPage skips a pokemon that cannot be fetched")
        void testGetPokemonPageSkipsNullPokemon()
        {
            when(service.getAllPokemons(10, 0)).thenReturn(pokemonList(1, "ditto"));
            when(service.getPokemonByIdOrName("ditto")).thenReturn(null);

            assertTrue(controller.getPokemonPage(1, 10).pokemon().isEmpty());
        }

        @Test
        @DisplayName("getPokemonPage skips a pokemon when both species lookups fail")
        void testGetPokemonPageSpeciesFallbackFails() throws Exception
        {
            when(service.getAllPokemons(10, 0)).thenReturn(pokemonList(1, "ditto"));
            when(service.getPokemonByIdOrName("ditto")).thenReturn(ditto);
            when(service.getPokemonSpeciesData("132")).thenThrow(new RuntimeException("species down"));
            when(service.callUrl(anyString())).thenThrow(new RuntimeException("url down"));

            assertTrue(controller.getPokemonPage(1, 10).pokemon().isEmpty());
        }
    }
}
