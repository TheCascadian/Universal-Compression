package com.thecascadian.universalcompression.client.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class CompressedUnbakedModel implements UnbakedModel {
   private final ResourceLocation parentModelLoc;
   private final int tier;
   private final ResourceLocation overlayLoc;
   private final boolean useCutout;

   public CompressedUnbakedModel(ResourceLocation parentBlockLoc, int tier) {
      this.parentModelLoc = ResourceLocation.fromNamespaceAndPath(parentBlockLoc.getNamespace(), "block/" + parentBlockLoc.getPath());
      this.tier = tier;
      this.overlayLoc = ResourceLocation.fromNamespaceAndPath("universalcompression", "block/tier_overlay_" + tier);
      this.useCutout = true;
   }

   public Collection<ResourceLocation> getDependencies() {
      return List.of(this.parentModelLoc);
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter) {
      modelGetter.apply(this.parentModelLoc);
   }

   public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
      BakedModel bakedParent = baker.bake(this.parentModelLoc, state);
      TextureAtlasSprite overlay = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, this.overlayLoc));
      ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
      if (bakedParent != null) {
         transforms = bakedParent.getTransforms();
      } else {
         BakedModel cube = baker.bake(ResourceLocation.parse("minecraft:block/cube_all"), state);
         transforms = cube.getTransforms();
      }

      return new CompressedBlockModel(bakedParent, overlay, null, this.tier, this.useCutout, transforms, null);
   }
}
