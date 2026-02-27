package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CompressionRecipeProvider extends RecipeProvider {

    public CompressionRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        RegistryHandler.COMPRESSED_IDS.forEach((parentId, tiers) -> {
            if (isBlacklisted(parentId)) return;

            Item parentItem = BuiltInRegistries.ITEM.get(parentId);
            ResourceLocation tier1Id = tiers.get(0);
            Item tier1Item = BuiltInRegistries.ITEM.get(tier1Id);
            if (parentItem != Items.AIR && tier1Item != Items.AIR) {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, tier1Item)
                        .pattern("XXX").pattern("XXX").pattern("XXX")
                        .define('X', parentItem)
                        .unlockedBy("has_" + parentId.getPath(), has(parentItem))
                        .save(output, ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID,
                                tier1Id.getPath() + "_from_parent"));

                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, parentItem, 9)
                        .requires(tier1Item)
                        .unlockedBy("has_" + tier1Id.getPath(), has(tier1Item))
                        .save(output, ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID,
                                parentId.getPath() + "_from_tier1"));
            }

            for (int i = 0; i < tiers.size() - 1; i++) {
                ResourceLocation lower = tiers.get(i);
                ResourceLocation upper = tiers.get(i + 1);
                Item lowerItem = BuiltInRegistries.ITEM.get(lower);
                Item upperItem = BuiltInRegistries.ITEM.get(upper);

                if (lowerItem == Items.AIR || upperItem == Items.AIR) continue;

                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, upperItem)
                        .pattern("XXX").pattern("XXX").pattern("XXX")
                        .define('X', lowerItem)
                        .unlockedBy("has_" + lower.getPath(), has(lowerItem))
                        .save(output, ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, upper.getPath() + "_from_" + lower.getPath()));

                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, lowerItem, 9)
                        .requires(upperItem)
                        .unlockedBy("has_" + upper.getPath(), has(upperItem))
                        .save(output, ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, lower.getPath() + "_decompose"));
            }
        });
    }

    private boolean isBlacklisted(ResourceLocation id) {
        if (RegistryHandler.isBlacklisted(id)) {
            return true;
        }
        try {
            String ns = id.getNamespace();
            String idString = id.toString();
            for (String entry : CompressionConfig.COMMON.blacklist.get()) {
                if (entry.equals(ns) || entry.equals(idString)) return true;
            }
        } catch (IllegalStateException e) {
            // config unavailable – no blacklist applied
        }
        return false;
    }
}