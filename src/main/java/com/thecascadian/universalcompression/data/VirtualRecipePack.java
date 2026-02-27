package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.config.CompressionConfig;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class VirtualRecipePack implements PackResources {

    private static final int PACK_FORMAT = 48;

    private final PackLocationInfo location;

    /**
     * Lazily-built index of all generated recipe resources.
     * Built once on first access; null until then.
     */
    private Map<ResourceLocation, IoSupplier<InputStream>> recipeIndex = null;

    public VirtualRecipePack(PackLocationInfo location) {
        this.location = location;
    }

    private Map<ResourceLocation, IoSupplier<InputStream>> getIndex() {
        if (recipeIndex == null) {
            recipeIndex = new HashMap<>();
            buildIndex(recipeIndex);
        }
        return recipeIndex;
    }

    private static void buildIndex(Map<ResourceLocation, IoSupplier<InputStream>> map) {
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
            ResourceLocation parentId = entry.getKey();
            List<ResourceLocation> tierIds = entry.getValue();
            if (isBlacklisted(parentId))
                continue;

            String parentItemId = parentId.toString();
            ResourceLocation tier1Id = tierIds.get(0);
            String tier1ItemId = UniversalCompression.MODID + ":" + tier1Id.getPath();

            putRecipe(map, "recipe/" + tier1Id.getPath() + "_from_parent.json",
                    () -> shapedJson(parentItemId, tier1ItemId, 1));
            putRecipe(map, "recipe/" + parentId.getPath() + "_from_tier1.json",
                    () -> shapelessJson(tier1ItemId, parentItemId, 9));

            for (int i = 0; i < tierIds.size() - 1; i++) {
                ResourceLocation lowerId = tierIds.get(i);
                ResourceLocation upperId = tierIds.get(i + 1);
                String lowerItemId = UniversalCompression.MODID + ":" + lowerId.getPath();
                String upperItemId = UniversalCompression.MODID + ":" + upperId.getPath();

                putRecipe(map,
                        "recipe/" + upperId.getPath() + "_from_" + lowerId.getPath() + ".json",
                        () -> shapedJson(lowerItemId, upperItemId, 1));
                putRecipe(map,
                        "recipe/" + lowerId.getPath() + "_decompose.json",
                        () -> shapelessJson(upperItemId, lowerItemId, 9));
            }
        }
    }

    private static void putRecipe(Map<ResourceLocation, IoSupplier<InputStream>> map,
            String path, Supplier<String> jsonSupplier) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, path);
        map.put(rl, makeSupplier(jsonSupplier));
    }

    private static IoSupplier<InputStream> makeSupplier(Supplier<String> jsonSupplier) {
        return () -> new ByteArrayInputStream(jsonSupplier.get().getBytes(StandardCharsets.UTF_8));
    }

    private static String shapedJson(String ingredientId, String resultId, int count) {
        return "{"
                + "\"type\":\"minecraft:crafting_shaped\","
                + "\"category\":\"building\","
                + "\"pattern\":[\"XXX\",\"XXX\",\"XXX\"],"
                + "\"key\":{\"X\":{\"item\":\"" + ingredientId + "\"}},"
                + "\"result\":{\"id\":\"" + resultId + "\",\"count\":" + count + "}"
                + "}";
    }

    private static String shapelessJson(String ingredientId, String resultId, int count) {
        return "{"
                + "\"type\":\"minecraft:crafting_shapeless\","
                + "\"category\":\"building\","
                + "\"ingredients\":[{\"item\":\"" + ingredientId + "\"}],"
                + "\"result\":{\"id\":\"" + resultId + "\",\"count\":" + count + "}"
                + "}";
    }

    private static boolean isBlacklisted(ResourceLocation id) {
        // reuse the registry handler's logic so we don't drift
        if (RegistryHandler.isBlacklisted(id)) {
            return true;
        }
        try {
            for (String entry : CompressionConfig.COMMON.blacklist.get()) {
                if (entry.equals(id.getNamespace()) || entry.equals(id.toString()))
                    return true;
            }
        } catch (IllegalStateException e) {
            // config not loaded yet; nothing is blacklisted from the pack
        }
        return false;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.SERVER_DATA)
            return null;
        return getIndex().get(location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.SERVER_DATA)
            return;
        if (!namespace.equals(UniversalCompression.MODID))
            return;
        getIndex().forEach((loc, supplier) -> {
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                output.accept(loc, supplier);
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.SERVER_DATA)
            return Set.of();
        return Set.of(UniversalCompression.MODID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Universal Compression Virtual Recipes"),
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
        recipeIndex = null;
    }
}
