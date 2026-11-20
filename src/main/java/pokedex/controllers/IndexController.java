package pokedex.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import pokedex.services.DarkmodeService;
import pokedex.services.GifService;
import skaro.pokeapi.resource.pokemon.Pokemon;
import pokedexapi.service.PokemonApiService;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Hidden
@Controller
public class IndexController extends BaseController
{
    /* Logging instance */
    private static final Logger LOGGER = LogManager.getLogger(IndexController.class);
    private static final String TILE_COLOR_SESSION_KEY = "tileColorParam";
    private static final String DEFAULT_TILE_COLOR = "#4CAF50";
    private static final String PAGE_SESSION_KEY = "page";
    private static final String PKMN_PER_PAGE_SESSION_KEY = "pkmnPerPage";
    private static final int DEFAULT_PKMN_PER_PAGE = 10;
    private static final int MAX_PKMN_PER_PAGE = 50;

    @Autowired
    public IndexController(PokemonApiService pokemonService,
                           ObjectMapper objectMapper,
                           Environment environment,
                           DarkmodeService darkmodeService,
                           GifService gifService)
    {
        super(pokemonService, objectMapper, environment, darkmodeService, gifService);
    }

    @GetMapping("/")
    public ModelAndView homepage(ModelAndView mav, HttpSession httpSession,
           @RequestParam(name = "darkmode", required = false) String darkmode,
           @RequestParam(name = "tileColor", required = false) String tileColor,
           @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
           @RequestParam(name = "chosenType", required = false) String chosenType)
    {
        long startTime = System.nanoTime();
        if (StringUtils.hasText(darkmode)) {
            httpSession.setAttribute("isDarkMode", Boolean.parseBoolean(darkmode));
            darkmodeService.setDarkmode(Boolean.parseBoolean(darkmode));
        }
        if (StringUtils.hasText(tileColor)) {
            httpSession.setAttribute(TILE_COLOR_SESSION_KEY, sanitizeTileColor(tileColor));
        }
        final int pageToView = pageNumber != null ? pageNumber : getSessionPage(httpSession);
        ModelAndView homepage = renderHomepage(mav, httpSession, pageToView, chosenType);

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000_000.0;
        LOGGER.debug("Homepage accessed with duration: {} secs", duration);

        return homepage;
    }

    private int getSessionPkmnPerPage(HttpSession httpSession)
    {
        return httpSession.getAttribute(PKMN_PER_PAGE_SESSION_KEY) instanceof Integer perPage
                ? perPage : DEFAULT_PKMN_PER_PAGE;
    }

    private int getSessionPage(HttpSession httpSession)
    {
        return httpSession.getAttribute(PAGE_SESSION_KEY) instanceof Integer page ? page : 1;
    }

    private ModelAndView renderHomepage(ModelAndView mav, HttpSession httpSession, int requestedPage, String chosenType)
    {
        final boolean isDarkMode = darkmodeService.isDarkmode();
        final boolean showGifs = gifService.isShowGifs();
        final int pkmnPerPage = getSessionPkmnPerPage(httpSession);

        int page = Math.max(1, requestedPage);
        PokemonPage pokemonPage = getPokemonPage(page, pkmnPerPage);
        if (pokemonPage.pokemon().isEmpty()) {
            LOGGER.warn("Initial homepage fetch returned empty pokemonMap for page {}, retrying once", page);
            pokemonPage = getPokemonPage(page, pkmnPerPage);
        }
        final int totalPages = calculateTotalPages(pokemonPage.totalPokemon(), pkmnPerPage);
        if (page > totalPages) {
            page = totalPages;
            pokemonPage = getPokemonPage(page, pkmnPerPage);
        }
        httpSession.setAttribute(PAGE_SESSION_KEY, page);
        final Map<Integer, Pokemon> pokemonMap = pokemonPage.pokemon();

        mav.addObject("pokemonMap", pokemonMap);
        mav.addObject("pokemonSprites", getPokemonSprites(pokemonMap));
        mav.addObject("pokemonIds", new ArrayList<>(pokemonMap.keySet()));
        mav.addObject("showGifs", showGifs);
        mav.addObject("pkmnPerPage", pkmnPerPage);
        mav.addObject("totalPokemon", pokemonPage.totalPokemon());
        mav.addObject("totalPages", totalPages);
        mav.addObject("page", page);
        mav.addObject("uniqueTypes", getUniqueTypes());
        mav.addObject("chosenType", chosenType);
        mav.addObject("isDarkMode", isDarkMode);
        Object tileColorFromSession = httpSession.getAttribute(TILE_COLOR_SESSION_KEY);
        mav.addObject("tileColorParam", tileColorFromSession instanceof String ? tileColorFromSession : DEFAULT_TILE_COLOR);
        mav.addObject("env",
                Arrays.asList(environment.getActiveProfiles())
                        .contains("production") ? "production" : "dev");
        mav.setViewName("index");
        return mav;
    }

