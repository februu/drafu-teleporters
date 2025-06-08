package dev.febru.drafuteleporters;

import dev.febru.drafuteleporters.handlers.BlockBreakHandler;
import dev.febru.drafuteleporters.handlers.ItemDropHandler;
import dev.febru.drafuteleporters.managers.TeleporterDataManager;
import dev.febru.drafuteleporters.payloads.TeleportRequestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.febru.drafuteleporters.managers.TeleporterDataManager.getAllTeleporters;

public class Main implements ModInitializer {

    public static final String MOD_ID = "drafuteleporters";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private int ticksElapsed = 0;

    @Override
    public void onInitialize() {

        // Register BlockBreak event
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            BlockBreakHandler.onBlockBroken(world, pos, state);
        });

        // Register TP Request handling
        PayloadTypeRegistry.playC2S().register(TeleportRequestPayload.ID, TeleportRequestPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TeleportRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {

                UUID playerUuid = payload.playerUuid();
                ServerPlayerEntity player = context.server().getPlayerManager().getPlayer(playerUuid);

                if (player != null) {

                if (payload.cancelled()) {
                    player.giveItemStack(new ItemStack(Items.ENDER_PEARL, 1));
                    return;
                }

                TeleporterDataManager.TeleporterData selected = payload.teleporterData();

                    double midX = (selected.pos1.getX() + selected.pos2.getX()) / 2.0 + 0.5;
                    double midY = (selected.pos2.getY()) + 1.0;
                    double midZ = (selected.pos1.getZ() + selected.pos2.getZ()) / 2.0 + 0.5;

                    player.requestTeleport(midX, midY, midZ);
                    ServerWorld world = player.getServerWorld();
                    player.sendMessage(Text.literal("✦ Teleported to " + selected.name + " ✦"), true);

                    // Schedule particles and sound for next tick
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);

                            // Schedule particles and sound for next tick on main server thread
                            context.server().execute(() -> {

                                // Spawn particle effects at destination
                                world.spawnParticles(
                                        ParticleTypes.PORTAL,           // Particle type
                                        midX, midY, midZ,               // Position
                                        150,                             // Count
                                        0.5, 1.0, 0.5,   // Spread (x, y, z)
                                        0.1                             // Speed
                                );

                                // Add extra sparkle effect
                                world.spawnParticles(
                                        ParticleTypes.END_ROD,
                                        midX, midY + 0.5, midZ,
                                        150,
                                        0.3, 0.5, 0.3,
                                        0.05
                                );

                                // Play teleport sound
                                world.playSound(
                                        null,                            // Player (null = everyone nearby hears it)
                                        midX, midY, midZ,                       // Position
                                        SoundEvents.ENTITY_ENDERMAN_TELEPORT,   // Sound
                                        SoundCategory.PLAYERS,                  // Category
                                        1.0f,                                   // Volume
                                        1.0f                                    // Pitch
                                );
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            LOGGER.warn("Particle spawn thread interrupted", e);
                        }
                    }).start();
                }
            });
        });
    }
}
