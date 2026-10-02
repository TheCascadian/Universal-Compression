# Universal Compression 0.0.4

## Fixed
- **Tier 9 compression now exists.** Blocks can be compressed up to 9 times, as the project page says. Previously the game stopped at Tier 8, so Tier 8 had a decompress recipe but no compress recipe.
- **Basalt and Polished Basalt now get compressed variants.** Pillar-style blocks that only have a rotation (axis) property were being skipped.
- **Recipe ID collisions fixed.** Tier 1 to parent decompress recipes are now named with the mod namespace, so same-named blocks from different mods no longer overwrite each other's recipes.
- **Missing textures fixed generically.** Compressed blocks now reuse the original block's real model instead of guessing its texture from the block name, so logs, grass, pillars and any block with `_top`/`_side` textures render correctly, as do resource-pack retextures.
- **Inventory shading and rendering.** Item transforms, lighting and shading now come from the original block, and the tier overlay no longer z-fights with the base texture. Items in inventories, the hotbar and the creative tab now use the proper item lighting layer, so faces are shaded like vanilla blocks instead of looking flat.
- **Blacklists are now applied when blocks are registered.** The config was not loaded yet at that point, so every blacklist was silently skipped and blocked items (dragon egg, mushrooms, vines, shrubs and more) showed up in the creative tab. The mod now reads `universalcompression-common.toml` directly at that stage, so your own list edits apply too, and falls back to the built-in defaults if the file is missing. The `latest.log` line `Config not loaded yet at registration; reading config file directly.` is expected.

## Added
- **Whitelist:** new `whitelist_blocks` option in the common config. Add full block IDs (for example `xycraft_world:kivi`) to always compress them, even if their mod or name matches a blacklist rule. This lets you blacklist a whole namespace and still keep individual blocks.
- **Config localization:** every config option, including the new whitelist, now has a translated name and tooltip in the config screen. The JEI "Compressing" and "Decompressing" category titles are translated too.

## Notes
`PLEASE, MAKE A BACKUP OF YOUR WORLD. I WILL NOT BE "FIXING" MISSING BLOCKS. CHANGELOG ABOVE AND NOTES BELOW EXPLAIN WHY.`
---
- Blacklist changes still need a game restart to take effect.
- Existing worlds: Tier 9 is a new set of blocks and items, so nothing already in your world changes. Because blacklists now apply correctly, some blocks that were wrongly registered before will no longer exist; any of those already in a world will be removed.
- Basalt: Smooth Basalt was not found to be excluded by any rule in the code. If its recipes are still missing, please report it with your `latest.log`.
- Requires Minecraft 1.21.1 and NeoForge. JEI is optional.