    @GetMapping("/toggleGifs")
    @ResponseBody
    public Boolean toggleGifs()
    {
        gifService.toggleShowGifs();
        final boolean showGifs = gifService.isShowGifs();
        LOGGER.info("showGifs: {}", showGifs);
        return showGifs;
    }

    @GetMapping("/toggleDarkmode")
    @ResponseBody
    public Boolean toggleDarkmode(
            @RequestParam(name = "darkmode", required = false) Boolean requestedDarkmode,
            HttpSession httpSession)
    {
        if (requestedDarkmode == null) {
            darkmodeService.toggleDarkmode();
        } else {
            darkmodeService.setDarkmode(requestedDarkmode);
        }
        final boolean isDarkMode = darkmodeService.isDarkmode();
        httpSession.setAttribute("isDarkMode", isDarkMode);
        LOGGER.info("isDarkMode: {}", isDarkMode);
        return isDarkMode;
    }

    @GetMapping(value = "/page")
    public ModelAndView page(@RequestParam(name = "pageNumber", required = true) int pageNumber,
                             ModelAndView mav, HttpSession httpSession)
    {
        LOGGER.info("pagination, page to view: {}", pageNumber);
        return renderHomepage(mav, httpSession, pageNumber, null);
    }

    /**
     * Stores the number of Pokemon per page for this session and returns the page that
     * keeps the first visible Pokemon on screen.
     */
    @GetMapping("/pkmnPerPage")
    @ResponseBody
    public ResponseEntity<String> getPokemonPerPage(@RequestParam(name = "pkmnPerPage", required = false, defaultValue = "10") int pkmnPerPage,
                                                     @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                     HttpSession httpSession)
    {
        if (pkmnPerPage <= 0) {
            return ResponseEntity.badRequest().body("Invalid number of Pokemon per page");
        }
        final int oldPerPage = getSessionPkmnPerPage(httpSession);
        final int newPerPage = Math.min(pkmnPerPage, MAX_PKMN_PER_PAGE);
        final int firstVisibleIndex = (Math.max(1, currentPage) - 1) * oldPerPage;
        final int newPage = firstVisibleIndex / newPerPage + 1;
        httpSession.setAttribute(PKMN_PER_PAGE_SESSION_KEY, newPerPage);
        httpSession.setAttribute(PAGE_SESSION_KEY, newPage);
        LOGGER.info("pkmnPerPage updated to: {}", newPerPage);
        return ResponseEntity.ok().body(String.valueOf(newPage));
    }

    @GetMapping(value = "/getPokemonByType")
    @ResponseBody
    public ResponseEntity<String> getPokemonByType(@RequestParam(name = "chosenType", required = false, defaultValue = "") String chosenType)
    {
        // TODO: Fix filterByType
        LOGGER.info("Type filter requested: {}", chosenType);
        return ResponseEntity.ok().body("chosenType set");
    }

    private String sanitizeTileColor(String colorValue) {
        return colorValue != null && colorValue.matches("^#[0-9A-Fa-f]{6}$")
                ? colorValue
                : DEFAULT_TILE_COLOR;
    }

}