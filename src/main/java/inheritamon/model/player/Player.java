package inheritamon.model.player;

import inheritamon.model.data.DataHandler;
import inheritamon.model.inventory.*;
import inheritamon.model.npcs.Roster;
import inheritamon.model.npcs.types.PlayerPokemon;

import java.io.Serializable;
import java.util.*;

/**
 * @author Bernard, Jeremias
 * Class that holds all the data of the player.
 * Initially created for player abilities but expanded to hold the player roster and inventory as well.
 * Will either be loaded from a save file or created from scratch
 */
public class Player implements Serializable {

    /**
     * Enum for the trainer abilities
     */
    public enum TrainerAbility {
        CLIMBER, SWIMMER, RICH
    }

    private Roster roster;
    private Inventory inventory;
    private ArrayList<TrainerAbility> abilities =
            new ArrayList<>();

    /**
     * Constructor for the PlayerData
     */
    public Player() {
        roster = new Roster();
        inventory = new Inventory();
    }

    /**
     * Returns the player roster
     *
     * @return The player roster
     */
    public Roster getRoster() {
        return roster;
    }

    /**
     * Returns the player inventory
     *
     * @return The player inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the player abilities
     *
     * @return The player abilities
     */
    public ArrayList<TrainerAbility> getAbilities() {
        return abilities;
    }

    /**
     * Adds an ability to the player
     *
     * @param ability The ability to add
     */
    public void addAbility(TrainerAbility ability) {

        // Only add if the ability is not already in the list
        if (!abilities.contains(ability)) {
            abilities.add(ability);
        }

    }

    /**
     * Adds starter data to the player like a starter pokemon and a perk
     *
     * @param pokemon The starter pokemon
     * @param perk   The starter perk
     */
    public void addStarterData(String pokemon, String perk) {

        DataHandler dataHandler = DataHandler.getInstance();

        roster.addPokemon(
                new PlayerPokemon(dataHandler.getPokemonData(pokemon)));

        // Convert the perk into a trainer ability
        TrainerAbility ability = TrainerAbility.valueOf(perk.toUpperCase());

        // If the ability is rich, add 1000 coins to the inventory
        if (ability == TrainerAbility.RICH) {
            inventory.addCoins(1000);
        }

        addAbility(ability);

        System.out.println("Added starter data");

    }

}
