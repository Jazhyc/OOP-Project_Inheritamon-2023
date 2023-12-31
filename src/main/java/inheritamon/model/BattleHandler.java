package inheritamon.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import inheritamon.model.data.DataHandler;
import inheritamon.model.data.language.LanguageConfiguration;
import inheritamon.model.inventory.Inventory;
import inheritamon.model.inventory.Item;
import inheritamon.model.npcs.Roster;
import inheritamon.model.npcs.moves.NormalAbility;
import inheritamon.model.npcs.types.*;
import inheritamon.model.player.Player;

/**
 * @author Jeremias
 * A class to handle battles, takes the player and the enemy pokemon as
 * parameters
 */
public class BattleHandler {

    /**
     * An Hashmap of all the moves as ability objects
     */
    private final HashMap<String, NormalAbility> moveData;

    private PropertyChangeListener moveListener;
    private PropertyChangeListener dialogueListener;
    private PropertyChangeListener playerRosterListener;
    private PropertyChangeListener inventoryListener;

    /**
     * The time to wait between each turn
     */
    private final int WAIT_TIME = 1000;

    /**
     * Listeners for notifying the player and enemy pokemon of changes to their stats in the view
     */
    private final PropertyChangeListener[] statListeners =
            new PropertyChangeListener[2];

    /**
     * Listeners for notifying the player and enemy pokemon of changes to their sprites in the view
     */
    private final PropertyChangeListener[] spriteListeners =
            new PropertyChangeListener[2];

    /**
     * Listeners for notifying the game model and the view of changes to the battle state
     */
    private final PropertyChangeListener[] battleStateListeners =
            new PropertyChangeListener[2];

    private PlayerPokemon playerPokemon;
    private Pokemon enemyPokemon;
    private Roster playerRoster;
    private Inventory playerInventory;
    private int turn;

    /**
     * The constructor for the battle handler
     */
    public BattleHandler() {
        this.moveData = DataHandler.getInstance().getAllAbilities();
    }

    /**
     * A method to start the battle on a different thread
     */
    public void startBattle(Player playerData, Pokemon enemyPokemon) {

        // Create a new thread
        Thread battleThread = new Thread(
                () -> battleLoop(playerData, enemyPokemon));

        // Start the thread
        battleThread.start();

    }

    private void battleLoop(Player playerData, Pokemon enemyPokemon) {

        LanguageConfiguration config = LanguageConfiguration.getInstance();

        turn = 0;
        String ability;
        Pokemon attacker;
        Pokemon defender;

        String formattedString;
        setUpBattle(playerData, enemyPokemon, config);

        while (!playerRoster.allFainted() && enemyPokemon.getHP() > 0) {

            attacker = (turn % 2 == 0) ? playerPokemon : enemyPokemon;
            defender = (turn % 2 == 0) ? enemyPokemon : playerPokemon;

            formattedString = String.format(config.getText("TurnStart"),
                    attacker.getName());
            notifyDialogueListener(formattedString);

            // Get the ability to use
            ability = attacker.useMove(defender.getAllNumericalStats());

            // Check if the ability is Run
            if (ability.equals("Run")) {
                handleRun(config);
                return;
            }

            // Add item functionality
            if (ability.startsWith("item")) {
                handleItemUse(ability);
                continue;
            }

            // Checked if the ability returned starts with switch
            if (ability.startsWith("switch")) {
                getPokemonToSwitchTo(playerRoster, enemyPokemon, ability);
                continue;
            }

            String localAbilityName = config.getLocalMoveName(ability);
            formattedString =
                    String.format(config.getText("Attack"), attacker.getName(),
                            localAbilityName);
            notifyDialogueListener(formattedString);
            wait(WAIT_TIME);

            reportDamage(enemyPokemon, ability, attacker, defender);

            // If the player pokemon fainted, notify the listeners
            if (playerPokemon.isFainted()) {
                handleFaint(playerRoster, enemyPokemon, config);
                continue;
            }

            System.out.println("--------------------------------------");

            turn++;

        }

        String conclusion;

        conclusion = determineConclusion(playerRoster, enemyPokemon);
        notifyBattleStateListener(conclusion);
    }

    private void reportDamage(Pokemon enemyPokemon, String ability,
                              Pokemon attacker, Pokemon defender) {
        // Use the ability
        Integer damageDealt =
                moveData.get(ability).executeMove(defender, attacker);

        // Check the damage for display purposes
        checkDamage(attacker, damageDealt);

        notifyStatListener(playerPokemon, enemyPokemon);
        wait(WAIT_TIME);
    }

    private void handleRun(LanguageConfiguration config) {
        String formattedString;
        formattedString = String.format(config.getText("Run"));
        notifyDialogueListener(formattedString);
        wait(WAIT_TIME);
        notifyBattleStateListener("Draw");
    }

