package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

public class VirtualLootPack implements PackResources {
   private static final int PACK_FORMAT = 48;
   private final PackLocationInfo location;
   private Map<ResourceLocation, IoSupplier<InputStream>> lootIndex = null;

   public VirtualLootPack(PackLocationInfo location) {
      this.location = location;
   }

   private Map<ResourceLocation, IoSupplier<InputStream>> getIndex() {
      if (this.lootIndex == null) {
         this.lootIndex = new HashMap<>();
         buildIndex(this.lootIndex);
      }

      return this.lootIndex;
   }

   private static void buildIndex(Map<ResourceLocation, IoSupplier<InputStream>> map) {
      for (Entry<ResourceLocation, List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
         for (ResourceLocation compId : entry.getValue()) {
            ResourceLocation resourceKey = ResourceLocation.fromNamespaceAndPath("universalcompression", "loot_table/blocks/" + compId.getPath() + ".json");
            String blockId = "universalcompression:" + compId.getPath();
            map.put(resourceKey, () -> {
               byte[] bytes = createSelfDropLootTable(blockId).getBytes(StandardCharsets.UTF_8);
               return new ByteArrayInputStream(bytes);
            });
         }
      }
   }

   private static String createSelfDropLootTable(String blockId) {
      return "{\"type\":\"minecraft:block\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\""
         + blockId
         + "\"}],\"conditions\":[{\"condition\":\"minecraft:survives_explosion\"}]}]}";
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... paths) {
      return null;
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
      return type != PackType.SERVER_DATA ? null : this.getIndex().get(location);
   }

   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      if (type == PackType.SERVER_DATA) {
         if (namespace.equals("universalcompression")) {
            this.getIndex().forEach((loc, supplier) -> {
               if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                  output.accept(loc, supplier);
               }
            });
         }
      }
   }

   public Set<String> getNamespaces(PackType type) {
      return type != PackType.SERVER_DATA ? Set.of() : Set.of("universalcompression");
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
      return (T)(deserializer == PackMetadataSection.TYPE
         ? new PackMetadataSection(Component.literal("Universal Compression Virtual Loot Tables"), 48, Optional.empty())
         : null);
   }

   public PackLocationInfo location() {
      return this.location;
   }

   public void close() {
      this.lootIndex = null;
   }
}
