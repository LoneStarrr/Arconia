package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.core.helper.LanguageHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import lonestarrr.arconia.common.block.tile.CenterPedestalTileEntity;
import lonestarrr.arconia.common.block.tile.PedestalTileEntity;
import lonestarrr.arconia.common.item.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Center Pedestal block. Used for crafting rituals. This one outputs the result of the crafting recipe.
 */
public class CenterPedestal extends Block {
    public static final VoxelShape SHAPE;
    private static final String LANG_PREFIX = LanguageHelper.block("center_pedestal");

    static {
        VoxelShape base0 = makeCuboidShape(2, 0, 2, 14, 1, 14);
        VoxelShape base1 = makeCuboidShape(3, 1, 3, 13, 2, 13);
        VoxelShape center = makeCuboidShape(4, 2, 4, 12, 12, 12);
        VoxelShape top0 = makeCuboidShape(3, 12, 3, 13, 13, 13);
        VoxelShape top1 = makeCuboidShape(2, 13, 2, 14, 14, 14);
        SHAPE = VoxelShapes.or(base0, base1, center, top0, top1);

    }

    public CenterPedestal() {
        super(Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F).sound(SoundType.STONE));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CenterPedestalTileEntity();
    }

    @Override
    public ActionResultType onBlockActivated(
            BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        ItemStack playerStack = player.getHeldItem(hand);

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null || !(tile instanceof CenterPedestalTileEntity)) {
            return ActionResultType.PASS;
        }
        CenterPedestalTileEntity cte = (CenterPedestalTileEntity) tile;

        if (!cte.getItemOnDisplay().isEmpty()) {
            ItemStack displayedItem = cte.getItemOnDisplay();
            if (player.addItemStackToInventory(displayedItem)) {
                cte.removeItem();
            }
            return ActionResultType.SUCCESS;
        }

        if (playerStack.isEmpty() || playerStack.getItem() != ModItems.cloverStaff) {
            return ActionResultType.PASS;
        }

        if (!world.isRemote()) {
            if (!cte.isRitualOngoing()) {
                if (cte.startRitual()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 1, 10);
                } else {
                    player.sendMessage(new TranslationTextComponent(LANG_PREFIX + ".ritual_start_failed"), Util.DUMMY_UUID);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }
}
