package io.github.pixelrace.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.ArrayList;
import java.util.List;

public class VehicleLoader {

    public static List<Vehicle> loadAllMods() {
        List<Vehicle> loadedVehicles = new ArrayList<>();
        FileHandle vehiclesDir = Gdx.files.internal("vehicles");

        if (!vehiclesDir.exists() || !vehiclesDir.isDirectory()) {
            Gdx.app.error("VehicleLoader", "Diretório assets/vehicles não encontrado.");
            return loadedVehicles;
        }

        // Itera por todas as pastas dentro de assets/vehicles
        for (FileHandle modFolder : vehiclesDir.list()) {
            if (modFolder.isDirectory() && modFolder.name().startsWith("PixelRace_")) {
                FileHandle xmlFile = modFolder.child("vehicle.xml");

                if (xmlFile.exists()) {
                    try {
                        Vehicle vehicle = parseVehicle(modFolder, xmlFile);
                        if (vehicle != null) {
                            loadedVehicles.add(vehicle);
                            Gdx.app.log("VehicleLoader", "Mod carregado: " + vehicle.name);
                        }
                    } catch (Exception e) {
                        Gdx.app.error("VehicleLoader", "Erro ao carregar mod em " + modFolder.name(), e);
                    }
                }
            }
        }

        return loadedVehicles;
    }

    private static Vehicle parseVehicle(FileHandle modFolder, FileHandle xmlFile) {
        XmlReader reader = new XmlReader();
        Element root = reader.parse(xmlFile);

        String name = root.getAttribute("name", modFolder.name());

        Element dimElem = root.getChildByName("dimensions");
        int width = dimElem.getIntAttribute("width", 150);
        int height = dimElem.getIntAttribute("height", 80);
        int wheelSize = dimElem.getIntAttribute("wheelSize", 32);

        Element wheelPosElem = root.getChildByName("wheelPositions");
        int frontX = wheelPosElem.getIntAttribute("frontX", 110);
        int frontY = wheelPosElem.getIntAttribute("frontY", 60);
        int rearX = wheelPosElem.getIntAttribute("rearX", 30);
        int rearY = wheelPosElem.getIntAttribute("rearY", 60);

        Element physElem = root.getChildByName("physics");
        double mass = physElem.getFloatAttribute("mass", 1000f);
        double torque = physElem.getFloatAttribute("baseTorque", 15f);
        double maxRpm = physElem.getFloatAttribute("maxRpm", 6000f);
        double speedMax = physElem.getFloatAttribute("speedMax", 180f);

        Element gearElem = root.getChildByName("gears");
        String[] ratiosStr = gearElem.getAttribute("ratios").split(",");
        double[] gearRatios = new double[ratiosStr.length];
        for (int i = 0; i < ratiosStr.length; i++) {
            gearRatios[i] = Double.parseDouble(ratiosStr[i].trim());
        }

        Element textElem = root.getChildByName("textures");
        String bodyRelPath = textElem != null ? textElem.getAttribute("body", "") : "";
        String wheelRelPath = textElem != null ? textElem.getAttribute("wheel", "") : "";

        String bodyPath = !bodyRelPath.isEmpty() ? modFolder.child(bodyRelPath).path() : "";
        String wheelPath = !wheelRelPath.isEmpty() ? modFolder.child(wheelRelPath).path() : "";

        Vehicle vehicle = new Vehicle(
            name, width, height, wheelSize,
            frontX, frontY, rearX, rearY,
            mass, torque, maxRpm, speedMax, gearRatios,
            bodyPath, wheelPath
        );

        Element sndElem = root.getChildByName("sounds");
        if (sndElem != null) {
            String start = resolveSoundPath(modFolder, sndElem.getAttribute("start", null));
            String stop  = resolveSoundPath(modFolder, sndElem.getAttribute("stop", null));
            String gear  = resolveSoundPath(modFolder, sndElem.getAttribute("gear", null));

            java.util.Map<Integer, String> rpmSoundPaths = new java.util.HashMap<>();
            for (int rpm = 1000; rpm <= 10000; rpm += 1000) {
                String key = rpm + "rpm";
                if (sndElem.hasAttribute(key)) {
                    String path = resolveSoundPath(modFolder, sndElem.getAttribute(key, null));
                    if (path != null) {
                        rpmSoundPaths.put(rpm, path);
                    }
                }

            vehicle.setAudioPaths(start, stop, gear, rpmSoundPaths);
        }

        }

        return vehicle;
    }

    private static String resolveSoundPath(FileHandle modFolder, String soundFileName) {
        if (soundFileName == null || soundFileName.isEmpty()) return null;
        FileHandle soundHandle = modFolder.child(soundFileName);
        return soundHandle.exists() ? soundHandle.path() : null;
    }
}
