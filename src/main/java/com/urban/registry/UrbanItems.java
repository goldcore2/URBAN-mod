package com.urban.registry;

import java.util.function.Supplier;

import com.urban.Urban;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block items for every Urban block plus the creative mode tab that holds them.
 */
public final class UrbanItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Urban.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Urban.MODID);

    public static final DeferredItem<BlockItem> ASPHALT = blockItem(UrbanBlocks.ASPHALT);
    public static final DeferredItem<BlockItem> ROAD_MARKING_WHITE = blockItem(UrbanBlocks.ROAD_MARKING_WHITE);
    public static final DeferredItem<BlockItem> ROAD_MARKING_YELLOW = blockItem(UrbanBlocks.ROAD_MARKING_YELLOW);
    public static final DeferredItem<BlockItem> CROSSWALK = blockItem(UrbanBlocks.CROSSWALK);
    public static final DeferredItem<BlockItem> SIDEWALK = blockItem(UrbanBlocks.SIDEWALK);

    public static final DeferredItem<BlockItem> SIGN_STOP = blockItem(UrbanBlocks.SIGN_STOP);
    public static final DeferredItem<BlockItem> SIGN_PRIORITY = blockItem(UrbanBlocks.SIGN_PRIORITY);
    public static final DeferredItem<BlockItem> SIGN_YIELD = blockItem(UrbanBlocks.SIGN_YIELD);
    public static final DeferredItem<BlockItem> SIGN_SPEED_50 = blockItem(UrbanBlocks.SIGN_SPEED_50);
    public static final DeferredItem<BlockItem> SIGN_AUTOBAHN = blockItem(UrbanBlocks.SIGN_AUTOBAHN);
    public static final DeferredItem<BlockItem> SIGN_CITY_LIMIT = blockItem(UrbanBlocks.SIGN_CITY_LIMIT);
    public static final DeferredItem<BlockItem> SIGN_EXIT = blockItem(UrbanBlocks.SIGN_EXIT);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> URBAN_TAB = CREATIVE_TABS.register("urban",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.urban"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ASPHALT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ASPHALT.get());
                        output.accept(ROAD_MARKING_WHITE.get());
                        output.accept(ROAD_MARKING_YELLOW.get());
                        output.accept(CROSSWALK.get());
                        output.accept(SIDEWALK.get());
                        output.accept(SIGN_STOP.get());
                        output.accept(SIGN_PRIORITY.get());
                        output.accept(SIGN_YIELD.get());
                        output.accept(SIGN_SPEED_50.get());
                        output.accept(SIGN_AUTOBAHN.get());
                        output.accept(SIGN_CITY_LIMIT.get());
                        output.accept(SIGN_EXIT.get());
                    })
                    .build());

    private static <T extends Block> DeferredItem<BlockItem> blockItem(DeferredBlock<T> block) {
        return ITEMS.registerSimpleBlockItem(block);
    }

    private UrbanItems() {
    }
}
