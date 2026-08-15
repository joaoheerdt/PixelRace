package io.github.pixelrace.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.pixelrace.vehicles.clutch.ManualClutch;
import io.github.pixelrace.vehicles.sound.EngineSound;

import java.util.Map;

public class Vehicle {
    public String name;
    protected float x, y;
    protected int width, height, wheelSize;
    protected int frontWheelX, frontWheelY, rearWheelX, rearWheelY;
    protected Texture bodyTexture, wheelTexture;
    protected double speed, currentRpm, maxRpm, baseTorque, mass, speedMax;
    protected int currentGear;
    protected double[] gearRatios;
    protected double wheelAngle;
    protected boolean isEngineOn;

    protected int volumeEfeitos = 50;
    private EngineSound engineSound;
    private ManualClutch clutch = new ManualClutch();


    public Vehicle(String name, int width, int height, int wheelSize,
                   int frontWheelX, int frontWheelY, int rearWheelX, int rearWheelY,
                   double mass, double baseTorque, double maxRpm, double speedMax, double[] gearRatios,
                   String bodyPath, String wheelPath) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.wheelSize = wheelSize;
        this.frontWheelX = frontWheelX;
        this.frontWheelY = frontWheelY;
        this.rearWheelX = rearWheelX;
        this.rearWheelY = rearWheelY;
        this.mass = mass;
        this.baseTorque = baseTorque;
        this.maxRpm = maxRpm;
        this.speedMax = speedMax;
        this.gearRatios = gearRatios;

        this.bodyTexture = new Texture(Gdx.files.internal(bodyPath));
        this.wheelTexture = new Texture(Gdx.files.internal(wheelPath));

