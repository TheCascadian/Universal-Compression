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
 * A single recipe instance that matches any full 3×3 grid of the same item
 * which has a next compression tier. Replaces O(blocks × tiers) vanilla recipe
 * JSONs with one dynamic recipe evaluated at craft time.
 */
public class CompressRecipe implements CraftingRecipe {

    public static final MapCodec<CompressRecipe> CODEC = MapCodec.unit(new CompressRecipe());
    public static final StreamCodec<RegistryFriendlyByteBuf, CompressRecipe> STREAM_CODEC =
            StreamCodec.unit(new CompressRecipe());

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // Require exactly 9 filled slots.
        int filled = 0;
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty()) filled++;
        }
        if (filled != 9) return false;

        // All must be the same item.
        Item first = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (first == null) first = stack.getItem();
            else if (stack.getItem() != first) return false;
        }
        if (first == null) return false;

        return nextTier(first) != ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Item first = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty()) { first = s.getItem(); break; }
        }
        return first == null ? ItemStack.EMPTY : nextTier(first);
    }

    private static ItemStack nextTier(Item item) {
        if (!(item instanceof BlockItem bi)) return ItemStack.EMPTY;
        Block block = bi.getBlock();
        ResourceLocation parentId;
        int currentTier;

        if (block instanceof CompressedBlock cb) {
            parentId = BuiltInRegistries.BLOCK.getKey(cb.getParentBlock());
            currentTier = cb.getCompressionTier(); // 1-indexed
        } else {
            parentId = BuiltInRegistries.BLOCK.getKey(block);
            currentTier = 0;
        }

        List<ResourceLocation> tiers = RegistryHandler.getTiers(parentId);
        if (tiers.isEmpty() || currentTier >= tiers.size()) return ItemStack.EMPTY;

        Item nextItem = BuiltInRegistries.ITEM.get(tiers.get(currentTier));
        return nextItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(nextItem);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
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
        return RegistryHandler.COMPRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        // must return our custom recipe type so that the correct serializer
        // is used during recipe synchronization.  Using RecipeType.CRAFTING
        // causes vanilla's crafting serializer to be invoked, which can't
        // handle our dynamic instance and triggered the encoder exception
        // seen when the client attempted to load the server's recipe list.
        return RegistryHandler.COMPRESS_TYPE.get();
    }
}
