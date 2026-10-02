package com.thecascadian.universalcompression.client.recipebook;

import com.thecascadian.universalcompression.config.CompressionConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class RecipeBookVisibility {
   private RecipeBookVisibility() {
   }

   public static List<RecipeCollection> filterCollections(List<RecipeCollection> collections) {
      boolean showCompress = getConfigBool(true, () -> (Boolean)CompressionConfig.COMMON.showCompressInRecipeBook.get());
      boolean showDecompress = getConfigBool(true, () -> (Boolean)CompressionConfig.COMMON.showDecompressInRecipeBook.get());
      if (showCompress && showDecompress) {
         return collections;
      } else {
         List<RecipeCollection> filtered = new ArrayList<>(collections);
         filtered.removeIf(collection -> shouldHideCollection(collection, showCompress, showDecompress));
         return filtered;
      }
   }

   public static void refreshOpenRecipeBook() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.screen instanceof RecipeUpdateListener listener) {
         listener.recipesUpdated();
      }
   }

   private static boolean shouldHideCollection(RecipeCollection collection, boolean showCompress, boolean showDecompress) {
      return collection.getRecipes()
         .stream()
         .allMatch(recipe -> shouldHideRecipe(collection.registryAccess(), (RecipeHolder<?>)recipe, showCompress, showDecompress));
   }

   private static boolean shouldHideRecipe(RegistryAccess registryAccess, RecipeHolder<?> holder, boolean showCompress, boolean showDecompress) {
      if (!"universalcompression".equals(holder.id().getNamespace())) {
         return false;
      } else {
         return !showCompress && isCompressionRecipe(registryAccess, holder) ? true : !showDecompress && isDecompressionRecipe(registryAccess, holder);
      }
   }

   private static boolean isCompressionRecipe(RegistryAccess registryAccess, RecipeHolder<?> holder) {
      ItemStack result = holder.value().getResultItem(registryAccess);
      ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
      return resultId != null && "universalcompression".equals(resultId.getNamespace());
   }

   private static boolean isDecompressionRecipe(RegistryAccess registryAccess, RecipeHolder<?> holder) {
      if (isCompressionRecipe(registryAccess, holder)) {
         return false;
      } else {
         for (Ingredient ingredient : holder.value().getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
               ResourceLocation ingredientId = BuiltInRegistries.ITEM.getKey(stack.getItem());
               if (ingredientId != null && "universalcompression".equals(ingredientId.getNamespace())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean getConfigBool(boolean defaultValue, Supplier<Boolean> supplier) {
      try {
         return supplier.get();
      } catch (IllegalStateException var3) {
         return defaultValue;
      }
   }
}
