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
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VirtualClientPack implements PackResources {

    private static final int PACK_FORMAT = 34;

    private final PackLocationInfo location;

    private static volatile Map<ResourceLocation, IoSupplier<InputStream>> assetCache;

    public VirtualClientPack(PackLocationInfo location) {
        this.location = location;
        // Do NOT call ensureAssetCache() here.
        // This constructor is invoked by Pack.readMetaAndCreate during
        // AddPackFindersEvent,
        // which fires before RegisterEvent. At that point COMPRESSED_IDS is still
        // empty,
        // so building the cache here would freeze an empty asset map for the lifetime
        // of
        // the JVM. The cache is built on first access via getResource/listResources, by
        // which time block registration is complete.
    }

    private static void ensureAssetCache() {
        if (assetCache == null) {
            synchronized (VirtualClientPack.class) {
                if (assetCache == null) {
                    Map<ResourceLocation, IoSupplier<InputStream>> map = new LinkedHashMap<>();
                    buildAssetCache(map);
                    assetCache = Collections.unmodifiableMap(map);
                }
            }
        }
    }

    private static void buildAssetCache(Map<ResourceLocation, IoSupplier<InputStream>> map) {
        Map<String, String> lang = new HashMap<>();

        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
            String parentPath = entry.getKey().getPath();
            String parentNamespace = entry.getKey().getNamespace();

            String baseName = Arrays.stream(parentPath.split("_"))
                    .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                    .collect(Collectors.joining(" "));

            for (int i = 0; i < entry.getValue().size(); i++) {
                ResourceLocation compId = entry.getValue().get(i);
                int tier = i + 1;
                String compPath = compId.getPath();

                lang.put("block." + UniversalCompression.MODID + "." + compPath,
                        "Compressed " + baseName + " (Tier " + tier + ")");

                map.put(
                        ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID,
                                "blockstates/" + compPath + ".json"),
                        makeSupplier(() -> makeBlockstate(compPath)));

                final String fp = parentPath, fn = parentNamespace;
                final int ft = tier;
                map.put(
                        ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID,
                                "models/block/" + compPath + ".json"),
                        makeSupplier(() -> makeModel(fn, fp, ft)));

                map.put(
                        ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID,
                                "models/item/" + compPath + ".json"),
                        makeSupplier(() -> makeItemModel(compPath)));
            }
        }

        map.put(
                ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, "lang/en_us.json"),
                makeSupplier(() -> makeLangFile(lang)));
    }

    private static IoSupplier<InputStream> makeSupplier(Supplier<String> jsonSupplier) {
        return () -> new ByteArrayInputStream(jsonSupplier.get().getBytes(StandardCharsets.UTF_8));
    }

    private static String makeBlockstate(String compPath) {
        return "{\"variants\":{\"\":{\"model\":\"" + UniversalCompression.MODID + ":block/" + compPath + "\"}}}";
    }

    private static String makeModel(String parentNamespace, String parentPath, int tier) {
        return String.format(
                "{\"loader\":\"%s:compressed\","
                        + "\"render_type\":\"neoforge:cutout\","
                        + "\"textures\":{"
                        + "\"texture\":\"%s:block/%s\","
                        + "\"overlay\":\"%s:block/tier_overlay_%d\""
                        + "},"
                        + "\"tier\":%d}",
                UniversalCompression.MODID,
                parentNamespace, parentPath,
                UniversalCompression.MODID, tier,
                tier);
    }

    private static String makeItemModel(String compPath) {
        return "{\"parent\":\"" + UniversalCompression.MODID + ":block/" + compPath + "\"}";
    }

    private static String makeLangFile(Map<String, String> lang) {
        StringBuilder b = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : lang.entrySet()) {
            if (!first)
                b.append(',');
            first = false;
            b.append('"').append(e.getKey()).append('"')
                    .append(':').append('"').append(e.getValue()).append('"');
        }
        b.append('}');
        return b.toString();
    }

    // -------------------------------------------------------------------------
    // PackResources
    // -------------------------------------------------------------------------

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES)
            return null;
        ensureAssetCache();
        return assetCache.get(location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES)
            return;
        if (!namespace.equals(UniversalCompression.MODID))
            return;
        ensureAssetCache();
        assetCache.forEach((loc, supplier) -> {
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                output.accept(loc, supplier);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.CLIENT_RESOURCES)
            return Set.of();
        return Set.of(UniversalCompression.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Universal Compression Virtual Client Assets"),
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
    }
}
