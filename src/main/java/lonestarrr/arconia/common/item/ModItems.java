package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

public final class ModItems {
    public static final Item clover = new Item(defaultBuilder());
    public static final Item goldCoin = new Item(defaultBuilder());
    public static final Item cloverStaff = new CloverStaff(defaultBuilder().stacksTo(1));
    public static final Item fourLeafClover = new Item(defaultBuilder());
    public static final Item threeLeafClover = new Item(defaultBuilder());
    public static final Item magicInABottle = new MagicInABottle(defaultBuilder());

    private static final Map<RainbowColor, Item> arconiumEssences = new HashMap<>();
    private static final Map<RainbowColor, Item> rainbowSeeds = new HashMap<>();
    private static final Map<RainbowColor, ColoredRoot> coloredRoots = new HashMap<>();
    private static final Map<RainbowColor, Item> arconiumIngots = new HashMap<>();

    public static Item.Properties defaultBuilder() {
        // TODO make my own creative tab. With blackjack, and hookers.
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }

    public static void registerItems(RegistryEvent.Register<Item> evt) {
        // Register stand-alone items, not associated with a block - those go into ModBlocks
        IForgeRegistry<Item> r = evt.getRegistry();

        Item.Properties builder;

        builder = defaultBuilder();

        register(r, goldCoin, ItemNames.GOLD_COIN);
        register(r, cloverStaff, ItemNames.CLOVER_STAFF);
        register(r, fourLeafClover, ItemNames.FOUR_LEAF_CLOVER);
        register(r, threeLeafClover, ItemNames.THREE_LEAF_CLOVER);
        register(r, magicInABottle, ItemNames.MAGIC_IN_A_BOTTLE);

        for (RainbowColor tier: RainbowColor.values()) {
            Item essence = new Item(builder);
            register(r, essence, tier.getTierName() + ItemNames.ARCONIUM_ESSENCE_SUFFIX);
            arconiumEssences.put(tier, essence);

            ColoredRoot coloredRoot = new ColoredRoot(builder, tier);
            register(r, coloredRoot, tier.getTierName() + ItemNames.COLORED_TREE_ROOT_SUFFIX);
            coloredRoots.put(tier, coloredRoot);

            Item arconiumIngot = new Item(builder);
            register(r, arconiumIngot, tier.getTierName() + ItemNames.ARCONIUM_INGOT_SUFFIX);
            arconiumIngots.put(tier, arconiumIngot);

            // TODO decide the fate of seeds and crops
            // Seeds are a special case. They are created through BlockItem and thus associated with the crop.
//            RainbowCropBlock crop = ModBlocks.getRainbowCrop(tier);
//            Item seed = new BlockNamedItem(crop, builder);
//            register(r, seed, crop.getSeedResourceName());
//            rainbowSeeds.put(tier, seed);
        }
    }

//    public static final Item getRainbowSeed(RainbowColor tier) {
//        return rainbowSeeds.get(tier);
//    }

    public static final Item getArconiumEssence(RainbowColor tier) {
        return arconiumEssences.get(tier);
    }

    public static final Item getArconiumIngot(RainbowColor tier) {
        return arconiumIngots.get(tier);
    }

    public static final ColoredRoot getColoredRoot(RainbowColor tier) {
        return coloredRoots.get(tier);
    }

}