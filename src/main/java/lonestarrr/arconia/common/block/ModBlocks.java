package lonestarrr.arconia.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.BlockNames;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Arconia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
//    private static final Map<RainbowColor, RainbowCropBlock> rainbowCrops = new HashMap<>();
    private static final Map<RainbowColor, RainbowCrateBlock> rainbowCrates = new HashMap<>();
    private static final Map<RainbowColor, ResourceTreeLeaves> resourceTreeLeaves = new HashMap<>();
    private static final Map<RainbowColor, ResourceTreeSapling> resourceTreeSaplings = new HashMap<>();
    private static final Map<RainbowColor, ResourceTreeRootBlock> treeRootBlocks = new HashMap<>();
    private static final Map<RainbowColor, ArconiumBlock> arconiumBlocks = new HashMap<>();

    public static final CloverBlock clover = new CloverBlock();
    public static final PotBlock pot = new PotBlock();
    public static final ResourceGenBlock resourceGenBlock = new ResourceGenBlock();
    public static final Pedestal pedestal = new Pedestal();
    public static final CenterPedestal centerPedestal = new CenterPedestal();
    public static final Orb orb = new Orb();
    public static final PotMultiBlockPrimary potMultiBlockPrimary = new PotMultiBlockPrimary(); //no associated item
    public static final PotMultiBlockSecondary potMultiBlockSecondary = new PotMultiBlockSecondary(); //no associated item

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();

        register(r, clover, BlockNames.CLOVER);
        register(r, pot, BlockNames.POT);
        register(r, resourceGenBlock, BlockNames.RESOURCEGEN_BLOCK);
        register(r, pedestal, BlockNames.PEDESTAL);
        register(r, centerPedestal, BlockNames.CENTER_PEDESTAL);
        register(r, orb, BlockNames.ORB);
        register(r, potMultiBlockPrimary, BlockNames.POT_MULTIBLOCK_PRIMARY);
        register(r, potMultiBlockSecondary, BlockNames.POT_MULTIBLOCK_SECONDARY);

        // RainbowColor tiered colorBlocks
        for (RainbowColor color: RainbowColor.values()) {
            RainbowCrateBlock crate = new RainbowCrateBlock(color);
            register(r, crate, color.getTierName() + BlockNames.RAINBOW_CRATE_SUFFIX);
            rainbowCrates.put(color, crate);

            ResourceTreeRootBlock resourceTreeRootBlock = new ResourceTreeRootBlock(color);
            register(r, resourceTreeRootBlock, color.getTierName() + BlockNames.TREE_ROOT_BLOCK_SUFFIX);
            treeRootBlocks.put(color, resourceTreeRootBlock);

            ResourceTreeLeaves leaves = new ResourceTreeLeaves(color);
            register(r, leaves, color.getTierName() + BlockNames.LEAVES_SUFFIX);
            resourceTreeLeaves.put(color, leaves);

            ResourceTreeSapling sapling = new ResourceTreeSapling(color);
            register(r, sapling, color.getTierName() + BlockNames.SAPLING_SUFFIX);
            resourceTreeSaplings.put(color, sapling);

            // TODO decide on the fate of crops. They are not dynamically colored and they ugly
//            RainbowCropBlock crop = new RainbowCropBlock(color);
//            register(r, crop, color.getTierName() + BlockNames.RAINBOW_CROP_SUFFIX);
//            rainbowCrops.put(color, crop);

            ArconiumBlock arconiumBlock = new ArconiumBlock(color);
            register(r, arconiumBlock, color.getTierName() + BlockNames.ARCONIUM_BLOCK_SUFFIX);
            arconiumBlocks.put(color, arconiumBlock);
        }
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();
        // TODO create my own creative tab, e.g. https://github.com/Vazkii/Botania/blob/1.15/src/main/java/vazkii/botania/common/core/BotaniaCreativeTab.java
        Item.Properties builder = ModItems.defaultBuilder();

        registerBlockItem(r, clover, builder);
        registerBlockItem(r, pot, builder);
        registerBlockItem(r, resourceGenBlock, builder);
        registerBlockItem(r, pedestal, builder);
        registerBlockItem(r, centerPedestal, builder);
        registerBlockItem(r, orb, builder); // TODO replace me with actual item?



        for (ResourceTreeRootBlock root: treeRootBlocks.values()) {
            registerBlockItem(r, root, builder);
        }

        for (RainbowCrateBlock crate: rainbowCrates.values()) {
            registerBlockItem(r, crate, builder);
        }

        for (ResourceTreeSapling sapling: resourceTreeSaplings.values()) {
            registerBlockItem(r, sapling, builder);
        }

        for (ResourceTreeLeaves leaves: resourceTreeLeaves.values()) {
            registerBlockItem(r, leaves, builder);
        }

        for (ArconiumBlock arconiumBlock: arconiumBlocks.values()) {
            registerBlockItem(r, arconiumBlock, builder);
        }
    }

//    public static RainbowCropBlock getRainbowCrop(RainbowColor tier) {
//        return rainbowCrops.get(tier);
//    }

    public static ArconiumBlock getArconiumBlock(RainbowColor tier) { return arconiumBlocks.get(tier); }

    public static RainbowCrateBlock getRainbowCrateBlock(RainbowColor tier) {
        return rainbowCrates.get(tier);
    }

    public static ResourceTreeSapling getMoneyTreeSapling(RainbowColor tier) {
        return resourceTreeSaplings.get(tier);
    }

    public static ResourceTreeLeaves getMoneyTreeLeaves(RainbowColor tier) {
        return resourceTreeLeaves.get(tier);
    }

    public static ResourceTreeRootBlock getResourceTreeRootBlock(RainbowColor tier) {
        return treeRootBlocks.get(tier);
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