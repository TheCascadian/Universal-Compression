package com.thecascadian.universalcompression.block;

import com.thecascadian.universalcompression.UniversalCompression;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.lang.reflect.Field;
import java.util.List;

public class CompressedBlock extends Block {
    private final Block parentBlock;
    private final int compressionTier;
    private final ResourceLocation compressedId;

    public CompressedBlock(Block parent, int tier, ResourceLocation compressedId) {
        super(copyProperties(parent));
        this.parentBlock = parent;
        this.compressionTier = tier;
        this.compressedId = compressedId;
    }

    private static BlockBehaviour.Properties copyProperties(Block from) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(from);

        // ofFullCopy will copy most of the parent's characteristics but will also
        // initialize the internal loot table supplier to point at the parent's
        // table.  We don't care about the parent's drops at all; the compressed
        // block should drop itself via our virtual loot pack.  Instead of using
        // reflection to clear an internal field (which ended up *not* affecting
        // the supplier and therefore did nothing), we simply override
        // getLootTable() in the block class.  That override ignores whatever the
        // properties say and returns the correct key based on the compressed id.

        // Safely copy map color
        MapColor color;
        try {
            color = from.defaultBlockState().getMapColor(null, null);
        } catch (Exception ignored) {
            color = MapColor.NONE;
        }
        props.mapColor(color);

        // Safely copy light emission
        int tempLight = 0;
        try {
            tempLight = from.defaultBlockState().getLightEmission(null, null);
        } catch (Exception ignored) {
        }
        final int light = tempLight;
        props.lightLevel(p -> light);

        return props;
    }

    public Block getParentBlock() {
        return parentBlock;
    }

    public int getCompressionTier() {
        return compressionTier;
    }

    public ResourceLocation getCompressedId() {
        return compressedId;
    }

    // legacy override removed; defaultDestroyTime adjustment below handles hardness

    @Override
    public float defaultDestroyTime() {
        // divide the parent's hardness by tier so higher compression isn't absurdly slow
        return super.defaultDestroyTime() / (float) compressionTier;
    }

    // our custom blocks use a virtual loot pack and we do not rely on the
    // vanilla loot table machinery; overriding the protected getDrops method is
    // the easiest way to make sure the block always drops itself.  This avoids
    // reflection hacks entirely and works regardless of what loot table key the
    // BlockBehaviour properties end up holding.
    @Override
    protected List<net.minecraft.world.item.ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return List.of(new net.minecraft.world.item.ItemStack(this));
    }
}