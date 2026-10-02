package com.thecascadian.universalcompression.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.thecascadian.universalcompression.UniversalCompression;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class CompressedBlockModelLoader implements IGeometryLoader<CompressedBlockModelLoader.Geometry> {
   public static final String ID = "compressed";

   public CompressedBlockModelLoader.Geometry read(JsonObject json, JsonDeserializationContext ctx) {
      ResourceLocation parentModel = json.has("parent") ? ResourceLocation.parse(json.get("parent").getAsString()) : null;
      boolean useCutout = false;
      if (json.has("render_type")) {
         String rt = json.get("render_type").getAsString();
         useCutout = rt.contains("cutout");
      }

      ResourceLocation textureLocation = null;
      ResourceLocation overlayLocation = null;
      if (json.has("textures")) {
         JsonObject textures = json.getAsJsonObject("textures");
         if (textures.has("texture")) {
            textureLocation = ResourceLocation.parse(textures.get("texture").getAsString());
         }

         if (textures.has("overlay")) {
            overlayLocation = ResourceLocation.parse(textures.get("overlay").getAsString());
         }
      }

      ResourceLocation parentBlock = json.has("parent_block") ? ResourceLocation.parse(json.get("parent_block").getAsString()) : null;
      int tier = json.get("tier").getAsInt();
      if (overlayLocation == null) {
         overlayLocation = ResourceLocation.fromNamespaceAndPath("universalcompression", "block/tier_overlay_" + tier);
      }

      return new CompressedBlockModelLoader.Geometry(parentModel, textureLocation, overlayLocation, tier, useCutout, parentBlock);
   }

   public static class Geometry implements IUnbakedGeometry<CompressedBlockModelLoader.Geometry> {
      private final ResourceLocation parentModel;
      private final ResourceLocation textureLocation;
      private final ResourceLocation overlayLocation;
      private final int tier;
      private final boolean useCutout;
      private final ResourceLocation parentBlock;

      public Geometry(ResourceLocation parentModel, ResourceLocation textureLocation, ResourceLocation overlayLocation, int tier, boolean useCutout, ResourceLocation parentBlock) {
         this.parentBlock = parentBlock;
         this.parentModel = parentModel;
         this.textureLocation = textureLocation;
         this.overlayLocation = overlayLocation;
         this.tier = tier;
         this.useCutout = useCutout;
      }

      public BakedModel bake(
         IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides
      ) {
         TextureAtlasSprite baseSprite = null;
         BakedModel bakedParent = null;
         ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
         if (this.textureLocation != null) {
            baseSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, this.textureLocation));
            if (baseSprite.contents().name().toString().contains("missing")) {
               UniversalCompression.LOGGER.warn("Base texture {} (tier {}) resolved to missing sprite", this.textureLocation, this.tier);
            }

            BakedModel cube = baker.bake(ResourceLocation.parse("minecraft:block/cube_all"), modelState);
            transforms = cube.getTransforms();
         } else if (this.parentModel != null) {
            bakedParent = baker.bake(this.parentModel, modelState);
            transforms = bakedParent.getTransforms();
         }

         TextureAtlasSprite overlaySprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, this.overlayLocation));
         if (overlaySprite.contents().name().toString().contains("missing")) {
            UniversalCompression.LOGGER.warn("Overlay texture {} (tier {}) resolved to missing sprite", this.overlayLocation, this.tier);
         }

         return new CompressedBlockModel(bakedParent, overlaySprite, baseSprite, this.tier, this.useCutout, transforms, this.parentBlock);
      }

      public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
         if (this.parentModel != null) {
            modelGetter.apply(this.parentModel);
         }
      }
   }
}
