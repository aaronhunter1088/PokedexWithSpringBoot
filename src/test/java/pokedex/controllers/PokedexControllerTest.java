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

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("PokedexController")
class PokedexControllerTest
{
    private PokemonApiService service;
    private DarkmodeService darkmodeService;
    private GifService gifService;
    private PokedexController controller;
    private Pokemon ditto;

    @BeforeEach
    void setup()
    {
        service = mock(PokemonApiService.class);
        darkmodeService = new DarkmodeService();
        gifService = new GifService();
        controller = new PokedexController(service, TestFixtures.JSON_MAPPER, new MockEnvironment(), darkmodeService, gifService);
        ditto = TestFixtures.ditto();

        when(service.getPokemonByIdOrName(anyString())).thenReturn(ditto);
        when(service.getPokemonSpeciesData(anyString())).thenReturn(TestFixtures.species());
        when(service.getPokemonLocationAreaEncounters(anyString())).thenReturn(List.of("viridian-forest"));
        when(service.getAllTypes()).thenReturn(List.of("fire", "water"));
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("pokedex renders the pokedex view with the pokemon")
        void testPokedex()
        {
            final ModelAndView mav = controller.pokedex("ditto", new ModelAndView());

            assertEquals("pokedex", mav.getViewName());
            assertEquals("132", mav.getModel().get("pokemonId"));
            final Pokemon pokemon = (Pokemon) mav.getModel().get("pokemon");
            assertEquals("ditto", pokemon.name());
            assertEquals("Normal", pokemon.type());
            assertEquals(List.of("viridian-forest"), pokemon.locations());
            assertEquals(ditto.sprites().getFrontDefault(), mav.getModel().get("defaultImage"));
            assertEquals(List.of("fire", "water"), mav.getModel().get("uniqueTypes"));
            final int random = (int) mav.getModel().get("randomDescriptionNumber");
            assertTrue(random >= 0 && random < pokemon.descriptions().size());
        }

        @Test
        @DisplayName("pokedex reflects the darkmode and gif preferences")
        void testPokedexPreferences()
        {
            darkmodeService.setDarkmode(true);
            gifService.setShowGifs(true);

            final ModelAndView mav = controller.pokedex("ditto", new ModelAndView());

            assertEquals(true, mav.getModel().get("isDarkMode"));
            assertEquals(true, mav.getModel().get("showGifs"));
        }

        @Test
        @DisplayName("pokedex looks up deoxys as deoxys-normal")
        void testPokedexDeoxys()
        {
            controller.pokedex("Deoxys", new ModelAndView());

            verify(service).getPokemonByIdOrName("deoxys-normal");
        }

        @Test
        @DisplayName("setupPokedex adds the application specific fields")
        void testSetupPokedex()
        {
            final Pokemon pokemon = controller.setupPokedex(ditto);

            assertEquals("green", pokemon.color());
        }

        @Test
        @DisplayName("setupPokedex falls back to the species url when the lookup fails")
        void testSetupPokedexSpeciesFallback() throws Exception
        {
            @SuppressWarnings("unchecked")
            final HttpResponse<String> response = mock(HttpResponse.class);
            when(response.statusCode()).thenReturn(200);
            when(response.body()).thenReturn(TestFixtures.speciesJson());
            when(service.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("species down"));
            when(service.callUrl(ditto.species().url())).thenReturn(response);

            assertEquals("green", controller.setupPokedex(ditto).color());
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("pokedex throws when the pokemon cannot be found")
        void testPokedexMissingPokemon()
        {
            when(service.getPokemonByIdOrName(anyString())).thenReturn(null);

            assertThrows(NullPointerException.class, () -> controller.pokedex("missingno", new ModelAndView()));
        }

        @Test
        @DisplayName("setupPokedex returns the original pokemon when the species url returns an error")
        void testSetupPokedexFallbackNon200() throws Exception
        {
            @SuppressWarnings("unchecked")
            final HttpResponse<String> response = mock(HttpResponse.class);
            when(response.statusCode()).thenReturn(404);
            when(service.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("species down"));
            when(service.callUrl(anyString())).thenReturn(response);

            assertSame(ditto, controller.setupPokedex(ditto));
        }

        @Test
        @DisplayName("setupPokedex returns the original pokemon when every species lookup fails")
        void testSetupPokedexAllLookupsFail() throws Exception
        {
            when(service.getPokemonSpeciesData(anyString())).thenThrow(new RuntimeException("species down"));
            when(service.callUrl(anyString())).thenThrow(new RuntimeException("url down"));

            final Pokemon pokemon = controller.setupPokedex(ditto);

            assertSame(ditto, pokemon);
            assertNull(pokemon.type());
        }
    }
}
