package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.shaders.Shaders;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;

public class TextureAtlasSprite
{
    private final String iconName;
    protected List<int[][]> framesTextureData = Lists.<int[][]>newArrayList();
    protected int[][] interpolatedFrameData;
    private AnimationMetadataSection animationMetadata;
    protected boolean rotated;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    protected int frameCounter;
    protected int tickCounter;
    private int indexInMap = -1;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    public int glSpriteTextureId = -1;
    public TextureAtlasSprite spriteSingle = null;
    public boolean isSpriteSingle = false;
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public boolean isShadersSprite = false;
    public boolean isDependencyParent = false;
    public boolean isEmissive = false;
    public TextureAtlasSprite spriteEmissive = null;
    private int animationIndex = -1;
    private boolean animationActive = false;

    private TextureAtlasSprite(String p_i10_1_, boolean p_i10_2_)
    {
        this.iconName = p_i10_1_;
        this.isSpriteSingle = p_i10_2_;
    }

    public TextureAtlasSprite(String spriteName)
    {
        this.iconName = spriteName;

        if (Config.isMultiTexture())
        {
            this.spriteSingle = new TextureAtlasSprite(this.getIconName() + ".spriteSingle", true);
        }
    }

    protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation spriteResourceLocation)
    {
        return new TextureAtlasSprite(spriteResourceLocation.toString());
    }

    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        this.originX = originInX;
        this.originY = originInY;
        this.rotated = rotatedIn;
        float f = (float)(0.009999999776482582D / (double)inX);
        float f1 = (float)(0.009999999776482582D / (double)inY);
        this.minU = (float)originInX / (float)((double)inX) + f;
        this.maxU = (float)(originInX + this.width) / (float)((double)inX) - f;
        this.minV = (float)originInY / (float)inY + f1;
        this.maxV = (float)(originInY + this.height) / (float)inY - f1;
        this.baseU = Math.min(this.minU, this.maxU);
        this.baseV = Math.min(this.minV, this.maxV);

        if (this.spriteSingle != null)
        {
            this.spriteSingle.initSprite(this.width, this.height, 0, 0, false);
        }

        if (this.spriteNormal != null)
        {
            this.spriteNormal.copyFrom(this);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.copyFrom(this);
        }
    }

    public void copyFrom(TextureAtlasSprite atlasSpirit)
    {
        this.originX = atlasSpirit.originX;
        this.originY = atlasSpirit.originY;
        this.width = atlasSpirit.width;
        this.height = atlasSpirit.height;
        this.rotated = atlasSpirit.rotated;
        this.minU = atlasSpirit.minU;
        this.maxU = atlasSpirit.maxU;
        this.minV = atlasSpirit.minV;
        this.maxV = atlasSpirit.maxV;

        if (atlasSpirit != Config.getTextureMap().getMissingSprite())
        {
            this.indexInMap = atlasSpirit.indexInMap;
        }

        this.baseU = atlasSpirit.baseU;
        this.baseV = atlasSpirit.baseV;
        this.sheetWidth = atlasSpirit.sheetWidth;
        this.sheetHeight = atlasSpirit.sheetHeight;
        this.glSpriteTextureId = atlasSpirit.glSpriteTextureId;
        this.mipmapLevels = atlasSpirit.mipmapLevels;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.initSprite(this.width, this.height, 0, 0, false);
        }

        this.animationIndex = atlasSpirit.animationIndex;
    }

    /**
     * Returns the X position of this icon on its texture sheet, in pixels.
     */
    public int getOriginX()
    {
        return this.originX;
    }

    /**
     * Returns the Y position of this icon on its texture sheet, in pixels.
     */
    public int getOriginY()
    {
        return this.originY;
    }

    /**
     * Returns the width of the icon, in pixels.
     */
    public int getIconWidth()
    {
        return this.width;
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    public int getIconHeight()
    {
        return this.height;
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    public float getMinU()
    {
        return this.minU;
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    public float getMaxU()
    {
        return this.maxU;
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    public float getInterpolatedU(double u)
    {
        float f = this.maxU - this.minU;
        return this.minU + f * (float)u / 16.0F;
    }

    /**
     * The opposite of getInterpolatedU. Takes the return value of that method and returns the input to it.
     */
    public float getUnInterpolatedU(float u)
    {
        float f = this.maxU - this.minU;
        return (u - this.minU) / f * 16.0F;
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    public float getMinV()
    {
        return this.minV;
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    public float getMaxV()
    {
        return this.maxV;
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    public float getInterpolatedV(double v)
    {
        float f = this.maxV - this.minV;
        return this.minV + f * (float)v / 16.0F;
    }

    /**
     * The opposite of getInterpolatedV. Takes the return value of that method and returns the input to it.
     */
    public float getUnInterpolatedV(float p_188536_1_)
    {
        float f = this.maxV - this.minV;
        return (p_188536_1_ - this.minV) / f * 16.0F;
    }

    public String getIconName()
    {
        return this.iconName;
    }

    public void updateAnimation()
    {
        if (this.animationMetadata != null)
        {
            this.animationActive = SmartAnimations.isActive() ? SmartAnimations.isSpriteRendered(this.animationIndex) : true;
            ++this.tickCounter;

            if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
            {
                int i = this.animationMetadata.getFrameIndex(this.frameCounter);
                int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
                this.frameCounter = (this.frameCounter + 1) % j;
                this.tickCounter = 0;
                int k = this.animationMetadata.getFrameIndex(this.frameCounter);
                boolean flag = false;
                boolean flag1 = this.isSpriteSingle;

                if (!this.animationActive)
                {
                    return;
                }

                if (i != k && k >= 0 && k < this.framesTextureData.size())
                {
                    TextureUtil.uploadTextureMipmap(this.framesTextureData.get(k), this.width, this.height, this.originX, this.originY, flag, flag1);
                }
            }
            else if (this.animationMetadata.isInterpolate())
            {
                if (!this.animationActive)
                {
                    return;
                }

                this.updateAnimationInterpolated();
            }
        }
    }

    private void updateAnimationInterpolated()
    {
        double d0 = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
        int i = this.animationMetadata.getFrameIndex(this.frameCounter);
        int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
        int k = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % j);

        if (i != k && k >= 0 && k < this.framesTextureData.size())
        {
            int[][] aint = this.framesTextureData.get(i);
            int[][] aint1 = this.framesTextureData.get(k);

            if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != aint.length)
            {
                this.interpolatedFrameData = new int[aint.length][];
            }

            for (int l = 0; l < aint.length; ++l)
            {
                if (this.interpolatedFrameData[l] == null)
                {
                    this.interpolatedFrameData[l] = new int[aint[l].length];
                }

                if (l < aint1.length && aint1[l].length == aint[l].length)
                {
                    for (int i1 = 0; i1 < aint[l].length; ++i1)
                    {
                        int j1 = aint[l][i1];
                        int k1 = aint1[l][i1];
                        int l1 = this.interpolateColor(d0, j1 >> 16 & 255, k1 >> 16 & 255);
                        int i2 = this.interpolateColor(d0, j1 >> 8 & 255, k1 >> 8 & 255);
                        int j2 = this.interpolateColor(d0, j1 & 255, k1 & 255);
                        this.interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
                    }
                }
            }

            TextureUtil.uploadTextureMipmap(this.interpolatedFrameData, this.width, this.height, this.originX, this.originY, false, false);
        }
    }

    private int interpolateColor(double p_188535_1_, int p_188535_3_, int p_188535_4_)
    {
        return (int)(p_188535_1_ * (double)p_188535_3_ + (1.0D - p_188535_1_) * (double)p_188535_4_);
    }

    public int[][] getFrameTextureData(int index)
    {
        return this.framesTextureData.get(index);
    }

    public int getFrameCount()
    {
        return this.framesTextureData.size();
    }

    public void setIconWidth(int newWidth)
    {
        this.width = newWidth;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setIconWidth(this.width);
        }
    }

    public void setIconHeight(int newHeight)
    {
        this.height = newHeight;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setIconHeight(this.height);
        }
    }

    public void loadSprite(PngSizeInfo sizeInfo, boolean p_188538_2_) throws IOException
    {
        this.resetSprite();
        this.width = sizeInfo.pngWidth;
        this.height = sizeInfo.pngHeight;

        if (p_188538_2_)
        {
            this.height = this.width;
        }
        else if (sizeInfo.pngHeight != sizeInfo.pngWidth)
        {
            throw new RuntimeException("broken aspect ratio and not an animation");
        }

        if (this.spriteSingle != null)
        {
            this.spriteSingle.width = this.width;
            this.spriteSingle.height = this.height;
        }
    }

    public void loadSpriteFrames(IResource resource, int mipmaplevels) throws IOException
    {
        BufferedImage bufferedimage = TextureUtil.readBufferedImage(resource.getInputStream());

        if (this.width != bufferedimage.getWidth())
        {
            bufferedimage = TextureUtils.scaleImage(bufferedimage, this.width);
        }

        AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)resource.getMetadata("animation");
        int[][] aint = new int[mipmaplevels][];
        aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
        bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[0], 0, bufferedimage.getWidth());

        if (animationmetadatasection == null)
        {
            this.framesTextureData.add(aint);
        }
        else
        {
            int i = bufferedimage.getHeight() / this.width;

            if (animationmetadatasection.getFrameCount() > 0)
            {

                for (Integer integer : animationmetadatasection.getFrameIndexSet()) {
                    int j = integer;

                    if (j >= i) {
                        throw new RuntimeException("invalid frameindex " + j);
                    }

                    this.allocateFrameTextureData(j);
                    this.framesTextureData.set(j, getFrameTextureData(aint, this.width, this.width, j));
                }

                this.animationMetadata = animationmetadatasection;
            }
            else
            {
                List<AnimationFrame> list = Lists.<AnimationFrame>newArrayList();

                for (int l = 0; l < i; ++l)
                {
                    this.framesTextureData.add(getFrameTextureData(aint, this.width, this.width, l));
                    list.add(new AnimationFrame(l, -1));
                }

                this.animationMetadata = new AnimationMetadataSection(list, this.width, this.height, animationmetadatasection.getFrameTime(), animationmetadatasection.isInterpolate());
            }
        }

        if (!this.isShadersSprite)
        {
            if (Config.isShaders())
            {
                this.loadShadersSprites();
            }

            for (int[][] aint2 : this.framesTextureData) {
                if (aint2 != null && !this.iconName.startsWith("minecraft:blocks/leaves_")) {
                    for (int[] aint1 : aint2) {
                        this.fixTransparentColor(aint1);
                    }
                }
            }

            if (this.spriteSingle != null)
            {
                IResource iresource = Config.getResourceManager().getResource(resource.getResourceLocation());
                this.spriteSingle.loadSpriteFrames(iresource, mipmaplevels);
            }
        }
        if (bufferedimage instanceof AutoCloseable autoCloseable) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void generateMipmaps(int level)
    {
        List<int[][]> list = Lists.<int[][]>newArrayList();

        for (int i = 0; i < this.framesTextureData.size(); ++i)
        {
            final int[][] aint = this.framesTextureData.get(i);

            if (aint != null)
            {
                try
                {
                    list.add(TextureUtil.generateMipmapData(level, this.width, aint));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
                    crashreportcategory.addCrashSection("Frame index", Integer.valueOf(i));
                    crashreportcategory.addDetail("Frame sizes", new ICrashReportDetail<String>()
                    {
                        public String call() throws Exception
                        {
                            StringBuilder stringbuilder = new StringBuilder();

                            for (int[] aint1 : aint)
                            {
                                if (stringbuilder.length() > 0)
                                {
                                    stringbuilder.append(", ");
                                }

                                stringbuilder.append(aint1 == null ? "null" : aint1.length);
                            }

                            return stringbuilder.toString();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        this.setFramesTextureData(list);

        if (this.spriteSingle != null)
        {
            this.spriteSingle.generateMipmaps(level);
        }
    }

    private void allocateFrameTextureData(int index)
    {
        if (this.framesTextureData.size() <= index)
        {
            for (int i = this.framesTextureData.size(); i <= index; ++i)
            {
                this.framesTextureData.add(null);
            }
        }

        if (this.spriteSingle != null)
        {
            this.spriteSingle.allocateFrameTextureData(index);
        }
    }

    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int p_147962_3_)
    {
        int[][] aint = new int[data.length][];

        for (int i = 0; i < data.length; ++i)
        {
            int[] aint1 = data[i];

            if (aint1 != null)
            {
                aint[i] = new int[(rows >> i) * (columns >> i)];
                System.arraycopy(aint1, p_147962_3_ * aint[i].length, aint[i], 0, aint[i].length);
            }
        }

        return aint;
    }

    public void clearFramesTextureData()
    {
        this.framesTextureData.clear();

        if (this.spriteSingle != null)
        {
            this.spriteSingle.clearFramesTextureData();
        }
    }

    public boolean hasAnimationMetadata()
    {
        return this.animationMetadata != null;
    }

    public void setFramesTextureData(List<int[][]> newFramesTextureData)
    {
        this.framesTextureData = newFramesTextureData;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setFramesTextureData(newFramesTextureData);
        }
    }

    private void resetSprite()
    {
        this.animationMetadata = null;
        this.setFramesTextureData(Lists.newArrayList());
        this.frameCounter = 0;
        this.tickCounter = 0;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.resetSprite();
        }
    }

    public String toString()
    {
        return "TextureAtlasSprite{name='" + this.iconName + '\'' + ", frameCount=" + this.framesTextureData.size() + ", rotated=" + this.rotated + ", x=" + this.originX + ", y=" + this.originY + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0=" + this.minV + ", v1=" + this.maxV + '}';
    }

    public boolean hasCustomLoader(IResourceManager p_hasCustomLoader_1_, ResourceLocation p_hasCustomLoader_2_)
    {
        return false;
    }

    public boolean load(IResourceManager p_load_1_, ResourceLocation p_load_2_, Function<ResourceLocation, TextureAtlasSprite> p_load_3_)
    {
        return true;
    }

    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.<ResourceLocation>of();
    }

    public int getIndexInMap()
    {
        return this.indexInMap;
    }

    public void setIndexInMap(int p_setIndexInMap_1_)
    {
        this.indexInMap = p_setIndexInMap_1_;
    }

    public void updateIndexInMap(CounterInt p_updateIndexInMap_1_)
    {
        if (this.indexInMap < 0)
        {
            this.indexInMap = p_updateIndexInMap_1_.nextValue();
        }
    }

    public int getAnimationIndex()
    {
        return this.animationIndex;
    }

    public void setAnimationIndex(int p_setAnimationIndex_1_)
    {
        this.animationIndex = p_setAnimationIndex_1_;

        if (this.spriteNormal != null)
        {
            this.spriteNormal.setAnimationIndex(p_setAnimationIndex_1_);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.setAnimationIndex(p_setAnimationIndex_1_);
        }
    }

    public boolean isAnimationActive()
    {
        return this.animationActive;
    }

    private void fixTransparentColor(int[] p_fixTransparentColor_1_)
    {
        if (p_fixTransparentColor_1_ != null)
        {
            long i = 0L;
            long j = 0L;
            long k = 0L;
            long l = 0L;

            for (int i1 = 0; i1 < p_fixTransparentColor_1_.length; ++i1)
            {
                int j1 = p_fixTransparentColor_1_[i1];
                int k1 = j1 >> 24 & 255;

                if (k1 >= 16)
                {
                    int l1 = j1 >> 16 & 255;
                    int i2 = j1 >> 8 & 255;
                    int j2 = j1 & 255;
                    i += (long)l1;
                    j += (long)i2;
                    k += (long)j2;
                    ++l;
                }
            }

            if (l > 0L)
            {
                int l2 = (int)(i / l);
                int i3 = (int)(j / l);
                int j3 = (int)(k / l);
                int k3 = l2 << 16 | i3 << 8 | j3;

                for (int l3 = 0; l3 < p_fixTransparentColor_1_.length; ++l3)
                {
                    int i4 = p_fixTransparentColor_1_[l3];
                    int k2 = i4 >> 24 & 255;

                    if (k2 <= 16)
                    {
                        p_fixTransparentColor_1_[l3] = k3;
                    }
                }
            }
        }
    }

    public double getSpriteU16(float p_getSpriteU16_1_)
    {
        float f = this.maxU - this.minU;
        return (double)((p_getSpriteU16_1_ - this.minU) / f * 16.0F);
    }

    public double getSpriteV16(float p_getSpriteV16_1_)
    {
        float f = this.maxV - this.minV;
        return (double)((p_getSpriteV16_1_ - this.minV) / f * 16.0F);
    }

    public void bindSpriteTexture()
    {
        if (this.glSpriteTextureId < 0)
        {
            this.glSpriteTextureId = TextureUtil.glGenTextures();
            TextureUtil.allocateTextureImpl(this.glSpriteTextureId, this.mipmapLevels, this.width, this.height);
            TextureUtils.applyAnisotropicLevel();
        }

        TextureUtils.bindTexture(this.glSpriteTextureId);
    }

    public void deleteSpriteTexture()
    {
        if (this.glSpriteTextureId >= 0)
        {
            TextureUtil.deleteTexture(this.glSpriteTextureId);
            this.glSpriteTextureId = -1;
        }
    }

    public float toSingleU(float p_toSingleU_1_)
    {
        p_toSingleU_1_ = p_toSingleU_1_ - this.baseU;
        float f = (float)this.sheetWidth / (float)this.width;
        p_toSingleU_1_ = p_toSingleU_1_ * f;
        return p_toSingleU_1_;
    }

    public float toSingleV(float p_toSingleV_1_)
    {
        p_toSingleV_1_ = p_toSingleV_1_ - this.baseV;
        float f = (float)this.sheetHeight / (float)this.height;
        p_toSingleV_1_ = p_toSingleV_1_ * f;
        return p_toSingleV_1_;
    }

    public List<int[][]> getFramesTextureData()
    {
        List<int[][]> list = new ArrayList<int[][]>();
        list.addAll(this.framesTextureData);
        return list;
    }

    public AnimationMetadataSection getAnimationMetadata()
    {
        return this.animationMetadata;
    }

    public void setAnimationMetadata(AnimationMetadataSection p_setAnimationMetadata_1_)
    {
        this.animationMetadata = p_setAnimationMetadata_1_;
    }

    private void loadShadersSprites()
    {
        if (Shaders.configNormalMap)
        {
            String s = this.iconName + "_n";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            resourcelocation = Config.getTextureMap().completeResourceLocation(resourcelocation);

            if (Config.hasResource(resourcelocation))
            {
                this.spriteNormal = new TextureAtlasSprite(s);
                this.spriteNormal.isShadersSprite = true;
                this.spriteNormal.copyFrom(this);
                Config.getTextureMap().generateMipmaps(Config.getResourceManager(), this.spriteNormal);
            }
        }

        if (Shaders.configSpecularMap)
        {
            String s1 = this.iconName + "_s";
            ResourceLocation resourcelocation1 = new ResourceLocation(s1);
            resourcelocation1 = Config.getTextureMap().completeResourceLocation(resourcelocation1);

            if (Config.hasResource(resourcelocation1))
            {
                this.spriteSpecular = new TextureAtlasSprite(s1);
                this.spriteSpecular.isShadersSprite = true;
                this.spriteSpecular.copyFrom(this);
                Config.getTextureMap().generateMipmaps(Config.getResourceManager(), this.spriteSpecular);
            }
        }
    }
}
