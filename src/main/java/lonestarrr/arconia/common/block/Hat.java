package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.HatTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A leprechaun's hat. Used in combination with a pot of gold to collect resources.
 */
public class Hat extends Block {
    private static final VoxelShape shape = makeCuboidShape(0, 0, 0, 16, 10, 16);

    public Hat() {
        super(Block.Properties.create(Material.WOOL, MaterialColor.GREEN).hardnessAndResistance(1.0F).notSolid());
    }

    @Override
    public VoxelShape getShape(
            BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HatTileEntity();
    }

    /**
     * Set the resource to be generated on a hat placed in the world
     * @param world
     * @param pos
     * @param tier
     * @param resource
     * @param interval Frequency with which resource is generated. An interval of 1 is fastest. Interval length is determined by the pot of gold and is typically
     *                 no less than 5 ticks.
     * @param coinCost Number of coins it takes to generate the resource
     * @return True if a hat was placed in the world at the given location
     */
    public static boolean setResourceGenerated(World world, BlockPos pos, RainbowColor tier, ItemStack resource, int interval, int coinCost) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof HatTileEntity) {
            HatTileEntity rte = (HatTileEntity) te;
            rte.setResourceGenerated(tier, resource, interval, coinCost);
            return true;
        }
        return false;
    }
}
