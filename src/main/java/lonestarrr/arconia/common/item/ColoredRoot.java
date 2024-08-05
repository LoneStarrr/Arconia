package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ColoredRoot extends Item {
    private RainbowColor tier;
    private static final String TAG_ITEM = "item";
    private static final String TAG_COUNT = "count";

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
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ITEM)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.of(tag.getCompound(TAG_ITEM));
    }

    /**
     * Colored roots can be enchanted through a ritual with a specific item. Once enchanted, right-clicking the root near an activated resource tree will
     * have that tree produce this resource.
     *
     * @param coloredRootStack colored root to set resource on
     * @param resourceItem     resource to set
     * @param count            Number of items generated per event. Must not exceed item's max stack count
     *                         Data is stored in NBT so that it can be used for any item from any mod by only adding a pedestal ritual recipe.
     */
    public static void setResourceItem(
            @Nonnull ItemStack coloredRootStack, @Nonnull ItemLike resourceItem, @Nonnull int count) {
        CompoundTag tag = coloredRootStack.getOrCreateTag();
        ItemStack stack = new ItemStack(resourceItem);
        int maxCount = stack.getMaxStackSize();
        int stackCount = count > maxCount ? maxCount : count;
        stack.setCount(stackCount);
        tag.put(TAG_ITEM, stack.serializeNBT());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        ItemStack resource = getResourceItem(stack);
        if (!resource.isEmpty()) {
            tooltip.add(resource.getItem().getName(resource));
        }
    }
}