        this.x = 50;
        this.y = 260;
        this.wheelAngle = 0;
        this.currentGear = 0;
        this.currentRpm = 0.0;
    }

    public boolean isEngineOn() {
        return isEngineOn;
    }

    public double getCurrentSpeed() {
        return this.speed;
    }

    public boolean isClutchPressed() {
        if (clutch != null) {
            return clutch.isClutchPressed();
        }
        return false;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCurrentRpm() {
        return this.currentRpm;
    }

    public void setCurrentRpm(double currentRpm) {
        this.currentRpm = currentRpm;
    }

    public int getCurrentGear() {
        return this.currentGear;
    }

    public ManualClutch getClutch() {
        return clutch;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setAudioPaths(String startPath, String stopPath, Map<Integer, String> rpmSoundPaths, String gearPath) {
        Sound startSound = startPath != null ? Gdx.audio.newSound(Gdx.files.internal(startPath)) : null;
        Sound stopSound = stopPath != null ? Gdx.audio.newSound(Gdx.files.internal(stopPath)) : null;
        Sound gearSound = gearPath != null ? Gdx.audio.newSound(Gdx.files.internal(gearPath)) : null;
        Sound[] rpmSoundsArray = new Sound[11];

        for (Map.Entry<Integer, String> entry : rpmSoundPaths.entrySet()) {
            int rpmValue = entry.getKey();
            String path = entry.getValue();
            int index = rpmValue / 1000;

            if (index >= 1 && index <= 10) {
                rpmSoundsArray[index] = Gdx.audio.newSound(Gdx.files.internal(path));
            }
        }

        this.engineSound = new EngineSound(startSound, stopSound, gearSound, rpmSoundsArray);
    }

    public void setVolumeEfeitos(int volume) {
        this.volumeEfeitos = MathUtils.clamp(volume, 0, 100);
    }

    public void toggleEngine() {
        if (this.isEngineOn) {
            if (engineSound != null) engineSound.stopEngine();
            this.isEngineOn = false;
            this.speed = 0.0;
        } else {
            this.isEngineOn = true;
            this.currentRpm = 1000;
            this.speed = 0;
            if (engineSound != null) engineSound.startEngine();

        }
    }

    public void changeGear(int newGear) {
        if (newGear >= 0 && newGear < gearRatios.length && clutch.isClutchPressed()) {
            double oldRatio = (currentGear == 0) ? 1.0 : gearRatios[currentGear];
            double newRatio = (newGear == 0) ? 1.0 : gearRatios[newGear];
            this.currentRpm *= (newRatio / oldRatio);
            this.currentGear = newGear;

            if (engineSound != null) engineSound.gearChance();
        }
    }

    public void stallEngine() {
        this.isEngineOn = false;
        this.currentRpm = 0.0;
        this.speed = 0.0;
        if (engineSound != null) engineSound.stopEngine();
    }

    public void updatePhysics(boolean isAccelerating, boolean isBraking) {
        clutch.updatePhysics(this, isAccelerating, isBraking);

        if (!this.isEngineOn()) isAccelerating = false;

        if (this.isEngineOn() && isAccelerating && this.speed < 0.5 && this.currentGear > 1 && !clutch.isClutchPressed()) {
            stallEngine();
            return;
        }
        if (this.isEngineOn && this.currentRpm == 1000 && this.currentGear > 1) {
            stallEngine();
            return;
        }

        if (currentGear == 0 || clutch.isClutchPressed()) {
            if (isAccelerating) this.currentRpm += 120.0;
            else if (isBraking) this.currentRpm -= 80.0;
            else this.currentRpm -= 40.0;
            this.speed *= 0.99;
        } else {
            double gearRatio = gearRatios[currentGear];
            double targetRpm = (this.speed / 0.015) * gearRatio;

            if (Math.abs(this.currentRpm - targetRpm) > 500) {
                this.currentRpm = MathUtils.lerp((float) this.currentRpm, (float) targetRpm, 0.4f);
            }
            if (isAccelerating) {
                double engineForce = (baseTorque * 30 * gearRatio) * getTorqueFactor();
                double dragForce = 0.42 * (this.speed * this.speed);
                this.currentRpm += ((engineForce - dragForce) / mass);
            } else {
                this.currentRpm -= this.isEngineOn() ? 25 : 50;
            }
            this.speed = (this.currentRpm / gearRatio) * 0.015;
        }

        double minRpm = this.isEngineOn() ? 1000.0 : 0.0;
        this.currentRpm = MathUtils.clamp((float) this.currentRpm, (float) minRpm, (float) maxRpm);
        this.wheelAngle += this.speed * 0.04;

        if (engineSound != null) engineSound.rpm((int) currentRpm);
    }

    public void draw(SpriteBatch batch, float virtualHeight) {
        float bodyDrawY = virtualHeight - y - height;
        batch.draw(bodyTexture, x, bodyDrawY, width, height);
        float angleDeg = (float) Math.toDegrees(-wheelAngle);
        batch.draw(wheelTexture, x + frontWheelX - wheelSize / 2f, virtualHeight - (y + frontWheelY) - wheelSize / 2f, wheelSize / 2f, wheelSize / 2f, wheelSize, wheelSize, 1f, 1f, angleDeg, 0, 0, wheelTexture.getWidth(), wheelTexture.getHeight(), false, false);
        batch.draw(wheelTexture, x + rearWheelX - wheelSize / 2f, virtualHeight - (y + rearWheelY) - wheelSize / 2f, wheelSize / 2f, wheelSize / 2f, wheelSize, wheelSize, 1f, 1f, angleDeg, 0, 0, wheelTexture.getWidth(), wheelTexture.getHeight(), false, false);
    }

    public void dispose() {
        if (bodyTexture != null) bodyTexture.dispose();
        if (wheelTexture != null) wheelTexture.dispose();
        if (engineSound != null) engineSound.dispose();
    }


    public double getTorqueFactor() {
        double faixaIdeal = maxRpm * 0.6;
        if (currentRpm < faixaIdeal) return 1.0;
        if (currentRpm > maxRpm) return 0.4;
        return 1.0 - (((currentRpm - faixaIdeal) / (maxRpm - faixaIdeal)) * 0.6);
    }

    public int getWidth() {
        return this.width;
    }

    public void stopEngineSounds() {
        if (engineSound != null) {
            engineSound.stopEngine();
        }
    }
}
