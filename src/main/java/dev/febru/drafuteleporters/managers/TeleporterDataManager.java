package dev.febru.drafuteleporters.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TeleporterDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "regions.json";

    // Static list of all teleporters, always kept up to date
    private static List<TeleporterData> teleporters = new ArrayList<>();
    private static boolean isLoaded = false;

    public static class TeleporterData {
        public String name;
        public BlockPos pos1;
        public BlockPos pos2;

        public TeleporterData(String name, BlockPos pos1, BlockPos pos2) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public TeleporterData() {}
    }

    private static File getConfigFile() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        return new File(configDir, FILE_NAME);
    }

    // Ensure teleporters list is loaded
    private static void ensureLoaded() {
        if (!isLoaded) {
            teleporters = loadRegionsFromFile();
            isLoaded = true;
        }
    }

    // Get the static list of all teleporters
    public static List<TeleporterData> getAllTeleporters() {
        ensureLoaded();
        return new ArrayList<>(teleporters); // Return copy for safety
    }

    public static void saveRegions(List<TeleporterData> regions) {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(regions, writer);
            // Update static list
            teleporters = new ArrayList<>(regions);
            isLoaded = true;
        } catch (IOException e) {
            System.err.println("Failed to save regions: " + e.getMessage());
        }
    }

    // Load regions from file (renamed to avoid confusion)
    private static List<TeleporterData> loadRegionsFromFile() {
        File file = getConfigFile();

        if (!file.exists())
            return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<TeleporterData>>(){}.getType();
            List<TeleporterData> regions = GSON.fromJson(reader, listType);
            return regions != null ? regions : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load regions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Public method to load regions (updates static list)
    public static List<TeleporterData> loadRegions() {
        teleporters = loadRegionsFromFile();
        isLoaded = true;
        return new ArrayList<>(teleporters);
    }

    public static boolean addRegion(String name, BlockPos pos1, BlockPos pos2) {
        ensureLoaded();

        // Check if region with given name already exists
        if (getRegion(name) != null)
            return false;


        // Check if any existing region's pos1 is within 100 blocks of new region's pos1
        for (TeleporterData existingTeleporter : teleporters) {
            double distance = Math.sqrt(
                    Math.pow(pos1.getX() - existingTeleporter.pos1.getX(), 2) +
                            Math.pow(pos1.getY() - existingTeleporter.pos1.getY(), 2) +
                            Math.pow(pos1.getZ() - existingTeleporter.pos1.getZ(), 2)
            );

            if (distance < 100)
                return false;

        }

        TeleporterData newTeleporter = new TeleporterData(name, pos1, pos2);
        teleporters.add(newTeleporter);
        saveRegions(teleporters);
        return true;
    }

    public static void removeRegion(String name) {
        ensureLoaded();
        teleporters.removeIf(region -> region.name.equals(name));
        saveRegions(teleporters);
    }

    public static TeleporterData getRegion(String name) {
        ensureLoaded();
        return teleporters.stream()
                .filter(region -> region.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}