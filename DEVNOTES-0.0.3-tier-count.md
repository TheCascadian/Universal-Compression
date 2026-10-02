# Why the last version stopped at Tier 8

Developer notes for 0.0.3 -> 0.0.4.

## The off-by-one

Tiers are numbered from 1, not 0. Tier 1 is the first compression of the parent block (9 parent blocks -> 1 Tier 1 block). The registry loop was:

    for (int tier = 1; tier <= 8; tier++)

That registers tiers 1 through 8, which is eight tiers. The "starts at 0, so 8 means 9" reasoning does not apply here because the loop starts at 1 and the tier number is part of the block ID (`<ns>_<path>_compressed_<tier>`), the overlay texture (`tier_overlay_<tier>`) and the hardness divisor. There is no tier 0. A count of 9 needs `<= 9`.

The constant `MAX_TIERS = 8` was also inlined by javac into the loop bounds and the list capacity in `RegistryHandler`, so it was never obvious from the build output.

## Why it showed up as "Tier 8 has no compress recipe"

Recipes are generated per adjacent pair in the tier list (`lower -> upper`, 9 to 1). With 8 tiers there are 7 compress recipes and the top tier can only be decompressed. The overlay `tier_overlay_9.png` was shipped but never used, which is what the reporter noticed.

## Fix

`MAX_TIERS` is now 9 and both the loop and the list capacity use the constant. Recipe generation (`VirtualRecipePack`, `CompressionRecipeProvider`) iterates the registered tier list, so it needed no change.

## Basalt

`onRegisterBlocks` skipped every block whose default state had any property (`getProperties().isEmpty()`). Basalt and Polished Basalt are `RotatedPillarBlock`s with only `AXIS`, so they were silently skipped. The check now allows blocks whose only property is `RotatedPillarBlock.AXIS`. Smooth Basalt has no properties and matches no blacklist entry, so its cause is not identified.

## Recipe ID collisions

Decompress recipes for tier 1 were keyed by `parentId.getPath()` only, so two mods with a block of the same path overwrote one another. They now include the namespace.

## Whitelist

`RegistryHandler.isWhitelisted()` is checked first in `isBlacklisted()` and in the two recipe-side `isBlacklisted` wrappers (which re-check the legacy `blacklist` list and would otherwise undo the whitelist). Hard-coded class exclusions in `isExcluded` (flowers, slabs, stairs, block entities...) are not overridden.
