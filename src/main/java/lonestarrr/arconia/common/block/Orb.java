package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import lonestarrr.arconia.common.block.tile.OrbTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Pulls in items of a specific type near it.
 */
public class Orb extends Block {
    public static final VoxelShape SHAPE;

    static {
        // I Used Plotz Modeler to generate the sphere model, easy to step over it layer by layer, then copied that into blockbench
        VoxelShape layer1 = makeCuboidShape(5, 1, 5, 11, 2, 11);
        VoxelShape layer2 = makeCuboidShape(4, 2, 4, 12, 3, 12);
        VoxelShape layer3 = makeCuboidShape(3, 3, 3, 13, 4, 13);
        VoxelShape layer4 = makeCuboidShape(2, 4, 2, 14, 5, 14);
        VoxelShape layer5_10 = makeCuboidShape(1, 5, 1, 15, 11, 15);
        VoxelShape layer11 = makeCuboidShape(2, 11, 2, 14, 12, 14);
        VoxelShape layer12 = makeCuboidShape(3, 12, 3, 13, 13, 13);
        VoxelShape layer13 = makeCuboidShape(4, 13, 4, 12, 14, 12);
        VoxelShape layer14 = makeCuboidShape(5, 14, 5, 11, 15, 11);
        SHAPE = VoxelShapes.or(layer1, layer2, layer3, layer4, layer5_10, layer11, layer12, layer13, layer14);
    }

    public Orb() {
        super(Block.Properties.create(Material.GLASS, MaterialColor.AIR).hardnessAndResistance(0.5F).setLightLevel(s->15).sound(SoundType.GLASS));
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
        return new OrbTileEntity();
    }

    @Override
    public ActionResultType onBlockActivated(
            BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult rayTraceResult) {
//        return super.onBlockActivated(blockState, world, blockPos, playerEntity, hand, rayTraceResult);
        OrbTileEntity tile = null;
        if (world.getTileEntity(blockPos) != null && world.getTileEntity(blockPos) instanceof OrbTileEntity) {
            tile = (OrbTileEntity) world.getTileEntity(blockPos);
        }

        if (tile == null) {
            return ActionResultType.PASS;
        }

        if (playerEntity.isSneaking()) {
            ItemStack stack = tile.popItem();
            if (stack.isEmpty()) {
                return ActionResultType.PASS;
            }
            return ActionResultType.SUCCESS;
        } else {
            ItemStack held = playerEntity.getHeldItem(hand);
            if (held.isEmpty()) {
                return ActionResultType.PASS;
            }
            if (tile.addItem(held)) {
                return ActionResultType.SUCCESS;
                // TODO play 'positive ploink' sound effect
            }
            return ActionResultType.PASS;
        }
    }
}
