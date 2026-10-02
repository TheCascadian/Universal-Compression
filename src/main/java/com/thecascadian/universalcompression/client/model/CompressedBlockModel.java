package com.thecascadian.universalcompression.client.model;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.extensions.IBakedModelExtension;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

public class CompressedBlockModel implements BakedModel, IBakedModelExtension {
   private static final FaceBakery FACE_BAKERY = new FaceBakery();
   private static final ResourceLocation MODEL_NAME = ResourceLocation.fromNamespaceAndPath("universalcompression", "compressed");
   private final BakedModel parentModel;
   private final TextureAtlasSprite baseSprite;
   private final TextureAtlasSprite overlaySprite;
   private final int tier;
   private final boolean useCutout;
   private final ItemTransforms transforms;
   private final List<BakedQuad>[] baseQuads;
   private final List<BakedQuad>[] overlayQuads;
   @Nullable
   private final ResourceLocation parentBlockId;
   private volatile BakedModel resolvedParent;
   private volatile BlockState resolvedParentState;

   public CompressedBlockModel(
      @Nullable BakedModel parentModel,
      @Nullable TextureAtlasSprite overlaySprite,
      @Nullable TextureAtlasSprite baseSprite,
      int tier,
      boolean useCutout,
      ItemTransforms transforms,
      @Nullable ResourceLocation parentBlockId
   ) {
      this.parentBlockId = parentBlockId;
      this.parentModel = parentModel;
      this.baseSprite = baseSprite;
      this.overlaySprite = overlaySprite;
      this.tier = tier;
      this.useCutout = useCutout;
      this.transforms = transforms;
      this.baseQuads = buildAllFaceQuads(baseSprite);
      this.overlayQuads = buildAllFaceQuads(overlaySprite);
   }

   /**
    * The parent block's real baked model, looked up lazily because the block model
    * shaper is not populated yet while this model is being baked. Using it keeps the
    * parent's own textures, face shading and transforms, whatever the parent block is.
    */
   @Nullable
   private BakedModel resolveParent() {
      BakedModel cached = this.resolvedParent;
      if (cached != null) {
         return cached;
      }
      if (this.parentBlockId == null) {
         return null;
      }
      Block block = BuiltInRegistries.BLOCK.get(this.parentBlockId);
      if (block == Blocks.AIR) {
         return null;
      }
      Minecraft mc = Minecraft.getInstance();
      BlockState parentState = block.defaultBlockState();
      BakedModel model = mc.getBlockRenderer().getBlockModelShaper().getBlockModel(parentState);
      if (model == null || model == mc.getModelManager().getMissingModel()) {
         return null;
      }
      this.resolvedParentState = parentState;
      this.resolvedParent = model;
      return model;
   }

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
      // overlay is pushed a hair outside the cube so it does not z-fight with the base faces
      Vector3f from = new Vector3f(-0.01F, -0.01F, -0.01F);
      Vector3f to = new Vector3f(16.01F, 16.01F, 16.01F);
      BlockFaceUV uv = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
      BlockElementFace face = new BlockElementFace(dir, -1, sprite.contents().name().toString(), uv);
      ModelState identityState = new ModelState() {
         public Transformation getRotation() {
            return Transformation.identity();
         }

         public boolean isUvLocked() {
            return false;
         }
      };
      return FACE_BAKERY.bakeQuad(from, to, face, sprite, dir, identityState, (BlockElementRotation)null, true);
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
      List<BakedQuad> result = new ArrayList<>();
      BakedModel real = this.resolveParent();
      if (real != null) {
         result.addAll(real.getQuads(this.resolvedParentState, side, rand));
         if (side != null && this.overlaySprite != null) {
            result.addAll(this.overlayQuads[side.ordinal()]);
         }
         return result;
      }
      if (side == null) {
         if (this.parentModel != null && this.baseSprite == null) {
            result.addAll(this.parentModel.getQuads(state, null, rand));
         }

         return result;
      } else {
         if (this.parentModel != null && this.baseSprite == null) {
            result.addAll(this.parentModel.getQuads(state, side, rand));
         } else if (this.baseSprite != null) {
            result.addAll(this.baseQuads[side.ordinal()]);
         }

         if (this.overlaySprite != null) {
            result.addAll(this.overlayQuads[side.ordinal()]);
         }

         return result;
      }
   }

   public ChunkRenderTypeSet getRenderTypes(@Nullable BlockState state, RandomSource rand, ModelData data) {
      return this.useCutout ? ChunkRenderTypeSet.of(new RenderType[]{RenderType.cutout()}) : BakedModel.super.getRenderTypes(state, rand, data);
   }

   public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
      // Items must use the entity-style block sheet: the raw chunk cutout layer has no
      // normal-based lighting, which is what flattened the shading in inventories.
      return this.useCutout
         ? List.of(net.neoforged.neoforge.client.RenderTypeHelper.getEntityRenderType(RenderType.cutout(), true))
         : BakedModel.super.getRenderTypes(itemStack, fabulous);
   }

   public boolean useAmbientOcclusion() {
      BakedModel real = this.resolveParent();
      if (real != null) {
         return real.useAmbientOcclusion();
      }
      return this.parentModel == null || this.parentModel.useAmbientOcclusion();
   }

   public boolean isGui3d() {
      return this.parentModel == null || this.parentModel.isGui3d();
   }

   public boolean usesBlockLight() {
      return this.parentModel == null || this.parentModel.usesBlockLight();
   }

   public boolean isCustomRenderer() {
      return this.parentModel != null && this.parentModel.isCustomRenderer();
   }

   public TextureAtlasSprite getParticleIcon() {
      BakedModel real = this.resolveParent();
      if (real != null) {
         return real.getParticleIcon();
      }
      if (this.parentModel != null) {
         return this.parentModel.getParticleIcon();
      } else {
         return this.baseSprite != null ? this.baseSprite : this.overlaySprite;
      }
   }

   public ItemOverrides getOverrides() {
      return this.parentModel != null ? this.parentModel.getOverrides() : ItemOverrides.EMPTY;
   }

   public ItemTransforms getTransforms() {
      BakedModel real = this.resolveParent();
      return real != null ? real.getTransforms() : this.transforms;
   }

   public int getTier() {
      return this.tier;
   }
}
