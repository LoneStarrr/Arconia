package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * Pedestal block. Used for crafting rituals. Because every magic mod needs 8 pedestals in a circle for crafting things. Except perhaps for Botania. The
 * magic mod. Get it? Wink wink nudge nudge.
 */
public class Pedestal extends Block {
    public static final VoxelShape SHAPE;

    static {
        VoxelShape base0 = box(2, 0, 2, 14, 1, 14);
        VoxelShape base1 = box(3, 1, 3, 13, 2, 13);
        VoxelShape center = box(4, 2, 4, 12, 12, 12);
        VoxelShape top0 = box(3, 12, 3, 13, 13, 13);
        VoxelShape top1 = box(2, 13, 2, 14, 14, 14);
        SHAPE = VoxelShapes.or(base0, base1, center, top0, top1);
    }

    public Pedestal() {
        super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PedestalTileEntity();
    }

    @Override
    public ActionResultType use(
            BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        ItemStack playerStack = player.getItemInHand(hand);

        TileEntity tile = world.getBlockEntity(pos);
        if (tile == null || !(tile instanceof PedestalTileEntity)) {
            return ActionResultType.PASS;
        }

        ItemStack currentItem = ((PedestalTileEntity) tile).getItemOnDisplay();

        if (currentItem.isEmpty()) {
            if (playerStack.isEmpty()) {
                return ActionResultType.FAIL;
            }
            ((PedestalTileEntity) tile).putItem(playerStack);
            if (playerStack.getCount() > 1) {
                playerStack.setCount(playerStack.getCount() - 1);
            } else {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return ActionResultType.SUCCESS;
        } else {
            if (player.addItem(currentItem)) {
                ((PedestalTileEntity) tile).removeItem();
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
    }
}