    private void setUpBattle(Player playerData, Pokemon enemyPokemon,
                             LanguageConfiguration config) {
        this.playerRoster = playerData.getRoster();

        int playerPokemonIndex = playerRoster.getAlivePokemon();
        playerPokemon = (PlayerPokemon) playerRoster.getPokemon(playerPokemonIndex);
        this.enemyPokemon = enemyPokemon;
        this.playerInventory = playerData.getInventory();

        notifyStatListener(playerPokemon, enemyPokemon);
        notifyMoveListener(playerPokemon);
        notifyPokemonSpriteListener(playerPokemon, enemyPokemon);
        notifyPlayerRosterListener();
        notifyInventoryListener();
        notifyBattleStateListener("Start");

        // Beginning of the battle
        // Get the BattleStart string from language config
        String formattedString = String.format(config.getText("BattleStart"),
                enemyPokemon.getName());
        notifyDialogueListener(formattedString);
        wait(WAIT_TIME);
    }

    private void handleFaint(Roster playerRoster, Pokemon enemyPokemon,
                             LanguageConfiguration config) {
        String formattedString;
        formattedString = String.format(config.getText("Fainted"),
                playerPokemon.getName());
        notifyDialogueListener(formattedString);
        wait(WAIT_TIME);

        // Get the next pokemon if there is one
        if (!playerRoster.allFainted()) {
            getPokemonToSwitchTo(playerRoster, enemyPokemon,
                    "switch" + playerRoster.getAlivePokemon());
        }

        // Notify the roster listener
        notifyPlayerRosterListener();
    }

    private String determineConclusion(Roster playerRoster,
                                       Pokemon enemyPokemon) {

        String formattedString;
        LanguageConfiguration config = LanguageConfiguration.getInstance();

        String conclusion;
        if (enemyPokemon.getHP() <= 0) {
            formattedString = String.format(config.getText("Fainted"),
                    enemyPokemon.getName());
            notifyDialogueListener(formattedString);
            wait(WAIT_TIME);
            notifyDialogueListener(config.getText("Victory"));
            wait(WAIT_TIME);
            handleLoot();
            conclusion = "Victory";
        } else if (playerRoster.allFainted()) {
            notifyDialogueListener(config.getText("AllFainted"));
            wait(WAIT_TIME);
            notifyDialogueListener(config.getText("Defeat"));
            conclusion = "Defeat";
        } else {
            conclusion = "Draw";
        }

        wait(WAIT_TIME);
        return conclusion;
    }

    private void handleLoot() {

        DataHandler dataHandler = DataHandler.getInstance();
        LanguageConfiguration config = LanguageConfiguration.getInstance();

        // Get the loot from the enemy pokemon
        String loot = enemyPokemon.getLoot();
        Integer coins = enemyPokemon.getNumericalStat("Coins");
        Item item = new Item(dataHandler.getItemData(loot));
        playerInventory.addItem(item);
        playerInventory.addCoins(coins);

        String formattedString =
                String.format(config.getText("Loot"), item.getItemName(),
                        coins);
        notifyDialogueListener(formattedString);
        wait(WAIT_TIME);

    }

    private void getPokemonToSwitchTo(Roster playerRoster,
                                      Pokemon enemyPokemon, String ability) {
        System.out.println(ability);

        // Get the pokemon index to switch to using regex
        int pokemonToSwitchTo =
                Integer.parseInt(ability.replaceAll("[^0-9]", ""));
        playerPokemon = (PlayerPokemon) playerRoster.getPokemon(pokemonToSwitchTo);

        // Notify the listeners
        notifyMoveListener(playerPokemon);
        notifyPokemonSpriteListener(playerPokemon, enemyPokemon);
        notifyStatListener(playerPokemon, enemyPokemon);

        LanguageConfiguration config = LanguageConfiguration.getInstance();
        String formattedString = String.format(config.getText("Switch"),
                playerPokemon.getName());
        notifyDialogueListener(formattedString);
        wait(WAIT_TIME);

        // Skip the rest of the turn
        turn++;
    }

    private void checkDamage(Pokemon attacker, Integer damageDealt) {

        LanguageConfiguration config = LanguageConfiguration.getInstance();
        String formattedString;

        if (damageDealt == -1) {
            formattedString = String.format(config.getText("LackOfMP"),
                    attacker.getName());
            notifyDialogueListener(formattedString);
        } else if (damageDealt < 0) {
            formattedString =
                    String.format(config.getText("Heal"), attacker.getName(),
                            Math.abs(damageDealt));
            notifyDialogueListener(formattedString);
        } else if (damageDealt > 0) {
            formattedString =
                    String.format(config.getText("Damage"), attacker.getName(),
                            damageDealt);
            notifyDialogueListener(formattedString);
        } else {
            formattedString =
                    String.format(config.getText("Miss"), attacker.getName());
            notifyDialogueListener(formattedString);
        }

    }

