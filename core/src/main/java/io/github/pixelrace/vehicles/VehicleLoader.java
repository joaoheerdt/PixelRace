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

        // Tenta encontrar a pasta vehicles tanto na raiz interna quanto com prefixo assets/
        FileHandle vehiclesDir = Gdx.files.internal("vehicles");
        if (!vehiclesDir.exists()) {
            vehiclesDir = Gdx.files.internal("assets/vehicles");
        }
        if (!vehiclesDir.exists()) {
            vehiclesDir = Gdx.files.local("assets/vehicles");
        }

        if (!vehiclesDir.exists() || !vehiclesDir.isDirectory()) {
            Gdx.app.error("VehicleLoader", "Diretório de veículos não encontrado em: " + vehiclesDir.path());
            return loadedVehicles;
        }

        for (FileHandle modFolder : vehiclesDir.list()) {
            if (modFolder.isDirectory() && modFolder.name().startsWith("PixelRace_")) {
                FileHandle xmlFile = modFolder.child("vehicle.xml");

                // Suporte caso o arquivo esteja nomeado como vehicles.xml
                if (!xmlFile.exists()) {
                    xmlFile = modFolder.child("vehicles.xml");
                }

                if (xmlFile.exists()) {
                    try {
                        Vehicle vehicle = parseVehicle(modFolder, xmlFile);
                        if (vehicle != null) {
                            loadedVehicles.add(vehicle);
                            Gdx.app.log("VehicleLoader", "Mod carregado com sucesso: " + vehicle.name);
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
        int width = dimElem != null ? dimElem.getIntAttribute("width", 150) : 150;
        int height = dimElem != null ? dimElem.getIntAttribute("height", 80) : 80;
        int wheelSize = dimElem != null ? dimElem.getIntAttribute("wheelSize", 32) : 32;

        Element wheelPosElem = root.getChildByName("wheelPositions");
        int frontX = wheelPosElem != null ? wheelPosElem.getIntAttribute("frontX", 110) : 110;
        int frontY = wheelPosElem != null ? wheelPosElem.getIntAttribute("frontY", 60) : 60;
        int rearX = wheelPosElem != null ? wheelPosElem.getIntAttribute("rearX", 30) : 30;
        int rearY = wheelPosElem != null ? wheelPosElem.getIntAttribute("rearY", 60) : 60;

        Element physElem = root.getChildByName("physics");
        double mass = physElem != null ? physElem.getFloatAttribute("mass", 1000f) : 1000.0;
        double torque = physElem != null ? physElem.getFloatAttribute("baseTorque", 15f) : 15.0;
        double maxRpm = physElem != null ? physElem.getFloatAttribute("maxRpm", 6000f) : 6000.0;
        double speedMax = physElem != null ? physElem.getFloatAttribute("speedMax", 180f) : 180.0;

        Element gearElem = root.getChildByName("gears");
        double[] gearRatios = new double[]{1.0, 3.8, 2.0, 1.2, 0.9};
        if (gearElem != null && gearElem.hasAttribute("ratios")) {
            String[] ratiosStr = gearElem.getAttribute("ratios").split(",");
            gearRatios = new double[ratiosStr.length];
            for (int i = 0; i < ratiosStr.length; i++) {
                gearRatios[i] = Double.parseDouble(ratiosStr[i].trim());
            }
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
                String key = "rpm" + rpm;
                if (sndElem.hasAttribute(key)) {
                    String path = resolveSoundPath(modFolder, sndElem.getAttribute(key, null));
                    if (path != null) {
                        rpmSoundPaths.put(rpm, path);
                    }
                }
            }

            // Chamada colocada corretamente fora do loop for
            vehicle.setAudioPaths(start, stop, rpmSoundPaths, gear);
        }

        return vehicle;
    }

    private static String resolveSoundPath(FileHandle modFolder, String soundFileName) {
        if (soundFileName == null || soundFileName.isEmpty()) return null;
        FileHandle soundHandle = modFolder.child(soundFileName);
        return soundHandle.exists() ? soundHandle.path() : null;
    }
}
