package inheritamon.model.data;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import inheritamon.model.npcs.moves.NormalAbility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Jeremias
 * A class to handle and load data with the appropriate exceptions
 * We use csv files to store certain data regarding the game
 * Singleton class
 */
public final class DataHandler {

    // Singleton pattern
    private static DataHandler dataHandler;
    /**
     * Details regarding the parameters of each character like defense, attack, etc.
     */
    private final HashMap<String, HashMap<String, String>> characterData =
            new HashMap<>();
    /**
     * Details regarding the parameters of each move like modifier, cost, etc.
     */
    private final HashMap<String, HashMap<String, String>> moveData =
            new HashMap<>();
    /**
     * Details regarding the parameters of each item like type, effectiveness, etc.
     */
    private final HashMap<String, HashMap<String, String>> itemData =
            new HashMap<>();
    /**
     * The sprites for each pokemon, front and back.
     */
    private final HashMap<String, HashMap<String, BufferedImage>>
            characterSprites = new HashMap<>();
    /**
     * Icons used in the menu.
     */
    private final HashMap<String, BufferedImage> icons = new HashMap<>();
    /**
     * The background images for the battles.
     */
    private final HashMap<String, BufferedImage> battleBackgrounds =
            new HashMap<>();
    /**
     * The tiles used in the map.
     */
    private final HashMap<String, BufferedImage> tiles = new HashMap<>();
    /**
     * The textures for the player.
     */
    private final HashMap<String, BufferedImage> characterTextures =
            new HashMap<>();
    /**
     * The descriptions in each language for menu elements.
     */
    private final HashMap<String, HashMap<String, String>> languageData =
            new HashMap<>();
    /**
     * The sprites for each item.
     */
    private final HashMap<String, BufferedImage> inventorySprites =
            new HashMap<>();
    /**
     * The sprites for each object on the map.
     */
    private final HashMap<String, BufferedImage> objectSprites =
            new HashMap<>();
    /**
     * The sounds that are played in the game.
     */
    private final HashMap<String, AudioInputStream> sounds = new HashMap<>();
    /**
     * The music that is played in the game.
     */
    private final HashMap<String, AudioInputStream> music = new HashMap<>();

    /**
     * The constructor for the DataHandler class.
     */
    private DataHandler() {
        loadAllData();
    }

    /**
     * Get an instance of the DataHandler class.
     *
     * @return the DataHandler instance
     */
    public static DataHandler getInstance() {
        if (dataHandler == null) {
            dataHandler = new DataHandler();
        }
        return dataHandler;
    }

    // Obtained from
    // https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java