    // Code authored by Jona Janssen
    private void handleItemUse(String ability) {

        // Get the item index to use using regex
        int itemToUse = Integer.parseInt(ability.replaceAll("[^0-9]", ""));

        // Get the item
        Item item = playerInventory.getItem(itemToUse);

        // Remove the item from the inventory
        playerInventory.removeItem(itemToUse);

        LanguageConfiguration config = LanguageConfiguration.getInstance();
        String formattedString =
                String.format(config.getText("Item"), item.getItemName());
        notifyDialogueListener(formattedString);
        notifyInventoryListener();
        wait(WAIT_TIME);

        // Use the item
        if (item.useItem(enemyPokemon, playerPokemon, playerRoster)) {
            // End the battle
            enemyPokemon.killPokemon();
            formattedString = String.format(config.getText("Capture"),
                    enemyPokemon.getName());
            notifyDialogueListener(formattedString);
            wait(WAIT_TIME);
        }

        // Notify the listeners
        notifyInventoryListener();
        notifyStatListener(playerPokemon, enemyPokemon);

        // Skip the rest of the turn
        turn++;

    }

    private int[] getPokemonDisplayStats(Pokemon pokemon) {
        int[] stats = new int[4];
        stats[0] = pokemon.getHP();
        stats[1] = pokemon.getNumericalStat("MaxHP");
        stats[2] = pokemon.getMP();
        stats[3] = pokemon.getNumericalStat("MaxMP");
        return stats;
    }

    private void addListener(PropertyChangeListener[] listeners,
                             PropertyChangeListener newListener) {
        // Add the listener to the array
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == null) {
                listeners[i] = newListener;
                break;
            }
        }
    }

    /**
     * Adds a listener to the battle for a certain parameter
     *
     * @param listenerType The type of listener to add
     * @param listener     The listener to add
     */
    public void addListener(String listenerType,
                            PropertyChangeListener listener) {
        switch (listenerType) {
            case "pokemonSprite" -> addListener(spriteListeners, listener);
            case "stat" -> addListener(statListeners, listener);
            case "playerRoster" -> this.playerRosterListener = listener;
            case "inventory" -> this.inventoryListener = listener;
            case "battleState" -> addListener(battleStateListeners, listener);
            case "dialogue" -> this.dialogueListener = listener;
            case "moves" -> this.moveListener = listener;
            default -> throw new IllegalArgumentException(
                    "Invalid listener type: " + listenerType);
        }
    }

    private void notifyDialogueListener(String dialogue) {
        dialogueListener.propertyChange(
                new PropertyChangeEvent(this, "dialogue", null, dialogue));
    }

    private void notifyPokemonSpriteListener(Pokemon playerPokemon,
                                             Pokemon enemyPokemon) {
        spriteListeners[0].propertyChange(
                new PropertyChangeEvent(this, "playerSprite", null,
                        playerPokemon.getName()));
        spriteListeners[1].propertyChange(
                new PropertyChangeEvent(this, "enemySprite", null,
                        enemyPokemon.getName()));
    }

    private void notifyMoveListener(Pokemon playerPokemon) {

        // Create a string array of the moves
        String[] moves = new String[playerPokemon.getMoves().size()];
        for (int i = 0; i < playerPokemon.getMoves().size(); i++) {
            moves[i] = playerPokemon.getMoves().get(i);
        }

        moveListener.propertyChange(
                new PropertyChangeEvent(this, "moves", null, moves));
    }

    private void notifyStatListener(Pokemon playerPokemon,
                                    Pokemon enemyPokemon) {

        // Get the stats for the player pokemon
        int[] playerStats = getPokemonDisplayStats(playerPokemon);
        statListeners[0].propertyChange(
                new PropertyChangeEvent(this, "playerStats", null,
                        playerStats));

        // Get the stats for the enemy pokemon
        int[] enemyStats = getPokemonDisplayStats(enemyPokemon);
        statListeners[1].propertyChange(
                new PropertyChangeEvent(this, "enemyStats", null, enemyStats));
    }

    private void notifyPlayerRosterListener() {

        // Create a pokemon array of the pokemon
        Pokemon[] playerRosterArray = playerRoster.getArray();

        playerRosterListener.propertyChange(
                new PropertyChangeEvent(this, "playerInventory", null,
                        playerRosterArray));
    }

    private void notifyInventoryListener() {
        // Create a copy of the player's inventory
        Inventory inventory = playerInventory;
        inventoryListener.propertyChange(
                new PropertyChangeEvent(this, "playerRoster", null, inventory));
    }

    private void notifyBattleStateListener(String conclusion) {
        // Loop over the listeners and notify them
        for (PropertyChangeListener listener : battleStateListeners) {
            listener.propertyChange(
                    new PropertyChangeEvent(this, "conclusion", null,
                            conclusion));
        }
    }

    /**
     * Gets the player's active pokemon
     *
     * @return The player's active pokemon
     */
    public PlayerPokemon getActivePlayerPokemon() {
        return playerPokemon;
    }

    private void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
