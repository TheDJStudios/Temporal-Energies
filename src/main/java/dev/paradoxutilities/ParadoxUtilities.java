package dev.paradoxutilities;

import dev.paradoxutilities.block.BinaryCoreBlock;
import dev.paradoxutilities.block.PowerCoreBlock;
import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.block.entity.PowerCoreBlockEntity;
import dev.paradoxutilities.compat.ait.AitCompat;
import dev.paradoxutilities.compat.computercraft.ComputerCraftCompat;
import dev.paradoxutilities.compat.create.CreateCompat;
import dev.paradoxutilities.compat.tardisrefined.TardisRefinedCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import team.reborn.energy.api.EnergyStorage;

public final class ParadoxUtilities implements ModInitializer {
    public static final String MOD_ID = "paradox_utilities";

    public static final Block POWER_CORE = new PowerCoreBlock(coreSettings());
    public static final Block BINARY_CORE = new BinaryCoreBlock(coreSettings());

    public static final BlockEntityType<PowerCoreBlockEntity> POWER_CORE_BLOCK_ENTITY =
            BlockEntityType.Builder.create(PowerCoreBlockEntity::new, POWER_CORE).build(null);
    public static final BlockEntityType<BinaryCoreBlockEntity> BINARY_CORE_BLOCK_ENTITY =
            BlockEntityType.Builder.create(BinaryCoreBlockEntity::new, BINARY_CORE).build(null);

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.paradox_utilities"))
            .icon(() -> POWER_CORE.asItem().getDefaultStack())
            .entries((context, entries) -> {
                entries.add(POWER_CORE);
                if (FabricLoader.getInstance().isModLoaded("computercraft")) {
                    entries.add(BINARY_CORE);
                }
            })
            .build();

    private static AbstractBlock.Settings coreSettings() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.DEEPSLATE_GRAY)
                .strength(3.5F, 6.0F)
                .sounds(BlockSoundGroup.METAL)
                .requiresTool();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        registerBlock("power_core", POWER_CORE);
        registerBlock("binary_core", BINARY_CORE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("power_core"), POWER_CORE_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("binary_core"), BINARY_CORE_BLOCK_ENTITY);
        Registry.register(Registries.ITEM_GROUP, id("main"), ITEM_GROUP);

        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> blockEntity.energyStorage(),
                POWER_CORE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("ait")) {
            AitCompat.initialize();
        }
        if (FabricLoader.getInstance().isModLoaded("create")) {
            CreateCompat.initialize();
        }
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            ComputerCraftCompat.initialize();
        }
        if (FabricLoader.getInstance().isModLoaded("tardis_refined")) {
            TardisRefinedCompat.initialize();
        }
    }

    private static void registerBlock(String path, Block block) {
        Registry.register(Registries.BLOCK, id(path), block);
        Registry.register(Registries.ITEM, id(path), new BlockItem(block, new Item.Settings()));
    }
}
