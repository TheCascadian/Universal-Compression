package com.thecascadian.universalcompression.block;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.neoforged.fml.ModList;

public class CompressedBlockItem extends BlockItem {
   public CompressedBlockItem(CompressedBlock block, Properties properties) {
      super(block, properties);
   }

   public Component getName(ItemStack stack) {
      CompressedBlock block = (CompressedBlock)this.getBlock();
      Component parentName = block.getParentBlock().getName();
      int tier = block.getCompressionTier();
      return Component.translatable("block.universalcompression.compressed_block_name", new Object[]{parentName, tier});
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      CompressedBlock block = (CompressedBlock)this.getBlock();
      ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(block.getParentBlock());
      String namespace = parentId.getNamespace();
      String sourceName = ModList.get()
         .getModContainerById(namespace)
         .map(mc -> mc.getModInfo().getDisplayName())
         .orElse(namespace.equals("minecraft") ? "Minecraft" : namespace);
      if (!sourceName.equals("Minecraft")) {
         tooltip.add(Component.translatable("tooltip.universalcompression.introduced_by", new Object[]{sourceName}).withStyle(ChatFormatting.DARK_AQUA));
      }
   }
}
