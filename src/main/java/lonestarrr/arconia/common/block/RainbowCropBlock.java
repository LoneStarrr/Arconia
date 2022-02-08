package lonestarrr.arconia.common.block;

import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;
import lonestarrr.arconia.common.block.tile.ResourceGenTileEntity;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A crop that can imbue gold coins with a magic rainbow color property. Once it's matured, gold coins can be added
 * to it that will then slowly be imbued with the color. Tiered, because progression.
 */
public class RainbowCropBlock extends CropBlock {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CROP_AGE);
    }

    @Override
    public boolean isBonemealSuccess(Level worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
        Block soil = state.getBlock();
        if (!(soil instanceof ResourceGenBlock)) {
            return false;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
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
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.getArconiumEssence(this.color));
    }

//    public String getSeedResourceName() {
//        return color.getTierName() + ItemNames.RAINBOW_SEED_SUFFIX;
//    }
}
