package com.urban.worldgen.piece;

import com.urban.registry.UrbanBlocks;
import com.urban.registry.UrbanStructurePieces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Generates one whole city as a flat plate of streets laid out in a uniform
 * grid. Each "street band" is rendered with a German cross-section: raised
 * sidewalks on the outside, solid white edge lines (Randmarkierung), a dashed
 * white centre line, and asphalt in between. Intersections place a traffic sign
 * on one corner.
 *
 * <p>The footprint can be arbitrarily large (cities are ~100x the area of a
 * village); the piece only ever writes blocks inside the {@code box} it is
 * handed for the chunk currently being generated, so the cost is spread across
 * many chunks.
 */
public class StreetGridPiece extends StructurePiece {
    private static final int FOUNDATION_DEPTH = 4;
    private static final int CLEARANCE = 6;

    private final int baseY;
    private final int blockSize;
    private final int streetWidth;
    private final long seed;

    public StreetGridPiece(int baseY, BoundingBox box, int blockSize, int streetWidth, long seed) {
        super(UrbanStructurePieces.CITY_GRID.get(), 0, box);
        this.baseY = baseY;
        this.blockSize = blockSize;
        this.streetWidth = streetWidth;
        this.seed = seed;
    }

    public StreetGridPiece(CompoundTag tag) {
        super(UrbanStructurePieces.CITY_GRID.get(), tag);
        this.baseY = tag.getInt("baseY");
        this.blockSize = tag.getInt("blockSize");
        this.streetWidth = tag.getInt("streetWidth");
        this.seed = tag.getLong("seed");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("baseY", this.baseY);
        tag.putInt("blockSize", this.blockSize);
        tag.putInt("streetWidth", this.streetWidth);
        tag.putLong("seed", this.seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, net.minecraft.world.level.ChunkPos chunkPos,
                            BlockPos pos) {
        int period = this.blockSize + this.streetWidth;
        int originX = this.boundingBox.minX();
        int originZ = this.boundingBox.minZ();
        int sizeX = this.boundingBox.getXSpan();
        int sizeZ = this.boundingBox.getZSpan();

        int minX = Math.max(box.minX(), this.boundingBox.minX());
        int maxX = Math.min(box.maxX(), this.boundingBox.maxX());
        int minZ = Math.max(box.minZ(), this.boundingBox.minZ());
        int maxZ = Math.min(box.maxZ(), this.boundingBox.maxZ());

        BlockState asphalt = UrbanBlocks.ASPHALT.get().defaultBlockState();
        BlockState white = UrbanBlocks.ROAD_MARKING_WHITE.get().defaultBlockState();
        BlockState crosswalk = UrbanBlocks.CROSSWALK.get().defaultBlockState();
        BlockState sidewalk = UrbanBlocks.SIDEWALK.get().defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        for (int wx = minX; wx <= maxX; wx++) {
            int lx = wx - originX;
            int mx = Math.floorMod(lx, period);
            boolean streetX = mx < this.streetWidth;
            for (int wz = minZ; wz <= maxZ; wz++) {
                int lz = wz - originZ;
                int mz = Math.floorMod(lz, period);
                boolean streetZ = mz < this.streetWidth;

                BlockState surface;
                if (!streetX && !streetZ) {
                    surface = grass;
                } else if (streetX && streetZ) {
                    // intersection: asphalt, with zebra crosswalks on the outermost rows
                    boolean edge = mx == 0 || mx == this.streetWidth - 1 || mz == 0 || mz == this.streetWidth - 1;
                    boolean stripe = (Math.floorMod(lx + lz, 2) == 0);
                    surface = (edge && stripe) ? crosswalk : asphalt;
                } else {
                    int band = streetX ? mx : mz;
                    int run = streetX ? lz : lx;
                    surface = laneSurface(band, run, asphalt, white, sidewalk);
                }

                buildColumn(level, box, wx, wz, surface);

                // place a traffic sign on each grid corner (one per cell)
                if (mx == 0 && mz == 0 && lx > 0 && lz > 0 && lx < sizeX - 1 && lz < sizeZ - 1) {
                    placeSign(level, box, wx, wz, lx, lz, sizeX, sizeZ, period);
                }
            }
        }
    }

    private BlockState laneSurface(int band, int run, BlockState asphalt, BlockState white, BlockState sidewalk) {
        if (band == 0 || band == this.streetWidth - 1) {
            return sidewalk;
        }
        if (band == 1 || band == this.streetWidth - 2) {
            return white; // solid edge line (Randmarkierung)
        }
        if (band == this.streetWidth / 2 && Math.floorMod(run, 6) < 3) {
            return white; // dashed centre line
        }
        return asphalt;
    }

    private void buildColumn(WorldGenLevel level, BoundingBox box, int wx, int wz, BlockState surface) {
        boolean road = !surface.is(Blocks.GRASS_BLOCK);
        BlockState fill = road ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();

        setBlock(level, box, wx, this.baseY, wz, surface);
        for (int y = 1; y <= FOUNDATION_DEPTH; y++) {
            setBlock(level, box, wx, this.baseY - y, wz, fill);
        }
        for (int y = 1; y <= CLEARANCE; y++) {
            setBlock(level, box, wx, this.baseY + y, wz, Blocks.AIR.defaultBlockState());
        }
    }

    private void placeSign(WorldGenLevel level, BoundingBox box, int wx, int wz, int lx, int lz, int sizeX,
                           int sizeZ, int period) {
        long h = mix(this.seed, lx / period, lz / period);
        boolean border = lx <= period || lz <= period || lx >= sizeX - period || lz >= sizeZ - period;

        Block signBlock;
        if (border) {
            signBlock = UrbanBlocks.SIGN_CITY_LIMIT.get();
        } else {
            switch ((int) (Math.floorMod(h, 4))) {
                case 0 -> signBlock = UrbanBlocks.SIGN_PRIORITY.get();
                case 1 -> signBlock = UrbanBlocks.SIGN_STOP.get();
                case 2 -> signBlock = UrbanBlocks.SIGN_YIELD.get();
                default -> signBlock = UrbanBlocks.SIGN_SPEED_50.get();
            }
        }
        Direction facing = Direction.from2DDataValue((int) Math.floorMod(h >> 4, 4));
        BlockState state = signBlock.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
        setBlock(level, box, wx, this.baseY + 1, wz, state);
    }

    private void setBlock(WorldGenLevel level, BoundingBox box, int x, int y, int z, BlockState state) {
        BlockPos p = new BlockPos(x, y, z);
        if (box.isInside(p)) {
            level.setBlock(p, state, Block.UPDATE_CLIENTS);
        }
    }

    private static long mix(long seed, int a, int b) {
        long h = seed ^ (a * 0x9E3779B97F4A7C15L) ^ (b * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 33);
        h *= 0xFF51AFD7ED558CCDL;
        h ^= (h >>> 33);
        return h;
    }
}
