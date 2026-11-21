package pokedex.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.context.annotation.SessionScope;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DarkmodeService")
class DarkmodeServiceTest
{
    private DarkmodeService service;

    @BeforeEach
    void setup()
    {
        service = new DarkmodeService();
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @ParameterizedTest(name = "setDarkmode({0}) is returned by isDarkmode")
        @ValueSource(booleans = {true, false})
        @DisplayName("setDarkmode stores the value")
        void testSetDarkmode(boolean value)
        {
            service.setDarkmode(value);

            assertEquals(value, service.isDarkmode());
        }

        @Test
        @DisplayName("toggleDarkmode switches on and off")
        void testToggleDarkmode()
        {
            service.toggleDarkmode();
            assertTrue(service.isDarkmode());

            service.toggleDarkmode();
            assertFalse(service.isDarkmode());
        }

        @Test
        @DisplayName("service is session scoped")
        void testSessionScoped()
        {
            assertNotNull(DarkmodeService.class.getAnnotation(SessionScope.class));
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("darkmode is off by default")
        void testDefaultIsOff()
        {
            assertFalse(service.isDarkmode());
        }

        @Test
        @DisplayName("instances do not share state")
        void testInstancesAreIndependent()
        {
            service.setDarkmode(true);

            assertFalse(new DarkmodeService().isDarkmode());
        }
    }
}
