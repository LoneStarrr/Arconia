package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockSecondaryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Block that is part of a large multiblock pot - this is the secondary, passive block. It is invisible in the world, as the primary block
 * will render the large model
 */
public class PotMultiBlockSecondary extends Block {
    public PotMultiBlockSecondary() {
        super(Block.Properties.create(Material.IRON, MaterialColor.BLACK).hardnessAndResistance(2.0F).notSolid());
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PotMultiBlockSecondaryTileEntity();
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(world, pos, state, player);
        TileEntity te = world.getTileEntity(pos);
        if (te == null || !(te instanceof PotMultiBlockSecondaryTileEntity)) {
            return;
        }
        PotMultiBlockSecondaryTileEntity secondaryTE = (PotMultiBlockSecondaryTileEntity) te;
        BlockPos primaryPos = secondaryTE.getPrimaryPos();
        if (primaryPos != null) {
            PotMultiBlockPrimary.breakMultiBlock(world, primaryPos);
            // TODO doesn't that actually mess with this, e.g. the block being harvested right now sets the block to air? Yup. Best to just dump these blocks as
            // entities then
        }
        // TODO also add this on the primary block!
    }

    // TODO aabb based on where the primary block is

    @Override
    public VoxelShape getShape(
            BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getTileEntity(pos);
        VoxelShape defaultShape = makeCuboidShape(0, 0,0, 16, 16, 16);
        if (te == null || !(te instanceof PotMultiBlockSecondaryTileEntity)) {
            return defaultShape;
        }
        PotMultiBlockSecondaryTileEntity secondaryTE = (PotMultiBlockSecondaryTileEntity)te;
        BlockPos primaryPos = ((PotMultiBlockSecondaryTileEntity) te).getPrimaryPos();
        if (primaryPos == null) {
            return defaultShape;
        }

        int deltaX = primaryPos.getX() - pos.getX();
        int deltaZ = primaryPos.getZ() - pos.getZ();
        final int PADDING = 5;
        int x1 = 0, x2 = 16, y1 = 0, y2 = 16, z1 = 0, z2 = 16;

        if (deltaX > 0) {
            x1 = PADDING;
            x2 = 16;
        } else if (deltaX < 0) {
            x1 = 0;
            x2 = 16 - PADDING;
        }

        if (deltaZ > 0) {
            z1 = PADDING;
            z2 = 16;
        } else if (deltaZ < 0) {
            z1 = 0;
            z2 = 16 - PADDING;
        }

        return makeCuboidShape(x1, y1, z1, x2, y2, z2);
    }
}