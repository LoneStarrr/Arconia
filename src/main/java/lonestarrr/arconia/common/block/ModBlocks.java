package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Arconia.MOD_ID);
    private static final Map<RainbowColor, DeferredBlock<ArconiumTreeLeaves>> arconiumTreeLeaves = new HashMap<>();
    private static final Map<RainbowColor, DeferredBlock<ArconiumTreeSapling>> arconiumTreeSaplings = new HashMap<>();
    private static final Map<RainbowColor, DeferredBlock<ArconiumBlock>> arconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, DeferredBlock<RainbowGrassBlock>> rainbowGrassBlocks = new HashMap<>();

    public static final DeferredBlock<CloverBlock> clover = BLOCKS.register(BlockNames.CLOVER, CloverBlock::new);
    public static DeferredBlock<Pedestal> pedestal = BLOCKS.register(BlockNames.PEDESTAL, Pedestal::new);
    public static DeferredBlock<CenterPedestal> centerPedestal = BLOCKS.register(BlockNames.CENTER_PEDESTAL, CenterPedestal::new);
    public static DeferredBlock<Hat> hat = BLOCKS.register(BlockNames.HAT, Hat::new);
    public static DeferredBlock<WorldBuilder> worldBuilder = BLOCKS.register(BlockNames.WORLD_BUILDER, WorldBuilder::new);
    public static DeferredBlock<PotMultiBlockPrimary> potMultiBlockPrimary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_PRIMARY, PotMultiBlockPrimary::new);
    public static DeferredBlock<PotMultiBlockSecondary> potMultiBlockSecondary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_SECONDARY, PotMultiBlockSecondary::new); //no associated item

    static {
        // RainbowColor tiered colorBlocks
        for (RainbowColor color: RainbowColor.values()) {
            arconiumTreeLeaves.put(color, BLOCKS.register(color.getTierName() + BlockNames.LEAVES_SUFFIX, () -> new ArconiumTreeLeaves(color)));
            arconiumTreeSaplings.put(color, BLOCKS.register(color.getTierName() + BlockNames.SAPLING_SUFFIX, () -> new ArconiumTreeSapling(color)));
            arconiumBlocks.put(color, BLOCKS.register(color.getTierName() + BlockNames.ARCONIUM_BLOCK_SUFFIX, () -> new ArconiumBlock(color)));
            rainbowGrassBlocks.put(color, BLOCKS.register(color.getTierName() + BlockNames.RAINBOW_GRASS_BLOCK_SUFFIX, () -> new RainbowGrassBlock(color)));
        }

        registerBlockItems();
    }

    public static void registerBlockItems() {
        ModItems.ITEMS.registerSimpleBlockItem(clover);
        ModItems.ITEMS.registerSimpleBlockItem(pedestal);
        ModItems.ITEMS.registerSimpleBlockItem(centerPedestal);
        ModItems.ITEMS.registerSimpleBlockItem(hat);
        ModItems.ITEMS.registerSimpleBlockItem(worldBuilder);


        arconiumTreeSaplings.values().forEach(ModItems.ITEMS::registerSimpleBlockItem);
        arconiumTreeLeaves.values().forEach(ModItems.ITEMS::registerSimpleBlockItem);
        arconiumBlocks.values().forEach(ModItems.ITEMS::registerSimpleBlockItem);
        rainbowGrassBlocks.values().forEach(ModItems.ITEMS::registerSimpleBlockItem);
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
            }
        }
    }
    public static DeferredBlock<ArconiumBlock> getArconiumBlock(RainbowColor tier) { return arconiumBlocks.get(tier); }

    public static DeferredBlock<ArconiumTreeSapling> getArconiumTreeSapling(RainbowColor tier) {
        return arconiumTreeSaplings.get(tier);
    }

    public static DeferredBlock<ArconiumTreeLeaves> getArconiumTreeLeaves(RainbowColor tier) {
        return arconiumTreeLeaves.get(tier);
    }

    public static DeferredBlock<RainbowGrassBlock> getRainbowGrassBlock(RainbowColor tier) { return rainbowGrassBlocks.get(tier); }
}