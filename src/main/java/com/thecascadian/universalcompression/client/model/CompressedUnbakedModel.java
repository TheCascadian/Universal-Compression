package com.thecascadian.universalcompression.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import com.thecascadian.universalcompression.UniversalCompression;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CompressedUnbakedModel implements UnbakedModel {
    private final ResourceLocation parentModelLoc;
    private final int tier;
    private final ResourceLocation overlayLoc;
    private final boolean useCutout;

    public CompressedUnbakedModel(ResourceLocation parentBlockLoc, int tier) {
        this.parentModelLoc = ResourceLocation.fromNamespaceAndPath(
            parentBlockLoc.getNamespace(), "block/" + parentBlockLoc.getPath());
        this.tier = tier;
        this.overlayLoc = ResourceLocation.fromNamespaceAndPath(
            UniversalCompression.MODID, "block/tier_overlay_" + tier);
        // overlays are always cutout (transparent png)
        this.useCutout = true;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of(parentModelLoc);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter) {
        modelGetter.apply(parentModelLoc);
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        BakedModel bakedParent = baker.bake(parentModelLoc, state);
        TextureAtlasSprite overlay = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, overlayLoc));
        ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
        if (bakedParent != null) {
            transforms = bakedParent.getTransforms();
        } else {
            BakedModel cube = baker.bake(ResourceLocation.parse("minecraft:block/cube_all"), state);
            transforms = cube.getTransforms();
        }
        return new CompressedBlockModel(bakedParent, overlay, null, tier, useCutout, transforms);
    }
}