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

public class DecompressingCategory implements IRecipeCategory<DecompressDisplay> {
   private static final int BG_WIDTH = 68;
   private static final int BG_HEIGHT = 38;
   private static final int IN_X = 1;
   private static final int IN_Y = 10;
   private static final int OUT_X = 48;
   private static final int OUT_Y = 10;
   private final IDrawable background;
   private final IDrawable icon;

   public DecompressingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(68, 38);
      this.icon = buildIcon(guiHelper);
   }

   private static IDrawable buildIcon(IGuiHelper guiHelper) {
      try {
         for (List<ResourceLocation> tiers : RegistryHandler.COMPRESSED_IDS.values()) {
            if (tiers.size() >= 2) {
               Item item = (Item)BuiltInRegistries.ITEM.get(tiers.get(1));
               if (item != Items.AIR) {
                  return guiHelper.createDrawableItemStack(new ItemStack(item));
               }
            }
         }
      } catch (Throwable var4) {
      }

      return guiHelper.createDrawableItemStack(new ItemStack(Items.COBBLESTONE));
   }

   public RecipeType<DecompressDisplay> getRecipeType() {
      return CompressionJeiPlugin.DECOMPRESS_TYPE;
   }

   public Component getTitle() {
      return Component.translatable("jei.universalcompression.decompress");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, DecompressDisplay recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 1, 10).addItemStack(recipe.input());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 10).addItemStack(recipe.output());
   }
}
