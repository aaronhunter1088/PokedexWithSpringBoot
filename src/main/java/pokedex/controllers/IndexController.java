package pokedex.controllers;

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
import pokedexapi.service.PokemonApiService;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class IndexController extends BaseController
{
    /* Logging instance */
    private static final Logger LOGGER = LogManager.getLogger(IndexController.class);
    private static final String TILE_COLOR_SESSION_KEY = "tileColorParam";
    private static final String DEFAULT_TILE_COLOR = "#4CAF50";

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
           @RequestParam(name = "tileColor", required = false) String tileColor)
    {
        long startTime = System.nanoTime();
        if (StringUtils.hasText(darkmode)) {
            httpSession.setAttribute("isDarkMode", Boolean.parseBoolean(darkmode));
            darkmodeService.setDarkmode(Boolean.parseBoolean(darkmode));
        }
        if (StringUtils.hasText(tileColor)) {
            httpSession.setAttribute(TILE_COLOR_SESSION_KEY, sanitizeTileColor(tileColor));
        }
        if (StringUtils.hasText(darkmode) && StringUtils.hasText(tileColor)) {
            LOGGER.info("Homepage accessed with darkmode: {} and tileColor: {}", darkmode, tileColor);
            return new ModelAndView("redirect:/");
        } else if (StringUtils.hasText(darkmode)) {
            LOGGER.info("Homepage accessed with darkmode: {}", darkmode);
            return new ModelAndView("redirect:/");
        }

        ModelAndView homepage = renderHomepage(mav, httpSession);

        long endTime = System.nanoTime();

        double duration = (endTime - startTime) / 1_000_000_000.0;

        LOGGER.info("Homepage accessed with duration: {} secs", duration);
        return homepage;
    }

    private ModelAndView renderHomepage(ModelAndView mav, HttpSession httpSession)
    {
        isDarkMode = darkmodeService.isDarkmode();
        showGifs = gifService.isShowGifs();

        lastPageSearched = page;
        if (pokemonMap.isEmpty()) {
            updateSessionMap();
        } else {
            // update pokemonMap because clicked on a new page
            updateSessionMap();
            httpSession.setAttribute("pokemonMap", pokemonMap);
        }
        
        mav.addObject("pokemonMap", pokemonMap);
        mav.addObject("pokemonSprites", getPokemonSprites());
        this.page = lastPageSearched;
        mav.addObject("pokemonIds", new ArrayList<>(pokemonMap.keySet()));
        mav.addObject("defaultImagePresent", defaultImagePresent);
        mav.addObject("showGifs", showGifs);
        mav.addObject("pkmnPerPage", pkmnPerPage);
        mav.addObject("totalPokemon", totalPokemon);
        mav.addObject("totalPages", (int) Math.floor((double) totalPokemon / pkmnPerPage));
        mav.addObject("page", page);
        mav.addObject("uniqueTypes", getUniqueTypes());
        mav.addObject("chosenType", chosenType);
        //isDarkMode = darkmode.equals("true");
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
        showGifs = gifService.isShowGifs();
        LOGGER.info("showGifs: {}", showGifs);
        return showGifs;
    }

    @GetMapping("/toggleDarkmode")
    @ResponseBody
    public Boolean toggleDarkmode()
    {
        //darkmodeService.setDarkmode(!darkmodeService.isDarkmode());
        darkmodeService.toggleDarkMode();
        isDarkMode = darkmodeService.isDarkmode();
        LOGGER.info("isDarkMode: {}", isDarkMode);
        return isDarkMode;
    }

    @GetMapping(value = "/page")
    public ModelAndView page(@RequestParam(name = "pageNumber", required = true) int pageNumber,
                             ModelAndView mav, HttpSession httpSession)
    {
        LOGGER.info("pagination, page to view: {}", pageNumber);
        if (pageNumber < 1) {
            LOGGER.error("Page number must be at least 1, received: {}", pageNumber);
            return mav;
        } else if (pageNumber > Math.round((float) totalPokemon / pkmnPerPage)) {
            LOGGER.error("Cannot pick a number more than there are pages");
            return mav;
        }
        page = pageNumber;
        // Clear pokemon map to force reload with new page
        //this.pokemonMap.clear();
        // Clear session cache when changing pages
        httpSession.removeAttribute("pokemonMap");
        return renderHomepage(mav, httpSession);
    }

    @GetMapping("/pkmnPerPage")
    @ResponseBody
    public ResponseEntity<String> getPokemonPerPage(@RequestParam(name = "pkmnPerPage", required = false, defaultValue = "10") int pkmnPerPage,
                                                     HttpSession httpSession)
    {
        if (pkmnPerPage <= 0) {
            return ResponseEntity.badRequest().body("Invalid number of Pokemon per page");
        } else {
            if (pkmnPerPage > 50) {
                LOGGER.info(pkmnPerPage + " is too high. Defaulting to 50");
                this.pkmnPerPage = 50;
            } else {
                this.pkmnPerPage = pkmnPerPage;
            }
        }
        // Clear pokemon map to force reload with new page size
        this.pokemonMap.clear();
        // Reset page to 1
        this.page = 1;
        // Clear session cache when Pokemon per page changes
        httpSession.removeAttribute("pokemonMap");
        LOGGER.info("pkmnPerPage updated to: {}", pkmnPerPage);
        return ResponseEntity.ok().body("PkmnPerPage set");
    }

    @GetMapping(value = "/getPokemonByType")
    @ResponseBody
    public ResponseEntity<String> getPokemonByType(@RequestParam(name = "chosenType", required = false, defaultValue = "") String chosenType,
                                                   HttpSession httpSession)
    {
        String previousType = this.chosenType;
        this.chosenType = !"none".equals(chosenType) ? chosenType : null;
        // Reset page to 1 when changing filter
        this.page = 1;
        // Clear the pokemon map to force reload
        this.pokemonMap.clear();
        LOGGER.info("Type filter set to: {}", this.chosenType);
        return ResponseEntity.ok().body("chosenType set");
    }

    private String sanitizeTileColor(String colorValue) {
        return colorValue != null && colorValue.matches("^#[0-9A-Fa-f]{6}$")
                ? colorValue
                : DEFAULT_TILE_COLOR;
    }

}