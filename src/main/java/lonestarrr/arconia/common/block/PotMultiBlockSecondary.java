package lonestarrr.arconia.common.block;

import com.mojang.serialization.MapCodec;
import lonestarrr.arconia.common.block.entities.PotMultiBlockPrimaryBlockEntity;
import lonestarrr.arconia.common.block.entities.PotMultiBlockSecondaryBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Block that is part of a large multiblock pot - this is the secondary, passive block. It is invisible in the world, as the primary block
 * will render the large model
 */
public class PotMultiBlockSecondary extends BaseEntityBlock {
    private static final Map<PotPosition, VoxelShape> posShapes = new HashMap<>();
    static {
        posShapes.put(PotPosition.CENTER, box(0, 0, 0, 16, 16, 16));
        posShapes.put(PotPosition.N, box(0, 0, 0, 16, 16, 11));
        posShapes.put(PotPosition.NW, box(7, 0, 0, 16, 16, 9));
        posShapes.put(PotPosition.W, box(5, 0, 0, 16, 16, 16));
        posShapes.put(PotPosition.SW, box(7, 0, 7, 16, 16, 16));
        posShapes.put(PotPosition.S, box(0, 0, 5, 16, 16, 16));
        posShapes.put(PotPosition.SE, box(0, 0, 7, 9, 16, 16));
        posShapes.put(PotPosition.E, box(0, 0, 0, 11, 16, 16));
        posShapes.put(PotPosition.NE, box(0, 0, 0, 9, 16, 9));
    }

    public static EnumProperty<PotPosition> POT_POSITION = EnumProperty.create("pot_position", PotPosition.class);

    public PotMultiBlockSecondary() {
        super(Block.Properties.of().mapColor(MapColor.METAL).strength(4.0F).noOcclusion());
        registerDefaultState(this.getStateDefinition().any().setValue(POT_POSITION, PotPosition.CENTER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POT_POSITION);
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(
            ItemStack itemUsed, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide || hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(player instanceof ServerPlayer) || player instanceof FakePlayer) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        PotMultiBlockPrimaryBlockEntity primaryBE = getPrimaryBlockEntity(world, pos);
        if (primaryBE == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (itemUsed.isEmpty()) {
            RainbowColor potTier = primaryBE.getTier();
            if (potTier == null) {
                player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.no_tier"));
            } else {
                player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.show_tier", potTier.getTierName()));
            }
            return ItemInteractionResult.SUCCESS;
        } else if (itemUsed.getItem() instanceof ColoredRoot) {
            ItemStack resource = ColoredRoot.getResourceItem(itemUsed);

            if (resource.isEmpty()) {
                /* Players can remove treasure being extracted by using a single non-imbued root in their main hand.
                 * An item in the off-hand can be used to remove specific treasure.
                 */
                ItemStack offhandItem = player.getOffhandItem();
                ItemStack removedResource;
                if (offhandItem.isEmpty()) {
                    // no offhand item -> pop off the last treasure
                    removedResource = primaryBE.removeResourceGenerated(ItemStack.EMPTY);
                    if (removedResource.isEmpty()) {
                        player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.remove_resource_none_set"));
                        return ItemInteractionResult.FAIL;
                    }
                } else {
                    // pop off matching treasure if offhand item matches
                    removedResource = primaryBE.removeResourceGenerated(offhandItem);
                }

                if (removedResource.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.remove_resource_not_found"));
                    return ItemInteractionResult.FAIL;
                } else {
                    ItemStack root = makeImbuedRootFromItem((ColoredRoot)itemUsed.getItem(), removedResource);
                    itemUsed.shrink(1);
                    if (!player.getInventory().add(root)) {
                        player.drop(root, false);
                    }
                    player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.remove_resource_success", removedResource.getItem().getDescription()));
                    return ItemInteractionResult.SUCCESS;
                }
            } else {
                // Using an imbued root on the pot tells it to extract treasure
                if (!primaryBE.addResourceGenerated(resource)) {
                    player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.set_resource_full"));
                    return ItemInteractionResult.FAIL;
                } else {
                    itemUsed.shrink(1);
                    player.sendSystemMessage(Component.translatable("arconia.block.pot_multiblock.set_resource_success"));
                    return ItemInteractionResult.SUCCESS;
                }
            }

        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemStack makeImbuedRootFromItem(ColoredRoot root, ItemStack resource) {
        /* This allows treasure to be set on any color root, but that is ok. The difficulty lies in imbuing the root
         * for the first time, which is what gates the more difficult treasure.
         */
        RainbowColor tier = RainbowColor.RED;
        ItemStack rootStack = new ItemStack(root);
        ColoredRoot.setResourceItem(rootStack, resource);
        return rootStack;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PotMultiBlockSecondaryBlockEntity(pos, state); }

        // inspired by Barrier block
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
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
    public @NotNull BlockState playerWillDestroy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        BlockState result = super.playerWillDestroy(world, pos, state, player);

        BlockPos primaryPos = getPrimaryBlockPos(world, pos);
        if (primaryPos != null) {
            PotMultiBlockPrimary.breakMultiBlock(world, primaryPos);
        }
        return result;
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
     * @param level
     * @param pos
     * @param context
     * @return
     */
    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return posShapes.get(state.getValue(POT_POSITION));
    }

    public enum PotPosition implements StringRepresentable {
        N("n"),
        NW("nw"),
        W("w"),
        SW("sw"),
        S("s"),
        SE("se"),
        E("e"),
        NE("ne"),
        CENTER("center");

        private final String name;

        PotPosition(String name) {
            this.name = name;
        }

        /**
         *
         * @param x 0 <= x <= 2
         * @param z 0 <= z <= 2
         * @return
         */
        public static PotPosition getPositionFromOffset(int x, int z) {
            // TODO do this less horrible, move it elsewhere, coding with sleep deprivation = bad, mkay
            int idx = x << 2 | z;
            switch (idx) {
                case ((1 << 2) | 2): return N;
                case ((0 << 2) | 2): return NW;
                case ((0 << 2) | 1): return W;
                case ((0 << 2) | 0): return SW;
                case ((1 << 2) | 0): return S;
                case ((2 << 2) | 0): return SE;
                case ((2 << 2) | 1): return E;
                case ((2 << 2) | 2): return NE;
                default: return CENTER;
            }
        }
        @NotNull
        @Override
        public String getSerializedName() {
            return name;
        }
    }
}