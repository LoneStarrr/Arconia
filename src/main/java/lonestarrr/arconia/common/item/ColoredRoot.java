package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null || contents.getSlots() != 1) {
            return ItemStack.EMPTY;
        }
        return contents.getStackInSlot(0);
    }

    /**
     * Colored roots can be enchanted through a ritual with a specific item. Once enchanted, right-clicking the root near an activated resource tree will
     * have that tree produce this resource.
     *
     * @param coloredRootStack colored root to set resource on
     * @param resourceItem     resource to set
     */
    public static void setResourceItem(
            @Nonnull ItemStack coloredRootStack, @Nonnull ItemStack resourceItem) {
        coloredRootStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(new ArrayList<>(List.of(resourceItem))));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, ctx, tooltipComponents, tooltipFlag);
        ItemStack resource = getResourceItem(stack);
        if (!resource.isEmpty()) {
            tooltipComponents.add(resource.getItem().getName(resource));
        }
    }
}
