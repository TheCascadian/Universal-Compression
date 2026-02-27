package com.thecascadian.universalcompression.recipe;

import com.mojang.serialization.MapCodec;
import com.thecascadian.universalcompression.block.CompressedBlock;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import net.minecraft.core.HolderLookup;
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

import java.util.List;

/**
 * A single recipe instance that matches any single compressed block item anywhere
 * in the crafting grid, yielding 9× of the next lower tier (or parent block).
 */
public class DecompressRecipe implements CraftingRecipe {

    public static final MapCodec<DecompressRecipe> CODEC = MapCodec.unit(new DecompressRecipe());
    public static final StreamCodec<RegistryFriendlyByteBuf, DecompressRecipe> STREAM_CODEC =
            StreamCodec.unit(new DecompressRecipe());

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int filled = 0;
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty()) filled++;
        }
        if (filled != 1) return false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            return s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CompressedBlock;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (!(s.getItem() instanceof BlockItem bi)) return ItemStack.EMPTY;
            if (!(bi.getBlock() instanceof CompressedBlock cb)) return ItemStack.EMPTY;

            int tier = cb.getCompressionTier(); // 1-indexed
            Block parent = cb.getParentBlock();

            if (tier == 1) {
                Item parentItem = BuiltInRegistries.ITEM.get(BuiltInRegistries.BLOCK.getKey(parent));
                return parentItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(parentItem, 9);
            } else {
                ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
                List<ResourceLocation> tiers = RegistryHandler.getTiers(parentId);
                // tier N decompresses to tier N-1; tier N-1 is at index N-2
                ResourceLocation lowerTierId = tiers.get(tier - 2);
                Item lowerItem = BuiltInRegistries.ITEM.get(lowerTierId);
                return lowerItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(lowerItem, 9);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryHandler.DECOMPRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        // same rationale as CompressRecipe: the recipe needs to advertise the
        // custom type so the packet encoder/decoder uses our serializer.
        return RegistryHandler.DECOMPRESS_TYPE.get();
    }
}
