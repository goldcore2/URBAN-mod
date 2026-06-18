package com.urban;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.urban.registry.UrbanBlocks;
import com.urban.registry.UrbanItems;
import com.urban.registry.UrbanStructurePieces;
import com.urban.registry.UrbanStructures;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Main entry point for the Urban mod.
 *
 * <p>Urban adds large procedurally generated city structures (street grids with
 * German-style road markings and traffic signs) and an Autobahn network that
 * links neighbouring cities together via on/off ramps (Auffahrten/Abfahrten).
 */
@Mod(Urban.MODID)
public class Urban {
    public static final String MODID = "urban";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Urban(IEventBus modEventBus, ModContainer modContainer) {
        UrbanBlocks.BLOCKS.register(modEventBus);
        UrbanItems.ITEMS.register(modEventBus);
        UrbanItems.CREATIVE_TABS.register(modEventBus);
        UrbanStructures.STRUCTURE_TYPES.register(modEventBus);
        UrbanStructurePieces.STRUCTURE_PIECES.register(modEventBus);
    }
}
