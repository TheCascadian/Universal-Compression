package com.thecascadian.universalcompression.client.model;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.neoforged.neoforge.client.extensions.IBakedModelExtension;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;

public class CompressedBlockModel implements BakedModel, IBakedModelExtension {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ResourceLocation MODEL_NAME = ResourceLocation.fromNamespaceAndPath("universalcompression",
            "compressed");

    private final BakedModel parentModel;
    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite overlaySprite;
    private final int tier;
    /** whether this model (or one of its textures) should be rendered using the cutout layer */
    private final boolean useCutout;
    /** item transforms to use for held/inventory rendering */
    private final ItemTransforms transforms;

    // Pre-built quads indexed by Direction.ordinal()
    private final List<BakedQuad>[] baseQuads;
    private final List<BakedQuad>[] overlayQuads;

    public CompressedBlockModel(@Nullable BakedModel parentModel,
            @Nullable TextureAtlasSprite overlaySprite,
            @Nullable TextureAtlasSprite baseSprite,
            int tier,
            boolean useCutout,
            ItemTransforms transforms) {
        this.parentModel = parentModel;
        this.baseSprite = baseSprite;
        this.overlaySprite = overlaySprite;
        this.tier = tier;
        this.useCutout = useCutout;
        this.transforms = transforms;
        this.baseQuads = buildAllFaceQuads(baseSprite);
        this.overlayQuads = buildAllFaceQuads(overlaySprite);
    }

    @SuppressWarnings("unchecked")
    private static List<BakedQuad>[] buildAllFaceQuads(@Nullable TextureAtlasSprite sprite) {
        List<BakedQuad>[] result = new List[6];
        for (Direction dir : Direction.values()) {
            List<BakedQuad> list = new ArrayList<>(1);
            if (sprite != null) {
                list.add(bakeFaceQuad(dir, sprite));
            }
            result[dir.ordinal()] = list;
        }
        return result;
    }

    private static BakedQuad bakeFaceQuad(Direction dir, TextureAtlasSprite sprite) {
        Vector3f from = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f to   = new Vector3f(16.0f, 16.0f, 16.0f);

        BlockFaceUV uv = new BlockFaceUV(new float[] { 0.0f, 0.0f, 16.0f, 16.0f }, 0);
        BlockElementFace face = new BlockElementFace(dir, -1, sprite.contents().name().toString(), uv);

        ModelState identityState = new ModelState() {
            @Override
            public Transformation getRotation() {
                return Transformation.identity();
            }

            @Override
            public boolean isUvLocked() {
                return false;
            }
        };

        return FACE_BAKERY.bakeQuad(
                from,
                to,
                face,
                sprite,
                dir,
                identityState,
                (BlockElementRotation) null,
                true
        );
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state,
            @Nullable Direction side,
            RandomSource rand) {
        List<BakedQuad> result = new ArrayList<>();

        if (side == null) {
            // Null side = unculled pass used by item rendering.
            // Cube models normally return empty here; directional quads cover items too.
            if (parentModel != null && baseSprite == null) {
                result.addAll(parentModel.getQuads(state, null, rand));
            }
            return result;
        }

        // Directional pass — world block rendering and item face rendering.
        if (parentModel != null && baseSprite == null) {
            result.addAll(parentModel.getQuads(state, side, rand));
        } else if (baseSprite != null) {
            result.addAll(baseQuads[side.ordinal()]);
        }
        if (overlaySprite != null) {
            result.addAll(overlayQuads[side.ordinal()]);
        }
        return result;
    }

    // Render type control ---------------------------------------------------
    @Override
    public ChunkRenderTypeSet getRenderTypes(@Nullable BlockState state, RandomSource rand, net.neoforged.neoforge.client.model.data.ModelData data) {
        if (useCutout) {
            return ChunkRenderTypeSet.of(RenderType.cutout());
        }
        // delegate to the default behaviour (queried from ItemBlockRenderTypes)
        return BakedModel.super.getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        if (useCutout) {
            return List.of(RenderType.cutout());
        }
        return BakedModel.super.getRenderTypes(itemStack, fabulous);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return parentModel == null || parentModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parentModel == null || parentModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return parentModel == null || parentModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return parentModel != null && parentModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        if (parentModel != null)
            return parentModel.getParticleIcon();
        return baseSprite != null ? baseSprite : overlaySprite;
    }

    @Override
    public ItemOverrides getOverrides() {
        return parentModel != null ? parentModel.getOverrides() : ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    public int getTier() {
        return tier;
    }
}