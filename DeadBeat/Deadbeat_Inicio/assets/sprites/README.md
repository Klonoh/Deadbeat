# Deadbeat player sprite assets

- `player_walk.png`: **asset used by the game**. Transparent 192x640 PNG.
- 8 rows (DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT).
- 3 columns per row (idle, left-step, right-step); each frame measures **64x80 pixels**.
- `player_original.png`: the original generated image selected by the project owner (1254x1254).
- The source image was cropped and its green background removed. The character was
  resized with nearest-neighbor resampling and feet aligned across frames.
- Current on-screen size: 64x80 world units, with transparent padding.
- `PlayerSpriteRenderer` uses nearest-neighbor texture filtering (no blurring).

**To adjust sprite scale:** change drawing size/offset in `PlayerSpriteRenderer.draw`,
not the collision radius in `Player`. The sprite's artwork and hitbox are independent.

**To adjust walking tempo:** edit `STEP_SECONDS` and `WALK_SEQUENCE` in
`PlayerSpriteRenderer`.
