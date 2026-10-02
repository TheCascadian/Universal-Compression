package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class CompressionRecipeProvider extends RecipeProvider {
   public CompressionRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
      super(output, registries);
   }

   protected void buildRecipes(RecipeOutput output) {
      RegistryHandler.COMPRESSED_IDS
         .forEach(
            (parentId, tiers) -> {
               if (!this.isBlacklisted(parentId)) {
                  Item parentItem = (Item)BuiltInRegistries.ITEM.get(parentId);
                  ResourceLocation tier1Id = tiers.get(0);
                  Item tier1Item = (Item)BuiltInRegistries.ITEM.get(tier1Id);
                  if (parentItem != Items.AIR && tier1Item != Items.AIR) {
                     ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, tier1Item)
                        .pattern("XXX")
                        .pattern("XXX")
                        .pattern("XXX")
                        .define('X', parentItem)
                        .unlockedBy("has_" + parentId.getPath(), has(parentItem))
                        .save(output, ResourceLocation.fromNamespaceAndPath("universalcompression", tier1Id.getPath() + "_from_parent"));
                     ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, parentItem, 9)
                        .requires(tier1Item)
                        .unlockedBy("has_" + tier1Id.getPath(), has(tier1Item))
                        .save(output, ResourceLocation.fromNamespaceAndPath("universalcompression", parentId.getNamespace() + "_" + parentId.getPath() + "_from_tier1"));
                  }

                  for (int i = 0; i < tiers.size() - 1; i++) {
                     ResourceLocation lower = tiers.get(i);
                     ResourceLocation upper = tiers.get(i + 1);
                     Item lowerItem = (Item)BuiltInRegistries.ITEM.get(lower);
                     Item upperItem = (Item)BuiltInRegistries.ITEM.get(upper);
                     if (lowerItem != Items.AIR && upperItem != Items.AIR) {
                        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, upperItem)
                           .pattern("XXX")
                           .pattern("XXX")
                           .pattern("XXX")
                           .define('X', lowerItem)
                           .unlockedBy("has_" + lower.getPath(), has(lowerItem))
                           .save(output, ResourceLocation.fromNamespaceAndPath("universalcompression", upper.getPath() + "_from_" + lower.getPath()));
                        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, lowerItem, 9)
                           .requires(upperItem)
                           .unlockedBy("has_" + upper.getPath(), has(upperItem))
                           .save(output, ResourceLocation.fromNamespaceAndPath("universalcompression", lower.getPath() + "_decompose"));
                     }
                  }
               }
            }
         );
   }

   private boolean isBlacklisted(ResourceLocation id) {
      if (RegistryHandler.isWhitelisted(id)) {
         return false;
      }
      if (RegistryHandler.isBlacklisted(id)) {
         return true;
      } else {
         try {
            String ns = id.getNamespace();
            String idString = id.toString();

            for (String entry : CompressionConfig.COMMON.blacklist.get()) {
               if (entry.equals(ns) || entry.equals(idString)) {
                  return true;
               }
            }
         } catch (IllegalStateException var6) {
         }

         return false;
      }
   }
}
