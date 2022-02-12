package lonestarrr.arconia.common.block;

import lonestarrr.arconia.client.gui.crate.RainbowCrateContainer;
import lonestarrr.arconia.common.block.entities.ModBlockEntities;
import lonestarrr.arconia.common.block.entities.RainbowCrateBlockEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.network.NetworkHooks;
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RainbowCrateBlockEntity(this.tier, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, ModBlockEntities.getRainbowCrateBlockEntityType(tier), RainbowCrateBlockEntity::tick);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (level.isClientSide) {
            LOGGER.info("onBlockActivated: world.isRemote()");
            return InteractionResult.SUCCESS;
        }

        LOGGER.info("onBlockActivate: world is server");

        // namedContainerProvider -> this is the block entity
        MenuProvider namedContainerProvider = this.getMenuProvider(state, level, pos);
        if (namedContainerProvider != null) {
            if (!(player instanceof ServerPlayer)) {
                LOGGER.info("Player is not a ServerPlayerEntity");
                return InteractionResult.FAIL;  // should always be true, but just in case...
            }
            ServerPlayer serverPlayerEntity = (ServerPlayer)player;
            // Write location of the crate so the client can find the associated block entity - other packets will
            // keep the server block entity in sync with the block entity because its data is needed to render the GUI
            NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider,
                    (packetBuffer)->{packetBuffer.writeBlockPos(pos);});
        }
        return InteractionResult.SUCCESS;
    }

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    // Code is copied directly from vanilla eg ChestBlock, CampfireBlock
    public void onRemove(BlockState state, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof RainbowCrateBlockEntity) {
                RainbowCrateBlockEntity crateEntity = (RainbowCrateBlockEntity) blockEntity;
                crateEntity.dropAllContents(world, blockPos);
            }
//          world.updateComparatorOutputLevel(pos, this);  if the inventory is used to set redstone power for comparators
            super.onRemove(state, world, blockPos, newState, isMoving);  // call it last, because it removes the BlockEntity
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
            MenuType<RainbowCrateContainer> cType = IForgeMenuType.create(
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