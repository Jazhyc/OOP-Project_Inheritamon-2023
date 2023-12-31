package inheritamon.model;

import inheritamon.model.data.DataHandler;
import inheritamon.model.npcs.types.AttritionPokemon;
import inheritamon.model.npcs.types.Pokemon;
import inheritamon.model.npcs.types.RandomPokemon;
import inheritamon.model.npcs.types.RecklessPokemon;
import inheritamon.model.player.Player;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * @author Jeremias
 * A class to represent the main logic of the game
 * Holds the player data and the battle handler
 * Also notifies the listeners when the game state changes
 * Singleton class due to the nature of the world panel
 */
public class GameModel {

    // Singleton instance
    private static GameModel instance = null;
    /**
     * Listeners for the current game state
     */
    private final ArrayList<PropertyChangeListener> gameStateListeners =
            new ArrayList<>();
    private Player playerData;
    private BattleHandler battleHandler;
    private PropertyChangeListener rosterListener;
    private PropertyChangeListener itemListener;

    /**
     * Returns the singleton instance of the game model
     *
     * @return The singleton instance of the game model
     */
    public static GameModel getInstance() {
        if (instance == null) {
            instance = new GameModel();
        }
        return instance;
    }

    /**
     * Initializes the game model
     *
     * @param battleHandlerObject The battle handler
     */
    public void init(BattleHandler battleHandlerObject) {
        battleHandler = battleHandlerObject;
        setUpBattleStateListener();
    }

    /**
     * Creates a new player and notifies the listeners that the game state has
     * changed
     */
    public void startNewGame() {
        playerData = new Player();
        notifyGameStateListeners(GameState.SELECT_STARTER);
        notifyItemListener();
    }

    /**
     * Loads the player data and notifies the listeners that the game state has
     * changed
     */
    public void continueGame() {
        DataHandler dataHandler = DataHandler.getInstance();
        playerData = (Player) dataHandler.loadState("playerData");

        // Check if player data is null
        if (playerData == null) {
            System.out.println("Player data is null!");
            notifyGameStateListeners(GameState.MAIN_MENU);
            return;
        }

        notifyGameStateListeners(GameState.GAME_START);
        notifyRosterListener();
        notifyItemListener();
    }

    /**
     * Adds the starting pokemon and perk to the player data
     *
     * @param pokemon The starting pokemon
     * @param perk    The starting perk
     */
    public void addStarterData(String pokemon, String perk) {
        playerData.addStarterData(pokemon, perk);
        notifyGameStateListeners(GameState.GAME_START);
        notifyRosterListener();
    }

    /**
     * Adds a listener to the game state listeners which will be notified when the
     * game state changes
     *
     * @param listener The listener to add
     */
    public void addGameStateListener(PropertyChangeListener listener) {
        gameStateListeners.add(listener);
    }

    /**
     * Adds a listener to the roster listener which will be notified when the roster
     * changes
     * We add a listener here since the player data does not exist when the game
     * model is created
     *
     * @param listener The listener to add
     */
    public void addRosterListener(PropertyChangeListener listener) {
        rosterListener = listener;
    }

    /**
     * Adds a listener to the item listener which will be notified when the items change
     * @param listener The listener to add
     */
    public void addItemListener(PropertyChangeListener listener) {
        itemListener = listener;
    }

    private void notifyGameStateListeners(GameState event) {
        for (PropertyChangeListener listener : gameStateListeners) {

            // Pass the event to the listener
            listener.propertyChange(
                    new PropertyChangeEvent(this, "gameState", null, event));

        }
    }

    private void notifyRosterListener() {
        rosterListener.propertyChange(
                new PropertyChangeEvent(this, "roster", null,
                        playerData.getRoster().getArray()));
    }

    private void notifyItemListener() {
        itemListener.propertyChange(
                new PropertyChangeEvent(this, "items", null,
                        playerData.getInventory()));
    }

    /**
     * Saves the player data and world data
     */
    public void saveGame() {
        DataHandler dataHandler = DataHandler.getInstance();
        dataHandler.saveState(playerData, "playerData");

        // Save the world data later
    }

    /**
     * Changes the game state to the main menu
     */
    public void returnToMainMenu() {
        notifyGameStateListeners(GameState.MAIN_MENU);
    }

    private void setUpBattleStateListener() {
        battleHandler.addListener("battleState", e -> {
            notifyRosterListener();
            notifyItemListener();
        });
    }

    /**
     * Revitalizes all pokemon in the roster
     */
    public void revitalizePokemon() {
        playerData.getRoster().revitalizeAll();
        notifyRosterListener();
    }

    /**
     * Starts a pokemon battle, depending on the type of pokemon chosen
     *
     * @param type The type of pokemon to battle
     */
    public void startPokemonBattle(String type) {
        DataHandler dataHandler = DataHandler.getInstance();

        if (playerData.getRoster().allFainted()) {
            System.out.println("All pokemon fainted");
            return;
        }

        String[] pokemon = dataHandler.getPokemonNames();

        // Get a random pokemon as a string
        String randomPokemonName =
                pokemon[(int) (Math.random() * pokemon.length)];
        Pokemon randomPokemon = switch (type) {
            case "attrition" -> new AttritionPokemon(
                    dataHandler.getPokemonData(randomPokemonName));
            case "reckless" -> new RecklessPokemon(
                    dataHandler.getPokemonData(randomPokemonName));
            default -> new RandomPokemon(
                    dataHandler.getPokemonData(randomPokemonName));
        };

        battleHandler.startBattle(playerData, randomPokemon);
    }

    /**
     * Returns the player data
     *
     * @return The player data
     */
    public Player getPlayerData() {
        return playerData;
    }

    /**
     * Enum to represent the states that the game can be in
     */
    public enum GameState {
        SELECT_STARTER, GAME_START, MAIN_MENU
    }

    /**
     * Removes a pokemon from the roster.
     * @param index The index of the pokemon to remove
     */
    public void removePokemon(int index) {
        playerData.getRoster().removePokemon(index);
        notifyRosterListener();
    }

}
