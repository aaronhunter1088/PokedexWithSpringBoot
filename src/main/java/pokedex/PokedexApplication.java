package pokedex;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import pokedexapi.config.MyPokeApiReactorCachingConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

@Import(MyPokeApiReactorCachingConfiguration.class)
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"pokedex", "pokedexapi"})
public class PokedexApplication extends SpringBootServletInitializer
{

    public static void main(String[] args)
    {
        SpringApplication.run(PokedexApplication.class, args);
    }

    @NonNull
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    {
        return builder.sources(PokedexApplication.class);
    }

    @Bean
    @Profile("local")
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> localJspDocumentRootCustomizer()
    {
        return factory -> {
            Path webappPath = resolveWebappPath();
            if (webappPath != null && webappPath.toFile().isDirectory() && webappPath.toFile().canRead()) {
                factory.setDocumentRoot(webappPath.toFile());
            }
        };
    }

    private Path resolveWebappPath()
    {
        URL classesLocation = PokedexApplication.class.getProtectionDomain().getCodeSource().getLocation();

        try {
            Path classesPath = Path.of(new URI(classesLocation.toString()));
            Path classesParent = classesPath.getParent();
            Path projectRoot = classesParent != null ? classesParent.getParent() : null;
            return projectRoot != null ? projectRoot.resolve("src/main/webapp").normalize() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

}