package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Arconia.MOD_ID);
//    private static final Map<RainbowColor, RainbowCropBlock> rainbowCrops = new HashMap<>();
    private static final Map<RainbowColor, Supplier<ArconiumTreeLeaves>> arconiumTreeLeaves = new HashMap<>();
    private static final Map<RainbowColor, Supplier<ArconiumTreeSapling>> arconiumTreeSaplings = new HashMap<>();
    private static final Map<RainbowColor, Supplier<ArconiumBlock>> arconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, Supplier<InfiniteGoldArconiumBlock>> infiniteGoldArconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, Supplier<RainbowGrassBlock>> rainbowGrassBlocks = new HashMap<>();

    public static Supplier<CloverBlock> clover = BLOCKS.register(BlockNames.CLOVER, CloverBlock::new);
    public static Supplier<Pedestal> pedestal = BLOCKS.register(BlockNames.PEDESTAL, () -> new Pedestal());
    public static Supplier<CenterPedestal> centerPedestal = BLOCKS.register(BlockNames.CENTER_PEDESTAL, CenterPedestal::new);
    public static Supplier<Hat> hat = BLOCKS.register(BlockNames.HAT, () -> new Hat());
    public static Supplier<WorldBuilder> worldBuilder = BLOCKS.register(BlockNames.WORLD_BUILDER, () -> new WorldBuilder());
    public static Supplier<PotMultiBlockPrimary> potMultiBlockPrimary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_PRIMARY, () -> new PotMultiBlockPrimary());
    public static Supplier<PotMultiBlockSecondary> potMultiBlockSecondary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_SECONDARY, () -> new PotMultiBlockSecondary()); //no associated item

    static {
        // RainbowColor tiered colorBlocks
        for (RainbowColor color: RainbowColor.values()) {
            arconiumTreeLeaves.put(color, BLOCKS.register(color.getTierName() + BlockNames.LEAVES_SUFFIX, () -> new ArconiumTreeLeaves(color)));
            arconiumTreeSaplings.put(color, BLOCKS.register(color.getTierName() + BlockNames.SAPLING_SUFFIX, () -> new ArconiumTreeSapling(color)));
            arconiumBlocks.put(color, BLOCKS.register(color.getTierName() + BlockNames.ARCONIUM_BLOCK_SUFFIX, () -> new ArconiumBlock(color)));
            infiniteGoldArconiumBlocks.put(color, BLOCKS.register(color.getTierName() + BlockNames.INFINITE_GOLD_ARCONIUM_BLOCK_SUFFIX, () -> new InfiniteGoldArconiumBlock(color)));
            rainbowGrassBlocks.put(color, BLOCKS.register(color.getTierName() + BlockNames.RAINBOW_GRASS_BLOCK_SUFFIX, () -> new RainbowGrassBlock(color)));
        }

        registerItemBlocks();
    }

    public static void registerItemBlocks() {
        // TODO create my own creative tab, e.g. https://github.com/Vazkii/Botania/blob/1.15/src/main/java/vazkii/botania/common/core/BotaniaCreativeTab.java
        Item.Properties builder = ModItems.defaultBuilder();

        registerBlockItem(clover, builder);
        registerBlockItem(pedestal, builder);
        registerBlockItem(centerPedestal, builder);
        registerBlockItem(hat, builder);
        registerBlockItem(worldBuilder, builder);

        arconiumTreeSaplings.values().stream().forEach(b -> registerBlockItem(b, builder));
        arconiumTreeLeaves.values().stream().forEach(b -> registerBlockItem(b, builder));
        arconiumBlocks.values().stream().forEach(b -> registerBlockItem(b, builder));
        infiniteGoldArconiumBlocks.values().stream().forEach(b -> registerBlockItem(b, builder));
        rainbowGrassBlocks.values().stream().forEach(b -> registerBlockItem(b, builder));
    }

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Not adding items to creative tabs makes them undiscoverable in creative mode, even with JEI
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(pedestal.get());
            event.accept(centerPedestal.get());
            event.accept(worldBuilder.get());
            event.accept(hat.get());
        }
        else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(clover.get());
            for (RainbowColor color: RainbowColor.values()) {
                event.accept(arconiumTreeLeaves.get(color).get());
                event.accept(arconiumTreeSaplings.get(color).get());
                event.accept(rainbowGrassBlocks.get(color).get());
            }
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (RainbowColor color: RainbowColor.values()) {
                event.accept(arconiumBlocks.get(color).get());
                event.accept(infiniteGoldArconiumBlocks.get(color).get());
            }
        }
    }
    public static Supplier<ArconiumBlock> getArconiumBlock(RainbowColor tier) { return arconiumBlocks.get(tier); }

    public static Supplier<InfiniteGoldArconiumBlock> getInfiniteGoldArconiumBlock(RainbowColor tier) { return infiniteGoldArconiumBlocks.get(tier); }

    public static Supplier<ArconiumTreeSapling> getArconiumTreeSapling(RainbowColor tier) {
        return arconiumTreeSaplings.get(tier);
    }

    public static Supplier<ArconiumTreeLeaves> getArconiumTreeLeaves(RainbowColor tier) {
        return arconiumTreeLeaves.get(tier);
    }

    public static Supplier<RainbowGrassBlock> getRainbowGrassBlock(RainbowColor tier) { return rainbowGrassBlocks.get(tier); }

    public static void registerBlockItem(Supplier<? extends Block> block, Item.Properties builder) {
        ModItems.ITEMS.register(BLOCKS.getRegistry().get().getKey(block.get()).getPath(), () -> new BlockItem(block.get(), builder));
    }
}