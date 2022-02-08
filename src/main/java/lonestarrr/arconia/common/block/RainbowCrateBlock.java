package lonestarrr.arconia.common.block;

import lonestarrr.arconia.client.gui.crate.RainbowCrateContainer;
import lonestarrr.arconia.common.block.tile.RainbowCrateTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiered crates. Who does not like a li'l extra storage?
 */
public class RainbowCrateBlock extends BaseEntityBlock implements BlockColor {
    private static final Map<RainbowColor,MenuType<RainbowCrateContainer>> containerTypes =
            new HashMap<>(RainbowColor.values().length);

    private final RainbowColor tier;
    private static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Logger LOGGER = LogManager.getLogger();

    public RainbowCrateBlock(RainbowColor tier) {
        super(Block.Properties.of(Material.WOOD).strength(1.0F));
        this.tier = tier;
        BlockState defaultBlockState = this.stateDefinition.any().setValue(FACING, Direction.NORTH);
        this.registerDefaultState(defaultBlockState);
    }

    /**
     * BlockState properties for this block
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * When the block is placed into the world, calculates the correct BlockState based on which direction the player is facing
     * @param blockItemUseContext
     * @return
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockItemUseContext) {
        Level world = blockItemUseContext.getLevel();
        BlockPos blockPos = blockItemUseContext.getClickedPos();

        Direction direction = blockItemUseContext.getHorizontalDirection();  // north, east, south, or west

        BlockState blockState = defaultBlockState().setValue(FACING, direction);
        return blockState;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter worldIn) {
        return new RainbowCrateTileEntity(this.tier);
    }

    // not needed if your block implements ITileEntityProvider (in this case implemented by BlockContainer), but it
    //  doesn't hurt to include it anyway...
    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (worldIn.isClientSide) {
            LOGGER.info("onBlockActivated: world.isRemote()");
            return InteractionResult.SUCCESS;
        }

        LOGGER.info("onBlockActivate: world is server");

        // namedContainerProvider -> this is the tile entity
        MenuProvider namedContainerProvider = this.getMenuProvider(state, worldIn, pos);
        if (namedContainerProvider != null) {
            if (!(player instanceof ServerPlayer)) {
                LOGGER.info("Player is not a ServerPlayerEntity");
                return InteractionResult.FAIL;  // should always be true, but just in case...
            }
            ServerPlayer serverPlayerEntity = (ServerPlayer)player;
            // Write location of the crate so the client can find the associated tile entity - other packets will
            // keep the server tile entity in sync with the tile entity because its data is needed to render the GUI
            NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider,
                    (packetBuffer)->{packetBuffer.writeBlockPos(pos);});
        }
        return InteractionResult.SUCCESS;
    }

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    // Code is copied directly from vanilla eg ChestBlock, CampfireBlock
    public void onRemove(BlockState state, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = world.getBlockEntity(blockPos);
            if (tileentity instanceof RainbowCrateTileEntity) {
                RainbowCrateTileEntity tileEntity = (RainbowCrateTileEntity) tileentity;
                tileEntity.dropAllContents(world, blockPos);
            }
//          world.updateComparatorOutputLevel(pos, this);  if the inventory is used to set redstone power for comparators
            super.onRemove(state, world, blockPos, newState, isMoving);  // call it last, because it removes the TileEntity
        }
    }

    // render using a BakedModel
    // required because the default (super method) is INVISIBLE for ContainerBlock
    @Override
    public RenderShape getRenderShape(BlockState iBlockState) {
        return RenderShape.MODEL;
    }

    public RainbowColor getTier() {
        return this.tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }


    public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event)
    {
        IForgeRegistry<MenuType<?>> r = event.getRegistry();
        for (RainbowColor tier: RainbowColor.values()) {
            MenuType<RainbowCrateContainer> cType = IForgeContainerType.create(
                    (int windowId, Inventory playerInventory, FriendlyByteBuf extraData) ->
                            RainbowCrateContainer.createContainerClientSide(tier, windowId, playerInventory,
                                    extraData));
            containerTypes.put(tier, cType);
            register(r, cType, tier.getTierName() + "_rainbow_crate_container");
        }

        LOGGER.info("************ registered RainbowCrate containers");
    }

    public static MenuType<RainbowCrateContainer> getContainerTypeByTier(RainbowColor tier) {
        return containerTypes.get(tier);
    }
}