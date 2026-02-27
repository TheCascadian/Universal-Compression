package com.thecascadian.universalcompression.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration settings for Universal Compression.  The values here are
 * written to a TOML file on disk (`config/universalcompression-common.toml`)
 * which pack authors and end users can edit directly.  All list options have
 * extensive inline comments that explain their purpose; the generated file is
 * human‑readable, and changing any entry will take effect after a reload or
 * restart.
 */
public class CompressionConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        public final ModConfigSpec.ConfigValue<List<? extends String>> blacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> excludedMods;
        public final ModConfigSpec.ConfigValue<List<? extends String>> excludedBlocks;

        // new lists mirror the hard‑coded rules buried in RegistryHandler so that
        // pack makers can tweak behaviour without rebuilding the mod.
        public final ModConfigSpec.ConfigValue<List<? extends String>> nsExact;
        public final ModConfigSpec.ConfigValue<List<? extends String>> nsContains;
        public final ModConfigSpec.ConfigValue<List<? extends String>> nsStarts;
        public final ModConfigSpec.ConfigValue<List<? extends String>> pathContains;

        Common(ModConfigSpec.Builder builder) {
            builder.push("general");
            blacklist = builder
                    .comment("Legacy: list of mod IDs or fully qualified block IDs (e.g. 'create' or 'minecraft:tnt')")
                    .defineList("blacklist", new ArrayList<>(), o -> o instanceof String);
            excludedMods = builder
                    .comment("Mod IDs whose blocks should never be compressed (e.g. 'create', 'botania')")
                    .defineList("excluded_mods", new ArrayList<>(), o -> o instanceof String);
            excludedBlocks = builder
                    .comment("Fully qualified block IDs to exclude (e.g. 'minecraft:tnt', 'create:copper_casing')")
                    .defineList("excluded_blocks", new ArrayList<>(), o -> o instanceof String);

            nsExact = builder
                    .comment("Additional namespaces to blacklist exactly (e.g. 'mymod' will exclude all blocks from that mod")
                    .defineList("blacklist_namespaces_exact", new ArrayList<>(), o -> o instanceof String);
            nsContains = builder
                    .comment("Namespaces which if they contain the supplied string will be blacklisted (case‑sensitive). Useful for catching family mods.")
                    .defineList("blacklist_namespaces_contains", new ArrayList<>(), o -> o instanceof String);
            nsStarts = builder
                    .comment("Namespaces which if they start with the supplied prefix will be blacklisted.")
                    .defineList("blacklist_namespaces_starts", new ArrayList<>(), o -> o instanceof String);
            pathContains = builder
                    .comment("Block ID path substrings that trigger a blacklist (e.g. 'torch' will skip both 'mod:torch' and 'mod:torch_wall').")
                    .defineList("blacklist_paths_contains", new ArrayList<>(), o -> o instanceof String);

            builder.pop();
        }
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}