package com.thecascadian.universalcompression;

import com.thecascadian.universalcompression.data.VirtualClientPack;
import com.thecascadian.universalcompression.data.VirtualLootPack;
import com.thecascadian.universalcompression.data.VirtualRecipePack;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(
   modid = "universalcompression"
)
public class CommonSetup {
   private static final Logger LOGGER = LogManager.getLogger(CommonSetup.class);

   @SubscribeEvent
   public static void onAddPackFinders(AddPackFindersEvent event) {
      if (event.getPackType() == PackType.CLIENT_RESOURCES) {
         registerPack(
            event,
            "universalcompression:virtual_assets",
            "Universal Compression Virtual Assets",
            PackType.CLIENT_RESOURCES,
            info -> new VirtualClientPack(info)
         );
      }

      if (event.getPackType() == PackType.SERVER_DATA) {
         registerPack(
            event, "universalcompression:virtual_recipes", "Universal Compression Virtual Recipes", PackType.SERVER_DATA, info -> new VirtualRecipePack(info)
         );
         registerPack(
            event, "universalcompression:virtual_loot", "Universal Compression Virtual Loot Tables", PackType.SERVER_DATA, info -> new VirtualLootPack(info)
         );
      }
   }

   private static void registerPack(AddPackFindersEvent event, String id, String displayName, PackType type, Function<PackLocationInfo, PackResources> factory) {
      event.addRepositorySource(packConsumer -> {
         PackLocationInfo info = new PackLocationInfo(id, Component.literal(displayName), PackSource.BUILT_IN, Optional.empty());
         Pack pack = Pack.readMetaAndCreate(info, new ResourcesSupplier() {
            public PackResources openPrimary(PackLocationInfo i) {
               return factory.apply(i);
            }

            public PackResources openFull(PackLocationInfo i, Metadata metadata) {
               return factory.apply(i);
            }
         }, type, new PackSelectionConfig(true, Position.TOP, false));
         if (pack != null) {
            packConsumer.accept(pack);
         } else {
            LOGGER.error("Pack.readMetaAndCreate returned null for: {}", id);
         }
      });
   }

   @SubscribeEvent
   public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
      if (event.getTab() != RegistryHandler.COMPRESSED_TAB.get()) {
         List<ItemStack> toRemove = new ArrayList<>();
         ObjectBidirectionalIterator var2 = event.getParentEntries().iterator();

         while (var2.hasNext()) {
            ItemStack stack = (ItemStack)var2.next();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && id.getNamespace().equals("universalcompression")) {
               toRemove.add(stack);
            }
         }

         for (ItemStack stack : toRemove) {
            event.remove(stack, TabVisibility.PARENT_AND_SEARCH_TABS);
         }

         toRemove.clear();
         var2 = event.getSearchEntries().iterator();

         while (var2.hasNext()) {
            ItemStack stack = (ItemStack)var2.next();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && id.getNamespace().equals("universalcompression")) {
               toRemove.add(stack);
            }
         }

         for (ItemStack stack : toRemove) {
            event.remove(stack, TabVisibility.PARENT_AND_SEARCH_TABS);
         }
      }
   }
}
