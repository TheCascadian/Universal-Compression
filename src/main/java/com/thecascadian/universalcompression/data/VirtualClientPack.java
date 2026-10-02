package com.thecascadian.universalcompression.data;

import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

public class VirtualClientPack implements PackResources {
   private static final int PACK_FORMAT = 34;
   private final PackLocationInfo location;
   private static volatile Map<ResourceLocation, IoSupplier<InputStream>> assetCache;

   public VirtualClientPack(PackLocationInfo location) {
      this.location = location;
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

      for (Entry<ResourceLocation, List<ResourceLocation>> entry : RegistryHandler.COMPRESSED_IDS.entrySet()) {
         String parentPath = entry.getKey().getPath();
         String parentNamespace = entry.getKey().getNamespace();
         String baseName = Arrays.stream(parentPath.split("_")).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1)).collect(Collectors.joining(" "));

         for (int i = 0; i < entry.getValue().size(); i++) {
            ResourceLocation compId = entry.getValue().get(i);
            int tier = i + 1;
            String compPath = compId.getPath();
            lang.put("block.universalcompression." + compPath, "Compressed " + baseName + " (Tier " + tier + ")");
            map.put(
               ResourceLocation.fromNamespaceAndPath("universalcompression", "blockstates/" + compPath + ".json"), makeSupplier(() -> makeBlockstate(compPath))
            );
            map.put(
               ResourceLocation.fromNamespaceAndPath("universalcompression", "models/block/" + compPath + ".json"),
               makeSupplier(() -> makeModel(parentNamespace, parentPath, tier))
            );
            map.put(
               ResourceLocation.fromNamespaceAndPath("universalcompression", "models/item/" + compPath + ".json"), makeSupplier(() -> makeItemModel(compPath))
            );
         }
      }

      addStaticLangKeys(lang);
      map.put(ResourceLocation.fromNamespaceAndPath("universalcompression", "lang/en_us.json"), makeSupplier(() -> makeLangFile(lang)));
   }

   private static void addStaticLangKeys(Map<String, String> lang) {
      lang.put("jei.universalcompression.compress", "Compressing");
      lang.put("jei.universalcompression.decompress", "Decompressing");
      lang.put("tooltip.universalcompression.introduced_by", "Introduced by: %s");
      lang.put("universalcompression.config.general", "General");
      lang.put("universalcompression.config.general.blacklist", "Legacy Blacklist");
      lang.put("universalcompression.config.general.excluded_mods", "Excluded Mods");
      lang.put("universalcompression.config.general.excluded_blocks", "Excluded Blocks");
      lang.put("universalcompression.config.general.whitelist_blocks", "Whitelisted Blocks");
      lang.put(
         "universalcompression.config.general.whitelist_blocks.tooltip",
         "Fully-qualified block IDs that are always compressed, overriding every blacklist rule. Example: blacklist a whole mod, then whitelist 'xycraft_world:kivi'."
      );
      lang.put("universalcompression.config.general.enable_ns_exact", "Enable Exact Namespace Blacklist");
      lang.put("universalcompression.config.general.blacklist_namespaces_exact", "Blocked Namespaces (Exact)");
      lang.put("universalcompression.config.general.enable_ns_contains", "Enable Namespace Contains Blacklist");
      lang.put("universalcompression.config.general.blacklist_namespaces_contains", "Blocked Namespaces (Contains)");
      lang.put("universalcompression.config.general.enable_ns_starts", "Enable Namespace Starts-With Blacklist");
      lang.put("universalcompression.config.general.blacklist_namespaces_starts", "Blocked Namespaces (Starts With)");
      lang.put("universalcompression.config.general.enable_path_contains", "Enable Path Substring Blacklist");
      lang.put("universalcompression.config.general.blacklist_paths_contains", "Blocked Path Substrings");
      lang.put("universalcompression.config.general.hide_compressed_items_in_jei", "Hide Compressed Items in JEI");
      lang.put("universalcompression.config.general.show_compress_in_recipe_book", "Show Compress Recipes in Book");
      lang.put("universalcompression.config.general.show_decompress_in_recipe_book", "Show Decompress Recipes in Book");
      lang.put(
         "universalcompression.config.general.blacklist.tooltip",
         "Legacy list of mod IDs or fully-qualified block IDs to exclude from compression (e.g. 'create' or 'minecraft:tnt')."
      );
      lang.put(
         "universalcompression.config.general.excluded_mods.tooltip",
         "Mod IDs whose blocks should never receive compressed variants (e.g. 'create', 'botania')."
      );
      lang.put(
         "universalcompression.config.general.excluded_blocks.tooltip",
         "Fully-qualified block IDs to exclude individually (e.g. 'minecraft:tnt', 'create:copper_casing')."
      );
      lang.put(
         "universalcompression.config.general.enable_ns_exact.tooltip",
         "When off, the exact-namespace blacklist is ignored entirely (all entries in the list below are bypassed)."
      );
      lang.put(
         "universalcompression.config.general.blacklist_namespaces_exact.tooltip",
         "Mod namespaces to block by exact match. Removing an entry here re-enables compression for that entire mod."
      );
      lang.put("universalcompression.config.general.enable_ns_contains.tooltip", "When off, the namespace-contains blacklist is ignored entirely.");
      lang.put(
         "universalcompression.config.general.blacklist_namespaces_contains.tooltip",
         "Block any mod namespace that contains one of these substrings (case-sensitive). Useful for catching mod families."
      );
      lang.put("universalcompression.config.general.enable_ns_starts.tooltip", "When off, the namespace-starts-with blacklist is ignored entirely.");
      lang.put("universalcompression.config.general.blacklist_namespaces_starts.tooltip", "Block any mod namespace that starts with one of these prefixes.");
      lang.put("universalcompression.config.general.enable_path_contains.tooltip", "When off, the path-substring blacklist is ignored entirely.");
      lang.put(
         "universalcompression.config.general.blacklist_paths_contains.tooltip",
         "Block any block whose registry path contains one of these substrings (e.g. 'torch' skips both 'mod:torch' and 'mod:torch_wall'). Note: compound multi-condition rules are still enforced in code regardless of this list."
      );
      lang.put(
         "universalcompression.config.general.hide_compressed_items_in_jei.tooltip",
         "When enabled, all universalcompression:* items are hidden from JEI's ingredient panel to avoid clutter. They remain accessible via the mod's creative tab and the Compressing / Decompressing JEI categories."
      );
      lang.put(
         "universalcompression.config.general.show_compress_in_recipe_book.tooltip",
         "When enabled (default), 3×3 compression recipes appear in the crafting book. Disable to hide them from the recipe book UI only; the recipes remain craftable."
      );
      lang.put(
         "universalcompression.config.general.show_decompress_in_recipe_book.tooltip",
         "When enabled (default), decompression recipes (1 compressed block → 9 originals) appear in the crafting book. Disable to hide them from the recipe book UI only; the recipes remain craftable."
      );
   }

   private static IoSupplier<InputStream> makeSupplier(Supplier<String> jsonSupplier) {
      return () -> new ByteArrayInputStream(jsonSupplier.get().getBytes(StandardCharsets.UTF_8));
   }

   private static String makeBlockstate(String compPath) {
      return "{\"variants\":{\"\":{\"model\":\"universalcompression:block/" + compPath + "\"}}}";
   }

   private static String makeModel(String parentNamespace, String parentPath, int tier) {
      return String.format(
         "{\"loader\":\"%s:compressed\",\"render_type\":\"neoforge:cutout\",\"parent_block\":\"%s:%s\",\"textures\":{\"texture\":\"%s:block/%s\",\"overlay\":\"%s:block/tier_overlay_%d\"},\"tier\":%d}",
         "universalcompression",
         parentNamespace,
         parentPath,
         parentNamespace,
         parentPath,
         "universalcompression",
         tier,
         tier
      );
   }

   private static String makeItemModel(String compPath) {
      return "{\"parent\":\"universalcompression:block/" + compPath + "\"}";
   }

   private static String makeLangFile(Map<String, String> lang) {
      StringBuilder b = new StringBuilder("{");
      boolean first = true;

      for (Entry<String, String> e : lang.entrySet()) {
         if (!first) {
            b.append(',');
         }

         first = false;
         b.append('"').append(e.getKey()).append('"').append(':').append('"').append(e.getValue()).append('"');
      }

      b.append('}');
      return b.toString();
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... paths) {
      return null;
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
      if (type != PackType.CLIENT_RESOURCES) {
         return null;
      } else {
         ensureAssetCache();
         return assetCache.get(location);
      }
   }

   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      if (type == PackType.CLIENT_RESOURCES) {
         if (namespace.equals("universalcompression")) {
            ensureAssetCache();
            assetCache.forEach((loc, supplier) -> {
               if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                  output.accept(loc, supplier);
               }
            });
         }
      }
   }

   public Set<String> getNamespaces(PackType type) {
      return type != PackType.CLIENT_RESOURCES ? Set.of() : Set.of("universalcompression");
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
      return (T)(deserializer == PackMetadataSection.TYPE
         ? new PackMetadataSection(Component.literal("Universal Compression Virtual Client Assets"), 34, Optional.empty())
         : null);
   }

   public PackLocationInfo location() {
      return this.location;
   }

   public void close() {
   }
}
