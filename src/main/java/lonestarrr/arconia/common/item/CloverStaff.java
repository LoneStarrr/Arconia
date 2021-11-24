package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.PotOfGoldTrigger;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.PotMultiBlockPrimary;
import lonestarrr.arconia.common.block.PotMultiBlockSecondary;
import lonestarrr.arconia.common.block.tile.PotMultiBlockPrimaryTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockSecondaryTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Staff. Magic's wrench. Because every mod needs a staff or wrench.
 *
 */
public class CloverStaff extends Item {
    private static final String TAG_POT_POS = "pot_pos";

    public CloverStaff(Item.Properties builder) {
        super(builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack staff = context.getItem();

        BlockState bs = world.getBlockState(pos);
        if (bs.getBlock() == Blocks.GOLD_BLOCK) {
            return attemptFormMultiblock(player, world, pos) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
        } else if (bs.getBlock() == ModBlocks.potMultiBlockSecondary) {
            BlockPos potPos = storePotCoordinate(world, pos, staff);
            if (potPos != null) {
                // TODO do nicer.. sound effect? Or at least use the language feature
                if (!world.isRemote) {
                    context.getPlayer().sendMessage(new StringTextComponent("Stored coordinate of the pot of gold at " + potPos), Util.DUMMY_UUID);
                }
                return ActionResultType.SUCCESS;
            } else {
                if (!world.isRemote) {
                    context.getPlayer().sendMessage(new StringTextComponent("Invalid pot of gold multiblock structure?"), Util.DUMMY_UUID);
                }
            }
            return ActionResultType.PASS;
        } else if (bs.getBlock() == ModBlocks.hat) {
            if (!world.isRemote) {
                boolean linkedHat = linkHatToPot(world, pos, staff);
                if (linkedHat) {
                    context.getPlayer().sendMessage(new StringTextComponent("Linked hat to pot of gold"), Util.DUMMY_UUID);
                } else {
                    context.getPlayer().sendMessage(new StringTextComponent("Linking hat failed (did you select a pot of gold first?)"), Util.DUMMY_UUID);
                }
            }
            return ActionResultType.CONSUME;
        }

        return ActionResultType.PASS;
    }

    private static boolean linkHatToPot(World world, BlockPos hatPos, ItemStack staff) {
        BlockPos potPos = getPotPosition(staff);
        if (potPos == null) {
            return false;
        }

        TileEntity te= world.getTileEntity(potPos);
        if (te == null || !(te instanceof PotMultiBlockPrimaryTileEntity)) {
            return  false;
        }

        PotMultiBlockPrimaryTileEntity potTE = (PotMultiBlockPrimaryTileEntity) te;
        return potTE.linkHat(hatPos);
    }

    private static BlockPos getPotPosition(ItemStack staff) {
        CompoundNBT tag = staff.getTag();
        if (tag == null || !tag.contains(TAG_POT_POS)) {
            return null;
        }

        BlockPos potPos = BlockPos.fromLong(tag.getLong(TAG_POT_POS));
        return potPos;
    }

    private static BlockPos storePotCoordinate(World world, BlockPos pos, ItemStack staff) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof PotMultiBlockSecondaryTileEntity)) {
            return null;
        }
        PotMultiBlockSecondaryTileEntity potTE = (PotMultiBlockSecondaryTileEntity) te;
        BlockPos primaryPos = potTE.getPrimaryPos();
        if (primaryPos != null) {
            CompoundNBT tag = staff.getOrCreateTag();
            tag.putLong(TAG_POT_POS, primaryPos.toLong());
            // TODO indicate this in the description of the staff
            return primaryPos;
        }

        return null;
    }

    private static boolean attemptFormMultiblock(PlayerEntity player, World world, BlockPos pos) {
        // Might be an attempt to form a pot of gold multiblock
        if (world.isRemote) {
            return PotMultiBlockPrimary.canFormMultiBlock(world, pos);
        } else {
            boolean formed = PotMultiBlockPrimary.formMultiBlock(world, pos);
            if (formed) {
                PotOfGoldTrigger.INSTANCE.trigger((ServerPlayerEntity) player, (ServerWorld) world, pos);
            }
            return formed;
        }

    }
}