    /**
     * Check if a string is numeric.
     *
     * @param str The string to check
     * @return Whether the string is numeric
     */
    public static boolean isNumeric(String str) {
        return str.matches(
                "-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
    }

    /**
     * Converts a string of moves into an ArrayList of moves
     *
     * @param nonFormattedString The string of moves
     * @return An ArrayList of moves
     */
    public static ArrayList<String> convertMoveSetToString(
            String nonFormattedString) {

        ArrayList<String> moveSet =
                new ArrayList<>(Arrays.asList(nonFormattedString.split(";")));

        return new ArrayList<>(moveSet);
    }

    private void loadAllData() {

        loadData(characterData, "monster_stats.csv");
        loadData(moveData, "move_stats.csv");
        loadData(itemData, "items.csv");
        loadData(languageData, "languages.csv");
        loadCharacterImages();
        loadGeneralImages(icons, "icons");
        loadGeneralImages(battleBackgrounds, "battleBackgrounds");
        loadGeneralImages(tiles, "tiles");
        loadGeneralImages(characterTextures, "characterTextures");
        loadGeneralImages(inventorySprites, "inventorySprites");
        loadGeneralImages(objectSprites, "objects");
        loadAudio(sounds, "sounds");
        loadAudio(music, "music");

    }

    private void loadData(HashMap<String, HashMap<String, String>> data,
                          String fileName) {

        ArrayList<String> attributes = new ArrayList<>();

        // Try with resources to automatically close the scanner
        try (Scanner characterDataScanner = new Scanner(Objects.requireNonNull(
                DataHandler.class.getResourceAsStream("/" + fileName)))) {

            // Read the first line of the file and put the attributes into the attributes
            // ArrayList
            String firstLine = characterDataScanner.nextLine();
            String[] firstLineSplit = firstLine.split(",");
            attributes.addAll(Arrays.asList(firstLineSplit));

            // Loop through the rest of the file and put the data into the correct HashMap
            while (characterDataScanner.hasNextLine()) {
                String line = characterDataScanner.nextLine();
                String[] lineSplit = line.split(",");
                HashMap<String, String> characterDataEntry = new HashMap<>();
                for (int i = 0; i < lineSplit.length; i++) {
                    characterDataEntry.put(attributes.get(i), lineSplit[i]);
                }
                data.put(lineSplit[0], characterDataEntry);
            }


        } catch (NullPointerException e) {
            System.out.println("File not found");
            Runtime.getRuntime().halt(0);
        }

    }

    // Images are obtained from Pokemon
    private void loadCharacterImages() {

        System.out.println(characterData.keySet());

        try {
            // Go through all the keys in the characterData HashMap and load the images
            for (String characterName : characterData.keySet()) {
                HashMap<String, BufferedImage> characterImagesEntry =
                        new HashMap<>();
                characterImagesEntry.put("front", ImageIO.read(
                        Objects.requireNonNull(DataHandler.class.getResource(
                                "/battleSprites/" + characterName + ".png"))));
                characterImagesEntry.put("back", ImageIO.read(
                        Objects.requireNonNull(DataHandler.class.getResource(
                                "/battleSprites/" + characterName +
                                        "Back.png"))));
                characterSprites.put(characterName, characterImagesEntry);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private void loadGeneralImages(HashMap<String, BufferedImage> images,
                                   String folderName) {

        try {
            // Get all file names in the icons folder using File
            File iconsFolder = new File(Objects.requireNonNull(
                            DataHandler.class.getResource("/" + folderName + "/"))
                    .toURI());
            File[] iconFiles = iconsFolder.listFiles();

            // Load each icon into the icons hashmap
            assert iconFiles != null;
            for (File iconFile : iconFiles) {
                String iconName = iconFile.getName().replace(".png", "");
                BufferedImage iconImage = ImageIO.read(iconFile);
                images.put(iconName, iconImage);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void loadAudio(HashMap<String, AudioInputStream> audios,
                           String folderName) {
        try {
            // Get the URL of the folder containing the audio files
            URL folderUrl = getClass().getResource("/" + folderName + "/");

            // Create a file object from the folder URL
            assert folderUrl != null;
            File folder = new File(folderUrl.toURI());

            // Get a list of all the files in the folder
            File[] files = folder.listFiles();

            // Load each audio file into the HashMap
            assert files != null;
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".wav")) {
                    // Get the name of the audio file without the extension
                    String name = file.getName()
                            .substring(0, file.getName().lastIndexOf("."));

                    // Load the audio file into an AudioInputStream
                    AudioInputStream audioStream =
                            AudioSystem.getAudioInputStream(file);

                    // Add the audio stream to the HashMap
                    audios.put(name, audioStream);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the state of the game
     *
     * @param data     The object to save
     * @param fileName The name of the file to save to
     */
    public void saveState(Object data, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(data);
            out.close();
            fileOut.close();
            System.out.println(
                    "Serialized data is saved in " + fileName + ".ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the state of the game
     *
     * @param fileName The name of the file to load from
     * @return The object that was loaded
     */
    public Object loadState(String fileName) {

        try {
            FileInputStream fileIn = new FileInputStream(fileName + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object data = in.readObject();
            in.close();
            fileIn.close();
            System.out.println(
                    "Serialized data is loaded from " + fileName + ".ser");
            return data;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(
                    fileName + "File is corrupted or does not exist");
        }

        return null;

    }

    private HashMap<String, String> getData(
            HashMap<String, HashMap<String, String>> dataMap, String dataName,
            String dataType) {
        try {
            return new HashMap<>(dataMap.get(dataName));
        } catch (NullPointerException e) {
            System.out.println(dataType + " not found");

            // Exit the program, hopefully
            Runtime.getRuntime().halt(0);
            return null;
        }
    }

    /**
     * Gets the data of a specific pokemon
     *
     * @param characterName The name of the pokemon
     * @return The data of the pokemon as a HashMap
     */
    public HashMap<String, String> getPokemonData(String characterName) {
        return getData(characterData, characterName, "Character");
    }

    /**
     * The names of all the pokemon in the game
     *
     * @return The names of all the pokemon
     */
    public String[] getPokemonNames() {
        return characterData.keySet().toArray(new String[0]);
    }

    /**
     * Gets the data of a specific move
     *
     * @param moveName The name of the move
     * @return The data of the move as a HashMap
     */
    public HashMap<String, String> getMoveData(String moveName) {
        return getData(moveData, moveName, "Move");
    }

    /**
     * Gets the data of a specific item
     *
     * @param itemName The name of the item
     * @return The data of the item as a HashMap
     */
    public HashMap<String, String> getItemData(String itemName) {
        return getData(itemData, itemName, "Item");
    }

    /**
     * Gets the data of all language configurations of the menus
     *
     * @return The data of all menus
     */
    public HashMap<String, HashMap<String, String>> getLanguageData() {
        return new HashMap<>(languageData);
    }

    /**
     * Converts the data of all the moves into an ArrayList of move objects
     *
     * @return The data of the moves as an Ability
     */
    public HashMap<String, NormalAbility> getAllAbilities() {

        HashMap<String, NormalAbility> abilities = new HashMap<>();

        for (String moveName : moveData.keySet()) {
            HashMap<String, String> moveData = getMoveData(moveName);
            NormalAbility ability = new NormalAbility(moveData);
            abilities.put(moveName, ability);
        }

        return new HashMap<>(abilities);
    }

    /**
     * Gets the sprites of entities that are displayed on the map
     *
     * @return The sprites of entities that are displayed on the map
     */
    public HashMap<String, HashMap<String, BufferedImage>> getAllCharacterSprites() {
        return characterSprites;
    }

    /**
     * Gets the two sprites of a specific pokemon
     *
     * @param pokemonName The name of the pokemon
     * @return The sprites of the pokemon
     */
    public HashMap<String, BufferedImage> getPokemonSprite(String pokemonName) {
        return characterSprites.get(pokemonName);
    }

    /**
     * Gets the icons of certain UI elements
     *
     * @return The icons of certain UI elements
     */
    public HashMap<String, BufferedImage> getIcons() {
        return icons;
    }

    private <T> T getImage(Map<String, T> imageMap, String imageName,
                           String imageType) {
        T image = imageMap.get(imageName);
        if (image == null) {
            throw new IllegalArgumentException(
                    imageType + " not found: " + imageName);
        }
        return image;
    }

    /**
     * Gets the background of a specific battle
     *
     * @param backgroundName The name of the background
     * @return The background of the battle
     */
    public BufferedImage getBackground(String backgroundName) {
        return getImage(battleBackgrounds, backgroundName, "Background");
    }

    /**
     * Gets the image of a specific tile
     *
     * @param tileName The name of the tile
     * @return The image of the tile
     */
    public BufferedImage getTileImage(String tileName) {
        return getImage(tiles, tileName, "Tile");
    }

    /**
     * Gets a specific texture of a character displayed on the map
     *
     * @param textureName The name of the texture
     * @return The texture of the character
     */
    public BufferedImage getCharacterTexture(String textureName) {
        return getImage(characterTextures, textureName, "Texture");
    }

    /**
     * Gets the sprite of a specific item
     *
     * @param spriteName The name of the item
     * @return The sprite of the item
     */
    public BufferedImage getItemSprite(String spriteName) {
        return getImage(inventorySprites, spriteName, "Inventory Sprite");
    }

    /**
     * Gets the sprite of a specific object
     *
     * @param spriteName The name of the object
     * @return The sprite of the object
     */
    public BufferedImage getObjectSprite(String spriteName) {
        return getImage(objectSprites, spriteName, "Object Sprite");
    }

    /**
     * Gets all audio files of a specific type
     *
     * @param audioType The type of audio files
     * @return All audio files of a specific type
     */
    public HashMap<String, AudioInputStream> getAudios(String audioType) {

        if (audioType.equals("Music")) {
            return music;
        } else if (audioType.equals("Sounds")) {
            return sounds;
        } else {
            throw new IllegalArgumentException(
                    "Audio type not found: " + audioType);
        }

    }

}