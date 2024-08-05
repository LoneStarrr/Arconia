package lonestarrr.arconia.client.effects;

import lonestarrr.arconia.client.gui.render.GhostBlockRenderer;
import lonestarrr.arconia.client.gui.render.OutlineBlockRenderer;
import lonestarrr.arconia.common.core.BuildPattern;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.BuildPatternTier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.List;
import java.util.Map;

/**
 * Renders build pattern previews, which are activated using the clover staff - deprecated for now in favor of
 * highlighting pattern block positions
 *
 */
public class BuildPatternPreview {
    private static final int PREVIEW_MIN_LIGHT_LEVEL = 0;
    private static final int PREVIEW_MAX_LIGHT_LEVEL = 15;
    private static final float PREVIEW_LIGHT_CYCLE_TICKS = 20f;
    private static final float BAD_BLOCKS_SPAWN_TICKS = 40f;

    private static boolean renderPreview = false;
    private static float lastRenderTime;
    private static boolean lightIncreasing;
    private static float lastLightLevel;
    private static Map<BlockPos, BlockState> previewPattern;
    private static List<BlockPos> badBlockPositions;
    private static float lastBadBlocksRenderTime;

    /**
     * Toggle rendering of a client-only preview of a pattern to build
     * @param tier
     *  Which pattern tier to render
     * @param pos
     *  Where to render the pattern (bottom-left block in the pattern goes here)
     * @param direction
     *  In which direction to render (n/s/w/e only)
     */
    public static void togglePreview(RainbowColor tier, BlockPos pos, Direction direction) {
        if (!renderPreview) {
            enablePreview(tier, pos, direction);
        } else {
            disablePreview();
        }
    }

    public static void enableHighlightBadBlocks(List<BlockPos> blockPositions) {
        // This will trigger adding particles at next render time. Done such to trigger this on the client side only
        lastBadBlocksRenderTime = 0;
        badBlockPositions = blockPositions;
    }

    public static void disableHighlightBadBlocks() {
        badBlockPositions = null;
    }

    private static void enablePreview(RainbowColor tier, BlockPos pos, Direction direction) {
        final BuildPattern pattern = BuildPatternTier.getPattern(tier);
        // bottom left coordinate in pattern is to be placed on blockpos that player clicked on
        final Vec3i patternCoordinate = new Vec3i(0, pos.getY(), pattern.getHeight() - 1);
        previewPattern = pattern.getBlockStates(direction, patternCoordinate, pos);
        resetLightAnimation();
        renderPreview = true;
    }

    public static void disablePreview() {
        renderPreview = false;
        previewPattern = null;
        disableHighlightBadBlocks();
    }

    private static void resetLightAnimation() {
        lastRenderTime = 0;
        lightIncreasing = true;
        lastLightLevel = 0;
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }
        renderPreview(event);
        showBadBlockPositions();
    }

    private static void renderPreview(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        // "Animate" preview by cycling the light value based on game time
        if (!renderPreview) {
            return;
        }

        // TODO use the nicer logic from HighlightPatternStructure rendering
        Level world = Minecraft.getInstance().level;
        float gameTime = world.getGameTime(); // ticks since start
        float ticksperHalfCycle = 15;
        float cycleCount = gameTime % ticksperHalfCycle;
        boolean upCycle = gameTime % (ticksperHalfCycle * 2) >= ticksperHalfCycle;
        float lightLevel = 0;
        float scale = (cycleCount / ticksperHalfCycle) * 0.1F;
        if (upCycle) {
            scale = 0.6F - scale;
        } else {
            scale = 0.5F + scale;
        }

        /*
        // 1 cycle is increasing light, then decreasing light
        lightLevel = cycleCount / ticksperHalfCycle * PREVIEW_MAX_LIGHT_LEVEL;
        if (upCycle) {
            lightLevel = PREVIEW_MAX_LIGHT_LEVEL - lightLevel;
        }
        */

        for(Map.Entry<BlockPos, BlockState> p: previewPattern.entrySet()) {
            BlockState patternBlockState = p.getValue();
            BlockPos worldPos = p.getKey();

            if (world.getBlockState(worldPos).isAir()) {
                if (patternBlockState.isAir()) {
                    continue;
                }
                GhostBlockRenderer.renderGhostBlock(event.getPoseStack(), worldPos, patternBlockState, (int)lightLevel, scale);
            } else if (!world.getBlockState(worldPos).getBlock().equals(patternBlockState.getBlock())) {
                // A block is placed in the world that does not match the pattern - show a block outline indicating
                // it as such
                //float hue = gameTime % 100 / 100F; // cycle hue as animation effect
                float hue = 0F;
                OutlineBlockRenderer.renderOutline(event.getPoseStack(), worldPos, hue, OutlineBlockRenderer.LINE_8_NO_DEPTH);
            }
        }
    }

    private static void showBadBlockPositions() {
        if (badBlockPositions == null) {
            return;
        }

        float gameTime = Minecraft.getInstance().level.getGameTime(); // ticks since start
        if (lastBadBlocksRenderTime == 0) {
            lastBadBlocksRenderTime = gameTime;
        }
        if (gameTime - lastBadBlocksRenderTime < BAD_BLOCKS_SPAWN_TICKS) {
            return;
        }
        lastBadBlocksRenderTime = gameTime;

        Level world = Minecraft.getInstance().level; //client world

        for (BlockPos pos: badBlockPositions) {
            if (!world.isEmptyBlock(pos.offset(0, 1, 0))) {
                continue;
            }
            // Example taken from CampfireBlock
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.25D, (double)pos.getZ() + 0.5D, 0.0D, 0.05D, 0.0D);
        }
    }

}

