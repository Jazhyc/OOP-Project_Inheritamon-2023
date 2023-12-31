package inheritamon.view.world;

import inheritamon.Main;
import inheritamon.controller.GameController;
import inheritamon.model.GameModel;
import inheritamon.model.GameModel.GameState;
import inheritamon.view.world.sidebar.InventoryPanel;
import inheritamon.view.world.sidebar.PokemonDataPanel;
import inheritamon.view.world.sidebar.SidePanel;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * @author Jeremias
 * A class to represent the game panel
 * This panel will hold the world panel and the side panel
 */
public class GamePanel extends JPanel {

    public PlayerAvatar playerAvatar;
    WorldPanel worldPanel;
    SidePanel sidePanel;
    PokemonDataPanel pokemonDataPanel;

    InventoryPanel inventoryPanel;

    /**
     * Constructor for the GamePanel class
     * @param gameController The battle controller
     * @param gameModel The game model
     */
    public GamePanel(GameController gameController, GameModel gameModel) {

        int screenWidth = Main.SCREEN_WIDTH;
        int screenHeight = Main.SCREEN_HEIGHT;

        // Use a layout that won't stretch the panels
        setLayout(new BorderLayout());

        // Create a layered pane to hold the game panel and the side panel
        JLayeredPane layeredPane = new JLayeredPane();

        // Add the panels to the layered pane
        pokemonDataPanel = new PokemonDataPanel(gameModel, gameController);
        inventoryPanel = new InventoryPanel(gameModel);
        sidePanel = new SidePanel(gameController, pokemonDataPanel, inventoryPanel);
        worldPanel = new WorldPanel(sidePanel);

        // Add the panels to the layered pane and set the side panel to be 1/4 of the
        // width of the game panel
        layeredPane.add(worldPanel, 0);
        layeredPane.add(sidePanel, 1);
        layeredPane.add(pokemonDataPanel, 1);
        layeredPane.add(inventoryPanel, 1);

        int sidePanelWidth = screenWidth / 4;
        sidePanel.setBounds(0, 0, sidePanelWidth, screenHeight);
        pokemonDataPanel.setBounds(sidePanelWidth, 0,
                screenWidth - sidePanelWidth, screenHeight);
        inventoryPanel.setBounds(sidePanelWidth, 0,
                screenWidth - sidePanelWidth, screenHeight);

        worldPanel.setBounds(0, 0, screenWidth, screenHeight);

        // This should be removed
        worldPanel.startGameThread();

        // Set the layers of the panels so that the side panel is on top of the game
        // panel
        layeredPane.setLayer(worldPanel, 0);
        layeredPane.setLayer(sidePanel, 1);
        layeredPane.setLayer(pokemonDataPanel, 1);

        // Make the side panel invisible
        sidePanel.setVisible(false);
        pokemonDataPanel.setVisible(false);
        inventoryPanel.setVisible(false);

        // Add the layered pane to the game panel
        add(layeredPane);

        setUpListener(gameModel);
    }

    private void setUpListener(GameModel gameModel) {

        gameModel.addGameStateListener(
                e -> setVisible(e.getNewValue() == GameState.GAME_START));

    }

}
