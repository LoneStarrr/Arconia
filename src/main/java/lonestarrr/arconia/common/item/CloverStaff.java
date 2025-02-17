package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.PotMultiBlockPrimary;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState bs = level.getBlockState(pos);
        if (bs.getBlock() == PotMultiBlockPrimary.INSIDE_BLOCK) {
            return attemptFormMultiblock(player, level, pos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else if (bs.getBlock() == ModBlocks.potMultiBlockSecondary.get()) {
            BlockPos potPos = storePotCoordinate(level, pos, staff);
            if (potPos != null) {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.translatable(LANG_PREFIX + ".selectpot.success", potPos.toShortString()));
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.translatable(LANG_PREFIX + ".selectpot.failed"));
                }
            }
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private static BlockPos getPotPosition(ItemStack staff) {
        CustomData customData = staff.get(DataComponents.CUSTOM_DATA);

        if (customData == null) return null;

        CompoundTag tag = customData.copyTag();

        if (!tag.contains(TAG_POT_POS)) {
            return null;
        }

        return BlockPos.of(tag.getLong(TAG_POT_POS));
    }

    private static BlockPos storePotCoordinate(Level world, BlockPos pos, ItemStack staff) {
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof PotMultiBlockSecondaryBlockEntity)) {
            return null;
        }
        PotMultiBlockSecondaryBlockEntity potTE = (PotMultiBlockSecondaryBlockEntity) te;
        BlockPos primaryPos = potTE.getPrimaryPos();
        if (primaryPos != null) {
            // This is not how you are supposed to store data, should register a specific data component, which is
            // specific to what you're using it for, not what type it is
            CompoundTag tag = new CompoundTag();
            tag.putLong(TAG_POT_POS, primaryPos.asLong());
            CustomData customData = CustomData.of(tag);
            staff.set(DataComponents.CUSTOM_DATA, customData);
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
