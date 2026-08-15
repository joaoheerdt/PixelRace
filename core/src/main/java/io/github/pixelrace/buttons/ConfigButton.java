package io.github.pixelrace.buttons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.pixelrace.vehicles.Vehicle;

public class ConfigButton {

    private Texture buttonGas, buttonGasPressed;
    private Texture buttonBrake, buttonBrakePressed;
    private Texture buttonClutch, buttonClutchPressed;

    private final Rectangle boundsGas = new Rectangle(1050, 20, 180, 256);
    private final Rectangle boundsBrake = new Rectangle(860, 20, 148, 207);
    private final Rectangle boundsClutch = new Rectangle(50, 20, 148, 256);

    private final Vector2 touchPos = new Vector2();

    public ConfigButton() {
        this.buttonGas = new Texture(Gdx.files.internal("buttons/gas.png"));
        this.buttonGasPressed = new Texture(Gdx.files.internal("buttons/gas_pressed.png"));
        this.buttonBrake = new Texture(Gdx.files.internal("buttons/brake.png"));
        this.buttonBrakePressed = new Texture(Gdx.files.internal("buttons/brake_pressed.png"));
        this.buttonClutch = new Texture(Gdx.files.internal("buttons/clutch.png"));
        this.buttonClutchPressed = new Texture(Gdx.files.internal("buttons/clutch_pressed.png"));
    }


    public void processInputs(Viewport viewport, Vehicle activeVehicle, TouchState touchState) {
        boolean touchGas = false;
        boolean touchBrake = false;
        boolean touchClutch = false;

        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i));
                viewport.unproject(touchPos);

                if (boundsGas.contains(touchPos.x, touchPos.y)) touchGas = true;
                if (boundsBrake.contains(touchPos.x, touchPos.y)) touchBrake = true;
                if (boundsClutch.contains(touchPos.x, touchPos.y)) touchClutch = true;
            }
        }

        touchState.isAccelerating = Gdx.input.isKeyPressed(Keys.D) || touchGas;
        touchState.isBraking = Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.S) || touchBrake;

        if (activeVehicle != null && activeVehicle.getClutch() != null) {
            boolean isClutchActive = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || touchClutch;
            activeVehicle.getClutch().setClutchPressed(isClutchActive);
        }
    }

    public void drawButtons(SpriteBatch batch, boolean isAccelerating, boolean isBraking, boolean isClutchPressed) {
        batch.draw(isAccelerating ? buttonGasPressed : buttonGas, boundsGas.x, boundsGas.y, boundsGas.width, boundsGas.height);
        batch.draw(isBraking ? buttonBrakePressed : buttonBrake, boundsBrake.x, boundsBrake.y, boundsBrake.width, boundsBrake.height);
        batch.draw(isClutchPressed ? buttonClutchPressed : buttonClutch, boundsClutch.x, boundsClutch.y, boundsClutch.width, boundsClutch.height);
    }

    public void dispose() {
        if (buttonGas != null) buttonGas.dispose();
        if (buttonGasPressed != null) buttonGasPressed.dispose();
        if (buttonBrake != null) buttonBrake.dispose();
        if (buttonBrakePressed != null) buttonBrakePressed.dispose();
        if (buttonClutch != null) buttonClutch.dispose();
        if (buttonClutchPressed != null) buttonClutchPressed.dispose();
    }
    public static class TouchState {
        public boolean isAccelerating;
        public boolean isBraking;
    }
}
