package com.thecascadian.universalcompression.recipe;

import com.mojang.serialization.MapCodec;
import com.thecascadian.universalcompression.block.CompressedBlock;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CompressRecipe implements CraftingRecipe {
   public static final MapCodec<CompressRecipe> CODEC = MapCodec.unit(new CompressRecipe());
   public static final StreamCodec<RegistryFriendlyByteBuf, CompressRecipe> STREAM_CODEC = StreamCodec.unit(new CompressRecipe());

   public boolean matches(CraftingInput input, Level level) {
      int filled = 0;

      for (int i = 0; i < input.size(); i++) {
         if (!input.getItem(i).isEmpty()) {
            filled++;
         }
      }

      if (filled != 9) {
         return false;
      } else {
         Item first = null;

         for (int ix = 0; ix < input.size(); ix++) {
            ItemStack stack = input.getItem(ix);
            if (first == null) {
               first = stack.getItem();
            } else if (stack.getItem() != first) {
               return false;
            }
         }

         return first == null ? false : nextTier(first) != ItemStack.EMPTY;
      }
   }

   public ItemStack assemble(CraftingInput input, Provider registries) {
      Item first = null;

      for (int i = 0; i < input.size(); i++) {
         ItemStack s = input.getItem(i);
         if (!s.isEmpty()) {
            first = s.getItem();
            break;
         }
      }

      return first == null ? ItemStack.EMPTY : nextTier(first);
   }

   private static ItemStack nextTier(Item item) {
      if (item instanceof BlockItem bi) {
         Block block = bi.getBlock();
         ResourceLocation parentId;
         int currentTier;
         if (block instanceof CompressedBlock cb) {
            parentId = BuiltInRegistries.BLOCK.getKey(cb.getParentBlock());
            currentTier = cb.getCompressionTier();
         } else {
            parentId = BuiltInRegistries.BLOCK.getKey(block);
            currentTier = 0;
         }

         List<ResourceLocation> tiers = RegistryHandler.getTiers(parentId);
         if (!tiers.isEmpty() && currentTier < tiers.size()) {
            Item nextItem = (Item)BuiltInRegistries.ITEM.get(tiers.get(currentTier));
            return nextItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(nextItem);
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width >= 3 && height >= 3;
   }

   public ItemStack getResultItem(Provider registries) {
      return ItemStack.EMPTY;
   }

   public boolean isSpecial() {
      return true;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.BUILDING;
   }

   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)RegistryHandler.COMPRESS_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)RegistryHandler.COMPRESS_TYPE.get();
   }
}
