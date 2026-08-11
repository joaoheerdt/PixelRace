package io.github.pixelrace.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameMap {

    private final Texture mapTexture;
    private double mapMovement;
    private final int mapWidth;
    private final int mapHeight;

    public GameMap(String imagePath) {
        this.mapTexture = new Texture(Gdx.files.internal(imagePath));
        this.mapMovement = 0;

        this.mapWidth = mapTexture.getWidth();
        this.mapHeight = mapTexture.getHeight();
    }

    public void update(double vehicleSpeed) {
        mapMovement -= (vehicleSpeed * 0.2);

        if (mapMovement <= -mapWidth) {
            mapMovement += mapWidth;
        }
    }

    public void draw(SpriteBatch batch, float virtualWidth, float virtualHeight) {
        double visualOffset = mapMovement % mapWidth;

        for (float posX = -mapWidth; posX < virtualWidth + mapWidth; posX += mapWidth) {
            batch.draw(mapTexture, (float) (posX + visualOffset), 0, mapWidth, virtualHeight);
        }
    }

    public void dispose() {
        mapTexture.dispose();
    }
}
