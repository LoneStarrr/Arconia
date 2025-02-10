package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.PotMultiBlockPrimary;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Staff. Magic's wrench. Because every mod needs a staff or wrench.
 *
 */
public class CloverStaff extends Item {
    private static final String TAG_POT_POS = "pot_pos";
    private static final String LANG_PREFIX = LanguageHelper.item("cloverstaff");


    public CloverStaff(Item.Properties builder) {
        super(builder);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack staff = context.getItemInHand();

        BlockState bs = level.getBlockState(pos);
        if (bs.getBlock() == PotMultiBlockPrimary.INSIDE_BLOCK) {
            return attemptFormMultiblock(player, level, pos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else if (bs.getBlock() == ModBlocks.potMultiBlockSecondary.get()) {
            BlockPos potPos = storePotCoordinate(level, pos, staff);
            if (potPos != null) {
                if (!level.isClientSide) {
                    context.getPlayer().sendSystemMessage(Component.translatable(LANG_PREFIX + ".selectpot.success", potPos.toShortString()));
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide) {
                    context.getPlayer().sendSystemMessage(Component.translatable(LANG_PREFIX + ".selectpot.failed"));
                }
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private static BlockPos getPotPosition(ItemStack staff) {
        CompoundTag tag = staff.getTag();
        if (tag == null || !tag.contains(TAG_POT_POS)) {
            return null;
        }

        BlockPos potPos = BlockPos.of(tag.getLong(TAG_POT_POS));
        return potPos;
    }

    private static BlockPos storePotCoordinate(Level world, BlockPos pos, ItemStack staff) {
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof PotMultiBlockSecondaryBlockEntity)) {
            return null;
        }
        PotMultiBlockSecondaryBlockEntity potTE = (PotMultiBlockSecondaryBlockEntity) te;
        BlockPos primaryPos = potTE.getPrimaryPos();
        if (primaryPos != null) {
            CompoundTag tag = staff.getOrCreateTag();
            tag.putLong(TAG_POT_POS, primaryPos.asLong());
            // TODO indicate this in the description of the staff
            return primaryPos;
        }

        return null;
    }

    private static boolean attemptFormMultiblock(Player player, Level world, BlockPos pos) {
        // Might be an attempt to form a pot of gold multiblock
        if (world.isClientSide) {
            return PotMultiBlockPrimary.canFormMultiBlock(world, pos);
        } else {
            boolean formed = PotMultiBlockPrimary.formMultiBlock(world, pos);
            if (formed) {
                ModCriteriaTriggers.CREATE_POT_OF_GOLD_TRIGGER.get().trigger((ServerPlayer) player);
            }
            return formed;
        }
    }
}
