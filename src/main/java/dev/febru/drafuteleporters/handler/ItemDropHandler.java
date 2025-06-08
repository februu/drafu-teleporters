package dev.febru.drafuteleporters.handler;

import dev.febru.drafuteleporters.manager.TeleporterDataManager;
import dev.febru.drafuteleporters.payloads.OpenTeleporterMenuPayload;
import dev.febru.drafuteleporters.structures.TeleporterStructure;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.List;

import static dev.febru.drafuteleporters.manager.TeleporterDataManager.getAllTeleporters;
import static dev.febru.drafuteleporters.structures.TeleporterStructure.validateTeleporterStructure;

public class ItemDropHandler {

    public static void onItemDropped(ItemEntity itemEntity) {

        if (itemEntity.getStack().getItem() != Items.ENDER_PEARL)
            return;

        if (!itemEntity.getWorld().getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD))
            return;

        if (!itemEntity.getWorld().isClient && itemEntity.getWorld().getServer() != null) {
            itemEntity.getWorld().getServer().execute(() -> {
                performStructureCheck(itemEntity);
            });

        }
    }

    private static void performStructureCheck(ItemEntity enderPearl) {
        if (enderPearl == null) {
            return;
        }

        Entity owner = enderPearl.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) {
            return;
        }

        BlockPos pearlPos = enderPearl.getBlockPos();
        TeleporterStructure.StructureResult result = validateTeleporterStructure(enderPearl.getWorld(), pearlPos);

        if (result.isValid) {

            String itemName = enderPearl.getStack().getName().getString();
            String defaultName = Items.ENDER_PEARL.getName().getString();

            if (!itemName.equals(defaultName)) {

                if (TeleporterDataManager.addRegion(itemName, result.pos1, result.pos2)) {
                    enderPearl.discard();

                    // Spawn particle effects
                    ServerWorld world = player.getServerWorld();
                    world.spawnParticles(
                            ParticleTypes.PORTAL,
                            result.center.getX(), result.center.getY(), result.center.getZ(),
                            300,
                            0.5, 1.0, 0.5,
                            0.1
                    );

                    // Add extra sparkle effect
                    world.spawnParticles(
                            ParticleTypes.END_ROD,
                            result.center.getX(), result.center.getY(), result.center.getZ(),
                            300,
                            0.3, 0.5, 0.3,
                            0.05
                    );

                    // Spawn lightning bolt
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.COMMAND);
                    if (lightning != null) {
                        lightning.refreshPositionAfterTeleport(result.center.getX(), result.center.getY(), result.center.getZ());
                        lightning.setCosmetic(true);
                        world.spawnEntity(lightning);
                    }
                }

            } else {
                for (TeleporterDataManager.TeleporterData teleporter : getAllTeleporters()) {
                    if (result.pos1.equals(teleporter.pos1) && result.pos2.equals(teleporter.pos2)) {
                        enderPearl.discard();
                        PacketByteBuf buf = PacketByteBufs.create();
                        List<TeleporterDataManager.TeleporterData> availableTeleporters = getAllTeleporters();
                        availableTeleporters.remove(teleporter);
                        ServerPlayNetworking.send(player, new OpenTeleporterMenuPayload(availableTeleporters));
                        return;
                    }
                }
            }
        }
    }
}