package com.urban.registry;

import com.urban.Urban;
import com.urban.worldgen.CityStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration of Urban's structure types.
 */
public final class UrbanStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Urban.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<CityStructure>> CITY =
            STRUCTURE_TYPES.register("city", () -> () -> CityStructure.CODEC);

    private UrbanStructures() {
    }
}
