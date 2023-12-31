package inheritamon.view.combat.actions;

import inheritamon.controller.GameController;
import inheritamon.model.BattleHandler;
import inheritamon.model.data.DataHandler;
import inheritamon.model.inventory.*;
import inheritamon.view.SoundHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * @author Jona Janssen
 * Inventory panel to be used in the battle menu. User can see the items they are carrying and click them to use them
 */

public class ItemsPanel extends JPanel {
    private final int SPRITE_SIZE = 80;
    private Inventory inventory;

    /**
     * The sound handler for sound effects
     */
    private final SoundHandler soundHandler;

    public ItemsPanel(BattleHandler battleHandler,
                      GameController battleController) {

        GridLayout gridLayout = new GridLayout(2, 3);
        setLayout(gridLayout);

        setUpListener(battleHandler, battleController);
        soundHandler = SoundHandler.getInstance();

    }

    /**
     * Sets up the listeners for the GUI panel
     * @param battleHandler
     * @param battleController
     */
    private void setUpListener(BattleHandler battleHandler,
                               GameController battleController) {

        DataHandler dataHandler = DataHandler.getInstance();

        battleHandler.addListener("inventory", e -> {
            inventory = (Inventory) e.getNewValue();

            // Remove all components
            removeAll();

            // Loop over the array using index
            for (int i = 0; i < inventory.getMaxSize(); i++) {

                // Skip if i is greater than the length of the array
                if (i >= inventory.getSize()) {

                    // Add an empty label
                    add(new JLabel());

                    continue;
                }

                Item item = inventory.getItem(i);

                // Get the name of the sprite
                String spriteName = item.getItemSprite();

                // Get the sprite from the data handler
                BufferedImage imageToDisplay =
                        dataHandler.getItemSprite(spriteName);

                addButton(battleController, i, imageToDisplay);

            }
        });

    }

    /**
     * Adds button to the GUI panel
     * @param battleController
     * @param i
     * @param imageToDisplay
     */
    private void addButton(GameController battleController, int i,
                           BufferedImage imageToDisplay) {
        // Skip if i is greater than the length of the array
        if (i < inventory.getSize()) {

            // Add the label with the sprite
            JLabel label = new JLabel(new ImageIcon(
                    imageToDisplay.getScaledInstance(SPRITE_SIZE, SPRITE_SIZE,
                            Image.SCALE_DEFAULT)));
            add(label);

            final int selectionIndex = i;

            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    soundHandler.playSound("select");
                    battleController.selectItem(selectionIndex);
                }
            });

        }
    }

}
