package com.urban.registry;

import com.urban.Urban;
import com.urban.worldgen.CityStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
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

    /** Data-driven structure set ({@code data/urban/worldgen/structure_set/cities.json}). */
    public static final ResourceKey<StructureSet> CITIES =
            ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(Urban.MODID, "cities"));

    private UrbanStructures() {
    }
}
