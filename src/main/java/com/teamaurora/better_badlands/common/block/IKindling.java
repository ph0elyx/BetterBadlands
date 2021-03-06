package com.teamaurora.better_badlands.common.block;

import com.teamaurora.better_badlands.core.registry.BetterBadlandsParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public interface IKindling {
    public static final IntegerProperty BURN_DISTANCE = IntegerProperty.create("burn_distance", 0, 21);
    public static final BooleanProperty IS_BURNED = BooleanProperty.create("burned");
    static final int TO_SCHEDULE = 30;
    //public static final int MAX_TIME = getEquation(21*20);
    default void onProjectileCollisionI(World worldIn, BlockState state, BlockRayTraceResult hit, ProjectileEntity projectile) {
        if (!worldIn.isRemote) {
            Entity entity = projectile.func_234616_v_();
            if (projectile.isBurning()) {
                BlockPos blockpos = hit.getPos();
                worldIn.setBlockState(hit.getPos(), state.with(getDistProperty(state),1));
            }
        }

    }
    public static final IntegerProperty BURN_TIMER = IntegerProperty.create("burn_timer", 0, 120);
    default void onBlockAddedI(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        boolean flag = false;
        for (Direction dir : Direction.values()) {
            if (worldIn.getBlockState(pos.offset(dir)).getBlock() == Blocks.LAVA) {
                flag = true;
            }
        }
        if (flag) {
            worldIn.destroyBlock(pos, false);
        } else {
            worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), TO_SCHEDULE);
        }
        //super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }

    default void neighborChangedI(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = false;
        for (Direction dir : Direction.values()) {
            if (worldIn.getBlockState(pos.offset(dir)).getBlock() == Blocks.LAVA) {
                flag = true;
            }
        }
        if (flag) {
            worldIn.destroyBlock(pos, false);
        }
        //super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    default int getDistFromBlockstate(BlockState state) {
        try {
            return state.get(BURN_DISTANCE);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    default IntegerProperty getDistProperty(BlockState state) {
        return BURN_DISTANCE;
    }
    default void animateTickI(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (getDistFromBlockstate(stateIn)>0) {
            for (int i = 0; i < 20; i++) {
                double d3 = (double) pos.getX() + rand.nextDouble() + (rand.nextDouble()/6);
                double d8 = (double) pos.getY() + rand.nextDouble() + (rand.nextDouble()/6);
                double d13 = (double) pos.getZ() + rand.nextDouble() + (rand.nextDouble()/6);
                worldIn.addParticle(ParticleTypes.FLAME, d3, d8, d13, 0.0, 0.0, 0.0);
            }
            int b = rand.nextInt(35)+5;
            for (int i = 0; i < b; i++) {
                double d3 = (double) pos.getX() + rand.nextDouble() + (rand.nextDouble()/6);
                double d8 = (double) pos.getY() + rand.nextDouble() + (rand.nextDouble()/6);
                double d13 = (double) pos.getZ() + rand.nextDouble() + (rand.nextDouble()/6);
                worldIn.addParticle(ParticleTypes.SMOKE, d3, d8, d13, 0.0, 0.0, 0.0);
            }
            if (rand.nextInt(64) == 0) {
                worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
            }
        }

    }
    //Idk what's up with the equation really but it's good to have here
    public static int getEquation(int x) {
        int i = 8;
        return (x * (x / 5)) / (25*i);
    }

    default int getAgeFromBlockstate(BlockState state) {
        try {
            return state.get(BURN_TIMER);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    default boolean getBurntFromblockstate(BlockState state) {
        try {
            return state.get(IS_BURNED);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    default IntegerProperty getAgeProperty(BlockState state) {
        return BURN_TIMER;
    }
    default void tickI(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int dist = getDistFromBlockstate(state);
        /*if (dist > 0) {
            //System.out.println("Got to line 92");
            //worldIn.spawnParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(), rand.nextInt(5)+5, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0);
        }*/
        if (getBurntFromblockstate(state)) {
            VoxelShape shapeIn = state.getShape(worldIn, pos);
            double d3 = (double)pos.getX() + Math.min(shapeIn.getBoundingBox().maxX, rand.nextDouble()) * (double)0.1F;
            double d8 = (double)pos.getY() + Math.min(shapeIn.getBoundingBox().maxY, rand.nextDouble());
            double d13 = (double)pos.getZ() + Math.min(shapeIn.getBoundingBox().maxZ, rand.nextDouble());
            worldIn.addParticle(ParticleTypes.LARGE_SMOKE, d3, d8, d13, 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(ParticleTypes.LAVA, d3, d8, d13, rand.nextInt(5)+1, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0);
            worldIn.spawnParticle(BetterBadlandsParticles.TWIG.get(), d3, d8, d13, rand.nextInt(10)+5, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0);

            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
            worldIn.playSound(null, pos, SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.PLAYERS, 1.0F, worldIn.rand.nextFloat() * 0.4F + 0.8F);

            for (Direction direction : Direction.values()) {
                BlockState stateo = worldIn.getBlockState(pos.offset(direction));
                if (stateo.getBlock().isFlammable(stateo, worldIn, pos, direction)) {
                    stateo.getBlock().catchFire(stateo, worldIn, pos, direction, null);
                    worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
            return;
        }

        if (dist > 0) {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, worldIn.rand.nextFloat() * 0.4F + 0.8F);
            if (dist < 21) {
                for (Direction dir : Direction.values()) {
                    BlockState stateo = worldIn.getBlockState(pos.offset(dir));
                    if (stateo.getBlock() instanceof IKindling) {
                        if (getDistFromBlockstate(stateo) == 0) {
                            worldIn.setBlockState(pos.offset(dir), stateo.with(getDistProperty(stateo), dist + 1));
                            worldIn.getPendingBlockTicks().scheduleTick(pos.offset(dir), stateo.getBlock(), TO_SCHEDULE);
                        }
                    } else if (stateo.getBlock() instanceof TNTBlock) {
                        stateo.getBlock().catchFire(stateo, worldIn, pos.offset(dir), dir, null);
                        worldIn.setBlockState(pos.offset(dir), Blocks.AIR.getDefaultState());
                    }
                }
            }
            worldIn.setBlockState(pos, state.with(IS_BURNED, true));
            worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), getEquation(dist));
            /*if (age >= (dist - 1) * 6) {
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
            } else {
                worldIn.setBlockState(pos, state.with(getAgeProperty(state), age));
            }*/
        }
    }

    default ActionResultType onBlockActivatedI(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        Item item = player.getHeldItem(handIn).getItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F, worldIn.rand.nextFloat() * 0.4F + 0.8F);
            worldIn.setBlockState(pos, state.with(getDistProperty(state),1));
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }
}
