package com.thecascadian.universalcompression.compat.jei;

import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CompressingCategory implements IRecipeCategory<CompressDisplay> {
   private static final int BG_WIDTH = 100;
   private static final int BG_HEIGHT = 58;
   private static final int OUT_X = 77;
   private static final int OUT_Y = 20;
   private final IDrawable background;
   private final IDrawable icon;

   public CompressingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(100, 58);
      this.icon = buildIcon(guiHelper);
   }

   private static IDrawable buildIcon(IGuiHelper guiHelper) {
      try {
         for (List<ResourceLocation> tiers : RegistryHandler.COMPRESSED_IDS.values()) {
            if (!tiers.isEmpty()) {
               Item item = (Item)BuiltInRegistries.ITEM.get(tiers.get(0));
               if (item != Items.AIR) {
                  return guiHelper.createDrawableItemStack(new ItemStack(item));
               }
            }
         }
      } catch (Throwable var4) {
      }

      return guiHelper.createDrawableItemStack(new ItemStack(Items.COBBLESTONE));
   }

   public RecipeType<CompressDisplay> getRecipeType() {
      return CompressionJeiPlugin.COMPRESS_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("jei.universalcompression.compress");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, CompressDisplay recipe, IFocusGroup focuses) {
      List<ItemStack> inputs = recipe.inputs();

      for (int i = 0; i < 9; i++) {
         int col = i % 3;
         int row = i / 3;
         builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18).addItemStack(inputs.get(i));
      }

      builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 20).addItemStack(recipe.output());
   }
}
