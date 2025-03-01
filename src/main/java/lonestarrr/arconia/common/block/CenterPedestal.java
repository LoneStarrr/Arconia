package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.block.entities.CenterPedestalBlockEntity;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Center Pedestal block. Used for crafting rituals. This one outputs the result of the crafting recipe.
 */
public class CenterPedestal extends BaseEntityBlock {
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
        super(Properties.of().mapColor(MapColor.STONE).strength(1.5F).sound(SoundType.STONE));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CenterPedestalBlockEntity(pos, state); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
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
            return createTickerHelper(type, ModBlockEntities.CENTER_PEDESTAL.get(), CenterPedestalBlockEntity::tick);
        }
        return null;
    }


    @Override
    public ItemInteractionResult useItemOn(
            ItemStack playerStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult traceResult) {
        CenterPedestalBlockEntity cbe = getBlockEntity(level, pos);
        if (cbe == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!cbe.getItemOnDisplay().isEmpty()) {
            ItemStack displayedItem = cbe.getItemOnDisplay();
            if (player.addItem(displayedItem)) {
                cbe.removeItem();
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (playerStack.isEmpty() || playerStack.getItem() != ModItems.cloverStaff.get()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            if (!cbe.isRitualOngoing()) {
                if (cbe.startRitual()) {
                    startRitualEffect(level, pos);
                } else {
                    player.sendSystemMessage(Component.translatable(LANG_PREFIX + ".ritual_start_failed"));
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    private static void startRitualEffect(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.AMBIENT, 1, 10);
    }

    private CenterPedestalBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CenterPedestalBlockEntity)) {
            return null;
        }
        return (CenterPedestalBlockEntity) blockEntity;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean p_60514_) {
        if (!level.isClientSide) {
            // Redstone signal? Let's gooo
            if (level.hasNeighborSignal(pos)) {
                CenterPedestalBlockEntity entity = getBlockEntity(level, pos);
                if (entity != null && !entity.isRitualOngoing()) {
                    if (entity.startRitual()) {
                        startRitualEffect(level, pos);
                    }
                }
            }
        }
        super.neighborChanged(blockState, level, pos, block, neighbor, p_60514_);
    }
}
