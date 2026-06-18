package com.urban.registry;

import com.urban.Urban;
import com.urban.worldgen.piece.AutobahnPiece;
import com.urban.worldgen.piece.StreetGridPiece;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration of the structure piece types used by the city / Autobahn worldgen.
 */
public final class UrbanStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Urban.MODID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CITY_GRID =
            STRUCTURE_PIECES.register("city_grid",
                    () -> (StructurePieceType.ContextlessType) StreetGridPiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> AUTOBAHN =
            STRUCTURE_PIECES.register("autobahn",
                    () -> (StructurePieceType.ContextlessType) AutobahnPiece::new);

    private UrbanStructurePieces() {
    }
}
