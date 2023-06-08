package inheritamon.model.tile;

import inheritamon.model.data.DataHandler;
import inheritamon.view.world.WorldPanel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileManager {

    WorldPanel gp;
    public Tile[] tile;
    public int mapTileNum[][];

    public TileManager(WorldPanel gp) {

        this.gp = gp;

        tile = new Tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/maps/map01.txt");
    }

    public void getTileImage() {

        DataHandler dh = DataHandler.getInstance();


        tile[0] = new Tile();
        tile[0].image = dh.getTileImage("Pass");

        tile[1] = new Tile();
        tile[1].image = dh.getTileImage("NotPass");
        tile[1].collision = true;

        tile[2] = new Tile();
        tile[2].image = dh.getCharacterTexture("Front 1");
        tile[2].collision = true;

    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while(row < gp.maxWorldRow) {

                String line = br.readLine();
                String numbers[] = line.split(" ");

                col = 0;  // Reset column index for each row
                while(col < gp.maxWorldCol && col < numbers.length) {
                    int num = Integer.parseInt(numbers[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }

                row++;
            }
            br.close();
        } catch (Exception e) {

        }
    }

    public void draw(Graphics2D g2) {
        //2.drawImage(tile[0].image, 0, 0, gp.tileSize, gp.tileSize, null);
        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            //Improve game performance
            // if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX && worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            //         worldY + gp.tileSize > gp.player.worldY - gp.player.screenY && worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            // }

            g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);

            worldCol++;

            if(worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }

    }
}