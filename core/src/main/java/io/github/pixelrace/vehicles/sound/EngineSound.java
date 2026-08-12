package io.github.pixelrace.vehicles.sound;

import com.badlogic.gdx.audio.Sound;

public class EngineSound implements VehicleSound {
    private Sound startEngine;
    private Sound stopEngine;
    private Sound gearChance;
    private Sound[] rpmSound;
    private int currentActiveFaixa = 0;

    public EngineSound(Sound startEngine, Sound stopEngine, Sound gearChance, Sound[] rpmSound) {
        this.startEngine = startEngine;
        this.stopEngine = stopEngine;
        this.gearChance = gearChance;
        this.rpmSound = rpmSound;
    }

    @Override
    public void startEngine() {
        if (startEngine != null) {
            startEngine.play();
        }
    }

    @Override
    public void stopEngine() {
        if (currentActiveFaixa > 0 && currentActiveFaixa < rpmSound.length && rpmSound[currentActiveFaixa] != null) {
            rpmSound[currentActiveFaixa].stop();
            currentActiveFaixa = 0;
        }
        if (stopEngine != null) {
            stopEngine.play();
        }
    }

    @Override
    public void gearChance() {
        if (gearChance != null) {
            gearChance.play();
        }
    }

    @Override
    public void rpm(int currentRPM) {
        int targetFaixa = currentRPM / 1000;

        if (targetFaixa > 10) {
            targetFaixa = 10;
        }

        if (targetFaixa < 1) {
            if (currentActiveFaixa != 0 && currentActiveFaixa < rpmSound.length && rpmSound[currentActiveFaixa] != null) {
                rpmSound[currentActiveFaixa].stop();
                currentActiveFaixa = 0;
            }
            return;
        }

        if (targetFaixa != currentActiveFaixa) {
            if (currentActiveFaixa > 0 && currentActiveFaixa < rpmSound.length && rpmSound[currentActiveFaixa] != null) {
                rpmSound[currentActiveFaixa].stop();
            }

            if (targetFaixa < rpmSound.length && rpmSound[targetFaixa] != null) {
                rpmSound[targetFaixa].loop();
            }

            currentActiveFaixa = targetFaixa;
        }
    }

    public void dispose() {
        if (startEngine != null) startEngine.dispose();
        if (stopEngine != null) stopEngine.dispose();
        if (gearChance != null) gearChance.dispose();

        if (rpmSound != null) {
            for (Sound sound : rpmSound) {
                if (sound != null) {
                    sound.dispose();
                }
            }
        }
    }
}
