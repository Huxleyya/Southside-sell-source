package net.optifine;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.optifine.config.ConnectedParser;
import net.optifine.config.EntityClassLocator;
import net.optifine.config.IObjectLocator;
import net.optifine.config.ItemLocator;

import net.optifine.util.PropertiesOrdered;

public class DynamicLights
{
    private static DynamicLightsMap mapDynamicLights = new DynamicLightsMap();
    private static Map<Class, Integer> mapEntityLightLevels = new HashMap<Class, Integer>();
    private static Map<Item, Integer> mapItemLightLevels = new HashMap<Item, Integer>();
    private static long timeUpdateMs = 0L;
    private static final double MAX_DIST = 7.5D;
    private static final double MAX_DIST_SQ = 56.25D;
    private static final int LIGHT_LEVEL_MAX = 15;
    private static final int LIGHT_LEVEL_FIRE = 15;
    private static final int LIGHT_LEVEL_BLAZE = 10;
    private static final int LIGHT_LEVEL_MAGMA_CUBE = 8;
    private static final int LIGHT_LEVEL_MAGMA_CUBE_CORE = 13;
    private static final int LIGHT_LEVEL_GLOWSTONE_DUST = 8;
    private static final int LIGHT_LEVEL_PRISMARINE_CRYSTALS = 8;
    private static final DataParameter<ItemStack> PARAMETER_ITEM_STACK = new DataParameter<ItemStack>(6, DataSerializers.ITEM_STACK);
    private static boolean initialized;

    public static void entityAdded(Entity entityIn, RenderGlobal renderGlobal)
    {
    }

    public static void entityRemoved(Entity entityIn, RenderGlobal renderGlobal)
    {
        synchronized (mapDynamicLights)
        {
            DynamicLight dynamiclight = mapDynamicLights.remove(entityIn.getEntityId());

            if (dynamiclight != null)
            {
                dynamiclight.updateLitChunks(renderGlobal);
            }
        }
    }

    public static void update(RenderGlobal renderGlobal)
    {
        long i = System.currentTimeMillis();

        if (i >= timeUpdateMs + 50L)
        {
            timeUpdateMs = i;

            if (!initialized)
            {
                initialize();
            }

            synchronized (mapDynamicLights)
            {
                updateMapDynamicLights(renderGlobal);

                if (mapDynamicLights.size() > 0)
                {
                    List<DynamicLight> list = mapDynamicLights.valueList();

                    for (int j = 0; j < list.size(); ++j)
                    {
                        DynamicLight dynamiclight = list.get(j);
                        dynamiclight.update(renderGlobal);
                    }
                }
            }
        }
    }

    private static void initialize()
    {
        initialized = true;
        mapEntityLightLevels.clear();
        mapItemLightLevels.clear();

        if (mapEntityLightLevels.size() > 0)
        {
            Config.dbg("DynamicLights entities: " + mapEntityLightLevels.size());
        }

        if (mapItemLightLevels.size() > 0)
        {
            Config.dbg("DynamicLights items: " + mapItemLightLevels.size());
        }
    }

    private static void loadModConfiguration(InputStream in, String path, String modId)
    {
        if (in != null)
        {
            try
            {
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("DynamicLights: Parsing " + path);
                ConnectedParser connectedparser = new ConnectedParser("DynamicLights");
                loadModLightLevels(properties.getProperty("entities"), mapEntityLightLevels, new EntityClassLocator(), connectedparser, path, modId);
                loadModLightLevels(properties.getProperty("items"), mapItemLightLevels, new ItemLocator(), connectedparser, path, modId);
            }
            catch (IOException var5)
            {
                Config.warn("DynamicLights: Error reading " + path);
            }
        }
    }

