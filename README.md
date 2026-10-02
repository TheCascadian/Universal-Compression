

![Universal Compression logo image](https://cdn.modrinth.com/data/cached_images/001fcbda049ad167a5011b3f77d710e4480cb730_0.webp)

![GitHub stars](https://img.shields.io/github/stars/TheCascadian/Universal-Compression?style=flat-square)
![GitHub watchers](https://img.shields.io/github/watchers/TheCascadian/Universal-Compression?style=flat-square)
[![Discord](https://img.shields.io/discord/1201161505442381884?label=Discord&logo=discord&style=flat-square)](https://discord.com/invite/RuaR7CBy7Z)

# Universal Compression
Compress (nearly) _any_ block in the game into progressively denser stacks, **up to nine tiers deep**.

- No upfront configuration required. Every block from every installed mod is supported automatically at startup.
- Nine blocks of any type craft into one compressed block. That block is itself compressible. Repeat up to nine times. The math gets silly fast, and that is the point.
- A tier overlay renders on top of the parent block's texture so compressed blocks are always visually identifiable at a glance.
- Loot and recipe tables are generated virtually at runtime, meaning there are no datapack files bloating your instance for every possible block combination.
- Blocks or entire mods can be excluded via the config file if needed.

Requires NeoForge 1.21+. MIT licensed.

Config file located at <instance>/config/universalcompression-common.toml for most modded instances, or follow the below for a standard install of the Minecraft Launcher:
1. Keybind: Windows+R (this opens a small menu at the bottom of your screen)
2. Type %appdata% in the input box, then press Enter key
3. Locate /.minecraft/ folder near top
4. Navigate to your config folder from there

---

<details>
<summary>Blacklist / Exclusion Filter</summary>

> ❌ = excluded &nbsp;|&nbsp; ✅ = refined to a specific rule
>
> Many blanket namespace exclusions exist because only a handful of blocks from that mod had missing or incorrect textures. Auditing each one individually was deferred during development and will be addressed over time.

---

### Excluded Mods (entire namespace)

| | | | |
|---|---|---|---|
| ❌ Ae2 | ❌ Aether | ❌ Allthecompressed | ❌ Antibuilt |
| ❌ Ars Nouveau | ❌ Bellsandwhistles | ❌ Biomeswevegone | ❌ Chipped |
| ❌ Compact Machines | ❌ Corail Tombstone | ❌ Create | ❌ Createaddition |
| ❌ Domum Ornamentum | ❌ Enderio | ❌ Eternal Starlight | ❌ Extended Industrialization |
| ❌ Factoryblocks | ❌ Forbidden Arcanus | ❌ Handcrafted | ❌ Herbsandharvest |
| ❌ Immersive Engineering | ❌ Luminax | ❌ Macaw (all variants) | ❌ Mamas Merrymaking |
| ❌ Merrymaking | ❌ Mining Gadgets | ❌ Modern Industrialization | ❌ Mrcrayfish |
| ❌ Mysticalagradditions | ❌ Mysticalagriculture | ❌ Nightfall | ❌ Occultism |
| ❌ Oh The Biomes | ❌ Oritech | ❌ Path | ❌ Productivetrees |
| ❌ Quarryplus | ❌ Rechiseled | ❌ Refurbished Furniture | ❌ Regions Unexplored |
| ❌ Rftoolsbase | ❌ Rftoolsbuilder | ❌ Soundmuffler | ❌ Stevescarts |
| ❌ Structurize | ❌ Supplementaries | ❌ Tombstone (all variants) | ❌ Troolvidr |
| ❌ Twilightforest | ❌ Utilitarian | ❌ Xnet | ❌ Xtones Reworked |
| ❌ Xycraft Override | ❌ Xycraft World | | |

---

### Excluded Block Path Patterns (substring match, any mod)

| | | | |
|---|---|---|---|
| ❌ Advanced Machine Frame | ❌ Air | ❌ Aluminum Storage | ❌ Amorous Bristle |
| ❌ Ancient Debris | ❌ Ancient Podzol | ❌ Arcane Crystal | ❌ Ashen Deepturf |
| ❌ Azalea / Flowering Azalea | ❌ Bamboo Shoot | ❌ Battery | ❌ Bench |
| ❌ Bioshroom | ❌ Block Bio Fuel | ❌ Block Of Plastic | ❌ Bubble / Dense Bubble |
| ❌ Bush | ❌ Cactus | ❌ Candle | ❌ Cartography Table |
| ❌ Cave Hyssop | ❌ Chiseled Quartz | ❌ Cluster | ❌ Cobweb / Half Cobweb |
| ❌ Comb Block | ❌ Crafting Table | ❌ Crate | ❌ Crimson Fungus |
| ❌ Debug | ❌ Deepturf / Frozen Deepturf | ❌ Delightful Dirt | ❌ Dimension Boundary |
| ❌ Dragon Ice Spikes | ❌ Dried Kelp Block | ❌ Duckweed | ❌ Enhanced Galgadorian / Galgadorian |
| ❌ Fern | ❌ Fire | ❌ Fletching Table | ❌ Fluid Placeholder |
| ❌ Forbidden Arcanus Upwind / Whirlwind | ❌ Frogspawn | ❌ Fur | ❌ Garden |
| ❌ Ghost | ❌ Glistering / Glistering Ivy / Glistering Wart | ❌ Gloomgourd | ❌ Grass |
| ❌ Herb | ❌ Honey | ❌ Icicle | ❌ Infested |
| ❌ Ink Mushroom Stem | ❌ Invisible | ❌ Kelp Plant | ❌ Lamp |
| ❌ Lava Factory Casing | ❌ Leaves | ❌ Lily | ❌ Lodestone |
| ❌ Luminis | ❌ Machine Casing | ❌ Machine Frame | ❌ Machine Void Air |
| ❌ Magma Block | ❌ Matrix Frame | ❌ Medium / Thin / Wide Pot | ❌ Melon |
| ❌ Milky Comb | ❌ Miners Light | ❌ Miserabell | ❌ Mistletoe |
| ❌ Mortar | ❌ Mushroom | ❌ Nether Sprout / Sprouts | ❌ Phantom Booster |
| ❌ Pity Machine Frame | ❌ Placeholder | ❌ Prismoss | ❌ Pumpkin |
| ❌ Redstone Bud | ❌ Regalium | ❌ Reinforced Deepslate | ❌ Reinforced Metal Block |
| ❌ Rice Bag | ❌ Root | ❌ Royal Jelly | ❌ Runic |
| ❌ Scintling | ❌ Sculk Tendrils Plant | ❌ Seeping Ink | ❌ Shelf |
| ❌ Shimmerweed | ❌ Simple Machine Frame | ❌ Smooth Kivi | ❌ Smooth Quartz |
| ❌ Smooth Red / Smooth Sandstone | ❌ Snow Block | ❌ Solid Compact Machine Wall | ❌ Soldering Table |
| ❌ Soul Herb | ❌ Soulless Sandstone | ❌ Spore Blossom | ❌ Station |
| ❌ Structure Void | ❌ Supreme Machine Frame | ❌ Torch | ❌ Trophy |
| ❌ Tropical Garden | ❌ Twisting Vine | ❌ Undergarden Goo / Goo Block | ❌ Unexplored Log / Plank |
| ❌ Veiled / Veiled Mushroom | ❌ Warped Fungus | ❌ Wax / Wax Block / Wax Brick / Wax Tile | ❌ Waypoint Placeholder |
| ❌ Weeping Vines Plant | ❌ Whirlwind | ❌ Wild Flax / Wild Fluffy | |

---

### Compound Rules (mod + path condition)

| | | |
|---|---|---|
| ❌ Ae2 — path contains `certus` | ❌ Ars Nouveau — path contains `sourceberry` | ❌ Compact Machines — path equals `wall` |
| ❌ Create — path contains `quartz` | ❌ Farmers Delight — path contains `crate` | ❌ Ice And Fire — path contains `scale` |
| ❌ Minecraft — path contains `bamboo` | ❌ Minecolonies — path contains `waypoint` | ❌ Mysticalagriculture / Mysticalagradditions — path contains `ore` |
| ❌ Naturesaura — path contains `light` | ❌ Securitycraft — path contains `quartz` | ❌ Undergarden — path equals `goo` or `goo_block` |

</details>
