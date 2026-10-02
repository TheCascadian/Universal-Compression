package com.thecascadian.universalcompression;

import com.thecascadian.universalcompression.client.model.CompressedBlockModelLoader;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;

@EventBusSubscriber(
   modid = "universalcompression",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientSetup {
   @SubscribeEvent
   public static void onRegisterGeometryLoaders(RegisterGeometryLoaders event) {
      event.register(ResourceLocation.fromNamespaceAndPath("universalcompression", "compressed"), new CompressedBlockModelLoader());
   }
}
