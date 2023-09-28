package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.entities.HatBlockEntity;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.util.FakePlayer;

/**
 * A leprechaun's hat. Used in combination with a pot of gold to collect resources.
 */
public class Hat extends BaseEntityBlock {
    private static final VoxelShape shape = box(0, 0, 0, 16, 10, 16);

    public Hat() {
        super(Block.Properties.of(Material.WOOL, MaterialColor.COLOR_GREEN).strength(1.0F).noOcclusion());
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HatBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer) || player instanceof FakePlayer) {
            return InteractionResult.PASS;
        }

        ItemStack itemUsed = player.getInventory().getSelected();

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || !(be instanceof HatBlockEntity)) {
            return InteractionResult.PASS;
        }

        HatBlockEntity hbe = (HatBlockEntity) be;
        BlockPos potPos = hbe.getLinkedPot();
        PotMultiBlockPrimaryBlockEntity potbe = null;

        if (potPos != null && level.getBlockEntity(potPos) instanceof PotMultiBlockPrimaryBlockEntity) {
            potbe = (PotMultiBlockPrimaryBlockEntity)level.getBlockEntity(potPos);
        }

        // Empty hand -> display info. Colored root -> set resource. Crouching + empty hand -> unset resource. Staff: link or unlink to pot (the latter is
        // implemented in the staff item)
        if (itemUsed.getItem() instanceof ColoredRoot) {
            ColoredRoot root = (ColoredRoot)itemUsed.getItem();

            if (potbe == null) {
                player.sendMessage(new TranslatableComponent("arconia.block.hat.not_linked_to_pot"), Util.NIL_UUID);
                return InteractionResult.FAIL;
            } else if (!hbe.getResourceGenerated().isEmpty()) {
                player.sendMessage(new TranslatableComponent("arconia.block.hat.resource_already_set"), Util.NIL_UUID);
                return InteractionResult.FAIL;
            } else if (root.getTier().getTier() > potbe.getTier().getTier()) {
                player.sendMessage(new TranslatableComponent("arconia.block.hat.resource_tier_too_high"), Util.NIL_UUID);
                return InteractionResult.FAIL;
            } else {
                // Ok ok ok, let's attempt set the resource already.
                ItemStack resource = ColoredRoot.getResourceItem(itemUsed);
                if (resource.isEmpty()) {
                    player.sendMessage(new TranslatableComponent("arconia.block.hat.resource_empty"), Util.NIL_UUID);
                    return InteractionResult.FAIL;
                }

                int count = ColoredRoot.getResourceCount(itemUsed);
                resource.setCount(count);

                hbe.setResourceGenerated(root.getTier(), resource);
                if (itemUsed.getCount() > 1) {
                    itemUsed.shrink(1);
                    player.setItemInHand(hand, itemUsed);
                } else {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
                player.sendMessage(new TranslatableComponent("arconia.block.hat.resource_set", resource.getItem().getName(resource)), Util.NIL_UUID);
                return InteractionResult.SUCCESS;
            }
        } else if (itemUsed.isEmpty()) {
            if (player.isCrouching()) {
                ItemStack resource = hbe.getResourceGenerated();
                if (!resource.isEmpty()) {
                    RainbowColor tier = hbe.getTier();
                    hbe.unsetResourceGenerated();
                    ItemStack root = new ItemStack(ModItems.getColoredRoot(tier));
                    ColoredRoot.setResourceItem(root, resource.getItem(), resource.getCount());
                    player.setItemInHand(hand, root);
                    player.sendMessage(new TranslatableComponent("arconia.block.hat.resource_unset"), Util.NIL_UUID);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                // Show information about the resource being set, link status as such
                ItemStack resource = hbe.getResourceGenerated();
                if (resource.isEmpty()) {
                    player.sendMessage(new TranslatableComponent("arconia.block.hat.info_resource_empty"), Util.NIL_UUID);
                } else {
                    MutableComponent mc = new TranslatableComponent("arconia.block.hat.info_resource", resource.getItem().getName(resource));
                    mc.append(new TranslatableComponent("color.minecraft." + hbe.getTier().toString()));
                    player.sendMessage(mc, Util.NIL_UUID);
                }

                if (potbe != null) {
                    player.sendMessage(new TranslatableComponent("arconia.block.hat.info_linked", potPos.toShortString()), Util.NIL_UUID);
                } else {
                    player.sendMessage(new TranslatableComponent("arconia.block.hat.info_unlinked"), Util.NIL_UUID);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}