Place 9 PNG overlay textures here named:

    tier_overlay_1.png
    tier_overlay_2.png
    ...
    tier_overlay_9.png

Each file must be a 16x16 RGBA image. Transparent pixels expose the parent
block texture beneath. Opaque pixels draw the tier indicator border on top.

These textures are stitched into the block texture atlas automatically by
NeoForge because they reside under assets/universalcompression/textures/block/.
No additional atlas registration is required.
