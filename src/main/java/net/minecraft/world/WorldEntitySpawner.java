package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.optifine.BlockPosM;
import net.optifine.reflect.Reflector;


public final class WorldEntitySpawner
{
    private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
    private final Set<ChunkPos> eligibleChunksForSpawning = Sets.<ChunkPos>newHashSet();
    private Map<Class, EntityLiving> mapSampleEntitiesByClass = new HashMap<Class, EntityLiving>();
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;
    private int countChunkPos;

    /**
     * adds all chunks within the spawn radius of the players to eligibleChunksForSpawning. pars: the world,
     * hostileCreatures, passiveCreatures. returns number of eligible chunks.
     */
    public int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate)
    {
        if (!spawnHostileMobs && !spawnPeacefulMobs)
        {
            return 0;
        }
        else
        {
            boolean flag = true;
            EntityPlayer entityplayer = null;

            if (worldServerIn.playerEntities.size() == 1)
            {
                entityplayer = worldServerIn.playerEntities.get(0);

                if (this.eligibleChunksForSpawning.size() > 0 && entityplayer != null && entityplayer.chunkCoordX == this.lastPlayerChunkX && entityplayer.chunkCoordZ == this.lastPlayerChunkZ)
                {
                    flag = false;
                }
            }

            if (flag)
            {
                this.eligibleChunksForSpawning.clear();
                int i = 0;

                for (EntityPlayer entityplayer1 : worldServerIn.playerEntities)
                {
                    if (!entityplayer1.isSpectator())
                    {
                        int j = MathHelper.floor(entityplayer1.posX / 16.0D);
                        int k = MathHelper.floor(entityplayer1.posZ / 16.0D);
                        int l = 8;

                        for (int i1 = -8; i1 <= 8; ++i1)
                        {
                            for (int j1 = -8; j1 <= 8; ++j1)
                            {
                                boolean flag1 = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                                ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

                                if (!this.eligibleChunksForSpawning.contains(chunkpos))
                                {
                                    ++i;

                                    if (!flag1 && worldServerIn.getWorldBorder().contains(chunkpos))
                                    {
                                        PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

                                        if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers())
                                        {
                                            this.eligibleChunksForSpawning.add(chunkpos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                this.countChunkPos = i;

                if (entityplayer != null)
                {
                    this.lastPlayerChunkX = entityplayer.chunkCoordX;
                    this.lastPlayerChunkZ = entityplayer.chunkCoordZ;
                }
            }

            int k4 = 0;
            BlockPos blockpos1 = worldServerIn.getSpawnPoint();
            BlockPosM blockposm = new BlockPosM(0, 0, 0);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.values())
            {
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnOnSetTickRate))
                {
                    int l4 = worldServerIn.countEntities(enumcreaturetype.getCreatureClass());
                    int i5 = enumcreaturetype.getMaxNumberOfCreature() * this.countChunkPos / MOB_COUNT_DIV;

                    if (l4 <= i5)
                    {
                        Collection<ChunkPos> collection = this.eligibleChunksForSpawning;

                        label179:

                        for (ChunkPos chunkpos1 : collection)
                        {
                            BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkpos1.x, chunkpos1.z, blockposm);
                            int k1 = blockpos.getX();
                            int l1 = blockpos.getY();
                            int i2 = blockpos.getZ();
                            IBlockState iblockstate = worldServerIn.getBlockState(blockpos);

                            if (!iblockstate.isNormalCube())
                            {
                                int j2 = 0;

                                for (int k2 = 0; k2 < 3; ++k2)
                                {
                                    int l2 = k1;
                                    int i3 = l1;
                                    int j3 = i2;
                                    int k3 = 6;
                                    Biome.SpawnListEntry biome$spawnlistentry = null;
                                    IEntityLivingData ientitylivingdata = null;
                                    int l3 = MathHelper.ceil(Math.random() * 4.0D);

                                    for (int i4 = 0; i4 < l3; ++i4)
                                    {
                                        l2 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        i3 += worldServerIn.rand.nextInt(1) - worldServerIn.rand.nextInt(1);
                                        j3 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        blockpos$mutableblockpos.setPos(l2, i3, j3);
                                        float f = (float)l2 + 0.5F;
                                        float f1 = (float)j3 + 0.5F;

                                        if (!worldServerIn.isAnyPlayerWithinRangeAt((double)f, (double)i3, (double)f1, 24.0D) && blockpos1.distanceSq((double)f, (double)i3, (double)f1) >= 576.0D)
                                        {
                                            if (biome$spawnlistentry == null)
                                            {
                                                biome$spawnlistentry = worldServerIn.getSpawnListEntryForTypeAt(enumcreaturetype, blockpos$mutableblockpos);

                                                if (biome$spawnlistentry == null)
                                                {
                                                    break;
                                                }
                                            }

                                            if (worldServerIn.canCreatureTypeSpawnHere(enumcreaturetype, biome$spawnlistentry, blockpos$mutableblockpos) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(biome$spawnlistentry.entityClass), worldServerIn, blockpos$mutableblockpos))
                                            {
                                                EntityLiving entityliving;

                                                try
                                                {
                                                    entityliving = this.mapSampleEntitiesByClass.get(biome$spawnlistentry.entityClass);

                                                    if (entityliving == null)
                                                    {
                                                        entityliving = biome$spawnlistentry.entityClass.getConstructor(World.class).newInstance(worldServerIn);

                                                        this.mapSampleEntitiesByClass.put(biome$spawnlistentry.entityClass, entityliving);
                                                    }
                                                }
                                                catch (Exception exception)
                                                {
                                                    exception.printStackTrace();
                                                    return k4;
                                                }

                                                entityliving.setLocationAndAngles((double)f, (double)i3, (double)f1, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);
                                                boolean flag2 = entityliving.getCanSpawnHere() && entityliving.isNotColliding();

                                                if (flag2)
                                                {
                                                    this.mapSampleEntitiesByClass.remove(biome$spawnlistentry.entityClass);

                                                    if (entityliving.isNotColliding())
                                                    {
                                                        ++j2;
                                                        worldServerIn.spawnEntity(entityliving);
                                                    }
                                                    else
                                                    {
                                                        entityliving.setDead();
                                                    }

                                                    int j4 = entityliving.getMaxSpawnedInChunk();

                                                    if (j2 >= j4)
                                                    {
                                                        continue label179;
                                                    }
                                                }

                                                k4 += j2;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return k4;
        }
    }

    private static BlockPos getRandomChunkPosition(World worldIn, int x, int z)
    {
        Chunk chunk = worldIn.getChunk(x, z);
        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    private static BlockPosM getRandomChunkPosition(World p_getRandomChunkPosition_0_, int p_getRandomChunkPosition_1_, int p_getRandomChunkPosition_2_, BlockPosM p_getRandomChunkPosition_3_)
    {
        Chunk chunk = p_getRandomChunkPosition_0_.getChunk(p_getRandomChunkPosition_1_, p_getRandomChunkPosition_2_);
        int i = p_getRandomChunkPosition_1_ * 16 + p_getRandomChunkPosition_0_.rand.nextInt(16);
        int j = p_getRandomChunkPosition_2_ * 16 + p_getRandomChunkPosition_0_.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeightValue(i & 15, j & 15) + 1, 16);
        int l = p_getRandomChunkPosition_0_.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        p_getRandomChunkPosition_3_.setXyz(i, l, j);
        return p_getRandomChunkPosition_3_;
    }

    public static boolean isValidEmptySpawnBlock(IBlockState state)
    {
        if (state.isBlockNormalCube())
        {
            return false;
        }
        else if (state.canProvidePower())
        {
            return false;
        }
        else if (state.getMaterial().isLiquid())
        {
            return false;
        }
        else
        {
            return !BlockRailBase.isRailBlock(state);
        }
    }

    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType spawnPlacementTypeIn, World worldIn, BlockPos pos)
    {
        if (!worldIn.getWorldBorder().contains(pos))
        {
            return false;
        }
        else
        {
            return spawnPlacementTypeIn == null ? false : spawnPlacementTypeIn.canSpawnAt(worldIn, pos);
        }
    }

    public static boolean canCreatureTypeSpawnBody(EntityLiving.SpawnPlacementType p_canCreatureTypeSpawnBody_0_, World p_canCreatureTypeSpawnBody_1_, BlockPos p_canCreatureTypeSpawnBody_2_)
    {
        IBlockState iblockstate = p_canCreatureTypeSpawnBody_1_.getBlockState(p_canCreatureTypeSpawnBody_2_);

        if (p_canCreatureTypeSpawnBody_0_ == EntityLiving.SpawnPlacementType.IN_WATER)
        {
            return iblockstate.getMaterial() == Material.WATER && p_canCreatureTypeSpawnBody_1_.getBlockState(p_canCreatureTypeSpawnBody_2_.down()).getMaterial() == Material.WATER && !p_canCreatureTypeSpawnBody_1_.getBlockState(p_canCreatureTypeSpawnBody_2_.up()).isNormalCube();
        }
        else
        {
            BlockPos blockpos = p_canCreatureTypeSpawnBody_2_.down();
            IBlockState iblockstate1 = p_canCreatureTypeSpawnBody_1_.getBlockState(blockpos);

            if (!iblockstate1.isTopSolid())
            {
                return false;
            }
            else
            {
                Block block = p_canCreatureTypeSpawnBody_1_.getBlockState(blockpos).getBlock();
                boolean flag1 = block != Blocks.BEDROCK && block != Blocks.BARRIER;
                return flag1 && isValidEmptySpawnBlock(iblockstate) && isValidEmptySpawnBlock(p_canCreatureTypeSpawnBody_1_.getBlockState(p_canCreatureTypeSpawnBody_2_.up()));
            }
        }
    }

    /**
     * Called during chunk generation to spawn initial creatures.
     *  
     * @param centerX The X coordinate of the point to spawn mobs arround.
     * @param centerZ The Z coordinate of the point to spawn mobs arround.
     * @param diameterX The X diameter of the rectangle to spawn mobs in
     * @param diameterZ The Z diameter of the rectangle to spawn mobs in
     */
    public static void performWorldGenSpawning(World worldIn, Biome biomeIn, int centerX, int centerZ, int diameterX, int diameterZ, Random randomIn)
    {
        List<Biome.SpawnListEntry> list = biomeIn.getSpawnableList(EnumCreatureType.CREATURE);

        if (!list.isEmpty())
        {
            while (randomIn.nextFloat() < biomeIn.getSpawningChance())
            {
                Biome.SpawnListEntry biome$spawnlistentry = (Biome.SpawnListEntry)WeightedRandom.getRandomItem(worldIn.rand, list);
                int i = biome$spawnlistentry.minGroupCount + randomIn.nextInt(1 + biome$spawnlistentry.maxGroupCount - biome$spawnlistentry.minGroupCount);
                IEntityLivingData ientitylivingdata = null;
                int j = centerX + randomIn.nextInt(diameterX);
                int k = centerZ + randomIn.nextInt(diameterZ);
                int l = j;
                int i1 = k;

                for (int j1 = 0; j1 < i; ++j1)
                {
                    boolean flag = false;

                    for (int k1 = 0; !flag && k1 < 4; ++k1)
                    {
                        BlockPos blockpos = worldIn.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k));

                        if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, worldIn, blockpos))
                        {
                            EntityLiving entityliving;

                            try
                            {
                                entityliving = biome$spawnlistentry.entityClass.getConstructor(World.class).newInstance(worldIn);
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                continue;
                            }

                            entityliving.setLocationAndAngles((double)((float)j + 0.5F), (double)blockpos.getY(), (double)((float)k + 0.5F), randomIn.nextFloat() * 360.0F, 0.0F);
                            worldIn.spawnEntity(entityliving);
                            ientitylivingdata = entityliving.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);
                            flag = true;
                        }

                        j += randomIn.nextInt(5) - randomIn.nextInt(5);

                        for (k += randomIn.nextInt(5) - randomIn.nextInt(5); j < centerX || j >= centerX + diameterX || k < centerZ || k >= centerZ + diameterX; k = i1 + randomIn.nextInt(5) - randomIn.nextInt(5))
                        {
                            j = l + randomIn.nextInt(5) - randomIn.nextInt(5);
                        }
                    }
                }
            }
        }
    }
}
