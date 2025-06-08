package dev.febru.drafuteleporters.client;

import dev.febru.drafuteleporters.Main;
import dev.febru.drafuteleporters.payloads.OpenTeleporterMenuPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import dev.febru.drafuteleporters.client.gui.TPScreen;

import static dev.febru.drafuteleporters.block.ModBlocks.TELEPORTER_CORE_BLOCK;

public class MainClient implements ClientModInitializer {

    @Override
    @SuppressWarnings("resource")
    public void onInitializeClient() {
        Main.LOGGER.info("Initializing client for " + Main.MOD_ID);

        BlockRenderLayerMap.INSTANCE.putBlock(TELEPORTER_CORE_BLOCK, RenderLayer.getTranslucent());

            PayloadTypeRegistry.playS2C().register(OpenTeleporterMenuPayload.ID, OpenTeleporterMenuPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(OpenTeleporterMenuPayload.ID, (payload, context) -> {
                context.client().execute(() -> {
                    context.client().setScreen(new TPScreen(Text.literal("Select destination"), payload.teleporters()));
                });
            });

    }

}
