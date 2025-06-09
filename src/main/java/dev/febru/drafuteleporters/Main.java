package dev.febru.drafuteleporters;

import dev.febru.drafuteleporters.block.ModBlocks;
import dev.febru.drafuteleporters.handler.BlockBreakHandler;
import dev.febru.drafuteleporters.manager.TeleporterDataManager;
import dev.febru.drafuteleporters.payloads.OpenTeleporterMenuPayload;
import dev.febru.drafuteleporters.payloads.TeleportRequestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.febru.drafuteleporters.manager.TeleporterDataManager.getAllTeleporters;

public class Main implements ModInitializer {

    public static final String MOD_ID = "drafuteleporters";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 10;

    @Override
    public void onInitialize() {

        ModBlocks.registerModBlocks();

        // Register BlockBreak event
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            BlockBreakHandler.onBlockBroken(world, pos, state);
        });

        // Register packets
        PayloadTypeRegistry.playS2C().register(OpenTeleporterMenuPayload.ID, OpenTeleporterMenuPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TeleportRequestPayload.ID, TeleportRequestPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TeleportRequestPayload.ID, TeleporterDataManager::teleportPlayer);

        System.out.println("DrafuTeleporters initialized.");

        // Register particle spawning every second
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            tickCounter++;
            if (tickCounter >= TICKS_PER_SECOND) {
                tickCounter = 0;

                for (TeleporterDataManager.TeleporterData teleporter : getAllTeleporters()) {
                    ServerWorld world = server.getOverworld();

                    DustParticleEffect dustEffect = new DustParticleEffect(0x349A89,1.5f);

                    world.spawnParticles(
                            dustEffect,      // Mob particle type
                            (double) (teleporter.pos1.getX() + teleporter.pos2.getX()) / 2 + 0.5,     // X coordinate
                            teleporter.pos2.getY() + 2.0,                                             // Y coordinate
                            (double) (teleporter.pos1.getZ() + teleporter.pos2.getZ()) / 2 + 0.5,     // Z coordinate
                            25,                             // Number of particles
                            0.25,                           // Delta X (spread)
                            1,                              // Delta Y (spread)
                            0.25,                           // Delta Z (spread)
                            0.1                             // Speed
                    );
                }

            }
        });

    }

}
