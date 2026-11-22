package pokedex.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.context.annotation.SessionScope;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GifService")
class GifServiceTest
{
    private GifService service;

    @BeforeEach
    void setup()
    {
        service = new GifService();
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @ParameterizedTest(name = "setShowGifs({0}) is returned by isShowGifs")
        @ValueSource(booleans = {true, false})
        @DisplayName("setShowGifs stores the value")
        void testSetShowGifs(boolean value)
        {
            service.setShowGifs(value);

            assertEquals(value, service.isShowGifs());
        }

        @Test
        @DisplayName("toggleShowGifs switches on and off")
        void testToggleShowGifs()
        {
            service.toggleShowGifs();
            assertTrue(service.isShowGifs());

            service.toggleShowGifs();
            assertFalse(service.isShowGifs());
        }

        @Test
        @DisplayName("service is session scoped")
        void testSessionScoped()
        {
            assertNotNull(GifService.class.getAnnotation(SessionScope.class));
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("gifs are off by default")
        void testDefaultIsOff()
        {
            assertFalse(service.isShowGifs());
        }

        @Test
        @DisplayName("instances do not share state")
        void testInstancesAreIndependent()
        {
            service.setShowGifs(true);

            assertFalse(new GifService().isShowGifs());
        }
    }
}
