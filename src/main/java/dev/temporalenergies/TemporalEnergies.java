package dev.temporalenergies;

import dev.temporalenergies.block.PowerTranslatorBlock;
import dev.temporalenergies.block.entity.PowerTranslatorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import team.reborn.energy.api.EnergyStorage;

public final class TemporalEnergies implements ModInitializer {
    public static final String MOD_ID = "temporalenergies";

    public static final Block POWER_TRANSLATOR = new PowerTranslatorBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.DEEPSLATE_GRAY)
            .strength(3.5F, 6.0F)
            .sounds(BlockSoundGroup.METAL)
            .requiresTool());

    public static final BlockEntityType<PowerTranslatorBlockEntity> POWER_TRANSLATOR_BLOCK_ENTITY =
            BlockEntityType.Builder.create(PowerTranslatorBlockEntity::new, POWER_TRANSLATOR).build(null);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, id("power_translator"), POWER_TRANSLATOR);
        Registry.register(Registries.ITEM, id("power_translator"), new BlockItem(POWER_TRANSLATOR, new Item.Settings()));
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("power_translator"), POWER_TRANSLATOR_BLOCK_ENTITY);

        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage(),
                POWER_TRANSLATOR_BLOCK_ENTITY);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(POWER_TRANSLATOR));
    }
}
