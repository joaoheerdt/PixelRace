package io.github.pixelrace.vehicles.sound;

public interface VehicleSound {
    void startEngine();
    void stopEngine();
    void gearChance();
    void rpm(int currentRPM);
}
