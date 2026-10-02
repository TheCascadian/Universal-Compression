package com.thecascadian.universalcompression.compat.jei;

import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@JeiPlugin
public class CompressionJeiPlugin implements IModPlugin {
   public static final RecipeType<CompressDisplay> COMPRESS_TYPE = RecipeType.create("universalcompression", "compress", CompressDisplay.class);
   public static final RecipeType<DecompressDisplay> DECOMPRESS_TYPE = RecipeType.create("universalcompression", "decompress", DecompressDisplay.class);
   private static volatile IJeiRuntime cachedRuntime = null;
   private static volatile boolean currentlyHidden = false;

   public ResourceLocation getPluginUid() {
      return ResourceLocation.fromNamespaceAndPath("universalcompression", "jei_plugin");
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      try {
         IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
         registration.addRecipeCategories(new IRecipeCategory[]{new CompressingCategory(guiHelper), new DecompressingCategory(guiHelper)});
      } catch (Throwable var3) {
         UniversalCompression.LOGGER.error("[UniversalCompression] Failed to register JEI categories", var3);
      }
   }

   public void registerRecipes(IRecipeRegistration registration) {
      try {
         List<CompressDisplay> compressDisplays = new ArrayList<>();
         List<DecompressDisplay> decompressDisplays = new ArrayList<>();

         for (Entry<ResourceLocation, List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
            ResourceLocation parentId = entry.getKey();
            List<ResourceLocation> tiers = entry.getValue();

            for (int i = 0; i < tiers.size(); i++) {
               ResourceLocation tierId = tiers.get(i);
               Item ingredientItem = i == 0 ? (Item)BuiltInRegistries.ITEM.get(parentId) : (Item)BuiltInRegistries.ITEM.get(tiers.get(i - 1));
               Item outputItem = (Item)BuiltInRegistries.ITEM.get(tierId);
               if (ingredientItem != Items.AIR && outputItem != Items.AIR) {
                  List<ItemStack> inputs = new ArrayList<>(9);

                  for (int j = 0; j < 9; j++) {
                     inputs.add(new ItemStack(ingredientItem));
                  }

                  compressDisplays.add(new CompressDisplay(inputs, new ItemStack(outputItem)));
                  ItemStack decompressOutput = new ItemStack(ingredientItem, 9);
                  decompressDisplays.add(new DecompressDisplay(new ItemStack(outputItem), decompressOutput));
               }
            }
         }

         registration.addRecipes(COMPRESS_TYPE, compressDisplays);
         registration.addRecipes(DECOMPRESS_TYPE, decompressDisplays);
         UniversalCompression.LOGGER
            .info("[UniversalCompression] Registered {} compress and {} decompress JEI recipes.", compressDisplays.size(), decompressDisplays.size());
      } catch (Throwable var14) {
         UniversalCompression.LOGGER.error("[UniversalCompression] Failed to register JEI recipes", var14);
      }
   }

   public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
      cachedRuntime = jeiRuntime;
      applyVisibility(jeiRuntime, true);
   }

   public void onRuntimeUnavailable() {
      cachedRuntime = null;
      currentlyHidden = false;
   }

   public static void refreshIngredientVisibility() {
      IJeiRuntime rt = cachedRuntime;
      if (rt != null) {
         applyVisibility(rt, false);
      }
   }

   public static void onClientIngredientsUpdated() {
      currentlyHidden = false;
      IJeiRuntime rt = cachedRuntime;
      if (rt != null) {
         applyVisibility(rt, true);
      }
   }

   public static void onClientSessionEnded() {
      currentlyHidden = false;
   }

   private static void applyVisibility(IJeiRuntime runtime, boolean forceReapply) {
      try {
         boolean shouldHide = true;

         try {
            shouldHide = (Boolean)CompressionConfig.COMMON.hideCompressedItemsInJEI.get();
         } catch (Exception var5) {
         }

         List<ItemStack> ucItems = collectUniversalCompressionItems();
         if (ucItems.isEmpty()) {
            return;
         }

         IIngredientManager mgr = runtime.getIngredientManager();
         if (!shouldHide || !forceReapply && currentlyHidden) {
            if (!shouldHide && currentlyHidden) {
               mgr.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ucItems);
               currentlyHidden = false;
               UniversalCompression.LOGGER.info("[UniversalCompression] Restored {} items to JEI ingredient list.", ucItems.size());
            }
         } else {
            mgr.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ucItems);
            currentlyHidden = true;
            UniversalCompression.LOGGER.info("[UniversalCompression] Hid {} items from JEI ingredient list.", ucItems.size());
         }
      } catch (Throwable var6) {
         UniversalCompression.LOGGER.error("[UniversalCompression] Failed to update JEI ingredient visibility", var6);
      }
   }

   private static List<ItemStack> collectUniversalCompressionItems() {
      List<ItemStack> ucItems = new ArrayList<>();

      for (Item item : BuiltInRegistries.ITEM) {
         ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
         if (key != null && "universalcompression".equals(key.getNamespace())) {
            ucItems.add(new ItemStack(item));
         }
      }

      return ucItems;
   }
}
