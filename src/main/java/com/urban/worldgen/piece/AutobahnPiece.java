package com.urban.worldgen.piece;

import com.urban.registry.UrbanBlocks;
import com.urban.registry.UrbanStructurePieces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
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

/**
 * A straight, axis-aligned stretch of Autobahn (or, when {@code ramp} is set, a
 * narrow on/off ramp - Auffahrt/Abfahrt). The mainline carries a configurable
 * number of lanes per direction, a central reservation (Mittelstreifen) with a
 * guard rail, hard shoulders (Standstreifen) and German-style white markings.
 *
 * <p>The road is laid at a fixed {@code roadY} (the originating city's ground
 * level) and embanked down to the terrain, so a long Autobahn forms causeways
 * and cuttings the way a real one does, while staying perfectly deterministic.
 */
public class AutobahnPiece extends StructurePiece {
    private static final int LANE_WIDTH = 3;
    private static final int CLEARANCE = 8;
    private static final int MAX_EMBANKMENT = 32;

    private final boolean alongX;
    private final int center;
    private final int start;
    private final int end;
    private final int roadY;
    private final int lanesPerDir;
    private final boolean ramp;

    public AutobahnPiece(boolean alongX, int center, int start, int end, int roadY, int lanesPerDir, boolean ramp) {
        super(UrbanStructurePieces.AUTOBAHN.get(), 0, boxFor(alongX, center, start, end, roadY, lanesPerDir, ramp));
        this.alongX = alongX;
        this.center = center;
        this.start = Math.min(start, end);
        this.end = Math.max(start, end);
        this.roadY = roadY;
        this.lanesPerDir = lanesPerDir;
        this.ramp = ramp;
    }

    public AutobahnPiece(CompoundTag tag) {
        super(UrbanStructurePieces.AUTOBAHN.get(), tag);
        this.alongX = tag.getBoolean("alongX");
        this.center = tag.getInt("center");
        this.start = tag.getInt("start");
        this.end = tag.getInt("end");
        this.roadY = tag.getInt("roadY");
        this.lanesPerDir = tag.getInt("lanes");
        this.ramp = tag.getBoolean("ramp");
    }

    private static int halfWidth(int lanesPerDir, boolean ramp) {
        if (ramp) {
            return 2;
        }
        int carriage = lanesPerDir * LANE_WIDTH;
        return 3 + carriage; // median(2) + carriageway + shoulder + guard rail
    }

    private static BoundingBox boxFor(boolean alongX, int center, int start, int end, int roadY, int lanesPerDir,
                                      boolean ramp) {
        int hw = halfWidth(lanesPerDir, ramp);
        int lo = Math.min(start, end);
        int hi = Math.max(start, end);
        int y0 = roadY - MAX_EMBANKMENT;
        int y1 = roadY + CLEARANCE;
        if (alongX) {
            return new BoundingBox(lo, y0, center - hw, hi, y1, center + hw);
        }
        return new BoundingBox(center - hw, y0, lo, center + hw, y1, hi);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putBoolean("alongX", this.alongX);
        tag.putInt("center", this.center);
        tag.putInt("start", this.start);
        tag.putInt("end", this.end);
        tag.putInt("roadY", this.roadY);
        tag.putInt("lanes", this.lanesPerDir);
        tag.putBoolean("ramp", this.ramp);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        int minX = Math.max(box.minX(), this.boundingBox.minX());
        int maxX = Math.min(box.maxX(), this.boundingBox.maxX());
        int minZ = Math.max(box.minZ(), this.boundingBox.minZ());
        int maxZ = Math.min(box.maxZ(), this.boundingBox.maxZ());
        int hw = halfWidth(this.lanesPerDir, this.ramp);

        for (int wx = minX; wx <= maxX; wx++) {
            for (int wz = minZ; wz <= maxZ; wz++) {
                int along = this.alongX ? wx : wz;
                int perp = this.alongX ? wz : wx;
                if (along < this.start || along > this.end) {
                    continue;
                }
                int d = perp - this.center;
                if (Math.abs(d) > hw) {
                    continue;
                }
                BlockState surface = surfaceAt(d, along);
                if (surface == null) {
                    continue;
                }
                buildColumn(level, box, wx, wz, surface);
                if (guardRailAt(d)) {
                    setBlock(level, box, wx, this.roadY + 1, wz, Blocks.IRON_BARS.defaultBlockState());
                }
            }
        }

        placeSigns(level, box, hw);
    }

