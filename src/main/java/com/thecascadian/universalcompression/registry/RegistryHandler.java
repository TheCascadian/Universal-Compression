package com.thecascadian.universalcompression.registry;

import com.mojang.serialization.MapCodec;
import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.block.CompressedBlock;
import com.thecascadian.universalcompression.block.CompressedBlockItem;
import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.recipe.CompressRecipe;
import com.thecascadian.universalcompression.recipe.DecompressRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;

@EventBusSubscriber(modid = UniversalCompression.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class RegistryHandler {

    private RegistryHandler() {
    }

    public static final Map<ResourceLocation, List<ResourceLocation>> COMPRESSED_IDS = new LinkedHashMap<>();

    public static final int MAX_TIERS = 8;

    public static final TagKey<Block> NON_COMPRESSIBLE_TAG = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, "non_compressible"));

    // -------------------------------------------------------------------------
    // Creative tab
    // -------------------------------------------------------------------------

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            UniversalCompression.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COMPRESSED_TAB = TABS
            .register("compressed_blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + UniversalCompression.MODID))
                    .icon(RegistryHandler::getFirstCompressedIcon)
                    .displayItems((params, output) -> {
                        List<ItemStack> stacks = COMPRESSED_IDS.values().stream()
                                .flatMap(List::stream)
                                .map(id -> BuiltInRegistries.ITEM.get(id))
                                .filter(item -> item != Items.AIR)
                                .map(ItemStack::new)
                                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                        // sort by registry name (namespace:path) for stable ordering
                        stacks.sort(Comparator.comparing(stack -> {
                            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
                            return rl == null ? "" : rl.toString();
                        }));
                        stacks.forEach(output::accept);
                    })
                    .build());

    // -------------------------------------------------------------------------
    // Recipe types and serializers
    // -------------------------------------------------------------------------

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
            UniversalCompression.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, UniversalCompression.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressRecipe>> COMPRESS_TYPE = RECIPE_TYPES.register(
            "compress",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, "compress")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<DecompressRecipe>> DECOMPRESS_TYPE = RECIPE_TYPES
            .register("decompress",
                    () -> RecipeType
                            .simple(ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, "decompress")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressRecipe>> COMPRESS_SERIALIZER = RECIPE_SERIALIZERS
            .register("compress", () -> new RecipeSerializer<CompressRecipe>() {
                @Override
                public MapCodec<CompressRecipe> codec() {
                    return CompressRecipe.CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, CompressRecipe> streamCodec() {
                    return CompressRecipe.STREAM_CODEC;
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecompressRecipe>> DECOMPRESS_SERIALIZER = RECIPE_SERIALIZERS
            .register("decompress", () -> new RecipeSerializer<DecompressRecipe>() {
                @Override
                public MapCodec<DecompressRecipe> codec() {
                    return DecompressRecipe.CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, DecompressRecipe> streamCodec() {
                    return DecompressRecipe.STREAM_CODEC;
                }
            });

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    public static void init(IEventBus modEventBus) {
        TABS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    // -------------------------------------------------------------------------
    // Block and item registration
    // -------------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterBlocks(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK))
            return;

        List<Map.Entry<ResourceKey<Block>, Block>> snapshot = new ArrayList<>(BuiltInRegistries.BLOCK.entrySet());

        for (Map.Entry<ResourceKey<Block>, Block> entry : snapshot) {
            ResourceLocation parentId = entry.getKey().location();
            Block parent = entry.getValue();

            if (parentId.getNamespace().equals(UniversalCompression.MODID))
                continue;
            if (parent == Blocks.AIR || parentId.getPath().contains("moving_piston"))
                continue;
            if (parent instanceof EntityBlock)
                continue;
            if (!parent.defaultBlockState().getProperties().isEmpty())
                continue;
            if (parent instanceof AbstractCauldronBlock || parentId.getPath().contains("cauldron"))
                continue;
            if (parent.defaultBlockState().is(NON_COMPRESSIBLE_TAG))
                continue;
            if (isExcluded(parent, parentId))
                continue;
            if (isBlacklisted(parentId))
                continue;

            List<ResourceLocation> tierIds = new ArrayList<>(MAX_TIERS);
            for (int tier = 1; tier <= MAX_TIERS; tier++) {
                String path = parentId.getNamespace() + "_" + parentId.getPath() + "_compressed_" + tier;
                ResourceLocation compressedId = ResourceLocation.fromNamespaceAndPath(UniversalCompression.MODID, path);
                tierIds.add(compressedId);
                final int t = tier;
                event.register(Registries.BLOCK, compressedId, () -> new CompressedBlock(parent, t, compressedId));
            }
            COMPRESSED_IDS.put(parentId, Collections.unmodifiableList(tierIds));
        }

        UniversalCompression.LOGGER.info("[UniversalCompression] Registered compressed variants for {} block(s).",
                COMPRESSED_IDS.size());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterItems(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM))
            return;

        for (List<ResourceLocation> tiers : COMPRESSED_IDS.values()) {
            for (ResourceLocation compressedId : tiers) {
                Block block = BuiltInRegistries.BLOCK.get(compressedId);
                if (block == null)
                    continue;
                event.register(Registries.ITEM, compressedId,
                        () -> new CompressedBlockItem((CompressedBlock) block, new Item.Properties()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public static List<ResourceLocation> getTiers(ResourceLocation parentId) {
        return COMPRESSED_IDS.getOrDefault(parentId, Collections.emptyList());
    }

    private static ItemStack getFirstCompressedIcon() {
        for (List<ResourceLocation> tiers : COMPRESSED_IDS.values()) {
            if (!tiers.isEmpty()) {
                Item item = BuiltInRegistries.ITEM.get(tiers.get(0));
                if (item != Items.AIR)
                    return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

    // ---------------------------------------------------------------------
    // structured blacklist data
    // ---------------------------------------------------------------------

    private static final Set<String> BLACKLISTED_NAMESPACE_EXACT = Set.of(
            "chipped", "allthecompressed", "path", "productivetrees",
            "xtones_reworked", "xtonesreworked", "domum_ornamentum",
            "domumornamentum", "modern_industrialization", "aether",
            "immersiveengineering", "immersive_engineering", "occultism",
            "luminax", "create", "createaddition",
            "extended_industrialization", "rftoolsbuilder", "rftoolsbase",
            "corail_tombstone", "antibuilt", "eternal_starlight",
            "xnet", "bellsandwhistles", "xycraftworld", "xycraft_world", "troolvidr",
            "soundmuffler", "ars_nouveau",
            "regions_unexplored", "herbsandharvest", "biomeswevegone",
            "xycraft_override", "twilightforest", "merrymaking",
            "mysticalagriculture", "mysticalagradditions",
            "ae2", /* conditional rules exist */
            "compact_machines", "compactmachines", "forbidden_arcanus", "refurbished_furniture", "mrcrayfish",
            "quarryplus",
            "utilitarian", "supplementaries", "mamas_merrymaking",
            "nightfall", "factoryblocks", "mininggadgets", "mining_gadgets", "stevescarts",
            "structurize");

    private static final Set<String> BLACKLISTED_NAMESPACE_CONTAINS = Set.of(
            "dyenamics", "rechiseled", "apothic", "macaw", "chromacarvings",
            "oritech", "handcrafted", "spellbooks", "factory",
            "productivebees", "tombstone");

    private static final Set<String> BLACKLISTED_NAMESPACE_STARTS = Set.of(
            "enderio", "oh_the_biomes", "com.mrcrayfish.furniture.refurbished",
            "macaws_furniture", "macawsfurniture", "mcw");

    private static final Set<String> BLACKLISTED_PATH_CONTAINS = Set.of(
            "cushion", "honey", "shelf", "egg", "cactus", "garden",
            "torch", "fur", "ink_mushroom_stem", "cave_hyssop", "ashen_deepturf",
            "candle", "smithing_table", "magma_block",
            "dreadful_dirt", "lodestone", "quartz_netherrack",
            "smooth_quartz", "quartz_block", "chiseled_quartz",
            "bamboo_shoot", "twisting_vine", "melon", "pumpkin",
            "ancient_podzol",
            "ancient_debris", "nether_sprout", "nether_sprouts",
            "wild_flax", "wild_fluffy",
            "snow_block", "dirt_path", "fletching_table",
            "smooth_red_sandstone", "smooth_sandstone",
            "lily", "mushroom", "debug",
            "structure_void", "machine_casing",
            "smooth_kivi", "aluminum_storage",
            "dried_kelp_block", "magical_soil",
            "shimmerweed", "miserabell", "butterbunch",
            "frozen_deepturf", "deepturf", "gloomgourd", "solid_compact_machine_wall",
            "medium_pot", "weeping_vines_plant", "spore_blossom", "waxed",
            "bush", "fern", "shrub", "root", "grass", "invisible",
            "icicle", "bioshroom", "crate", "block_of_plastic", "battery",
            "runic", "tropical_garden", "veiled", "thin_pot", "leaves",
            "lamp", "glass", "crafting_table", "supreme_machine_frame",
            "simple_machine_frame", "advanced_machine_frame", "pity_machine_frame",
            "enhanced_galgadorian", "galgadorian", "waypoint_placeholder", "placeholder", "crimson_fungus",
            "soul_herb", "duckweed", "reinforced_metal_block", "redstone_bud",
            "soulless_sandstone", "mortar", "lava_factory_casing", "air",
            "cluster", "amorous_bristle", "dragon_ice_spikes",
            "glistering_wart", "sculk_tendrils_plant", "kelp_plant",
            "fluid_placeholder", "wide_pot", "trophy", "wax brick",
            "wax block", "wax tile", "wax", "veiled_mushroom",
            "veiledmushroom", "comb_block", "combblock", "unexplored_plank",
            "unexplored_log", "mistletoe", "seeping_ink", "seepingink",
            "herb", "fire", "machine_void_air", "rice_bag",
            "forbidden_arcanus.upwind", "forbidden_arcanus.whirlwind", "machine_frame", "dimension_boundary",
            "scintling", "bubble", "delightful_dirt", "luminis",
            "glistering_ivy", "glistering ivy", "glistering", "cobweb", "half_cobweb",
            "prismoss", "frogspawn", "infested",
            "station", "bench", "matrix_frame", "milky_comb", "dense_bubble",
            "arcane_crystal", "whirlwind", "royal_jelly", "royaljelly", "ghost",
            "soldering_table", "cartography_table", "azalea", "flowering_azalea",
            "regalium", "phantom_booster", "block_bio_fuel", "reinforced_deepslate", "warped_fungus", "miners_light",
            "miner_light");

    private static boolean matchesSpecialBlacklist(String ns, String path) {
        // compound rules that can't be captured by simple contains/starts/equals
        if (ns.equals("securitycraft") && path.toLowerCase().contains("quartz"))
            return true;
        if (ns.equals("create") && path.toLowerCase().contains("quartz"))
            return true;
        if (ns.equals("iceandfire") && path.toLowerCase().contains("scale"))
            return true;
        if (ns.equals("ae2") && path.toLowerCase().contains("certus"))
            return true;
        if (ns.equals("farmersdelight") && path.contains("crate"))
            return true;
        if (ns.equals("ars_nouveau") && path.contains("sourceberry"))
            return true;
        // mystical agriculture ores
        if ((ns.equals("mysticalagriculture") || ns.equals("mysticalagradditions")) && path.contains("ore"))
            return true;
        if ((ns.contains("roots") && ns.contains("classic")))
            return true;
        if ((ns.contains("mrcrayfish") && ns.contains("refurbished")))
            return true;
        if (ns.equals("oritech") || ns.contains("oritech"))
            return true;
        // NaturesAura light blocks
        if (ns.equals("naturesaura") && path.toLowerCase().contains("light"))
            return true;
        // some mods use a generic wall block name
        if (ns.equals("compactmachines") && path.equals("wall"))
            return true;
        // all bamboo variants from vanilla minecraft
        if (ns.equals("minecraft") && path.contains("bamboo"))
            return true;
        // scintling blocks from undergarden or any scintling-namespaced mod
        if (ns.equals("undergarden") && path.contains("scintling"))
            return true;
        if (ns.contains("scintling"))
            return true;
        // undergarden goo blocks — previously had "undergarden:goo" with a colon in
        // BLACKLISTED_PATH_CONTAINS which never matched (only the path portion is
        // tested)
        if (ns.equals("undergarden") && (path.equals("goo") || path.equals("goo_block")))
            return true;
        // minecolonies waypoint placeholder
        if (ns.equals("minecolonies") && path.contains("waypoint"))
            return true;
        return false;
    }

    private static boolean matchesBlacklist(String ns, String path) {
        // built‑in rules first – always applied
        if (BLACKLISTED_NAMESPACE_EXACT.contains(ns))
            return true;
        for (String partial : BLACKLISTED_NAMESPACE_CONTAINS) {
            if (ns.contains(partial))
                return true;
        }
        for (String prefix : BLACKLISTED_NAMESPACE_STARTS) {
            if (ns.startsWith(prefix))
                return true;
        }
        for (String substr : BLACKLISTED_PATH_CONTAINS) {
            if (path.contains(substr))
                return true;
        }
        if (matchesSpecialBlacklist(ns, path))
            return true;

        // now look at the config lists; if the config system isn't initialised
        // yet we just ignore them and continue (the catch below mirrors other
        // usage sites).
        try {
            for (String entry : CompressionConfig.COMMON.nsExact.get()) {
                if (entry.equals(ns)) return true;
            }
            for (String entry : CompressionConfig.COMMON.nsContains.get()) {
                if (ns.contains(entry)) return true;
            }
            for (String entry : CompressionConfig.COMMON.nsStarts.get()) {
                if (ns.startsWith(entry)) return true;
            }
            for (String entry : CompressionConfig.COMMON.pathContains.get()) {
                if (path.contains(entry)) return true;
            }
        } catch (IllegalStateException e) {
            // config not yet loaded; safe to ignore
        }

        return false;
    }

    public static boolean isBlacklisted(ResourceLocation id) {
        String ns = id.getNamespace();
        String path = id.getPath();
        if (matchesBlacklist(ns, path)) {
            return true;
        }

        try {
            for (String entry : CompressionConfig.COMMON.blacklist.get()) {
                if (entry.equals(ns) || entry.equals(id.toString()))
                    return true;
            }
            for (String entry : CompressionConfig.COMMON.excludedMods.get()) {
                if (entry.equals(ns))
                    return true;
            }
            for (String entry : CompressionConfig.COMMON.excludedBlocks.get()) {
                if (entry.equals(id.toString()))
                    return true;
            }
        } catch (IllegalStateException e) {
            return false;
        }
        return false;
    }

    private static boolean isExcluded(Block block, ResourceLocation id) {
        // always skip air
        if (block == Blocks.CAVE_AIR)
            return true;

        // exclude classes that are clearly not compressible
        if (block instanceof net.minecraft.world.level.block.FlowerBlock
                || block instanceof net.minecraft.world.level.block.SaplingBlock
                || block instanceof net.minecraft.world.level.block.CarpetBlock
                || block instanceof net.minecraft.world.level.block.SlabBlock
                || block instanceof net.minecraft.world.level.block.StairBlock
                || block instanceof net.minecraft.world.level.block.VineBlock
                || block instanceof net.minecraft.world.level.block.FlowerPotBlock
                || block instanceof net.minecraft.world.level.block.BaseEntityBlock)
            return true;

        // delegate the rest of the logic to the centralized blacklist
        return isBlacklisted(id);
    }
}