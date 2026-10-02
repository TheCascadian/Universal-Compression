package com.thecascadian.universalcompression.mixin.client;

import com.thecascadian.universalcompression.client.recipebook.RecipeBookVisibility;
import java.util.List;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({RecipeBookComponent.class})
public abstract class RecipeBookComponentMixin {
   @Redirect(
      method = {"updateCollections"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;updateCollections(Ljava/util/List;Z)V",
         remap = false
      ),
      remap = false
   )
   private void universalcompression$filterCollections(RecipeBookPage page, List<RecipeCollection> collections, boolean resetPage) {
      page.updateCollections(RecipeBookVisibility.filterCollections(collections), resetPage);
   }
}
