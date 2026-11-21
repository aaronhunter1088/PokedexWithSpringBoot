package pokedex.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds evolution chain bodies shaped like the EvolutionApi response.
 */
final class ChainFixtures
{
    private ChainFixtures() {}

    static Map<String, Object> species(String name, int id)
    {
        final Map<String, Object> species = new HashMap<>();
        species.put("name", name);
        species.put("url", "https://pokeapi.co/api/v2/pokemon-species/" + id + "/");
        return species;
    }

    static Map<String, Object> details(int minLevel, String trigger)
    {
        final Map<String, Object> details = new HashMap<>();
        details.put("min_level", minLevel);
        details.put("trigger", new HashMap<>(Map.of("name", trigger)));
        details.put("time_of_day", "");
        return details;
    }

    @SafeVarargs
    static Map<String, Object> link(String name, int id, Map<String, Object> evolutionDetails, Map<String, Object>... next)
    {
        final Map<String, Object> link = new HashMap<>();
        link.put("species", species(name, id));
        link.put("is_baby", false);
        link.put("evolution_details", evolutionDetails == null ? new ArrayList<>() : new ArrayList<>(List.of(evolutionDetails)));
        link.put("evolves_to", new ArrayList<>(List.of(next)));
        return link;
    }

    /** bulbasaur (1) evolves at 16 into ivysaur (2) which evolves at 32 into venusaur (3) */
    static Map<String, Object> bulbasaurChain()
    {
        final Map<String, Object> chain = new HashMap<>();
        chain.put("id", 1);
        chain.put("chain", link("bulbasaur", 1, null,
                link("ivysaur", 2, details(16, "level-up"),
                        link("venusaur", 3, details(32, "level-up")))));
        return chain;
    }

    /** a pokemon that does not evolve */
    static Map<String, Object> singleChain()
    {
        final Map<String, Object> chain = new HashMap<>();
        chain.put("id", 2);
        chain.put("chain", link("tauros", 128, null));
        return chain;
    }
}
