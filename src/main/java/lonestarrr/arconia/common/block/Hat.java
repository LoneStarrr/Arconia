package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.HatTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HatTileEntity(pos, state);
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
    public static boolean setResourceGenerated(Level world, BlockPos pos, RainbowColor tier, ItemStack resource, int interval, int coinCost) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && te instanceof HatTileEntity) {
            HatTileEntity rte = (HatTileEntity) te;
            rte.setResourceGenerated(tier, resource, interval, coinCost);
            return true;
        }
        return false;
    }
}
