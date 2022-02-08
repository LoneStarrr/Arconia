package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.helper.LanguageHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;
import lonestarrr.arconia.common.item.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

/**
 * Center Pedestal block. Used for crafting rituals. This one outputs the result of the crafting recipe.
 */
public class CenterPedestal extends Block {
    public static final VoxelShape SHAPE;
    private static final String LANG_PREFIX = LanguageHelper.block("center_pedestal");

    static {
        VoxelShape base0 = box(2, 0, 2, 14, 1, 14);
        VoxelShape base1 = box(3, 1, 3, 13, 2, 13);
        VoxelShape center = box(4, 2, 4, 12, 12, 12);
        VoxelShape top0 = box(3, 12, 3, 13, 13, 13);
        VoxelShape top1 = box(2, 13, 2, 14, 14, 14);
        SHAPE = Shapes.or(base0, base1, center, top0, top1);

    }

    public CenterPedestal() {
        super(Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F).sound(SoundType.STONE));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new CenterPedestalTileEntity();
    }

    @Override
    public InteractionResult use(
            BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult traceResult) {
        ItemStack playerStack = player.getItemInHand(hand);

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null || !(tile instanceof CenterPedestalTileEntity)) {
            return InteractionResult.PASS;
        }
        CenterPedestalTileEntity cte = (CenterPedestalTileEntity) tile;

        if (!cte.getItemOnDisplay().isEmpty()) {
            ItemStack displayedItem = cte.getItemOnDisplay();
            if (player.addItem(displayedItem)) {
                cte.removeItem();
            }
            return InteractionResult.SUCCESS;
        }

        if (playerStack.isEmpty() || playerStack.getItem() != ModItems.cloverStaff) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide()) {
            if (!cte.isRitualOngoing()) {
                if (cte.startRitual()) {
                    world.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP, SoundSource.AMBIENT, 1, 10);
                } else {
                    player.sendMessage(new TranslatableComponent(LANG_PREFIX + ".ritual_start_failed"), Util.NIL_UUID);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
