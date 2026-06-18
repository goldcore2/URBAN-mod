package com.urban.registry;

import com.urban.Urban;
import com.urban.block.GermanSignBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All blocks added by Urban: road surfaces / markings used by the worldgen and
 * the German traffic signs.
 */
public final class UrbanBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Urban.MODID);

    // --- Road surface blocks ---
    public static final DeferredBlock<Block> ASPHALT = BLOCKS.registerSimpleBlock("asphalt",
            roadProps(MapColor.COLOR_GRAY));
    public static final DeferredBlock<Block> ROAD_MARKING_WHITE = BLOCKS.registerSimpleBlock("road_marking_white",
            roadProps(MapColor.SNOW));
    public static final DeferredBlock<Block> ROAD_MARKING_YELLOW = BLOCKS.registerSimpleBlock("road_marking_yellow",
            roadProps(MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> CROSSWALK = BLOCKS.registerSimpleBlock("crosswalk",
            roadProps(MapColor.SNOW));
    public static final DeferredBlock<Block> SIDEWALK = BLOCKS.registerSimpleBlock("sidewalk",
            roadProps(MapColor.STONE));

    // --- German traffic signs ---
    public static final DeferredBlock<GermanSignBlock> SIGN_STOP = BLOCKS.register("sign_stop",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_PRIORITY = BLOCKS.register("sign_priority",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_YIELD = BLOCKS.register("sign_yield",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_SPEED_50 = BLOCKS.register("sign_speed_50",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_AUTOBAHN = BLOCKS.register("sign_autobahn",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_CITY_LIMIT = BLOCKS.register("sign_city_limit",
            () -> new GermanSignBlock(signProps()));
    public static final DeferredBlock<GermanSignBlock> SIGN_EXIT = BLOCKS.register("sign_exit",
            () -> new GermanSignBlock(signProps()));

    private static BlockBehaviour.Properties roadProps(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties signProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.8F)
                .noOcclusion()
                .sound(SoundType.METAL);
    }

    private UrbanBlocks() {
    }
}
