package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.PedestalTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

/**
 * Pedestal block. Used for crafting rituals. Because every magic mod needs 8 pedestals in a circle for crafting things. Except perhaps for Botania. The
 * magic mod. Get it? Wink wink nudge nudge.
 */
public class Pedestal extends BaseEntityBlock {
    public static final VoxelShape SHAPE;

    static {
        VoxelShape base0 = box(2, 0, 2, 14, 1, 14);
        VoxelShape base1 = box(3, 1, 3, 13, 2, 13);
        VoxelShape center = box(4, 2, 4, 12, 12, 12);
        VoxelShape top0 = box(3, 12, 3, 13, 13, 13);
        VoxelShape top1 = box(2, 13, 2, 14, 14, 14);
        SHAPE = Shapes.or(base0, base1, center, top0, top1);
    }

    public Pedestal() {
        super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalTileEntity(pos, state);
    }

    @Override
    public InteractionResult use(
            BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult traceResult) {
        ItemStack playerStack = player.getItemInHand(hand);

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null || !(tile instanceof PedestalTileEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack currentItem = ((PedestalTileEntity) tile).getItemOnDisplay();

        if (currentItem.isEmpty()) {
            if (playerStack.isEmpty()) {
                return InteractionResult.FAIL;
            }
            ((PedestalTileEntity) tile).putItem(playerStack);
            if (playerStack.getCount() > 1) {
                playerStack.setCount(playerStack.getCount() - 1);
            } else {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return InteractionResult.SUCCESS;
        } else {
            if (player.addItem(currentItem)) {
                ((PedestalTileEntity) tile).removeItem();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }
}
