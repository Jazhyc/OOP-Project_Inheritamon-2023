package inheritamon.view.menu;

import javax.swing.*;

import inheritamon.controller.MenuController;
import inheritamon.model.GameModel;
import inheritamon.model.data.language.*;
import inheritamon.view.SoundHandler;

import java.awt.*;
import java.awt.event.*;

/**
 * @author Jeremias
 * The main menu of the game
 */
public class MainMenuPanel extends JPanel implements LanguageChangeListener {

    /**
     * The number of options on the main menu
     */
    private final int NUMBER_OF_PANELS = 3;

    /**
     * The title of the game
     */
    private final JLabel titleLabel;

    /**
     * The sound handler for playing sounds
     */
    private final SoundHandler soundHandler;

    /**
     * The buttons on the main menu
     */
    private final JLabel[] buttons = new JLabel[NUMBER_OF_PANELS];

    /**
     * Constructor for the MainMenuPanel
     *
     * @param controller The menu controller
     * @param model      The game model
     */
    public MainMenuPanel(MenuController controller, GameModel model) {

        addLanguageListener();

        setLayout(new GridBagLayout());

        soundHandler = SoundHandler.getInstance();
        addGameStateListener(model);

        // Get the configuration object
        LanguageConfiguration config = LanguageConfiguration.getInstance();

        titleLabel = new JLabel("Inheritamon");
        int titleSize = 36;
        titleLabel.setFont(new Font("Arial", Font.BOLD, titleSize));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        addTitle();

        addButtons(config, controller);

        // Set the background color
        setBackground(Color.WHITE);

        // Add a button to the top right that switches between the words EN and NL
        JButton languageButton = new JButton("EN");
        // Add more code for implementing the language switch
        languageButton.addActionListener(e -> {
            config.switchLanguage();

            // Switch the text of the button
            String buttonText = languageButton.getText();
            if (buttonText.equals("EN")) {
                languageButton.setText("NL");
            } else {
                languageButton.setText("EN");
            }
            soundHandler.playSound("select");
        });

        addLanguageSwitch(languageButton);
    }

    private void addLanguageSwitch(JButton languageButton) {
        // Add button to the top right, create a new GridBagConstraints object
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(languageButton, gbc);
    }

    private void addButtons(LanguageConfiguration config,
                            MenuController controller) {

        // Get the strings from config
        String[] buttonStrings = config.getOptions("MainMenu");

        // Create a new GridBagConstraints object
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // Create the buttons and add them one after the other
        for (int i = 0; i < buttonStrings.length; i++) {

            // Use JLabel instead of JButton
            buttons[i] = new JLabel(buttonStrings[i]);

            // Set the font
            int fontSize = 24;
            buttons[i].setFont(new Font("Arial", Font.BOLD, fontSize));

            final int index = i;

            // Add a mouse listener to the button
            buttons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    buttons[index].setForeground(Color.RED);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    buttons[index].setForeground(Color.BLACK);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(index, controller);
                }
            });

            // Change gbc to add the button to the panel
            gbc.gridy++;

            add(buttons[i], gbc);
        }

    }

    private void handleClick(int index, MenuController controller) {

        soundHandler.playSound("select");

        switch (index) {
            case 0 ->
                // Start game
                    controller.startGame();
            case 1 ->
                // Continue game
                    controller.continueGame();
            case 2 -> {
                // Exit game
                System.out.println("Exit game");
                System.exit(0);
            }
            default -> System.out.println("Invalid button");
        }
    }

    private void addTitle() {
        // Add the title label to the center of the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(titleLabel, gbc);
    }

    /**
     * Add a language change listener to the panel
     */
    public void addLanguageListener() {
        LanguageConfiguration config = LanguageConfiguration.getInstance();

        config.addLanguageChangeListener(e -> {
            System.out.println("Language changed");
            String[] buttonStrings = config.getOptions("MainMenu");
            for (int i = 0; i < buttonStrings.length; i++) {
                buttons[i].setText(buttonStrings[i]);
            }
        });
    }

    /**
     * Add a game state listener to the panel
     *
     * @param model The game model
     */
    public void addGameStateListener(GameModel model) {

        model.addGameStateListener(e -> setVisible(
                e.getNewValue() == GameModel.GameState.MAIN_MENU));
    }

}
