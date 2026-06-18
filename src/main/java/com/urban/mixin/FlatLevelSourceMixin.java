package com.urban.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.urban.registry.UrbanStructures;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * Superflat worlds only generate the structure sets listed in their
 * {@code structure_overrides} (by default just strongholds + villages), so the
 * Urban {@code cities} set would never spawn. This injects the Urban structure
 * set into the flat generator's structure state so cities also generate on
 * superflat worlds. Worlds that already generate every set (empty overrides)
 * are left untouched.
 */
@Mixin(FlatLevelSource.class)
public abstract class FlatLevelSourceMixin {

    @Inject(method = "createState", at = @At("HEAD"), cancellable = true)
    private void urban$includeCities(HolderLookup<StructureSet> lookup, RandomState randomState, long seed,
                                     CallbackInfoReturnable<ChunkGeneratorStructureState> cir) {
        FlatLevelSource self = (FlatLevelSource) (Object) this;
        Optional<HolderSet<StructureSet>> overrides = self.settings().structureOverrides();
        if (overrides.isEmpty()) {
            return; // every structure set already generates
        }
        Optional<Holder.Reference<StructureSet>> cities = lookup.get(UrbanStructures.CITIES);
        if (cities.isEmpty()) {
            return;
        }
        List<Holder<StructureSet>> sets = new ArrayList<>(overrides.get().stream().toList());
        if (!sets.contains(cities.get())) {
            sets.add(cities.get());
        }
        BiomeSource biomeSource = ((ChunkGenerator) self).getBiomeSource();
        cir.setReturnValue(ChunkGeneratorStructureState.createForFlat(randomState, seed, biomeSource, sets.stream()));
    }
}
