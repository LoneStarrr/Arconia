package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
     * Colored roots can be imbued through a ritual with a specific item. Once imbued, right-clicking the root near an activated resource tree will
     * have that tree produce this resource.
     *
     * @param coloredRootStack colored root to set resource on
     * @param resourceItem     resource to set
     */
    public static void setResourceItem(
            @Nonnull ItemStack coloredRootStack, @Nonnull ItemStack resourceItem) {
        coloredRootStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(new ArrayList<>(List.of(resourceItem))));
    }

    public static ItemStack getColoredRootWithResource(RainbowColor color, ItemStack resource) {
        Item root = ModItems.getColoredRoot(color).get();
        ItemStack coloredRoot = new ItemStack(root);
        setResourceItem(coloredRoot, resource.copy());
        return coloredRoot;
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


    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack root = context.getItemInHand();

        if (root.has(DataComponents.CONTAINER)) {
            // Imbued roots can't be used to color grass as it would consume the root and that would make some
            // users a bit angry.
            return InteractionResult.PASS;
        }
        // Attempt to change the block being touched to a rainbow grass block of the same color as the root

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
            // Consume the root
            root.shrink(1);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1F, 1F);
            ModCriteriaTriggers.TOUCH_GRASS_TRIGGER.get().trigger((ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
