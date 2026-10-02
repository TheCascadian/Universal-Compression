package com.thecascadian.universalcompression.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record CompressDisplay(List<ItemStack> inputs, ItemStack output) {
}
