package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.block.Hat;
import lonestarrr.arconia.common.block.tile.HatTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;
import lonestarrr.arconia.common.block.tile.ResourceTreeRootTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ColoredRoot extends Item {
    private RainbowColor tier;
    private static final String TAG_ITEM = "item";

    public ColoredRoot(Properties builder, RainbowColor tier) {
        super(builder);
        this.tier = tier;
    }

    public RainbowColor getTier() { return tier; }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return !getResourceItem(stack).isEmpty();
    }

    @Nonnull
    public static ItemStack getResourceItem(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ITEM)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.read(tag.getCompound("item"));
    }

    /**
     * Colored roots can be enchanted through a ritual with a specific item. Once enchanted, right-clicking the root near an activated resource tree will
     * have that tree produce this resource.
     * @param coloredRootStack colored root to set resource on
     * @param resourceItem resource to set
     *
     * Data is stored in NBT so that it can be used for any item from any mod by only adding a pedestal ritual recipe.
     */
    public static void setResourceItem(@Nonnull ItemStack coloredRootStack, @Nonnull IItemProvider resourceItem) {
        CompoundNBT tag = coloredRootStack.getOrCreateTag();
        tag.put(TAG_ITEM, new ItemStack(resourceItem, 1).serializeNBT());
    }

    /**
     * Tiered tree roots enchanted through a pedestal crafting ritual with a specific resource are able to assign this resource to a placed down
     * hat. The hat can then 'draw' this resource from a nearby active and linked pot of gold.
     * @param context
     * @return
     */
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();
        ItemStack heldItem = player.getHeldItem(context.getHand());

        if (heldItem.getItem() != this || world.getBlockState(pos).getBlock() != ModBlocks.hat) {
            return ActionResultType.PASS;
        }

        ItemStack resource = getResourceItem(heldItem); // Item to be produced

        if (resource.isEmpty()) {
            return ActionResultType.PASS;
        }

        boolean resourceSet = Hat.setResourceGenerated(world, pos, tier, resource);
        if (resourceSet) {
            if (heldItem.getCount() > 1) {
                heldItem.shrink(1);
                player.setHeldItem(context.getHand(), heldItem);
            } else {
                player.setHeldItem(context.getHand(), ItemStack.EMPTY);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(
            ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemStack resource = getResourceItem(stack);
        if (!resource.isEmpty()) {
            tooltip.add(resource.getItem().getDisplayName(resource));
        }
    }

    private boolean findNearbyTreeRoot(World world, BlockPos pos) {
        boolean foundTreeRoot = false;
        for (BlockPos scanPos: BlockPos.getAllInBoxMutable(pos.west().north(), pos.east().south())) {
            TileEntity te = world.getTileEntity(scanPos);
            if (te != null && te instanceof ResourceTreeRootTileEntity && ((ResourceTreeRootTileEntity)te).getTier().equals(tier)) {
                foundTreeRoot = true;
                break;
            }
        }
        return foundTreeRoot;
    }
}
