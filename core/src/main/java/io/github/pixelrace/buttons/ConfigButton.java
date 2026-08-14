package io.github.pixelrace.buttons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.pixelrace.GameMain;
import io.github.pixelrace.vehicles.clutch.ManualClutch;

import java.awt.*;

public class ConfigButton {

    private Texture buttonGas;
    private Texture buttonGasPressed;
    private Texture buttonBrake;
    private Texture buttonBrakePressed;
    private Texture buttonClutch;
    private Texture buttonClutchPressed;

    public ConfigButton() {
        this.buttonGas = new Texture(Gdx.files.internal("buttons/gas.png"));
        this.buttonGasPressed = new Texture(Gdx.files.internal("buttons/gas_pressed.png"));
        this.buttonBrake = new Texture(Gdx.files.internal("buttons/brake.png"));
        this.buttonBrakePressed = new Texture(Gdx.files.internal("buttons/brake_pressed.png"));
        this.buttonClutch = new Texture(Gdx.files.internal("buttons/clutch.png"));
        this.buttonClutchPressed = new Texture(Gdx.files.internal("buttons/clutch_pressed.png"));
    }

    public void drawButtons(SpriteBatch batch, boolean isAccelerating, boolean isBraking, boolean isClutchPressed) {
        if (isAccelerating) {
            batch.draw(buttonGasPressed, 1050, 40, 200, 285);
        } else {
            batch.draw(buttonGas, 1050, 40, 200, 285);
        }
        if (isBraking) {
            batch.draw(buttonBrakePressed, 860, 40, 165, 230);
        } else {
            batch.draw(buttonBrake, 860, 40, 165, 230);
        }
        if (isClutchPressed) {
            batch.draw(buttonClutchPressed, 50, 40, 165, 285);
        } else {
            batch.draw(buttonClutch, 50, 40, 165, 285);
        }
    }

    public void dispose() {
        if (buttonGas != null) buttonGas.dispose();
        if (buttonGasPressed != null) buttonGasPressed.dispose();
        if (buttonBrake != null) buttonBrake.dispose();
        if (buttonBrakePressed != null) buttonBrakePressed.dispose();
        if (buttonClutch != null) buttonClutch.dispose();
        if (buttonClutchPressed != null) buttonClutchPressed.dispose();
    }


}
