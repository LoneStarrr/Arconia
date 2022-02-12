package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.PotOfGoldTrigger;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.PotMultiBlockPrimary;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack staff = context.getItemInHand();

        BlockState bs = world.getBlockState(pos);
        if (bs.getBlock() == Blocks.GOLD_BLOCK) {
            return attemptFormMultiblock(player, world, pos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else if (bs.getBlock() == ModBlocks.potMultiBlockSecondary) {
            BlockPos potPos = storePotCoordinate(world, pos, staff);
            if (potPos != null) {
                if (!world.isClientSide) {
                    context.getPlayer().sendMessage(new TranslatableComponent(LANG_PREFIX + ".selectpot.success", potPos.toShortString()), Util.NIL_UUID);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!world.isClientSide) {
                    context.getPlayer().sendMessage(new TranslatableComponent(LANG_PREFIX + ".selectpot.failed"), Util.NIL_UUID);
                }
            }
            return InteractionResult.PASS;
        } else if (bs.getBlock() == ModBlocks.hat) {
            if (!world.isClientSide) {
                BlockPos potPos = getPotPosition(staff);
                if (potPos == null) {

                    return InteractionResult.CONSUME;
                }
                linkOrUnlinkHat(world, pos, potPos, context);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private static void linkOrUnlinkHat(Level world, BlockPos hatPos, BlockPos potPos, UseOnContext context) {
        String lang = LANG_PREFIX + ".linkhat";

        BlockEntity be = world.getBlockEntity(potPos);
        if (be == null || !(be instanceof PotMultiBlockPrimaryBlockEntity)) {
            lang += ".invalidpot";
        } else {
            // TODO the hat must track which pot it is linked to as well - to prevent double linking/unlinkin the wrong one
            PotMultiBlockPrimaryBlockEntity potBE = (PotMultiBlockPrimaryBlockEntity) be;
            if (potBE.isHatLinked(hatPos)) {
                if (potBE.unlinkHat(hatPos)) {
                    lang += ".unlinked";
                } else {
                    lang += ".unlink_failed";
                }
            } else {
                try {
                    potBE.linkHat(hatPos);
                    lang += ".linked";
                } catch (PotMultiBlockPrimaryBlockEntity.LinkHatException exc) {
                    switch (exc.code) {
                        case HAT_TOO_FAR:
                            lang += ".toofar";
                            break;
                        case HAT_NOT_FOUND:
                            lang += ".notfound";
                            break;
                        case TOO_MANY_HATS:
                            lang += ".toomanyhats";
                            break;
                        case ALREADY_LINKED:
                            lang += ".alreadylinked";
                            break;
                        case LINKED_TO_OTHER_POT:
                            lang += ".linked_other_pot";
                            break;
                    }
                }
            }
        }
        context.getPlayer().sendMessage(new TranslatableComponent(lang), Util.NIL_UUID);
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
                PotOfGoldTrigger.INSTANCE.trigger((ServerPlayer) player, (ServerLevel) world, pos);
            }
            return formed;
        }
    }
}
