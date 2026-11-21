package pokedex.controllers;

import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import pokedex.services.DarkmodeService;
import pokedex.services.GifService;
import pokedexapi.service.PokemonApiService;
import skaro.pokeapi.resource.FlavorText;
import skaro.pokeapi.resource.NamedApiResource;
import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.pokemon.*;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static pokedexapi.utilities.Constants.*;

@Controller
public class BaseController
{
    /* Logging instance */
    private static final Logger LOGGER = LogManager.getLogger(BaseController.class);
    private static final int FILTER_FETCH_PAGE_SIZE = 50; // Pokemon fetched per request while filtering by type
    
    protected final PokemonApiService pokemonApiService;
    protected final ObjectMapper objectMapper;
    protected final Environment environment;
    protected final DarkmodeService darkmodeService;
    protected final GifService gifService;
    Map<String, List<Pokemon>> filteredPokemonByType = new ConcurrentHashMap<>();
    Map<String, Boolean> filteringInProgress = new ConcurrentHashMap<>();
    private final ExecutorService filterExecutor = Executors.newFixedThreadPool(
        Math.min(Runtime.getRuntime().availableProcessors() * 2, 20));
    protected final AtomicBoolean retroactiveFetchingStarted = new AtomicBoolean(false);

    /**
     * One page of Pokemon along with the total number of Pokemon available.
     * @param pokemon the Pokemon on the requested page, keyed by id
     * @param totalPokemon the total number of Pokemon across all pages
     */
    protected record PokemonPage(Map<Integer, Pokemon> pokemon, int totalPokemon) {}

    public BaseController(PokemonApiService pokemonApiService,
                          ObjectMapper objectMapper,
                          Environment environment,
                          DarkmodeService darkmodeService,
                          GifService gifService)
    {
        this.pokemonApiService = pokemonApiService;
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.darkmodeService = darkmodeService;
        this.gifService = gifService;
    }

