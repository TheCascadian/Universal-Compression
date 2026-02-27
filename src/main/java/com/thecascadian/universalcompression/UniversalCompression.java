package com.thecascadian.universalcompression;

import com.thecascadian.universalcompression.config.CompressionConfig;
import com.thecascadian.universalcompression.registry.RegistryHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UniversalCompression.MODID)
public class UniversalCompression {
    public static final String MODID = "universalcompression";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public UniversalCompression(IEventBus modEventBus, ModContainer modContainer) {
        CompressionConfig.register(modContainer);
        // inform pack makers / server admins where the generated config lives
        LOGGER.info("UniversalCompression config available at config/{}-common.toml; " +
                "edit the blacklist and exclusion lists there (see comments in file).",
                MODID);
        RegistryHandler.init(modEventBus);
    }
}