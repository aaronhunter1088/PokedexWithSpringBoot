package pokedex.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;
import pokedex.services.DarkmodeService;
import pokedex.services.GifService;
import pokedexapi.service.PokemonApiService;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokeapi.resource.pokemonspecies.PokemonSpecies;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestScope
public class EvolutionsController extends BaseController
{
    /* Logging instance */
    private static final Logger LOGGER = LogManager.getLogger(EvolutionsController.class);
    Integer pokemonChainID;
    List<List<Integer>> pokemonFamilyIDs;
    List<Integer> allIDs;
    List<List<Pokemon>> pokemonFamily;
    Integer pokemonFamilySize = 0;
    List<Integer> stages;
    Integer stage = 0;

    @Autowired
    public EvolutionsController(PokemonApiService pokemonService,
                                ObjectMapper objectMapper,
                                Environment environment,
                                DarkmodeService darkmodeService,
                                GifService gifService)
    {
        super(pokemonService, objectMapper, environment, darkmodeService, gifService);
    }

    @GetMapping(value = "/evolutions/{pokemonId}")
    public ModelAndView getEvolutions(@PathVariable String pokemonId, ModelAndView mav,
                                      @RequestParam(name = "darkmode", required = false, defaultValue = "false") String darkmode)
    {
        this.pokemonChainID = getEvolutionChainID(pokemonId);
        setupEvolutions();
        mav.setViewName("evolutions");
        if (pokemonFamilySize == 0) {
            mav.addObject("pokemonFamilySize", pokemonFamilySize);
            return mav;
        }
        String name = retrievePokemon(pokemonId).name();
        String capitalizedName = name.substring(0,1).toUpperCase() + name.substring(1);
        mav.addObject("pokemonName", capitalizedName);
        mav.addObject("pokemonId", pokemonId);
        mav.addObject("stages", stages);
        mav.addObject("pokemonFamily", pokemonFamily);
        mav.addObject("allIDs", allIDs);
        mav.addObject("isDarkMode", darkmode.equals("true"));
        return mav;
    }

    private void setupEvolutions()
    {
        pokemonFamilyIDs = pokemonChainID == 0 ? null
                : getFamilyIDs(pokemonApiService.getPokemonChainData(String.valueOf(pokemonChainID)));
        if (pokemonFamilyIDs != null && !pokemonFamilyIDs.isEmpty() &&
                (pokemonFamilyIDs.size() != 1 || pokemonFamilyIDs.getFirst().size() != 1)) {
            setFamilySize();
            setStages();
            setAllIDs();
            pokemonFamily = new ArrayList<>();
            pokemonFamilyIDs.forEach(this::createListOfPokemonForIDList);
        } else {
            pokemonFamilySize = 0;
            stages = null;
            allIDs = null;
            pokemonFamily = null;
        }
    }

    private void setFamilySize()
    {
        pokemonFamilySize = pokemonFamilyIDs.stream().flatMap(Collection::stream).toList().size();
        LOGGER.info("familySize:{}", pokemonFamilySize);
    }

    private void setStages()
    {
        stages = new ArrayList<>();
        pokemonFamilyIDs.forEach(idList -> stages.add(++stage));
        LOGGER.info("stages:{}", stages.size());
    }

    private void setAllIDs()
    {
        allIDs = pokemonFamilyIDs.stream()
                .flatMap(Collection::stream)
                .sorted()
                .toList();
        LOGGER.info("allIDs:{}", allIDs);
    }

    public void createListOfPokemonForIDList(List<Integer> idList)
    {
        LOGGER.info("idList: {}, size: {}", idList, idList.size());
        List<Pokemon> pokemonList = new ArrayList<>();
        String previousId = "";
        for (Integer id : idList) {
            LOGGER.info("id:{}", id);
            Pokemon pokemonResponse = pokemonApiService.getPokemonByIdOrName(String.valueOf(id));
            PokemonSpecies speciesData = null;
            try {
                speciesData = pokemonApiService.getPokemonSpeciesData(String.valueOf(pokemonResponse.getId()));
                if (null != speciesData) previousId = String.valueOf(id);
            }
            catch (Exception e) {
                LOGGER.warn("No species data found for {}. Using previousId {}", pokemonResponse.getId(), previousId);
                try {
                    speciesData = pokemonApiService.getPokemonSpeciesData(previousId);
                }
                catch (Exception e2) {
                    LOGGER.error("No species data found using previousId {}", previousId);
                }
            }
            if (speciesData == null) {
                LOGGER.error("Skipping pokemon {} in the evolution family: no species data available", id);
                continue;
            }

            Pokemon pokemon = createPokemon(pokemonResponse, speciesData);
            pokemonList.add(pokemon);
            LOGGER.info("pokemon added to familyList: {} length is {}", pokemon, pokemonList.size());
        }
        pokemonList = pokemonList.stream().sorted().toList();
        pokemonFamily.add(pokemonList);
    }

}