    @PreDestroy
    public void cleanup()
    {
        LOGGER.info("Shutting down filter executor services");
        filterExecutor.shutdown();
        try {
            if (!filterExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                filterExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            filterExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the evolution chain id for a pokemon from its species data.
     * @param pokemonId the pokemon id or name
     * @return the chain id, or 0 if it could not be determined
     */
    protected Integer getEvolutionChainID(String pokemonId)
    {
        LOGGER.info("id: {}", pokemonId);
        try {
            final PokemonSpecies species = pokemonApiService.getPokemonSpeciesData(pokemonId);
            if (species != null && species.getEvolutionChain() != null) {
                return idFromUrl(species.getEvolutionChain().url());
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not get the evolution chain id for {}", pokemonId, e);
        }
        return 0;
    }

    /**
     * Flattens an evolution chain into stages of species ids, e.g. [[1], [2], [3]].
     * @param chainData the body returned by the EvolutionApi
     * @return one list of ids per stage, empty if the chain is missing
     */
    @SuppressWarnings("unchecked")
    protected List<List<Integer>> getFamilyIDs(Map<String, Object> chainData)
    {
        final List<List<Integer>> family = new ArrayList<>();
        if (chainData == null || !(chainData.get("chain") instanceof Map)) return family;
        List<Map<String, Object>> stage = List.of((Map<String, Object>) chainData.get("chain"));
        while (!stage.isEmpty()) {
            family.add(stage.stream()
                    .map(link -> idFromUrl(((Map<String, String>) link.get("species")).get("url")))
                    .toList());
            stage = stage.stream()
                    .flatMap(link -> ((List<Map<String, Object>>) link.get("evolves_to")).stream())
                    .toList();
        }
        LOGGER.info("family: {}", family);
        return family;
    }

    private static Integer idFromUrl(String url)
    {
        final String trimmed = url.replaceAll("/+$", "");
        return Integer.valueOf(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    /**
     * Gets the species color name, defaulting to white.
     * @param speciesData from pokeapi-reactor, may be null
     * @return the color name
     */
    private static String getColorName(final PokemonSpecies speciesData)
    {
        return speciesData != null && speciesData.getColor() != null ? speciesData.getColor().name() : "white";
    }

    /**
     * Returns a Pokemon with application specific properties
     * @param pokemon from pokeapi-reactor
     * @param speciesData from pokeapi-reactor
     * @return Pokemon object
     */
    protected Pokemon createPokemon(@NonNull Pokemon pokemon, @NonNull PokemonSpecies speciesData)
    {
        String defaultImage = pokemon.defaultImage() != null
                ? pokemon.defaultImage()
                : DEFAULT_IMAGE_URL(pokemon.id()) != null
                    ? DEFAULT_IMAGE_URL(pokemon.id())
                    : "/images/pokeball_search.png";
        String officialImage = pokemon.officialImage() != null
                ? pokemon.officialImage()
                : OFFICIAL_IMAGE_URL(pokemon.getId()) != null
                    ? OFFICIAL_IMAGE_URL(pokemon.getId())
                    : "/images/pokeball_search.png";
        String gifImage = pokemon.gifImage() != null
                ? pokemon.gifImage()
                : GIF_IMAGE_URL(pokemon.id()) != null
                    ? GIF_IMAGE_URL(pokemon.id())
                    : "/images/pokeball_search.png";
        String shinyImage = pokemon.shinyImage() != null
                ? pokemon.shinyImage()
                : SHINY_IMAGE_URL(pokemon.id()) != null
                    ? SHINY_IMAGE_URL(pokemon.id())
                    : "/images/pokeball_search.png";
        List<FlavorText> flavorTexts = speciesData.getFlavorTextEntries();
        List<FlavorText> pokemonDescriptions = flavorTexts != null ?
                flavorTexts.stream().filter(entry -> entry.getLanguage().name().equals("en"))
                        .toList() : new ArrayList<>();
        String description = !pokemonDescriptions.isEmpty() ?
                pokemonDescriptions.get(new Random().nextInt(pokemonDescriptions.size())).getFlavorText().replace("\n", "")
                : "No description available.";

        List<PokemonType> types = pokemon.types();
        String typeString = "";
        if (types.size() > 1) {
            LOGGER.debug("More than 1 pokemonType");
            typeString = types.get(0).getType().name().substring(0, 1).toUpperCase() + types.get(0).getType().name().substring(1)
                    + " & " + types.get(1).getType().name().substring(0, 1).toUpperCase() + types.get(1).getType().name().substring(1);
        } else {
            LOGGER.debug("One pokemonType");
            typeString = types.get(0).getType().name().substring(0, 1).toUpperCase() + types.get(0).getType().name().substring(1);
        }
        String pokemonLocation = pokemon.locationAreaEncounters();
        List<String> locationEncounters = pokemonApiService.getPokemonLocationAreaEncounters(pokemonLocation);

        List<String> moves = pokemon.moves().stream()
                .map(PokemonMove::getMove)
                .map(NamedApiResource::name)
                .sorted()
                .toList();
        // Catch a blank color on speciesData by calling endpoint directly
        pokemon = Pokemon.from(pokemon, Map.of(
                "defaultImage", defaultImage,
                "officialImage", officialImage,
                "gifImage", gifImage,
                "shinyImage", shinyImage,
                "color", getColorName(speciesData),
                "descriptions", pokemonDescriptions,
                "description", description,
                "type", typeString,
                "locations", locationEncounters,
                "moveNames", moves
        ));
        return pokemon;
    }

    /**
     * Fetch the pokemon resource
     * @param nameOrId String the name or id of a Pokemon
     * @return the Pokemon or null
     */
    public Pokemon retrievePokemon(String nameOrId)
    {
        LOGGER.info("retrievePokemon");
        try {
            return pokemonApiService.getPokemonByIdOrName(nameOrId);
        }
        catch (Exception e) {
            LOGGER.error("Could not find pokemon with value: {}", nameOrId);
            return null;
        }
    }

    protected Map<String, Object> generateDefaultAttributesMap()
    {
        return new TreeMap<>() {{
            put("name", null); // on screen
            put("gender", null);
            put("id", null);
            put("isBaby", null);
            put("heldItem", null); // on screen
            put("useItem", null); // on screen
            put("knownMove", null); // on screen
            put("knownMoveType", null); // on screen
            put("location", null); // on screen
            put("minAffection", null); // on screen
            put("minBeauty", null); // on screen
            put("minHappiness", null); // on screen
            put("minLevel", null); // on screen
            put("needsRain", null); // on screen
            put("timeOfDay", null); // on screen
            put("partySpecies", null);
            put("partyType", null); // not implemented
            put("trigger", null); // not implemented
            put("relativePhysicalStats", null);
            put("tradeSpecies", null);
            put("turnUpsideDown", null); // on screen
        }};
    }

    /**
     * Gets one page of Pokemon.
     * @param requestedPage the 1-based page number
     * @param pkmnPerPage the number of Pokemon per page
     * @return the Pokemon on the page and the total number of Pokemon
     */
    protected PokemonPage getPokemonPage(final int requestedPage, final int pkmnPerPage)
    {
        final int offset = Math.max(0, (requestedPage - 1));
        int expectedSize = 0;
        int totalPokemon = 0;
        Map<Integer, Pokemon> fetchedPokemonMap = new TreeMap<>();

        LOGGER.info("page number: {}", requestedPage);
        LOGGER.info("pkmnPerPage: {}", pkmnPerPage);

        LOGGER.debug("Injected service class: {}", pokemonApiService.getClass().getName());
        final NamedApiResourceList<Pokemon> pokemonList = pokemonApiService.getAllPokemons(pkmnPerPage, offset);
        if (pokemonList != null) totalPokemon = pokemonList.count();
        if (pokemonList == null || pokemonList.results() == null || pokemonList.results().isEmpty()) {
            LOGGER.warn("No pokemon resources returned for page {}", requestedPage);
        } else {
            LOGGER.debug("pokemonList size: {}", pokemonList.results().size());
            List<NamedApiResource<Pokemon>> listOfPokemon = pokemonList.results();
            LOGGER.debug("pokemonList limit size: {}", listOfPokemon.size());

            expectedSize = listOfPokemon.size();
            listOfPokemon.forEach(pkmn -> {
                Pokemon pokemon = pokemonApiService.getPokemonByIdOrName(pkmn.name());
                if (pokemon == null) {
                    LOGGER.warn("Skipping {} — getPokemonByIdOrName returned null (API may not be ready yet)", pkmn.name());
                    return;
                }
                PokemonSpecies speciesData = null;
                try {
                    speciesData = pokemonApiService.getPokemonSpeciesData(pokemon.id().toString());
                    if (speciesData != null) {
                        LOGGER.debug("speciesData.color: {}", speciesData.getColor().name());
                        pokemon = createPokemon(pokemon, speciesData);
                        fetchedPokemonMap.put(pokemon.id(), pokemon);
                    }
                }
                catch (Exception e) {
                    LOGGER.warn("No speciesData found using {} and service species call", pkmn);
                    if (pokemon.species() == null || pokemon.species().url() == null) {
                        LOGGER.error("Cannot fall back for {} — species URL is null", pokemon.id());
                        return;
                    }
                    LOGGER.info("Trying direct call with species: {}, url: {}", pokemon.species().name(), pokemon.species().url());
                    try {
                        String responseBody = pokemonApiService.callUrl(pokemon.species().url()).body();
                        speciesData = objectMapper.readValue(responseBody, PokemonSpecies.class);
                        LOGGER.info("Successfully retrieved speciesData for pokemon id: {}", pokemon.id());
                        pokemon = createPokemon(pokemon, speciesData);
                        fetchedPokemonMap.put(pokemon.id(), pokemon);
                    }
                    catch (Exception ex) {
                        LOGGER.error("No speciesData found using {} and direct call. Empty speciesData created.", pokemon.id());
                    }
                }
            });

            if (fetchedPokemonMap.size() >= expectedSize) {
                return new PokemonPage(fetchedPokemonMap, totalPokemon);
            }
            LOGGER.warn("Fetched {}/{} pokemon for page {}", fetchedPokemonMap.size(), expectedSize, requestedPage);
        }

        LOGGER.warn("Returning partial pokemonMap for page {} ({}/{})",
                requestedPage, fetchedPokemonMap.size(), expectedSize);
        return new PokemonPage(fetchedPokemonMap, totalPokemon);
    }

    /**
     * Calculates how many pages are needed to show every Pokemon.
     * @param totalPokemon the total number of Pokemon
     * @param pkmnPerPage the number of Pokemon per page
     * @return the number of pages, at least 1
     */
    protected static int calculateTotalPages(final int totalPokemon, final int pkmnPerPage)
    {
        return Math.max(1, (int) Math.ceil((double) totalPokemon / pkmnPerPage));
    }

    protected List<String> getUniqueTypes()
    {
        try {
            return pokemonApiService.getAllTypes();
        }
        catch (Exception e) {
            LOGGER.error("Error retrieving unique types: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the sprites for the given Pokemon
     * @param pokemonMap the Pokemon to get sprites for
     * @return Map of Pokemon Name with their sprite values
     */
    public Map<String, Map<String, String>> getPokemonSprites(final Map<Integer, Pokemon> pokemonMap) {
        //console.log(pokemon);
        Map<String, Map<String, String>> allSprites = new TreeMap<>();
        for(Pokemon pokemon : pokemonMap.values())
        {
            PokemonSprites sprites = pokemon.sprites();
            OtherSprites otherSprites = sprites.getOther();
            //console.log("getPokemonSprites");
            //console.log(sprites['front_default']);
            String frontImg = sprites.getFrontDefault();
            //this.defaultImagePresent = frontImg != null;
            String shinyImg = sprites.getFrontShiny();
            String officialImg = otherSprites.getOfficialArtwork().getFrontDefault();
            String gifImg = sprites.getVersions().getGenerationV().getBlackWhite().getAnimated().getFrontDefault();
            //this.showGifs = gifImg != null;
            TreeMap<String, String> spriteMap = new TreeMap<>();
            spriteMap.put("front", frontImg);
            spriteMap.put("shiny", shinyImg);
            spriteMap.put("official", officialImg);
            spriteMap.put("gif", gifImg);
            spriteMap.put("defaultImagePresent", Boolean.toString(frontImg != null));
            spriteMap.put("gifImagePresent", Boolean.toString(gifImg != null));
            allSprites.put(pokemon.name(), spriteMap);
        }
        return allSprites;
    }
}