    private BlockState surfaceAt(int d, int run) {
        BlockState asphalt = UrbanBlocks.ASPHALT.get().defaultBlockState();
        BlockState white = UrbanBlocks.ROAD_MARKING_WHITE.get().defaultBlockState();
        int a = Math.abs(d);

        if (this.ramp) {
            if (a == 2) {
                return white;
            }
            return asphalt;
        }

        if (a <= 1) {
            return Blocks.GRASS_BLOCK.defaultBlockState(); // median (Mittelstreifen)
        }
        int carriage = this.lanesPerDir * LANE_WIDTH;
        int c = a - 2; // 0-based across the carriageway
        if (c < carriage) {
            if (c == 0 || c == carriage - 1) {
                return white; // edge lines
            }
            if (c % LANE_WIDTH == 0 && Math.floorMod(run, 6) < 3) {
                return white; // dashed lane divider
            }
            return asphalt;
        }
        if (a == 2 + carriage) {
            return asphalt; // hard shoulder
        }
        return Blocks.GRASS_BLOCK.defaultBlockState(); // outer embankment edge under guard rail
    }

    private boolean guardRailAt(int d) {
        if (this.ramp) {
            return false;
        }
        int a = Math.abs(d);
        int carriage = this.lanesPerDir * LANE_WIDTH;
        return a == 1 || a == 3 + carriage;
    }

    private void buildColumn(WorldGenLevel level, BoundingBox box, int wx, int wz, BlockState surface) {
        setBlock(level, box, wx, this.roadY, wz, surface);
        BlockState fill = Blocks.STONE.defaultBlockState();
        for (int y = this.roadY - 1; y >= this.roadY - MAX_EMBANKMENT; y--) {
            BlockPos p = new BlockPos(wx, y, wz);
            if (!box.isInside(p)) {
                break;
            }
            if (!level.getBlockState(p).isAir() && !level.isWaterAt(p)) {
                break;
            }
            level.setBlock(p, fill, Block.UPDATE_CLIENTS);
        }
        for (int y = 1; y <= CLEARANCE; y++) {
            setBlock(level, box, wx, this.roadY + y, wz, Blocks.AIR.defaultBlockState());
        }
    }

    private void placeSigns(WorldGenLevel level, BoundingBox box, int hw) {
        if (this.ramp) {
            placeSign(level, box, this.start, this.center + hw, UrbanBlocks.SIGN_EXIT.get());
        } else {
            placeSign(level, box, this.start, this.center + hw, UrbanBlocks.SIGN_AUTOBAHN.get());
            placeSign(level, box, this.end, this.center - hw, UrbanBlocks.SIGN_AUTOBAHN.get());
        }
    }

    private void placeSign(WorldGenLevel level, BoundingBox box, int along, int perp, Block sign) {
        int wx = this.alongX ? along : perp;
        int wz = this.alongX ? perp : along;
        BlockState state = sign.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                this.alongX ? Direction.SOUTH : Direction.EAST);
        setBlock(level, box, wx, this.roadY + 1, wz, state);
    }

    private void setBlock(WorldGenLevel level, BoundingBox box, int x, int y, int z, BlockState state) {
        BlockPos p = new BlockPos(x, y, z);
        if (box.isInside(p)) {
            level.setBlock(p, state, Block.UPDATE_CLIENTS);
        }
    }
}
