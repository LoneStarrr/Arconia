package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ArconiumEssence extends Item {
    private RainbowColor tier;

    public ArconiumEssence(Item.Properties builder, RainbowColor tier) {
        super(builder);
        this.tier = tier;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack essence = context.getItemInHand();

        // Attempt to change the block being touched to a rainbow grass block of the same color as the essence

        if (!level.isClientSide) {
            BlockState bs = level.getBlockState(pos);
            boolean canChangeBlock = false;
            if (bs.getBlock() instanceof RainbowGrassBlock grassBlock) {
                // Allow recoloring of other rainbow grass blocks
                if (grassBlock.getTier() != this.tier) {
                    canChangeBlock = true;
                }
            } else if (bs.getBlock() == Blocks.GRASS_BLOCK) {
                // There is no 'grass blocks' tag sadly, so hardcoded it is.
                canChangeBlock = true;
            }

            if (!canChangeBlock) {
                return InteractionResult.PASS;
            }

            BlockState rainbowGrassBlock = ModBlocks.getRainbowGrassBlock(tier).get().defaultBlockState();
            level.setBlockAndUpdate(pos, rainbowGrassBlock);
            // Consume the essence
            essence.shrink(1);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1F, 1F);
            ModCriteriaTriggers.TOUCH_GRASS_TRIGGER.get().trigger((ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