    private static void loadModLightLevels(String prop, Map mapLightLevels, IObjectLocator ol, ConnectedParser cp, String path, String modId)
    {
        if (prop != null)
        {
            String[] astring = Config.tokenize(prop, " ");

            for (int i = 0; i < astring.length; ++i)
            {
                String s = astring[i];
                String[] astring1 = Config.tokenize(s, ":");

                if (astring1.length != 2)
                {
                    cp.warn("Invalid entry: " + s + ", in:" + path);
                }
                else
                {
                    String s1 = astring1[0];
                    String s2 = astring1[1];
                    String s3 = modId + ":" + s1;
                    ResourceLocation resourcelocation = new ResourceLocation(s3);
                    Object object = ol.getObject(resourcelocation);

                    if (object == null)
                    {
                        cp.warn("Object not found: " + s3);
                    }
                    else
                    {
                        int j = cp.parseInt(s2, -1);

                        if (j >= 0 && j <= 15)
                        {
                            mapLightLevels.put(object, j);
                        }
                        else
                        {
                            cp.warn("Invalid light level: " + s);
                        }
                    }
                }
            }
        }
    }

    private static void updateMapDynamicLights(RenderGlobal renderGlobal)
    {
        World world = renderGlobal.getWorld();

        if (world != null)
        {
            for (Entity entity : world.getLoadedEntityList())
            {
                int i = getLightLevel(entity);

                if (i > 0)
                {
                    int j = entity.getEntityId();
                    DynamicLight dynamiclight = mapDynamicLights.get(j);

                    if (dynamiclight == null)
                    {
                        dynamiclight = new DynamicLight(entity);
                        mapDynamicLights.put(j, dynamiclight);
                    }
                }
                else
                {
                    int k = entity.getEntityId();
                    DynamicLight dynamiclight1 = mapDynamicLights.remove(k);

                    if (dynamiclight1 != null)
                    {
                        dynamiclight1.updateLitChunks(renderGlobal);
                    }
                }
            }
        }
    }

    public static int getCombinedLight(BlockPos pos, int combinedLight)
    {
        double d0 = getLightLevel(pos);
        combinedLight = getCombinedLight(d0, combinedLight);
        return combinedLight;
    }

    public static int getCombinedLight(Entity entity, int combinedLight)
    {
        double d0 = (double)getLightLevel(entity);
        combinedLight = getCombinedLight(d0, combinedLight);
        return combinedLight;
    }

    public static int getCombinedLight(double lightPlayer, int combinedLight)
    {
        if (lightPlayer > 0.0D)
        {
            int i = (int)(lightPlayer * 16.0D);
            int j = combinedLight & 255;

            if (i > j)
            {
                combinedLight = combinedLight & -256;
                combinedLight = combinedLight | i;
            }
        }

        return combinedLight;
    }

    public static double getLightLevel(BlockPos pos)
    {
        double d0 = 0.0D;

        synchronized (mapDynamicLights)
        {
            List<DynamicLight> list = mapDynamicLights.valueList();
            int i = list.size();

            for (int j = 0; j < i; ++j)
            {
                DynamicLight dynamiclight = list.get(j);
                int k = dynamiclight.getLastLightLevel();

                if (k > 0)
                {
                    double d1 = dynamiclight.getLastPosX();
                    double d2 = dynamiclight.getLastPosY();
                    double d3 = dynamiclight.getLastPosZ();
                    double d4 = (double)pos.getX() - d1;
                    double d5 = (double)pos.getY() - d2;
                    double d6 = (double)pos.getZ() - d3;
                    double d7 = d4 * d4 + d5 * d5 + d6 * d6;

                    if (dynamiclight.isUnderwater() && !Config.isClearWater())
                    {
                        k = Config.limit(k - 2, 0, 15);
                        d7 *= 2.0D;
                    }

                    if (d7 <= 56.25D)
                    {
                        double d8 = Math.sqrt(d7);
                        double d9 = 1.0D - d8 / 7.5D;
                        double d10 = d9 * (double)k;

                        if (d10 > d0)
                        {
                            d0 = d10;
                        }
                    }
                }
            }
        }

        double d11 = Config.limit(d0, 0.0D, 15.0D);
        return d11;
    }

