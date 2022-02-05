package lonestarrr.arconia.common.block;

import lonestarrr.arconia.client.gui.crate.RainbowCrateContainer;
import lonestarrr.arconia.common.block.tile.RainbowCrateTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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

/**
 * Tiered crates. Who does not like a li'l extra storage?
 */
public class RainbowCrateBlock extends ContainerBlock implements IBlockColor {
    private static final Map<RainbowColor,ContainerType<RainbowCrateContainer>> containerTypes =
            new HashMap<>(RainbowColor.values().length);

    private final RainbowColor tier;
    private static final DirectionProperty FACING = HorizontalBlock.FACING;
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * When the block is placed into the world, calculates the correct BlockState based on which direction the player is facing
     * @param blockItemUseContext
     * @return
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        World world = blockItemUseContext.getLevel();
        BlockPos blockPos = blockItemUseContext.getClickedPos();

        Direction direction = blockItemUseContext.getHorizontalDirection();  // north, east, south, or west

        BlockState blockState = defaultBlockState().setValue(FACING, direction);
        return blockState;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
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
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (worldIn.isClientSide) {
            LOGGER.info("onBlockActivated: world.isRemote()");
            return ActionResultType.SUCCESS;
        }

        LOGGER.info("onBlockActivate: world is server");

        // namedContainerProvider -> this is the tile entity
        INamedContainerProvider namedContainerProvider = this.getMenuProvider(state, worldIn, pos);
        if (namedContainerProvider != null) {
            if (!(player instanceof ServerPlayerEntity)) {
                LOGGER.info("Player is not a ServerPlayerEntity");
                return ActionResultType.FAIL;  // should always be true, but just in case...
            }
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            // Write location of the crate so the client can find the associated tile entity - other packets will
            // keep the server tile entity in sync with the tile entity because its data is needed to render the GUI
            NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider,
                    (packetBuffer)->{packetBuffer.writeBlockPos(pos);});
        }
        return ActionResultType.SUCCESS;
    }

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    // Code is copied directly from vanilla eg ChestBlock, CampfireBlock
    public void onRemove(BlockState state, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(blockPos);
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
    public BlockRenderType getRenderShape(BlockState iBlockState) {
        return BlockRenderType.MODEL;
    }

    public RainbowColor getTier() {
        return this.tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }


    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        IForgeRegistry<ContainerType<?>> r = event.getRegistry();
        for (RainbowColor tier: RainbowColor.values()) {
            ContainerType<RainbowCrateContainer> cType = IForgeContainerType.create(
                    (int windowId, PlayerInventory playerInventory, PacketBuffer extraData) ->
                            RainbowCrateContainer.createContainerClientSide(tier, windowId, playerInventory,
                                    extraData));
            containerTypes.put(tier, cType);
            register(r, cType, tier.getTierName() + "_rainbow_crate_container");
        }

        LOGGER.info("************ registered RainbowCrate containers");
    }

    public static ContainerType<RainbowCrateContainer> getContainerTypeByTier(RainbowColor tier) {
        return containerTypes.get(tier);
    }
}