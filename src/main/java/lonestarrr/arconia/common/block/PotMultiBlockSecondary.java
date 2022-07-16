package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import lonestarrr.arconia.compat.theoneprobe.TOPDriver;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Block that is part of a large multiblock pot - this is the secondary, passive block. It is invisible in the world, as the primary block
 * will render the large model
 */
public class PotMultiBlockSecondary extends BaseEntityBlock implements TOPDriver {
    private static final VoxelShape[] shapes;
    private static final VoxelShape defaultShape = box(0, 0,0, 16, 16, 16);
    private static final int MAX_SHAPE_IDX = 2 << 2 | 2; // see calcShapeIndex()

    static {
        shapes = calculateShapes();
    }

    public PotMultiBlockSecondary() {

        super(Block.Properties.of(Material.METAL).strength(4.0F).noOcclusion());
    }

    @Override
    public InteractionResult use(
            BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer) || player instanceof FakePlayer) {
            return InteractionResult.PASS;
        }

        ItemStack itemUsed = player.getInventory().getSelected();
        // We buy gold
        if (itemUsed.isEmpty() || itemUsed.getItem() != Items.GOLD_INGOT) {
            return InteractionResult.PASS;
        }

        PotMultiBlockPrimaryBlockEntity primaryBE = getPrimaryBlockEntity(world, pos);
        if (primaryBE == null) {
            return InteractionResult.PASS;
        }

        int coinsAdded = primaryBE.addCoins(1);
        if (coinsAdded > 0) {
            itemUsed.setCount(itemUsed.getCount() - coinsAdded);
            world.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1, 1.3f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PotMultiBlockSecondaryBlockEntity(pos, state); }

        // inspired by Barrier block
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    // inspired by Barrier block
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // inspired by Barrier block
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);

        BlockPos primaryPos = getPrimaryBlockPos(world, pos);
        if (primaryPos != null) {
            PotMultiBlockPrimary.breakMultiBlock(world, primaryPos);
        }
    }

    private BlockPos getPrimaryBlockPos(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null || !(be instanceof PotMultiBlockSecondaryBlockEntity)) {
            return null;
        }
        PotMultiBlockSecondaryBlockEntity secondaryBE = (PotMultiBlockSecondaryBlockEntity) be;
        return secondaryBE.getPrimaryPos();
    }

    private PotMultiBlockPrimaryBlockEntity getPrimaryBlockEntity(Level world, BlockPos pos) {
        BlockPos primaryPos = getPrimaryBlockPos(world, pos);
        if (primaryPos == null) {
            return null;
        }

        BlockEntity be = world.getBlockEntity(primaryPos);
        return be != null && be instanceof PotMultiBlockPrimaryBlockEntity ? (PotMultiBlockPrimaryBlockEntity) be : null;
    }

    /**
     * The shape is determined by the position of this multiblock block relative to the center primary block, since this multiblock isn't a simple cuboid.
     * @param state
     * @param worldIn
     * @param pos
     * @param context
     * @return
     */
    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        if (be == null || !(be instanceof PotMultiBlockSecondaryBlockEntity)) {
            return defaultShape;
        }
        PotMultiBlockSecondaryBlockEntity secondaryBE = (PotMultiBlockSecondaryBlockEntity)be;
        BlockPos primaryPos = ((PotMultiBlockSecondaryBlockEntity) be).getPrimaryPos();
        if (primaryPos == null) {
            return defaultShape;
        }

        int deltaX = primaryPos.getX() - pos.getX();
        int deltaZ = primaryPos.getZ() - pos.getZ();
        return shapes[calcShapeIndex(deltaX, deltaZ)];
    }

    private static int calcShapeIndex(final int deltaX, final int deltaZ) {
        int dx = (deltaX > 0 ? 2 : deltaX < 0 ? 0 : 1);
        int dz = (deltaZ > 0 ? 2 : deltaZ < 0 ? 0 : 1);
        return dx << 2 | dz;
    }

    // Precalculates all possible shapes for the multiblock blocks as the getShape() method gets called frequently
    private static VoxelShape[] calculateShapes() {
        VoxelShape[] shapes = new VoxelShape[MAX_SHAPE_IDX + 1];
        for (int deltaX = -1; deltaX <2; deltaX++) {
            for (int deltaZ = -1; deltaZ < 2; deltaZ++) {
                int idx = calcShapeIndex(deltaX, deltaZ);
                shapes[idx] = calculateShape(deltaX, deltaZ);
            }
        }
        return shapes;
    }

    private static VoxelShape calculateShape(int deltaX, int deltaZ) {
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

        return box(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void addProbeInfo(
            ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        PotMultiBlockPrimaryBlockEntity entity = getPrimaryBlockEntity(world, data.getPos());
        if (entity == null) {
            return;
        }

        long coinCount = entity.getCoinCount();
        String lang;
        if (coinCount == 0) {
            lang = "none";
        } else if (coinCount < 10) {
            lang = "few";
        } else if (coinCount < 100) {
            lang = "tens";
        } else if (coinCount < 1000) {
            lang = "hundreds";
        } else if (coinCount < 10000) {
            lang = "thousands";
        } else {
            lang = "ludicrous";
        }

        // TODO use icons instead..?
        probeInfo.text(new TranslatableComponent(LanguageHelper.block("pot_multiblock") + ".coin_count." + lang));
    }
}