package com.thecascadian.universalcompression.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.thecascadian.universalcompression.UniversalCompression;
import net.minecraft.client.renderer.block.model.ItemOverrides;
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

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public class CompressedBlockModelLoader implements IGeometryLoader<CompressedBlockModelLoader.Geometry> {

    public static final String ID = "compressed";

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext ctx) {
        // Read texture locations directly from the JSON so we can construct
        // Materials explicitly in bake(), bypassing context.getMaterial() entirely.
        ResourceLocation parentModel = json.has("parent")
                ? ResourceLocation.parse(json.get("parent").getAsString())
                : null;
        
        boolean useCutout = false;
        if (json.has("render_type")) {
            String rt = json.get("render_type").getAsString();
            // the strings we care about are the names NeoForge uses in the generated
            // JSONs; treat anything containing "cutout" as cutout.
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

        int tier = json.get("tier").getAsInt();

        // Overlay location fallback derived from tier if not in JSON.
        if (overlayLocation == null) {
            overlayLocation = ResourceLocation.fromNamespaceAndPath(
                    UniversalCompression.MODID, "block/tier_overlay_" + tier);
        }

        return new Geometry(parentModel, textureLocation, overlayLocation, tier, useCutout);
    }

    public static class Geometry implements IUnbakedGeometry<Geometry> {
        private final ResourceLocation parentModel;
        private final ResourceLocation textureLocation;
        private final ResourceLocation overlayLocation;
        private final int tier;
        private final boolean useCutout;

        public Geometry(ResourceLocation parentModel, ResourceLocation textureLocation,
                ResourceLocation overlayLocation, int tier, boolean useCutout) {
            this.parentModel = parentModel;
            this.textureLocation = textureLocation;
            this.overlayLocation = overlayLocation;
            this.tier = tier;
            this.useCutout = useCutout;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context,
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelState,
                ItemOverrides overrides) {

            // Construct Materials explicitly using the locations read from JSON.
            // This avoids context.getMaterial() which can return a full-atlas proxy
            // if the texture key lookup fails internally.
            TextureAtlasSprite baseSprite = null;
            BakedModel bakedParent = null;
            ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;

            if (textureLocation != null) {
                baseSprite = spriteGetter.apply(
                        new Material(InventoryMenu.BLOCK_ATLAS, textureLocation));
                if (baseSprite.contents().name().toString().contains("missing")) {
                    UniversalCompression.LOGGER.warn(
                            "Base texture {} (tier {}) resolved to missing sprite",
                            textureLocation, tier);
                }
                // no explicit parent defined; use vanilla cube_all only for item transforms
                BakedModel cube = baker.bake(ResourceLocation.parse("minecraft:block/cube_all"), modelState);
                transforms = cube.getTransforms();
                // bakedParent left null so we don't render its faces
            } else if (parentModel != null) {
                bakedParent = baker.bake(parentModel, modelState);
                transforms = bakedParent.getTransforms();
            }

            TextureAtlasSprite overlaySprite = spriteGetter.apply(
                    new Material(InventoryMenu.BLOCK_ATLAS, overlayLocation));
            if (overlaySprite.contents().name().toString().contains("missing")) {
                UniversalCompression.LOGGER.warn(
                        "Overlay texture {} (tier {}) resolved to missing sprite",
                        overlayLocation, tier);
            }

            return new CompressedBlockModel(bakedParent, overlaySprite, baseSprite, tier, useCutout, transforms);
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
            if (parentModel != null) {
                modelGetter.apply(parentModel);
            }
        }
    }
}