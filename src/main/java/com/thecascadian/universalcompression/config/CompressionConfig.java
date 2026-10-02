package com.thecascadian.universalcompression.config;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CompressionConfig {
   public static final ModConfigSpec COMMON_SPEC;
   public static final CompressionConfig.Common COMMON;

   public static void register(ModContainer container) {
      container.registerConfig(Type.COMMON, COMMON_SPEC);
   }

   static {
      Builder builder = new Builder();
      COMMON = new CompressionConfig.Common(builder);
      COMMON_SPEC = builder.build();
   }

   public static class Common {
      public final ConfigValue<List<? extends String>> blacklist;
      public final ConfigValue<List<? extends String>> excludedMods;
      public final ConfigValue<List<? extends String>> excludedBlocks;
      public final ConfigValue<List<? extends String>> whitelistBlocks;
      public final BooleanValue enableNsExact;
      public final ConfigValue<List<? extends String>> nsExact;
      public final BooleanValue enableNsContains;
      public final ConfigValue<List<? extends String>> nsContains;
      public final BooleanValue enableNsStarts;
      public final ConfigValue<List<? extends String>> nsStarts;
      public final BooleanValue enablePathContains;
      public final ConfigValue<List<? extends String>> pathContains;
      public final BooleanValue hideCompressedItemsInJEI;
      public final BooleanValue showCompressInRecipeBook;
      public final BooleanValue showDecompressInRecipeBook;
      private static final List<String> DEFAULT_NS_EXACT = List.of(
         "chipped",
         "allthecompressed",
         "path",
         "productivetrees",
         "xtones_reworked",
         "xtonesreworked",
         "domum_ornamentum",
         "domumornamentum",
         "modern_industrialization",
         "aether",
         "immersiveengineering",
         "immersive_engineering",
         "occultism",
         "luminax",
         "create",
         "createaddition",
         "extended_industrialization",
         "rftoolsbuilder",
         "rftoolsbase",
         "corail_tombstone",
         "antibuilt",
         "eternal_starlight",
         "xnet",
         "bellsandwhistles",
         "xycraftworld",
         "xycraft_world",
         "troolvidr",
         "soundmuffler",
         "ars_nouveau",
         "regions_unexplored",
         "herbsandharvest",
         "biomeswevegone",
         "xycraft_override",
         "twilightforest",
         "merrymaking",
         "mysticalagriculture",
         "mysticalagradditions",
         "ae2",
         "compact_machines",
         "compactmachines",
         "forbidden_arcanus",
         "refurbished_furniture",
         "mrcrayfish",
         "quarryplus",
         "utilitarian",
         "supplementaries",
         "mamas_merrymaking",
         "nightfall",
         "factoryblocks",
         "mininggadgets",
         "mining_gadgets",
         "stevescarts",
         "structurize"
      );
      private static final List<String> DEFAULT_NS_CONTAINS = List.of(
         "dyenamics", "rechiseled", "apothic", "macaw", "chromacarvings", "oritech", "handcrafted", "spellbooks", "factory", "productivebees", "tombstone"
      );
      private static final List<String> DEFAULT_NS_STARTS = List.of(
         "enderio", "oh_the_biomes", "com.mrcrayfish.furniture.refurbished", "macaws_furniture", "macawsfurniture", "mcw"
      );
      private static final List<String> DEFAULT_PATH_CONTAINS = List.of(
         "cushion",
         "honey",
         "shelf",
         "egg",
         "cactus",
         "garden",
         "torch",
         "fur",
         "ink_mushroom_stem",
         "cave_hyssop",
         "ashen_deepturf",
         "candle",
         "smithing_table",
         "magma_block",
         "dreadful_dirt",
         "lodestone",
         "quartz_netherrack",
         "smooth_quartz",
         "quartz_block",
         "chiseled_quartz",
         "bamboo_shoot",
         "twisting_vine",
         "melon",
         "pumpkin",
         "ancient_podzol",
         "ancient_debris",
         "nether_sprout",
         "nether_sprouts",
         "wild_flax",
         "wild_fluffy",
         "snow_block",
         "dirt_path",
         "fletching_table",
         "smooth_red_sandstone",
         "smooth_sandstone",
         "lily",
         "mushroom",
         "debug",
         "structure_void",
         "machine_casing",
         "smooth_kivi",
         "aluminum_storage",
         "dried_kelp_block",
         "magical_soil",
         "shimmerweed",
         "miserabell",
         "butterbunch",
         "frozen_deepturf",
         "deepturf",
         "gloomgourd",
         "solid_compact_machine_wall",
         "medium_pot",
         "weeping_vines_plant",
         "spore_blossom",
         "waxed",
         "bush",
         "fern",
         "shrub",
         "root",
         "grass",
         "invisible",
         "icicle",
         "bioshroom",
         "crate",
         "block_of_plastic",
         "battery",
         "runic",
         "tropical_garden",
         "veiled",
         "thin_pot",
         "leaves",
         "lamp",
         "glass",
         "crafting_table",
         "supreme_machine_frame",
         "simple_machine_frame",
         "advanced_machine_frame",
         "pity_machine_frame",
         "enhanced_galgadorian",
         "galgadorian",
         "waypoint_placeholder",
         "placeholder",
         "crimson_fungus",
         "soul_herb",
         "duckweed",
         "reinforced_metal_block",
         "redstone_bud",
         "soulless_sandstone",
         "mortar",
         "lava_factory_casing",
         "air",
         "cluster",
         "amorous_bristle",
         "dragon_ice_spikes",
         "glistering_wart",
         "sculk_tendrils_plant",
         "kelp_plant",
         "fluid_placeholder",
         "wide_pot",
         "trophy",
         "wax brick",
         "wax block",
         "wax tile",
         "wax",
         "veiled_mushroom",
         "veiledmushroom",
         "comb_block",
         "combblock",
         "unexplored_plank",
         "unexplored_log",
         "mistletoe",
         "seeping_ink",
         "seepingink",
         "herb",
         "fire",
         "machine_void_air",
         "rice_bag",
         "forbidden_arcanus.upwind",
         "forbidden_arcanus.whirlwind",
         "machine_frame",
         "dimension_boundary",
         "scintling",
         "bubble",
         "delightful_dirt",
         "luminis",
         "glistering_ivy",
         "glistering ivy",
         "glistering",
         "cobweb",
         "half_cobweb",
         "prismoss",
         "frogspawn",
         "infested",
         "station",
         "bench",
         "matrix_frame",
         "milky_comb",
         "dense_bubble",
         "arcane_crystal",
         "whirlwind",
         "royal_jelly",
         "royaljelly",
         "ghost",
         "soldering_table",
         "cartography_table",
         "azalea",
         "flowering_azalea",
         "regalium",
         "phantom_booster",
         "block_bio_fuel",
         "reinforced_deepslate",
         "warped_fungus",
         "miners_light",
         "miner_light"
      );

      Common(Builder builder) {
         builder.translation("universalcompression.config.general").push("general");
         this.blacklist = builder.translation("universalcompression.config.general.blacklist")
            .comment("Legacy: list of mod IDs or fully qualified block IDs (e.g. 'create' or 'minecraft:tnt')")
            .gameRestart()
            .defineList("blacklist", new ArrayList(), () -> "", o -> o instanceof String);
         this.excludedMods = builder.translation("universalcompression.config.general.excluded_mods")
            .comment("Mod IDs whose blocks should never be compressed (e.g. 'create', 'botania')")
            .gameRestart()
            .defineList("excluded_mods", new ArrayList(), () -> "", o -> o instanceof String);
         this.excludedBlocks = builder.translation("universalcompression.config.general.excluded_blocks")
            .comment("Fully qualified block IDs to exclude (e.g. 'minecraft:tnt', 'create:copper_casing')")
            .gameRestart()
            .defineList("excluded_blocks", new ArrayList(), () -> "", o -> o instanceof String);
         this.whitelistBlocks = builder.translation("universalcompression.config.general.whitelist_blocks")
            .comment("Fully qualified block IDs that are always compressed, overriding every blacklist rule above (e.g. blacklist a whole mod but whitelist 'xycraft_world:kivi')")
            .gameRestart()
            .defineList("whitelist_blocks", new ArrayList(), () -> "", o -> o instanceof String);
         this.enableNsExact = builder.translation("universalcompression.config.general.enable_ns_exact")
            .comment("Enable/disable the exact-namespace blacklist entirely.")
            .gameRestart()
            .define("enable_ns_exact", true);
         this.nsExact = builder.translation("universalcompression.config.general.blacklist_namespaces_exact")
            .comment("Namespaces (mod IDs) to blacklist by exact match.  Edit this list to add/remove whole mods without recompiling.")
            .gameRestart()
            .defineList("blacklist_namespaces_exact", new ArrayList<>(DEFAULT_NS_EXACT), () -> "", o -> o instanceof String);
         this.enableNsContains = builder.translation("universalcompression.config.general.enable_ns_contains")
            .comment("Enable/disable the namespace-contains blacklist entirely.")
            .gameRestart()
            .define("enable_ns_contains", true);
         this.nsContains = builder.translation("universalcompression.config.general.blacklist_namespaces_contains")
            .comment("Blacklist any namespace that *contains* one of these substrings (case-sensitive).")
            .gameRestart()
            .defineList("blacklist_namespaces_contains", new ArrayList<>(DEFAULT_NS_CONTAINS), () -> "", o -> o instanceof String);
         this.enableNsStarts = builder.translation("universalcompression.config.general.enable_ns_starts")
            .comment("Enable/disable the namespace-starts-with blacklist entirely.")
            .gameRestart()
            .define("enable_ns_starts", true);
         this.nsStarts = builder.translation("universalcompression.config.general.blacklist_namespaces_starts")
            .comment("Blacklist any namespace that *starts with* one of these prefixes.")
            .gameRestart()
            .defineList("blacklist_namespaces_starts", new ArrayList<>(DEFAULT_NS_STARTS), () -> "", o -> o instanceof String);
         this.enablePathContains = builder.translation("universalcompression.config.general.enable_path_contains")
            .comment("Enable/disable the path-substring blacklist entirely.")
            .gameRestart()
            .define("enable_path_contains", true);
         this.pathContains = builder.translation("universalcompression.config.general.blacklist_paths_contains")
            .comment(
               "Blacklist any block whose registry path *contains* one of these substrings (e.g. 'torch' will skip both 'mod:torch' and 'mod:torch_wall').  Note: some compound rules are still enforced by hard-coded logic in RegistryHandler.matchesSpecialBlacklist."
            )
            .gameRestart()
            .defineList("blacklist_paths_contains", new ArrayList<>(DEFAULT_PATH_CONTAINS), () -> "", o -> o instanceof String);
         this.hideCompressedItemsInJEI = builder.translation("universalcompression.config.general.hide_compressed_items_in_jei")
            .comment(
               "When true (default), all universalcompression:* items are hidden from JEI's ingredient panel so they don't overwhelm the list.  They remain accessible via the mod's own creative tab and via the JEI Compressing / Decompressing categories.  Set to false if you want them to appear in the ingredient panel."
            )
            .define("hide_compressed_items_in_jei", true);
         this.showCompressInRecipeBook = builder.translation("universalcompression.config.general.show_compress_in_recipe_book")
            .comment(
               "When true (default), 3×3 compression recipes (e.g. 9 cobblestone → 1 compressed cobblestone) are visible in the crafting book.  Disable to hide them from the recipe book UI only; the recipes remain craftable."
            )
            .define("show_compress_in_recipe_book", true);
         this.showDecompressInRecipeBook = builder.translation("universalcompression.config.general.show_decompress_in_recipe_book")
            .comment(
               "When true (default), decompression recipes (1 compressed block → 9 originals) are visible in the crafting book.  Disable to hide them from the recipe book UI only; the recipes remain craftable."
            )
            .define("show_decompress_in_recipe_book", true);
         builder.pop();
      }
   }
}
