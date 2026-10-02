package com.thecascadian.universalcompression.compat.jei;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;

@EventBusSubscriber(
   modid = "universalcompression",
   value = {Dist.CLIENT}
)
public final class CompressionJeiClientEvents {
   private CompressionJeiClientEvents() {
   }

   @SubscribeEvent
   public static void onRecipesUpdated(RecipesUpdatedEvent event) {
      if (ModList.get().isLoaded("jei")) {
         CompressionJeiPlugin.onClientIngredientsUpdated();
      }
   }

   @SubscribeEvent
   public static void onClientLogin(LoggingIn event) {
      if (ModList.get().isLoaded("jei")) {
         CompressionJeiPlugin.onClientIngredientsUpdated();
      }
   }

   @SubscribeEvent
   public static void onClientLogout(LoggingOut event) {
      if (ModList.get().isLoaded("jei")) {
         CompressionJeiPlugin.onClientSessionEnded();
      }
   }
}
