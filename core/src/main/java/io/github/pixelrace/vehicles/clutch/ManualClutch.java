package io.github.pixelrace.vehicles.clutch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.pixelrace.vehicles.Vehicle;

public class ManualClutch {
    private boolean isClutchPressed = false;
    private int clutchKey = Input.Keys.SHIFT_LEFT;

    public boolean isClutchPressed() {
        return isClutchPressed;
    }

    public void setClutchPressed(boolean pressed) {
        this.isClutchPressed = pressed;
    }

    public void updatePhysics(Vehicle vehicle, boolean isAccelerating, boolean isBraking) {
        if (Gdx.input.isKeyPressed(clutchKey)) {
            this.isClutchPressed = true;
        }

        double rpm = vehicle.getCurrentRpm();
        if (isClutchPressed) {
            if (isAccelerating) {
                rpm += 120;
            } else if (isBraking) {
                rpm -= 80;
            } else {
                rpm -= 40;
            }
            vehicle.setCurrentRpm(rpm);
            vehicle.setSpeed(vehicle.getCurrentSpeed() * 0.99);
        }
    }
}
