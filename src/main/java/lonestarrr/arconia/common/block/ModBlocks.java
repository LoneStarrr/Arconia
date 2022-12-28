package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModBlocks {
//    private static final Map<RainbowColor, RainbowCropBlock> rainbowCrops = new HashMap<>();
    private static final Map<RainbowColor, ArconiumTreeLeaves> arconiumTreeLeaves = new HashMap<>();
    private static final Map<RainbowColor, ArconiumTreeSapling> arconiumTreeSaplings = new HashMap<>();
    private static final Map<RainbowColor, ArconiumBlock> arconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, InfiniteGoldArconiumBlock> infiniteGoldArconiumBlocks = new HashMap<>();

    public static CloverBlock clover;
    public static Pedestal pedestal;
    public static CenterPedestal centerPedestal;
    public static Hat hat;
    public static WorldBuilder worldBuilder;
    public static PotMultiBlockPrimary potMultiBlockPrimary; //no associated item
    public static PotMultiBlockSecondary potMultiBlockSecondary; //no associated item

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();

        // TODO port to DeferredRegistry
        clover = new CloverBlock();
        register(r, clover, BlockNames.CLOVER);

        pedestal = new Pedestal();
        register(r, pedestal, BlockNames.PEDESTAL);

        centerPedestal = new CenterPedestal();
        register(r, centerPedestal, BlockNames.CENTER_PEDESTAL);

        hat = new Hat();
        register(r, hat, BlockNames.HAT);

        worldBuilder = new WorldBuilder();
        register(r, worldBuilder, BlockNames.WORLD_BUILDER);

        potMultiBlockPrimary = new PotMultiBlockPrimary();
        register(r, potMultiBlockPrimary, BlockNames.POT_MULTIBLOCK_PRIMARY);

        potMultiBlockSecondary = new PotMultiBlockSecondary();
        register(r, potMultiBlockSecondary, BlockNames.POT_MULTIBLOCK_SECONDARY);

        // RainbowColor tiered colorBlocks
        for (RainbowColor color: RainbowColor.values()) {
            ArconiumTreeLeaves leaves = new ArconiumTreeLeaves(color);
            register(r, leaves, color.getTierName() + BlockNames.LEAVES_SUFFIX);
            arconiumTreeLeaves.put(color, leaves);

            ArconiumTreeSapling sapling = new ArconiumTreeSapling(color);
            register(r, sapling, color.getTierName() + BlockNames.SAPLING_SUFFIX);
            arconiumTreeSaplings.put(color, sapling);

            ArconiumBlock arconiumBlock = new ArconiumBlock(color);
            register(r, arconiumBlock, color.getTierName() + BlockNames.ARCONIUM_BLOCK_SUFFIX);
            arconiumBlocks.put(color, arconiumBlock);

            InfiniteGoldArconiumBlock infiniteGoldArconiumBlock = new InfiniteGoldArconiumBlock(color);
            register(r, infiniteGoldArconiumBlock, color.getTierName() + BlockNames.INFINITE_GOLD_ARCONIUM_BLOCK_SUFFIX);
            infiniteGoldArconiumBlocks.put(color, infiniteGoldArconiumBlock);
        }
    }

    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();
        // TODO create my own creative tab, e.g. https://github.com/Vazkii/Botania/blob/1.15/src/main/java/vazkii/botania/common/core/BotaniaCreativeTab.java
        Item.Properties builder = ModItems.defaultBuilder();

        registerBlockItem(r, clover, builder);
        registerBlockItem(r, pedestal, builder);
        registerBlockItem(r, centerPedestal, builder);
        registerBlockItem(r, hat, builder);
        registerBlockItem(r, worldBuilder, builder);

        arconiumTreeSaplings.values().stream().forEach(b -> registerBlockItem(r, b, builder));
        arconiumTreeLeaves.values().stream().forEach(b -> registerBlockItem(r, b, builder));
        arconiumBlocks.values().stream().forEach(b -> registerBlockItem(r, b, builder));
        infiniteGoldArconiumBlocks.values().stream().forEach(b -> registerBlockItem(r, b, builder));
    }

//    public static RainbowCropBlock getRainbowCrop(RainbowColor tier) {
//        return rainbowCrops.get(tier);
//    }

    public static ArconiumBlock getArconiumBlock(RainbowColor tier) { return arconiumBlocks.get(tier); }

    public static InfiniteGoldArconiumBlock getInfiniteGoldArconiumBlock(RainbowColor tier) { return infiniteGoldArconiumBlocks.get(tier); }

    public static ArconiumTreeSapling getArconiumTreeSapling(RainbowColor tier) {
        return arconiumTreeSaplings.get(tier);
    }

    public static ArconiumTreeLeaves getArconiumTreeLeaves(RainbowColor tier) {
        return arconiumTreeLeaves.get(tier);
    }

    public static <V extends IForgeRegistryEntry<V>> void registerBlockItem(IForgeRegistry<Item> reg, Block block, Item.Properties builder) {
        register(reg, new BlockItem(block, builder), block.getRegistryName());
    }

    public static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, IForgeRegistryEntry<V> thing, ResourceLocation name) {
        reg.register(thing.setRegistryName(name));
    }

    public static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, IForgeRegistryEntry<V> thing, String name) {
        register(reg, thing, new ResourceLocation(Arconia.MOD_ID, name));
    }
}