package com.urban.worldgen;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/**
 * Deterministic city placement math.
 *
 * <p>Cities are placed by a vanilla {@code random_spread} structure placement
 * (see {@code data/urban/worldgen/structure_set/cities.json}). This class
 * re-implements the exact same algorithm so that, while generating one city, we
 * can compute the world position of the neighbouring cities without any saved
 * state. That lets each city deterministically build the Autobahns that connect
 * it to its eastern and southern neighbours.
 *
 * <p>The constants here MUST stay in sync with the structure-set JSON.
 */
public final class UrbanLayout {
    /** Average spacing between cities, in chunks. Mirrors structure_set "spacing". */
    public static final int SPACING = 32;
    /** Minimum separation between cities, in chunks. Mirrors structure_set "separation". */
    public static final int SEPARATION = 24;
    /** Random salt. Mirrors structure_set "salt". */
    public static final int SALT = 1390985;

    /**
     * Computes the anchor chunk of the city in the given region. A "region" is a
     * {@link #SPACING}x{@link #SPACING} chunk cell; region indices are obtained
     * via {@link #regionCoord(int)} from a chunk coordinate.
     */
    public static ChunkPos cityAnchor(long seed, int regionX, int regionZ) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, SALT);
        int dx = random.nextInt(SPACING - SEPARATION);
        int dz = random.nextInt(SPACING - SEPARATION);
        return new ChunkPos(regionX * SPACING + dx, regionZ * SPACING + dz);
    }

    /** Region index for a chunk coordinate. */
    public static int regionCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, SPACING);
    }

    private UrbanLayout() {
    }
}
