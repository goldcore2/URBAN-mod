package com.urban.worldgen;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.urban.worldgen.piece.AutobahnPiece;
import com.urban.worldgen.piece.StreetGridPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * The "city" structure. Lays out a large procedurally generated street grid and,
 * for every city, deterministically builds the Autobahns that connect it to its
 * eastern and southern neighbours (so the whole world ends up linked into a
 * single road network) including on/off ramps (Auffahrten/Abfahrten).
 */
public class CityStructure extends Structure {
    public static final MapCodec<CityStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.intRange(2, 24).fieldOf("grid_radius").forGetter(s -> s.gridRadius),
            Codec.intRange(8, 48).fieldOf("block_size").forGetter(s -> s.blockSize),
            Codec.intRange(3, 12).fieldOf("street_width").forGetter(s -> s.streetWidth),
            Codec.intRange(1, 4).fieldOf("autobahn_min_lanes").forGetter(s -> s.lanesMin),
            Codec.intRange(1, 5).fieldOf("autobahn_max_lanes").forGetter(s -> s.lanesMax))
            .apply(instance, CityStructure::new));

    private final int gridRadius;
    private final int blockSize;
    private final int streetWidth;
    private final int lanesMin;
    private final int lanesMax;

    public CityStructure(Structure.StructureSettings settings, int gridRadius, int blockSize, int streetWidth,
                         int lanesMin, int lanesMax) {
        super(settings);
        this.gridRadius = gridRadius;
        this.blockSize = blockSize;
        this.streetWidth = streetWidth;
        this.lanesMin = lanesMin;
        this.lanesMax = Math.max(lanesMin, lanesMax);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstFreeHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        BlockPos center = new BlockPos(x, y - 1, z);
        return Optional.of(new Structure.GenerationStub(center, builder -> generatePieces(builder, context, center)));
    }

    private void generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context, BlockPos center) {
        long seed = context.seed();
        int period = this.blockSize + this.streetWidth;
        int cityHalf = this.gridRadius * period;
        int roadY = center.getY();

        BoundingBox cityBox = new BoundingBox(
                center.getX() - cityHalf, roadY - 8, center.getZ() - cityHalf,
                center.getX() + cityHalf, roadY + 8, center.getZ() + cityHalf);
        long gridSeed = seed ^ (center.getX() * 341873128712L) ^ (center.getZ() * 132897987541L);
        builder.addPiece(new StreetGridPiece(roadY, cityBox, this.blockSize, this.streetWidth, gridSeed));

        ChunkPos cp = context.chunkPos();
        int rx = UrbanLayout.regionCoord(cp.x);
        int rz = UrbanLayout.regionCoord(cp.z);
        addConnection(builder, seed, center, roadY, cityHalf, UrbanLayout.cityAnchor(seed, rx + 1, rz));
        addConnection(builder, seed, center, roadY, cityHalf, UrbanLayout.cityAnchor(seed, rx, rz + 1));
    }

    /** Builds an L-shaped Autobahn from this city to a neighbouring city, plus an on/off ramp at this end. */
    private void addConnection(StructurePiecesBuilder builder, long seed, BlockPos center, int roadY, int cityHalf,
                               ChunkPos neighbor) {
        int cx = center.getX();
        int cz = center.getZ();
        int ex = neighbor.getMiddleBlockX();
        int ez = neighbor.getMiddleBlockZ();

        long h = mix(seed, ex, ez);
        int lanes = this.lanesMin + (int) Math.floorMod(h, (this.lanesMax - this.lanesMin + 1));

        // Horizontal mainline (along X) at z = cz, from this city's edge toward the neighbour.
        int hStart = (ex >= cx) ? cx + cityHalf : cx - cityHalf;
        builder.addPiece(new AutobahnPiece(true, cz, hStart, ex, roadY, lanes, false));

        // Vertical mainline (along Z) at x = ex, from cz to the neighbour's edge.
        int vEnd = (ez >= cz) ? ez - cityHalf : ez + cityHalf;
        builder.addPiece(new AutobahnPiece(false, ex, cz, vEnd, roadY, lanes, false));

        // On/off ramp (Auffahrt/Abfahrt) linking the city grid to the mainline.
        int rampInner = (ex >= cx) ? cz - 2 : cz + 2;
        int rampOuter = (ex >= cx) ? cz - 24 : cz + 24;
        builder.addPiece(new AutobahnPiece(false, hStart, rampInner, rampOuter, roadY, 1, true));
    }

    @Override
    public StructureType<?> type() {
        return com.urban.registry.UrbanStructures.CITY.get();
    }

    private static long mix(long seed, int a, int b) {
        long h = seed ^ (a * 0x9E3779B97F4A7C15L) ^ (b * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 33);
        h *= 0xFF51AFD7ED558CCDL;
        h ^= (h >>> 33);
        return h;
    }
}
