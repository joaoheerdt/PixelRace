package io.github.pixelrace.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.Map;
import java.util.TreeMap;

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

    private Sound engineStartSound, engineStopSound, engineGearSound;
    private Sound engineIdleSound, engineDrivingSound;
    private long idleSoundId = -1;
    private long drivingSoundId = -1;

    protected String engineStartSoundPath;
    protected String engineStopSoundPath;
    protected TreeMap<Integer, Sound> rpmSounds = new TreeMap<>();

    protected String soundGearPath;

    protected int volumeEfeitos = 50;

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
        this.currentGear = 1;
        this.currentRpm = 0.0;
    }

    public void setAudioPaths(String start, String stop, Map<Integer, String> rpmPaths, String gear) {
        this.engineStartSoundPath = start;
        this.engineStopSoundPath = stop;
        this.soundGearPath = gear;

        for (Sound sound : this.rpmSounds.values()) {
            if (sound != null) sound.dispose();
        }
        this.rpmSounds.clear();

        if (rpmPaths != null) {
            for (Map.Entry<Integer, String> entry : rpmPaths.entrySet()) {
                Sound sound = loadSound(entry.getValue());
                if (sound != null) {
                    this.rpmSounds.put(entry.getKey(), sound);
                }
            }
        }

        if (start != null && !start.isEmpty()) engineStartSound = loadSound(start);
        if (stop != null && !stop.isEmpty()) engineStopSound = loadSound(stop);
        if (gear != null && !gear.isEmpty()) engineGearSound = loadSound(gear);
    }

    private Sound loadSound(String path) {
        try {
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) {
                Gdx.app.error("Vehicle", "Arquivo de som não encontrado: " + path);
                return null;
            }
            return Gdx.audio.newSound(fh);
        } catch (Exception e) {
            Gdx.app.error("Vehicle", "Erro ao carregar som: " + path, e);
            return null;
        }
    }

    public void setVolumeEfeitos(int volume) {
        this.volumeEfeitos = MathUtils.clamp(volume, 0, 100);
        updateEngineSound();
    }

    public int getWidth() {
        return this.width;
    }

    public boolean isEngineOn() {
        return isEngineOn;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void toggleEngine() {
        if (this.isEngineOn) {
            if (engineStopSound != null) engineStopSound.play(getLinearVolume());
            stopEngineSounds();
            this.isEngineOn = false;
        } else {
            this.isEngineOn = true;
            if (engineStartSound != null) {
                engineStartSound.play(getLinearVolume());
            }
            startEngineSounds();
        }
    }

    private float getLinearVolume() {
        return volumeEfeitos / 100f;
    }

    private void startEngineSounds() {
        if (engineIdleSound != null) {
            idleSoundId = engineIdleSound.loop(getLinearVolume());
        }
    }

    public void stopEngineSounds() {
        if (engineIdleSound != null && idleSoundId != -1) {
            engineIdleSound.stop(idleSoundId);
            idleSoundId = -1;
        }
        if (engineDrivingSound != null && drivingSoundId != -1) {
            engineDrivingSound.stop(drivingSoundId);
            drivingSoundId = -1;
        }
    }

    public void updateEngineSound() {
        if (!isEngineOn) return;

        boolean isMoving = this.speed > 0.1;
        float pitch = MathUtils.clamp((float) (currentRpm / 1000.0), 0.5f, 2.0f);
        float volume = getLinearVolume();

        if (isMoving) {
            if (engineIdleSound != null && idleSoundId != -1) {
                engineIdleSound.stop(idleSoundId);
                idleSoundId = -1;
            }
            if (engineDrivingSound != null && drivingSoundId == -1) {
                drivingSoundId = engineDrivingSound.loop(volume);
            }
            if (engineDrivingSound != null && drivingSoundId != -1) {
                engineDrivingSound.setVolume(drivingSoundId, volume);
                engineDrivingSound.setPitch(drivingSoundId, pitch);
            }
        } else {
            if (engineDrivingSound != null && drivingSoundId != -1) {
                engineDrivingSound.stop(drivingSoundId);
                drivingSoundId = -1;
            }
            if (engineIdleSound != null && idleSoundId == -1) {
                idleSoundId = engineIdleSound.loop(volume);
            }
            if (engineIdleSound != null && idleSoundId != -1) {
                engineIdleSound.setVolume(idleSoundId, volume);
                engineIdleSound.setPitch(idleSoundId, pitch);
            }
        }
    }

    public void changeGear(int newGear) {
        if (newGear >= 0 && newGear < gearRatios.length) {
            double oldRatio = (currentGear == 0) ? 1.0 : gearRatios[currentGear];
            double newRatio = (newGear == 0) ? 1.0 : gearRatios[newGear];
            this.currentRpm *= (newRatio / oldRatio);
            this.currentGear = newGear;

            if (engineGearSound != null) {
                engineGearSound.play(getLinearVolume());
            }
        }
    }

    public double getTorqueFactor() {
        double faixaIdeal = maxRpm * 0.6;
        if (currentRpm < faixaIdeal) return 1.0;
        if (currentRpm > maxRpm) return 0.4;
        double progresso = (currentRpm - faixaIdeal) / (maxRpm - faixaIdeal);
        return 1.0 - (progresso * 0.6);
    }

    public void stallEngine() {
        this.isEngineOn = false;
        this.currentRpm = 0.0;
        this.speed = 0.0;

        stopEngineSounds();

        if (engineStopSound != null) {
            engineStopSound.play(getLinearVolume());
        }

        Gdx.app.log("Vehicle", "O motor apagou! Arrancada em " + currentGear + "ª marcha.");
    }

    public void updatePhysics(boolean isAccelerating, boolean isBraking) {
        if (!this.isEngineOn()) isAccelerating = false;

        if (this.isEngineOn() && isAccelerating && this.speed < 0.5 && this.currentGear > 1) {
            stallEngine();
            return;
        }

        if (currentGear == 0) {
            if (isAccelerating) this.currentRpm += 120.0;
            else if (isBraking) this.currentRpm -= 80.0;
            else this.currentRpm -= 40.0;

            double rpmMinimo = this.isEngineOn() ? 1000.0 : 0.0;
            if (currentRpm < rpmMinimo) this.currentRpm = rpmMinimo;
            if (currentRpm > maxRpm) this.currentRpm = maxRpm;

            this.speed -= 0.05;
            if (isBraking) this.speed -= 1.0;
            if (this.speed < 0) this.speed = 0;

        } else {
            double gearRatio = gearRatios[currentGear];

            if (isAccelerating) {
                double engineForce = (baseTorque * 30 * gearRatio) * getTorqueFactor();
                double dragForce = 0.42 * (this.speed * this.speed);
                double netForce = engineForce - dragForce;
                if (netForce < 0) netForce = 0;

                this.currentRpm += (netForce / mass);
            } else if (isBraking) {
                this.currentRpm -= 80;
            } else {
                double queda = this.isEngineOn() ? 25 : 50;
                this.currentRpm -= queda;
            }

            double rpmMinimo = this.isEngineOn() ? 1000.0 : 0.0;
            if (currentRpm < rpmMinimo) this.currentRpm = rpmMinimo;
            if (currentRpm > maxRpm) this.currentRpm = maxRpm;

            if (this.isEngineOn() && this.currentRpm <= 1000.0 && !isAccelerating) {
                this.speed = 0;
            } else {
                this.speed = (this.currentRpm / gearRatio) * 0.015;
            }
        }

        this.wheelAngle += this.speed * 0.04;
        this.updateEngineSound();
    }
    public void draw(SpriteBatch batch, float virtualHeight) {
        float bodyDrawY = virtualHeight - y - height;
        batch.draw(bodyTexture, x, bodyDrawY, width, height);

        float angleDeg = (float) Math.toDegrees(-wheelAngle);

        float rearCenterX = x + rearWheelX;
        float rearCenterY = virtualHeight - (y + rearWheelY);
        batch.draw(wheelTexture,
                rearCenterX - wheelSize / 2f, rearCenterY - wheelSize / 2f,
                wheelSize / 2f, wheelSize / 2f,
                wheelSize, wheelSize,
                1f, 1f, angleDeg,
                0, 0, wheelTexture.getWidth(), wheelTexture.getHeight(),
                false, false);

        float frontCenterX = x + frontWheelX;
        float frontCenterY = virtualHeight - (y + frontWheelY);
        batch.draw(wheelTexture,
                frontCenterX - wheelSize / 2f, frontCenterY - wheelSize / 2f,
                wheelSize / 2f, wheelSize / 2f,
                wheelSize, wheelSize,
                1f, 1f, angleDeg,
                0, 0, wheelTexture.getWidth(), wheelTexture.getHeight(),
                false, false);
    }

    public void dispose() {
        if (bodyTexture != null) bodyTexture.dispose();
        if (wheelTexture != null) wheelTexture.dispose();
        if (engineStartSound != null) engineStartSound.dispose();
        if (engineStopSound != null) engineStopSound.dispose();
        if (engineGearSound != null) engineGearSound.dispose();
        if (engineIdleSound != null) engineIdleSound.dispose();
        if (engineDrivingSound != null) engineDrivingSound.dispose();
    }

    public double getCurrentSpeed() {
        return this.speed;
    }

    public double getCurrentRpm() {
        return this.currentRpm;
    }

    public int getCurrentGear() {
        return this.currentGear;
    }
}