    public static int getLightLevel(ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return 0;
        }
        else
        {
            Item item = itemStack.getItem();

            if (item instanceof ItemBlock)
            {
                ItemBlock itemblock = (ItemBlock)item;
                Block block = itemblock.getBlock();

                if (block != null)
                {
                    return block.getLightValue(block.getDefaultState());
                }
            }

            if (item == Items.LAVA_BUCKET)
            {
                return Blocks.LAVA.getLightValue(Blocks.LAVA.getDefaultState());
            }
            else if (item != Items.BLAZE_ROD && item != Items.BLAZE_POWDER)
            {
                if (item == Items.GLOWSTONE_DUST)
                {
                    return 8;
                }
                else if (item == Items.PRISMARINE_CRYSTALS)
                {
                    return 8;
                }
                else if (item == Items.MAGMA_CREAM)
                {
                    return 8;
                }
                else if (item == Items.NETHER_STAR)
                {
                    return Blocks.BEACON.getLightValue(Blocks.BEACON.getDefaultState()) / 2;
                }
                else
                {
                    if (!mapItemLightLevels.isEmpty())
                    {
                        Integer integer = mapItemLightLevels.get(item);

                        if (integer != null)
                        {
                            return integer.intValue();
                        }
                    }

                    return 0;
                }
            }
            else
            {
                return 10;
            }
        }
    }

    public static int getLightLevel(Entity entity)
    {
        if (entity == Config.getMinecraft().getRenderViewEntity() && !Config.isDynamicHandLight())
        {
            return 0;
        }
        else
        {
            if (entity instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entity;

                if (entityplayer.isSpectator())
                {
                    return 0;
                }
            }

            if (entity.isBurning())
            {
                return 15;
            }
            else
            {
                if (!mapEntityLightLevels.isEmpty())
                {
                    Integer integer = mapEntityLightLevels.get(entity.getClass());

                    if (integer != null)
                    {
                        return integer.intValue();
                    }
                }

                if (entity instanceof EntityFireball)
                {
                    return 15;
                }
                else if (entity instanceof EntityTNTPrimed)
                {
                    return 15;
                }
                else if (entity instanceof EntityBlaze)
                {
                    EntityBlaze entityblaze = (EntityBlaze)entity;
                    return entityblaze.isCharged() ? 15 : 10;
                }
                else if (entity instanceof EntityMagmaCube)
                {
                    EntityMagmaCube entitymagmacube = (EntityMagmaCube)entity;
                    return (double)entitymagmacube.squishFactor > 0.6D ? 13 : 8;
                }
                else
                {
                    if (entity instanceof EntityCreeper)
                    {
                        EntityCreeper entitycreeper = (EntityCreeper)entity;

                        if ((double)entitycreeper.getCreeperFlashIntensity(0.0F) > 0.001D)
                        {
                            return 15;
                        }
                    }

                    if (entity instanceof EntityLivingBase)
                    {
                        EntityLivingBase entitylivingbase = (EntityLivingBase)entity;
                        ItemStack itemstack3 = entitylivingbase.getHeldItemMainhand();
                        int i = getLightLevel(itemstack3);
                        ItemStack itemstack1 = entitylivingbase.getHeldItemOffhand();
                        int j = getLightLevel(itemstack1);
                        ItemStack itemstack2 = entitylivingbase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                        int k = getLightLevel(itemstack2);
                        int l = Math.max(i, j);
                        return Math.max(l, k);
                    }
                    else if (entity instanceof EntityItem)
                    {
                        EntityItem entityitem = (EntityItem)entity;
                        ItemStack itemstack = getItemStack(entityitem);
                        return getLightLevel(itemstack);
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
        }
    }

    public static void removeLights(RenderGlobal renderGlobal)
    {
        synchronized (mapDynamicLights)
        {
            List<DynamicLight> list = mapDynamicLights.valueList();

            for (int i = 0; i < list.size(); ++i)
            {
                DynamicLight dynamiclight = list.get(i);
                dynamiclight.updateLitChunks(renderGlobal);
            }

            mapDynamicLights.clear();
        }
    }

    public static void clear()
    {
        synchronized (mapDynamicLights)
        {
            mapDynamicLights.clear();
        }
    }

    public static int getCount()
    {
        synchronized (mapDynamicLights)
        {
            return mapDynamicLights.size();
        }
    }

    public static ItemStack getItemStack(EntityItem entityItem)
    {
        ItemStack itemstack = (ItemStack)entityItem.getDataManager().get(PARAMETER_ITEM_STACK);
        return itemstack;
    }
}
