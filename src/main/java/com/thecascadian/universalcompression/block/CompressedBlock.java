package com.thecascadian.universalcompression.block;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams.Builder;

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

   private static Properties copyProperties(Block from) {
      Properties props = Properties.ofFullCopy(from);

      MapColor color;
      try {
         color = from.defaultBlockState().getMapColor(null, null);
      } catch (Exception var6) {
         color = MapColor.NONE;
      }

      props.mapColor(color);
      int tempLight = 0;

      try {
         tempLight = from.defaultBlockState().getLightEmission(null, null);
      } catch (Exception var5) {
      }

      int light = tempLight;
      props.lightLevel(p -> light);
      return props;
   }

   public Block getParentBlock() {
      return this.parentBlock;
   }

   public int getCompressionTier() {
      return this.compressionTier;
   }

   public ResourceLocation getCompressedId() {
      return this.compressedId;
   }

   public float defaultDestroyTime() {
      return super.defaultDestroyTime() / (float)this.compressionTier;
   }

   protected List<ItemStack> getDrops(BlockState state, Builder builder) {
      return List.of(new ItemStack(this));
   }
}
