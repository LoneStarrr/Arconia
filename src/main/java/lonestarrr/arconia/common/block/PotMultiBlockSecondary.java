package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockPrimaryTileEntity;
import lonestarrr.arconia.common.block.tile.PotMultiBlockSecondaryTileEntity;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import lonestarrr.arconia.compat.theoneprobe.TOPDriver;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;

/**
 * Block that is part of a large multiblock pot - this is the secondary, passive block. It is invisible in the world, as the primary block
 * will render the large model
 */
public class PotMultiBlockSecondary extends Block implements TOPDriver {
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
    public ActionResultType use(
            BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isClientSide || hand != Hand.MAIN_HAND) {
            return ActionResultType.PASS;
        }

        if (!(player instanceof ServerPlayerEntity) || player instanceof FakePlayer) {
            return ActionResultType.PASS;
        }

        ItemStack itemUsed = player.inventory.getSelected();
        // We buy gold
        if (itemUsed.isEmpty() || itemUsed.getItem() != Items.GOLD_INGOT) {
            return ActionResultType.PASS;
        }

        PotMultiBlockPrimaryTileEntity primTile = getPrimaryTileEntity(world, pos);
        if (primTile == null) {
            return ActionResultType.PASS;
        }

        int coinsAdded = primTile.addCoins(1);
        if (coinsAdded > 0) {
            itemUsed.setCount(itemUsed.getCount() - coinsAdded);
            world.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundCategory.BLOCKS, 1, 1.3f);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
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

    // inspired by Barrier block
    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    // inspired by Barrier block
    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    // inspired by Barrier block
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(world, pos, state, player);

        BlockPos primaryPos = getPrimaryBlockPos(world, pos);
        if (primaryPos != null) {
            PotMultiBlockPrimary.breakMultiBlock(world, primaryPos);
        }
    }

    private BlockPos getPrimaryBlockPos(World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te == null || !(te instanceof PotMultiBlockSecondaryTileEntity)) {
            return null;
        }
        PotMultiBlockSecondaryTileEntity secondaryTE = (PotMultiBlockSecondaryTileEntity) te;
        return secondaryTE.getPrimaryPos();
    }

    private PotMultiBlockPrimaryTileEntity getPrimaryTileEntity(World world, BlockPos pos) {
        BlockPos primaryPos = getPrimaryBlockPos(world, pos);
        if (primaryPos == null) {
            return null;
        }

        TileEntity te = world.getBlockEntity(primaryPos);
        return te != null && te instanceof PotMultiBlockPrimaryTileEntity ? (PotMultiBlockPrimaryTileEntity) te : null;
    }

    /**
     * The shape is determined on the position of this multiblock block relative to the center primary block, since this multiblock isn't a simple cuboid.
     * @param state
     * @param worldIn
     * @param pos
     * @param context
     * @return
     */
    @Override
    public VoxelShape getShape(
            BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getBlockEntity(pos);
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
            ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        PotMultiBlockPrimaryTileEntity entity = getPrimaryTileEntity(world, data.getPos());
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
        probeInfo.text(new TranslationTextComponent(LanguageHelper.block("pot_multiblock") + ".coin_count." + lang));
    }
}