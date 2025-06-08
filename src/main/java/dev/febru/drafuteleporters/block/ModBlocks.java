package dev.febru.drafuteleporters.block;

import dev.febru.drafuteleporters.Main;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static dev.febru.drafuteleporters.Main.MOD_ID;

public class ModBlocks {

    public static Block TELEPORTER_CORE_BLOCK;

    private static Block registerBlock(String name, AbstractBlock.Settings settings) {

        Identifier id = Identifier.of(MOD_ID, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        settings.registryKey(blockKey);
        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, new Block(settings));
        registerBlockItem(name, registeredBlock, blockKey);
        return registeredBlock;

    }

    private static void registerBlockItem(String name, Block block, RegistryKey<Block> blockRegistryKey) {

        Identifier id = Identifier.of(MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings itemSettings = new Item.Settings().useItemPrefixedTranslationKey().registryKey(itemKey);
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, itemSettings));

    }

    public static void registerModBlocks() {
        Main.LOGGER.info("Registering blocks for " + MOD_ID);


        TELEPORTER_CORE_BLOCK = registerBlock("teleporter_core_block",
                AbstractBlock.Settings.create()
                        .strength(2.0f)
                        .requiresTool().nonOpaque()
                        .sounds(BlockSoundGroup.SCULK_CATALYST));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TELEPORTER_CORE_BLOCK);
        });
    }
}