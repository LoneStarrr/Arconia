package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Arconia.MOD_ID);
//    private static final Map<RainbowColor, RainbowCropBlock> rainbowCrops = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<ArconiumTreeLeaves>> arconiumTreeLeaves = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<ArconiumTreeSapling>> arconiumTreeSaplings = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<ArconiumBlock>> arconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<InfiniteGoldArconiumBlock>> infiniteGoldArconiumBlocks = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<RainbowGrassBlock>> rainbowGrassBlocks = new HashMap<>();

    public static RegistryObject<CloverBlock> clover = BLOCKS.register(BlockNames.CLOVER, () -> new CloverBlock());
    public static RegistryObject<Pedestal> pedestal = BLOCKS.register(BlockNames.PEDESTAL, () -> new Pedestal());
    public static RegistryObject<CenterPedestal> centerPedestal = BLOCKS.register(BlockNames.CENTER_PEDESTAL, CenterPedestal::new);
    public static RegistryObject<Hat> hat = BLOCKS.register(BlockNames.HAT, () -> new Hat());
    public static RegistryObject<WorldBuilder> worldBuilder = BLOCKS.register(BlockNames.WORLD_BUILDER, () -> new WorldBuilder());
    public static RegistryObject<PotMultiBlockPrimary> potMultiBlockPrimary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_PRIMARY, () -> new PotMultiBlockPrimary());
    public static RegistryObject<PotMultiBlockSecondary> potMultiBlockSecondary = BLOCKS.register(BlockNames.POT_MULTIBLOCK_SECONDARY, () -> new PotMultiBlockSecondary()); //no associated item

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

    public static void addToCreativeTabs(CreativeModeTabEvent.BuildContents event) {
        // Not adding items to creative tabs makes them undiscoverable in creative mode, even with JEI
        if (event.getTab() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(pedestal);
            event.accept(centerPedestal);
            event.accept(worldBuilder);
            event.accept(hat);
        }
        else if (event.getTab() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(clover);
            for (RainbowColor color: RainbowColor.values()) {
                event.accept(arconiumTreeLeaves.get(color));
                event.accept(arconiumTreeSaplings.get(color));
                event.accept(rainbowGrassBlocks.get(color));
            }
        } else if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS) {
            for (RainbowColor color: RainbowColor.values()) {
                event.accept(arconiumBlocks.get(color));
                event.accept(infiniteGoldArconiumBlocks.get(color));
            }
        }
    }
    public static RegistryObject<ArconiumBlock> getArconiumBlock(RainbowColor tier) { return arconiumBlocks.get(tier); }

    public static RegistryObject<InfiniteGoldArconiumBlock> getInfiniteGoldArconiumBlock(RainbowColor tier) { return infiniteGoldArconiumBlocks.get(tier); }

    public static RegistryObject<ArconiumTreeSapling> getArconiumTreeSapling(RainbowColor tier) {
        return arconiumTreeSaplings.get(tier);
    }

    public static RegistryObject<ArconiumTreeLeaves> getArconiumTreeLeaves(RainbowColor tier) {
        return arconiumTreeLeaves.get(tier);
    }

    public static RegistryObject<RainbowGrassBlock> getRainbowGrassBlock(RainbowColor tier) { return rainbowGrassBlocks.get(tier); }

    public static void registerBlockItem(RegistryObject<? extends Block> block, Item.Properties builder) {
        ModItems.ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), builder));
    }
}