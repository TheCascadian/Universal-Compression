package com.thecascadian.universalcompression;

import com.thecascadian.universalcompression.client.gui.UCConfigSectionScreen;
import com.thecascadian.universalcompression.client.recipebook.RecipeBookVisibility;
import com.thecascadian.universalcompression.compat.jei.CompressionJeiPlugin;
import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("universalcompression")
public class UniversalCompression {
   public static final String MODID = "universalcompression";
   public static final Logger LOGGER = LogManager.getLogger("universalcompression");

   public UniversalCompression(IEventBus modEventBus, ModContainer modContainer) {
      deleteStalConfigIfNeeded();
      CompressionConfig.register(modContainer);
      LOGGER.info(
         "UniversalCompression config available at config/{}-common.toml; edit the blacklist and exclusion lists there (see comments in file).",
         "universalcompression"
      );
      RegistryHandler.init(modEventBus);
      if (FMLEnvironment.dist == Dist.CLIENT) {
         modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (IConfigScreenFactory)(mc, screen) -> new ConfigurationScreen(
                  modContainer, screen, (cs, type, config, title) -> new UCConfigSectionScreen(cs, type, config, title)
               )
         );
      }

      modEventBus.addListener((net.neoforged.fml.event.config.ModConfigEvent event) -> {
         if ("universalcompression".equals(event.getConfig().getModId()) && FMLEnvironment.dist == Dist.CLIENT) {
            RecipeBookVisibility.refreshOpenRecipeBook();
            if (ModList.get().isLoaded("jei")) {
               CompressionJeiPlugin.refreshIngredientVisibility();
            }
         }
      });
   }

   private static void deleteStalConfigIfNeeded() {
      Path cfg = FMLPaths.CONFIGDIR.get().resolve("universalcompression-common.toml");
      if (Files.exists(cfg)) {
         try {
            String content = Files.readString(cfg, StandardCharsets.UTF_8);
            if (content.contains("blacklist_namespaces_exact = []")
               && content.contains("blacklist_namespaces_contains = []")
               && content.contains("blacklist_namespaces_starts = []")
               && content.contains("blacklist_paths_contains = []")) {
               Files.delete(cfg);
               LOGGER.info("[UniversalCompression] Deleted stale config (pre-default migration); it will be regenerated with built-in blacklist defaults.");
            }
         } catch (IOException var2) {
            LOGGER.warn("[UniversalCompression] Could not check/migrate stale config file", var2);
         }
      }
   }
}
