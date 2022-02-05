package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;

import java.util.Random;

/**
 * A crop that can imbue gold coins with a magic rainbow color property. Once it's matured, gold coins can be added
 * to it that will then slowly be imbued with the color. Tiered, because progression.
 */
public class RainbowCropBlock extends CropsBlock {
    // bounding box for this crop per growth stage
    private static final VoxelShape shapeAge0 = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
    public static final IntegerProperty CROP_AGE = IntegerProperty.create("age",0,4);

    private static Logger logger;
    private final RainbowColor color;

    public RainbowCropBlock(RainbowColor color) {
        // can only be created from a seed
        super(Block.Properties.of(Material.PLANT).noCollission().randomTicks().strength(0f).sound(SoundType.CROP));
        this.color = color;
    }

    /*
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // TODO define actual shapes and return different ones based on age
        return shapeAge0;
    }
    */

    @Override
    public IntegerProperty getAgeProperty() {
        return CROP_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(CROP_AGE);
    }

    @Override
    public boolean isBonemealSuccess(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean mayPlaceOn(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Block soil = state.getBlock();
        if (!(soil instanceof ResourceGenBlock)) {
            return false;
        }

        TileEntity te = worldIn.getBlockEntity(pos);
        if (te == null || !(te instanceof ResourceGenTileEntity)) {
            return false;
        }
        ResourceGenTileEntity rgte = (ResourceGenTileEntity)te;
        RainbowColor tier = rgte.getTier();
        // Only can be placed on  blocks of the same or higher tier
        return tier.compareTo(this.color) >= 0;
    }

    @Override
    public int getMaxAge() {
        // Number of growth stages
        return 4;
    }

//    @Override
//    protected IItemProvider getSeedsItem() {
//        return ModItems.getRainbowSeed(this.color);
//    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.getArconiumEssence(this.color));
    }

//    public String getSeedResourceName() {
//        return color.getTierName() + ItemNames.RAINBOW_SEED_SUFFIX;
//    }
}
