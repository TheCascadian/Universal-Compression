package com.thecascadian.universalcompression.block;

import com.thecascadian.universalcompression.UniversalCompression;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

import java.util.List;

public class CompressedBlockItem extends BlockItem {

    public CompressedBlockItem(CompressedBlock block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CompressedBlock block = (CompressedBlock) getBlock();
        Component parentName = block.getParentBlock().getName();
        int tier = block.getCompressionTier();
        return Component.translatable(
                "block." + UniversalCompression.MODID + ".compressed_block_name",
                parentName,
                tier
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CompressedBlock block = (CompressedBlock) getBlock();
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(block.getParentBlock());
        String namespace = parentId.getNamespace();

        String sourceName = ModList.get()
                .getModContainerById(namespace)
                .map(mc -> mc.getModInfo().getDisplayName())
                .orElse(namespace.equals("minecraft") ? "Minecraft" : namespace);

        tooltip.add(Component.translatable(
                "tooltip." + UniversalCompression.MODID + ".source_mod",
                sourceName
        ).withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.translatable(
                "tooltip." + UniversalCompression.MODID + ".introduced_by"
        ).withStyle(ChatFormatting.DARK_AQUA));
    }
}