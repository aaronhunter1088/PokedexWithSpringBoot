package pokedex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import pokedexapi.config.MyPokeApiReactorCachingConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PokedexApplication")
class PokedexApplicationTest
{
    private PokedexApplication application;

    @BeforeEach
    void setup()
    {
        application = new PokedexApplication();
    }

    @Nested
    @DisplayName("Positive")
    class Positive
    {
        @Test
        @DisplayName("scans the pokedex and pokedexapi packages")
        void testScanBasePackages()
        {
            final SpringBootApplication annotation = PokedexApplication.class.getAnnotation(SpringBootApplication.class);

            assertNotNull(annotation);
            assertEquals(Set.of("pokedex", "pokedexapi"), Set.copyOf(Arrays.asList(annotation.scanBasePackages())));
        }

        @Test
        @DisplayName("imports the caching configuration")
        void testImportsCachingConfiguration()
        {
            final Import annotation = PokedexApplication.class.getAnnotation(Import.class);

            assertNotNull(annotation);
            assertTrue(Arrays.asList(annotation.value()).contains(MyPokeApiReactorCachingConfiguration.class));
        }

        @Test
        @DisplayName("can be deployed as a war")
        void testIsServletInitializer()
        {
            assertInstanceOf(SpringBootServletInitializer.class, application);
        }

        @Test
        @DisplayName("configure registers the application as a source")
        void testConfigure()
        {
            final SpringApplicationBuilder builder = application.configure(new SpringApplicationBuilder());

            assertTrue(builder.build().getAllSources().contains(PokedexApplication.class));
        }

        @Test
        @DisplayName("the document root points at src/main/webapp when it exists")
        void testDocumentRootCustomizer()
        {
            final WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer =
                    application.localJspDocumentRootCustomizer();
            final TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

            customizer.customize(factory);

            final ArgumentCaptor<File> root = ArgumentCaptor.forClass(File.class);
            verify(factory).setDocumentRoot(root.capture());
            assertEquals(new File("src/main/webapp").getAbsoluteFile().toPath().normalize(),
                    root.getValue().getAbsoluteFile().toPath().normalize());
        }
    }

    @Nested
    @DisplayName("Negative")
    class Negative
    {
        @Test
        @DisplayName("the document root is not set until the customizer is applied")
        void testDocumentRootNotSetUntilApplied()
        {
            final TomcatServletWebServerFactory factory = mock(TomcatServletWebServerFactory.class);

            application.localJspDocumentRootCustomizer();

            verify(factory, never()).setDocumentRoot(any());
        }
    }
}
