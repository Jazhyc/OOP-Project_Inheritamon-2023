package inheritamon.view.world.sidebar;

import javax.swing.*;

import inheritamon.controller.GameController;
import inheritamon.model.data.language.*;
import inheritamon.view.SoundHandler;

import java.awt.event.*;
import java.util.*;
import java.awt.*;

/**
 * @author Jeremias
 */
public class SidePanel extends JPanel implements LanguageChangeListener {

    private ArrayList<String> options = new ArrayList<String>();
    private ArrayList<JLabel> buttonLabels = new ArrayList<JLabel>();

    private SoundHandler soundHandler;
    private JPanel pokemonDataPanel;
    private GameController gameController;

    public SidePanel(GameController gameController, JPanel pokemonDataPanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        addLanguageListener();

        soundHandler = SoundHandler.getInstance();
        this.pokemonDataPanel = pokemonDataPanel;
        this.gameController = gameController;

        LanguageConfiguration config = LanguageConfiguration.getInstance();

        options.addAll(Arrays.asList(config.getOptions("SidePanel")));
        
        // Create a font for the menu
        Font font = new Font("Arial", Font.BOLD, 40);

        // Add The text "Menu" to the top of the panel
        JLabel menuLabel = new JLabel("Inheritamon");
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        menuLabel.setForeground(Color.WHITE);
        menuLabel.setFont(font);
        add(menuLabel);

        // Use a bold font
        Font optionFont = new Font("Arial", Font.BOLD, 20);

        // Create a JLabel for each option
        for (String option : options) {

            // Center the labels and and add them to the panel
            JLabel label = new JLabel(option);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add some padding
            label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Make the font white
            label.setForeground(Color.WHITE);

            label.setFont(optionFont);
            add(label);

            // Add the label to the list of labels
            buttonLabels.add(label);

        }

        // Use a black background
        setBackground(Color.BLACK);

        addMouseMotionListener(new MouseMotionAdapter() {
                
            @Override
            public void mouseMoved(MouseEvent e) {

                int x = e.getX();
                int y = e.getY();

                // Use a for loop to check if the mouse is within the bounds of a button
                for (int i = 0; i < buttonLabels.size(); i++) {

                    JLabel button = buttonLabels.get(i);

                    // Get the bounds of the button
                    Rectangle bounds = button.getBounds();

                    // Check if the mouse is within the bounds of the button
                    if (bounds.contains(x, y)) {
                        button.setForeground(Color.YELLOW);
                    } else {
                        button.setForeground(Color.WHITE);
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
                
            // Add a check for mouse clicks
            @Override
            public void mouseClicked(MouseEvent e) {

                int x = e.getX();
                int y = e.getY();

                // Use a for loop to check if the mouse is within the bounds of a button
                for (int i = 0; i < buttonLabels.size(); i++) {

                    JLabel button = buttonLabels.get(i);

                    // Get the bounds of the button
                    Rectangle bounds = button.getBounds();

                    // Check if the mouse is within the bounds of the button
                    if (bounds.contains(x, y)) {
                        
                        handleButtonPress(i);
                        soundHandler.playSound("select");

                    }
                }
            }

        });
    }

    private void handleButtonPress(int i) {
                // Use a switch statement to check which button was clicked
                switch (i) {
                    case 0:
                        System.out.println("Demo Battle");
                        gameController.beginBattle();
                    case 1:
                        // Open the items menu
                        System.out.println("Items");
                        break;
                    case 2:
                        // Open the pokemon menu
                        System.out.println("Pokemon");
                        pokemonDataPanel.setVisible(!pokemonDataPanel.isVisible());
                        break;
                    case 3:
                        // Save the game
                        System.out.println("Save");
                        gameController.saveGame();
                        break;
                    case 4:
                        // Minimize the menu
                        System.out.println("Minimize Menu");
                        setVisible(false);
                        break;
                    case 5:
                        System.out.println("To Title");
                        setVisible(false);
                        gameController.returnToMainMenu();
                        break;
                    case 6:
                        // Exit the game
                        System.out.println("Exit Game");
                        gameController.saveGame();
                        System.exit(0);
                        break;
                }
            }

    public void addLanguageListener() {
        LanguageConfiguration config = LanguageConfiguration.getInstance();

        config.addLanguageChangeListener(e -> {
            options.clear();
            
            // Combine these two lines into one
            options.addAll(Arrays.asList(config.getOptions("SidePanel")));

            // Update the labels
            for (int i = 0; i < options.size(); i++) {
                buttonLabels.get(i).setText(options.get(i));
            }
        });

    }
    
}