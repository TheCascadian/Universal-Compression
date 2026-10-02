package com.thecascadian.universalcompression.registry;

import com.mojang.serialization.MapCodec;
import com.thecascadian.universalcompression.UniversalCompression;
import com.thecascadian.universalcompression.block.CompressedBlock;
import com.thecascadian.universalcompression.block.CompressedBlockItem;
import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.recipe.CompressRecipe;
import com.thecascadian.universalcompression.recipe.DecompressRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(
   modid = "universalcompression",
   bus = Bus.MOD
)
public final class RegistryHandler {
   public static final Map<ResourceLocation, List<ResourceLocation>> COMPRESSED_IDS = new LinkedHashMap<>();
   public static final int MAX_TIERS = 9;
   public static final TagKey<Block> NON_COMPRESSIBLE_TAG = TagKey.create(
      Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("universalcompression", "non_compressible")
   );
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "universalcompression");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COMPRESSED_TAB = TABS.register(
      "compressed_blocks",
      () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.universalcompression"))
            .icon(RegistryHandler::getFirstCompressedIcon)
            .displayItems(
               (params, output) -> {
                  List<ItemStack> stacks = COMPRESSED_IDS.values()
                     .stream()
                     .flatMap(Collection::stream)
                     .map(id -> (Item)BuiltInRegistries.ITEM.get(id))
                     .filter(item -> item != Items.AIR)
                     .<ItemStack>map(ItemStack::new)
                     .collect(Collectors.toCollection(ArrayList::new));
                  stacks.sort(Comparator.comparing(stack -> {
                     ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
                     return rl == null ? "" : rl.toString();
                  }));
                  stacks.forEach(output::accept);
               }
            )
            .build()
   );
   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "universalcompression");
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "universalcompression");
   public static final DeferredHolder<RecipeType<?>, RecipeType<CompressRecipe>> COMPRESS_TYPE = RECIPE_TYPES.register(
      "compress", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("universalcompression", "compress"))
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<DecompressRecipe>> DECOMPRESS_TYPE = RECIPE_TYPES.register(
      "decompress", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("universalcompression", "decompress"))
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressRecipe>> COMPRESS_SERIALIZER = RECIPE_SERIALIZERS.register(
      "compress", () -> new RecipeSerializer<CompressRecipe>() {
            public MapCodec<CompressRecipe> codec() {
               return CompressRecipe.CODEC;
            }

            public StreamCodec<RegistryFriendlyByteBuf, CompressRecipe> streamCodec() {
               return CompressRecipe.STREAM_CODEC;
            }
         }
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecompressRecipe>> DECOMPRESS_SERIALIZER = RECIPE_SERIALIZERS.register(
      "decompress", () -> new RecipeSerializer<DecompressRecipe>() {
            public MapCodec<DecompressRecipe> codec() {
               return DecompressRecipe.CODEC;
            }

            public StreamCodec<RegistryFriendlyByteBuf, DecompressRecipe> streamCodec() {
               return DecompressRecipe.STREAM_CODEC;
            }
         }
   );

   private RegistryHandler() {
   }

   public static void init(IEventBus modEventBus) {
      TABS.register(modEventBus);
      RECIPE_TYPES.register(modEventBus);
      RECIPE_SERIALIZERS.register(modEventBus);
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onRegisterBlocks(RegisterEvent event) {
      if (event.getRegistryKey().equals(Registries.BLOCK)) {
         for (Entry<ResourceKey<Block>, Block> entry : new ArrayList<>(BuiltInRegistries.BLOCK.entrySet())) {
            ResourceLocation parentId = entry.getKey().location();
            Block parent = entry.getValue();
            if (!parentId.getNamespace().equals("universalcompression")
               && parent != Blocks.AIR
               && !parentId.getPath().contains("moving_piston")
               && !(parent instanceof EntityBlock)
               && hasOnlyCosmeticProperties(parent)
               && !(parent instanceof AbstractCauldronBlock)
               && !parentId.getPath().contains("cauldron")
               && !parent.defaultBlockState().is(NON_COMPRESSIBLE_TAG)
               && !isExcluded(parent, parentId)
               && !isBlacklisted(parentId)) {
               List<ResourceLocation> tierIds = new ArrayList<>(MAX_TIERS);

               for (int tier = 1; tier <= MAX_TIERS; tier++) {
                  String path = parentId.getNamespace() + "_" + parentId.getPath() + "_compressed_" + tier;
                  ResourceLocation compressedId = ResourceLocation.fromNamespaceAndPath("universalcompression", path);
                  tierIds.add(compressedId);
                  int t = tier;
                  event.register(Registries.BLOCK, compressedId, () -> new CompressedBlock(parent, t, compressedId));
               }

               COMPRESSED_IDS.put(parentId, Collections.unmodifiableList(tierIds));
            }
         }

         UniversalCompression.LOGGER.info("[UniversalCompression] Registered compressed variants for {} block(s).", COMPRESSED_IDS.size());
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onRegisterItems(RegisterEvent event) {
      if (event.getRegistryKey().equals(Registries.ITEM)) {
         for (List<ResourceLocation> tiers : COMPRESSED_IDS.values()) {
            for (ResourceLocation compressedId : tiers) {
               Block block = (Block)BuiltInRegistries.BLOCK.get(compressedId);
               if (block != null) {
                  event.register(Registries.ITEM, compressedId, () -> new CompressedBlockItem((CompressedBlock)block, new Properties()));
               }
            }
         }
      }
   }

   public static List<ResourceLocation> getTiers(ResourceLocation parentId) {
      return COMPRESSED_IDS.getOrDefault(parentId, Collections.emptyList());
   }

   private static ItemStack getFirstCompressedIcon() {
      for (List<ResourceLocation> tiers : COMPRESSED_IDS.values()) {
         if (!tiers.isEmpty()) {
            Item item = (Item)BuiltInRegistries.ITEM.get(tiers.get(0));
            if (item != Items.AIR) {
               return new ItemStack(item);
            }
         }
      }

      return ItemStack.EMPTY;
   }

   private static boolean matchesSpecialBlacklist(String ns, String path) {
      if (ns.equals("securitycraft") && path.toLowerCase().contains("quartz")) {
         return true;
      } else if (ns.equals("create") && path.toLowerCase().contains("quartz")) {
         return true;
      } else if (ns.equals("iceandfire") && path.toLowerCase().contains("scale")) {
         return true;
      } else if (ns.equals("ae2") && path.toLowerCase().contains("certus")) {
         return true;
      } else if (ns.equals("farmersdelight") && path.contains("crate")) {
         return true;
      } else if (ns.equals("ars_nouveau") && path.contains("sourceberry")) {
         return true;
      } else if ((ns.equals("mysticalagriculture") || ns.equals("mysticalagradditions")) && path.contains("ore")) {
         return true;
      } else if (ns.contains("roots") && ns.contains("classic")) {
         return true;
      } else if (ns.contains("mrcrayfish") && ns.contains("refurbished")) {
         return true;
      } else if (!ns.equals("oritech") && !ns.contains("oritech")) {
         if (ns.equals("naturesaura") && path.toLowerCase().contains("light")) {
            return true;
         } else if (ns.equals("compactmachines") && path.equals("wall")) {
            return true;
         } else if (ns.equals("minecraft") && path.contains("bamboo")) {
            return true;
         } else if (ns.equals("undergarden") && path.contains("scintling")) {
            return true;
         } else if (ns.contains("scintling")) {
            return true;
         } else {
            return !ns.equals("undergarden") || !path.equals("goo") && !path.equals("goo_block")
               ? ns.equals("minecolonies") && path.contains("waypoint")
               : true;
         }
      } else {
         return true;
      }
   }

   /** Reads a config value; if the config is not loaded yet (registry events fire before it), reads the toml file directly, then falls back to the default. */
   @SuppressWarnings("unchecked")
   private static <T> T val(net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<T> v) {
      try {
         return v.get();
      } catch (IllegalStateException e) {
         if (EARLY_CONFIG == null) {
            com.electronwill.nightconfig.core.file.CommentedFileConfig cfg = null;
            try {
               java.nio.file.Path file = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("universalcompression-common.toml");
               if (java.nio.file.Files.exists(file)) {
                  cfg = com.electronwill.nightconfig.core.file.CommentedFileConfig.builder(file).build();
                  cfg.load();
               }
            } catch (Exception ex) {
               UniversalCompression.LOGGER.warn("[UniversalCompression] Could not read config file early; using built-in defaults.", ex);
            }
            EARLY_CONFIG = cfg;
            UniversalCompression.LOGGER.info(
               "[UniversalCompression] Config not loaded yet at registration; {}.", cfg != null ? "reading config file directly" : "using built-in defaults"
            );
         }
         if (EARLY_CONFIG != null) {
            Object raw = EARLY_CONFIG.get(v.getPath());
            if (raw != null && v.getDefault().getClass().isInstance(raw)) {
               return (T)raw;
            }
         }
         return v.getDefault();
      }
   }

   private static com.electronwill.nightconfig.core.file.CommentedFileConfig EARLY_CONFIG;

   private static boolean matchesBlacklist(String ns, String path) {
      if (matchesSpecialBlacklist(ns, path)) {
         return true;
      } else {
         try {
            if ((Boolean)val(CompressionConfig.COMMON.enableNsExact)) {
               for (String entry : val(CompressionConfig.COMMON.nsExact)) {
                  if (entry.equals(ns)) {
                     return true;
                  }
               }
            }

            if ((Boolean)val(CompressionConfig.COMMON.enableNsContains)) {
               for (String entryx : val(CompressionConfig.COMMON.nsContains)) {
                  if (ns.contains(entryx)) {
                     return true;
                  }
               }
            }

            if ((Boolean)val(CompressionConfig.COMMON.enableNsStarts)) {
               for (String entryxx : val(CompressionConfig.COMMON.nsStarts)) {
                  if (ns.startsWith(entryxx)) {
                     return true;
                  }
               }
            }

            if ((Boolean)val(CompressionConfig.COMMON.enablePathContains)) {
               for (String entryxxx : val(CompressionConfig.COMMON.pathContains)) {
                  if (path.contains(entryxxx)) {
                     return true;
                  }
               }
            }
         } catch (IllegalStateException var4) {
         }

         return false;
      }
   }

   private static boolean hasOnlyCosmeticProperties(Block block) {
      // pillar blocks (basalt, polished basalt, logs...) only carry an axis; a compressed
      // copy is a plain block anyway, so the property is no reason to skip them
      for (net.minecraft.world.level.block.state.properties.Property<?> property : block.defaultBlockState().getProperties()) {
         if (property != net.minecraft.world.level.block.RotatedPillarBlock.AXIS) {
            return false;
         }
      }
      return true;
   }

   public static boolean isWhitelisted(ResourceLocation id) {
      try {
         return val(CompressionConfig.COMMON.whitelistBlocks).contains(id.toString());
      } catch (IllegalStateException e) {
         return false;
      }
   }

   public static boolean isBlacklisted(ResourceLocation id) {
      if (isWhitelisted(id)) {
         return false;
      }
      String ns = id.getNamespace();
      String path = id.getPath();
      if (matchesBlacklist(ns, path)) {
         return true;
      } else {
         try {
            for (String entry : val(CompressionConfig.COMMON.blacklist)) {
               if (entry.equals(ns) || entry.equals(id.toString())) {
                  return true;
               }
            }

            for (String entryx : val(CompressionConfig.COMMON.excludedMods)) {
               if (entryx.equals(ns)) {
                  return true;
               }
            }

            for (String entryxx : val(CompressionConfig.COMMON.excludedBlocks)) {
               if (entryxx.equals(id.toString())) {
                  return true;
               }
            }

            return false;
         } catch (IllegalStateException var5) {
            return false;
         }
      }
   }

   private static boolean isExcluded(Block block, ResourceLocation id) {
      if (block == Blocks.CAVE_AIR) {
         return true;
      } else {
         return !(block instanceof FlowerBlock)
               && !(block instanceof SaplingBlock)
               && !(block instanceof CarpetBlock)
               && !(block instanceof SlabBlock)
               && !(block instanceof StairBlock)
               && !(block instanceof VineBlock)
               && !(block instanceof FlowerPotBlock)
               && !(block instanceof BaseEntityBlock)
            ? isBlacklisted(id)
            : true;
      }
   }
}
