package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.block.entities.OrbBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Pulls in items of a specific type near it.
 */
public class Orb extends BaseEntityBlock {
    public static final VoxelShape SHAPE;

    static {
        // I Used Plotz Modeler to generate the sphere model, easy to step over it layer by layer, then copied that into blockbench
        VoxelShape layer1 = box(5, 1, 5, 11, 2, 11);
        VoxelShape layer2 = box(4, 2, 4, 12, 3, 12);
        VoxelShape layer3 = box(3, 3, 3, 13, 4, 13);
        VoxelShape layer4 = box(2, 4, 2, 14, 5, 14);
        VoxelShape layer5_10 = box(1, 5, 1, 15, 11, 15);
        VoxelShape layer11 = box(2, 11, 2, 14, 12, 14);
        VoxelShape layer12 = box(3, 12, 3, 13, 13, 13);
        VoxelShape layer13 = box(4, 13, 4, 12, 14, 12);
        VoxelShape layer14 = box(5, 14, 5, 11, 15, 11);
        SHAPE = Shapes.or(layer1, layer2, layer3, layer4, layer5_10, layer11, layer12, layer13, layer14);
    }

    public Orb() {
        super(Block.Properties.of(Material.GLASS, MaterialColor.NONE).strength(0.5F).lightLevel(s->15).sound(SoundType.GLASS));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.ORB, OrbBlockEntity::tick);
        }
        return null;
    }

    @Override
    public InteractionResult use(
            BlockState blockState, Level world, BlockPos blockPos, Player playerEntity, InteractionHand hand, BlockHitResult rayTraceResult) {
//        return super.onBlockActivated(blockState, world, blockPos, playerEntity, hand, rayTraceResult);
        OrbBlockEntity orbEntity = null;
        if (world.getBlockEntity(blockPos) != null && world.getBlockEntity(blockPos) instanceof OrbBlockEntity) {
            orbEntity = (OrbBlockEntity) world.getBlockEntity(blockPos);
        }

        if (orbEntity == null) {
            return InteractionResult.PASS;
        }

        if (playerEntity.isShiftKeyDown()) {
            ItemStack stack = orbEntity.popItem();
            if (stack.isEmpty()) {
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        } else {
            ItemStack held = playerEntity.getItemInHand(hand);
            if (held.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (orbEntity.addItem(held)) {
                return InteractionResult.SUCCESS;
                // TODO play 'positive ploink' sound effect
            }
            return InteractionResult.PASS;
        }
    }
}
