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

import net.minecraft.item.Item.Properties;

public class ColoredRoot extends Item {
    private RainbowColor tier;
    private static final String TAG_ITEM = "item";
    private static final String TAG_INTERVAL = "interval";
    private static final String TAG_COUNT = "count";
    private static final String TAG_COIN_COST = "coin_cost";

    public ColoredRoot(Properties builder, RainbowColor tier) {
        super(builder);
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !getResourceItem(stack).isEmpty();
    }

    @Nonnull
    public static ItemStack getResourceItem(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ITEM)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.of(tag.getCompound(TAG_ITEM));
    }

    /**
     * @param stack
     * @return Resource generation interval for enchanted root item
     */
    @Nonnull
    public static int getResourceInterval(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_INTERVAL)) {
            return 1;
        }

        return tag.getInt(TAG_INTERVAL);
    }

    /**
     * @param stack
     * @return Resource generation count for enchanted root item
     */
    @Nonnull
    public static int getResourceCount(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COUNT)) {
            return 1;
        }

        return tag.getInt(TAG_COUNT);
    }

    /**
     * @param stack
     * @return Resource generation count for enchanted root item
     */
    @Nonnull
    public static int getResourceCoinCost(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COIN_COST)) {
            return 1;
        }

        return tag.getInt(TAG_COIN_COST);
    }

    /**
     * Colored roots can be enchanted through a ritual with a specific item. Once enchanted, right-clicking the root near an activated resource tree will
     * have that tree produce this resource.
     *
     * @param coloredRootStack colored root to set resource on
     * @param resourceItem     resource to set
     * @param interval         Frequency with which resource is generated. An interval of 1 is fastest. Interval length is determined by the pot of gold and is typically
     *                         no less than 5 ticks.
     * @param count            Number of items generated per event. Must not exceed item's max stack count
     * @param coinCost         Number of coins it takes to generate the resource
     *                         <p>
     *                         Data is stored in NBT so that it can be used for any item from any mod by only adding a pedestal ritual recipe.
     */
    public static void setResourceItem(
            @Nonnull ItemStack coloredRootStack, @Nonnull IItemProvider resourceItem, @Nonnull int interval, @Nonnull int count, int coinCost) {
        CompoundNBT tag = coloredRootStack.getOrCreateTag();
        ItemStack stack = new ItemStack(resourceItem);
        int maxCount = stack.getMaxStackSize();
        int stackCount = count > maxCount ? maxCount : count;
        stack.setCount(stackCount);
        tag.put(TAG_ITEM, stack.serializeNBT());
        tag.putInt(TAG_INTERVAL, interval < 1 ? 1 : interval);
        tag.putInt(TAG_COIN_COST, coinCost < 1 ? 1 : coinCost);
    }

    /**
     * Tiered tree roots enchanted through a pedestal crafting ritual with a specific resource are able to assign this resource to a placed down
     * hat. The hat can then 'draw' this resource from a nearby active and linked pot of gold.
     *
     * @param context
     * @return
     */
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack heldItem = player.getItemInHand(context.getHand());

        if (heldItem.getItem() != this || world.getBlockState(pos).getBlock() != ModBlocks.hat) {
            return ActionResultType.PASS;
        }

        ItemStack resource = getResourceItem(heldItem); // Item to be produced

        if (resource.isEmpty()) {
            return ActionResultType.PASS;
        }

        int count = getResourceCount(heldItem);
        resource.setCount(count);
        int interval = getResourceInterval(heldItem);
        int coinCost = getResourceCoinCost(heldItem);

        boolean resourceSet = Hat.setResourceGenerated(world, pos, tier, resource, interval, coinCost);

        if (resourceSet) {
            if (heldItem.getCount() > 1) {
                heldItem.shrink(1);
                player.setItemInHand(context.getHand(), heldItem);
            } else {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        ItemStack resource = getResourceItem(stack);
        if (!resource.isEmpty()) {
            tooltip.add(resource.getItem().getName(resource));
        }
    }
}
