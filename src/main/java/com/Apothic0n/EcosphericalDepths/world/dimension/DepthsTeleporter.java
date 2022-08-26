package com.Apothic0n.EcosphericalDepths.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

import static net.minecraft.world.level.block.Block.UPDATE_ALL;

public class DepthsTeleporter implements ITeleporter {
    public static BlockPos thisPos = BlockPos.ZERO;
    public static boolean insideDimension = true;

    public DepthsTeleporter(BlockPos pos, boolean insideDim) {
        thisPos = pos;
        insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
        double y = 61;

        if (!insideDimension) {
            y = 253;
        }

        BlockPos destinationPos = new BlockPos(thisPos.getX(), y, thisPos.getZ());

        int tries = 0;
        int direction = 2;
        if (destinationWorld == entity.getServer().overworld()) {
            direction = -2;
        }
        while ((destinationWorld.getBlockState(destinationPos).getMaterial() != Material.AIR) &&
                !destinationWorld.getBlockState(destinationPos).canBeReplaced(Fluids.WATER) &&
                destinationWorld.getBlockState(destinationPos.below()).getMaterial() != Material.AIR &&
                !destinationWorld.getBlockState(destinationPos.below()).canBeReplaced(Fluids.WATER) && tries < 25) {
            destinationPos = destinationPos.below(direction);
            tries++;
        }
        if (destinationPos.below().getY() >= 89) {
            destinationPos = destinationPos.atY(89);
            destinationWorld.setBlock(destinationPos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            destinationWorld.setBlock(destinationPos.below(), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
        }
        entity.teleportTo(destinationPos.getX(), destinationPos.below().getY(), destinationPos.getZ());
        return entity;
    }
}
