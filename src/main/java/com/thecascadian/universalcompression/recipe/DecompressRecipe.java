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

public class DecompressRecipe implements CraftingRecipe {
   public static final MapCodec<DecompressRecipe> CODEC = MapCodec.unit(new DecompressRecipe());
   public static final StreamCodec<RegistryFriendlyByteBuf, DecompressRecipe> STREAM_CODEC = StreamCodec.unit(new DecompressRecipe());

   public boolean matches(CraftingInput input, Level level) {
      int filled = 0;

      for (int i = 0; i < input.size(); i++) {
         if (!input.getItem(i).isEmpty()) {
            filled++;
         }
      }

      if (filled != 1) {
         return false;
      } else {
         for (int ix = 0; ix < input.size(); ix++) {
            ItemStack s = input.getItem(ix);
            if (!s.isEmpty()) {
               if (s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CompressedBlock) {
                  return true;
               }

               return false;
            }
         }

         return false;
      }
   }

   public ItemStack assemble(CraftingInput input, Provider registries) {
      for (int i = 0; i < input.size(); i++) {
         ItemStack s = input.getItem(i);
         if (!s.isEmpty()) {
            if (s.getItem() instanceof BlockItem bi) {
               if (bi.getBlock() instanceof CompressedBlock cb) {
                  int var14 = cb.getCompressionTier();
                  Block parent = cb.getParentBlock();
                  if (var14 == 1) {
                     Item parentItem = (Item)BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(parent));
                     return parentItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(parentItem, 9);
                  }

                  ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
                  List<ResourceLocation> tiers = RegistryHandler.getTiers(parentId);
                  ResourceLocation lowerTierId = tiers.get(var14 - 2);
                  Item lowerItem = (Item)BuiltInRegistries.ITEM.get(lowerTierId);
                  return lowerItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(lowerItem, 9);
               }

               return ItemStack.EMPTY;
            }

            return ItemStack.EMPTY;
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 1;
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
      return (RecipeSerializer<?>)RegistryHandler.DECOMPRESS_SERIALIZER.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)RegistryHandler.DECOMPRESS_TYPE.get();
   }
}
