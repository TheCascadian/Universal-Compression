package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class VirtualLootPack implements PackResources {

    private static final int PACK_FORMAT = 48;

    private final PackLocationInfo location;

    /**
     * Lazily-built index of all generated loot table resources.
     * Built once on first access; null until then.
     */
    private Map<ResourceLocation, IoSupplier<InputStream>> lootIndex = null;

    public VirtualLootPack(PackLocationInfo location) {
        this.location = location;
    }

    private Map<ResourceLocation, IoSupplier<InputStream>> getIndex() {
        if (lootIndex == null) {
            lootIndex = new HashMap<>();
            buildIndex(lootIndex);
        }
        return lootIndex;
    }

    private static void buildIndex(Map<ResourceLocation, IoSupplier<InputStream>> map) {
        for (Map.Entry<ResourceLocation, java.util.List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
            for (ResourceLocation compId : entry.getValue()) {
                ResourceLocation resourceKey = ResourceLocation.fromNamespaceAndPath(
                        UniversalCompression.MODID,
                        "loot_table/blocks/" + compId.getPath() + ".json");
                final String blockId = UniversalCompression.MODID + ":" + compId.getPath();
                map.put(resourceKey, () -> {
                    byte[] bytes = createSelfDropLootTable(blockId).getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                });
            }
        }
    }

    private static String createSelfDropLootTable(String blockId) {
        return "{"
                + "\"type\":\"minecraft:block\","
                + "\"pools\":[{"
                + "\"rolls\":1,"
                + "\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"" + blockId + "\"}],"
                + "\"conditions\":[{\"condition\":\"minecraft:survives_explosion\"}]"
                + "}]"
                + "}";
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.SERVER_DATA) return null;
        return getIndex().get(location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.SERVER_DATA) return;
        if (!namespace.equals(UniversalCompression.MODID)) return;
        getIndex().forEach((loc, supplier) -> {
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                output.accept(loc, supplier);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.SERVER_DATA) return Set.of();
        return Set.of(UniversalCompression.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Universal Compression Virtual Loot Tables"),
                    PACK_FORMAT,
                    Optional.empty());
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        lootIndex = null;
    }
}
