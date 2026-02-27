package com.thecascadian.universalcompression;

import com.thecascadian.universalcompression.data.VirtualClientPack;
import com.thecascadian.universalcompression.data.VirtualLootPack;
import com.thecascadian.universalcompression.data.VirtualRecipePack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = UniversalCompression.MODID)
public class CommonSetup {

    private static final Logger LOGGER = LogManager.getLogger(CommonSetup.class);

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            registerPack(event,
                    UniversalCompression.MODID + ":virtual_assets",
                    "Universal Compression Virtual Assets",
                    PackType.CLIENT_RESOURCES,
                    info -> new VirtualClientPack(info));
        }
        if (event.getPackType() == PackType.SERVER_DATA) {
            registerPack(event,
                    UniversalCompression.MODID + ":virtual_recipes",
                    "Universal Compression Virtual Recipes",
                    PackType.SERVER_DATA,
                    info -> new VirtualRecipePack(info));
            registerPack(event,
                    UniversalCompression.MODID + ":virtual_loot",
                    "Universal Compression Virtual Loot Tables",
                    PackType.SERVER_DATA,
                    info -> new VirtualLootPack(info));
        }
    }

    private static void registerPack(AddPackFindersEvent event,
            String id,
            String displayName,
            PackType type,
            Function<PackLocationInfo, PackResources> factory) {
        event.addRepositorySource(packConsumer -> {
            PackLocationInfo info = new PackLocationInfo(
                    id,
                    Component.literal(displayName),
                    PackSource.BUILT_IN,
                    Optional.empty());
            Pack pack = Pack.readMetaAndCreate(
                    info,
                    new Pack.ResourcesSupplier() {
                        @Override public PackResources openPrimary(PackLocationInfo i) { return factory.apply(i); }
                        @Override public PackResources openFull(PackLocationInfo i, Pack.Metadata metadata) { return factory.apply(i); }
                    },
                    type,
                    new PackSelectionConfig(true, Pack.Position.TOP, false));
            if (pack != null) {
                packConsumer.accept(pack);
            } else {
                LOGGER.error("Pack.readMetaAndCreate returned null for: {}", id);
            }
        });
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == RegistryHandler.COMPRESSED_TAB.get()) return;

        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack stack : event.getParentEntries()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && id.getNamespace().equals(UniversalCompression.MODID)) toRemove.add(stack);
        }
        for (ItemStack stack : toRemove) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        toRemove.clear();
        for (ItemStack stack : event.getSearchEntries()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && id.getNamespace().equals(UniversalCompression.MODID)) toRemove.add(stack);
        }
        for (ItemStack stack : toRemove) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
