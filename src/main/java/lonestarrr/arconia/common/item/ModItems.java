package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

import static lonestarrr.arconia.common.block.ModBlocks.register;

public final class ModItems {
    public static Item goldCoin;
    public static Item cloverStaff;
    public static Item fourLeafClover;
    public static Item threeLeafClover;
    public static Item magicInABottle;

    private static final Map<RainbowColor, Item> arconiumEssences = new HashMap<>();
    private static final Map<RainbowColor, ColoredRoot> coloredRoots = new HashMap<>();
    private static final Map<RainbowColor, Item> arconiumIngots = new HashMap<>();

    private static final Map<RainbowColor, Item> arconiumSickles = new HashMap<>();

    public static Item.Properties defaultBuilder() {
        // TODO make my own creative tab. With blackjack, and hookers.
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }

    public static void registerItems(RegistryEvent.Register<Item> evt) {
        // Register stand-alone items, not associated with a block - those go into ModBlocks
        IForgeRegistry<Item> r = evt.getRegistry();

        Item.Properties builder;

        builder = defaultBuilder();

        // TODO port to DeferredRegistry

        goldCoin = new Item(defaultBuilder());
        register(r, goldCoin, ItemNames.GOLD_COIN);

        cloverStaff = new CloverStaff(defaultBuilder().stacksTo(1));
        register(r, cloverStaff, ItemNames.CLOVER_STAFF);

        fourLeafClover = new Item(defaultBuilder());
        register(r, fourLeafClover, ItemNames.FOUR_LEAF_CLOVER);

        threeLeafClover = new Item(defaultBuilder());
        register(r, threeLeafClover, ItemNames.THREE_LEAF_CLOVER);

        magicInABottle = new MagicInABottle(defaultBuilder());
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
        }

        // Arconium sickles. Numerical values: base attack modifier, attack speed modifier.
        registerSickle(r, RainbowColor.RED, new HoeItem(Tiers.WOOD, 4, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));
        registerSickle(r, RainbowColor.ORANGE, new HoeItem(Tiers.STONE, 4, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));
        registerSickle(r, RainbowColor.YELLOW, new HoeItem(Tiers.IRON, 4, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));
        registerSickle(r, RainbowColor.GREEN, new HoeItem(Tiers.GOLD, 7, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));
        registerSickle(r, RainbowColor.LIGHT_BLUE, new HoeItem(Tiers.DIAMOND, 5, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));
        registerSickle(r, RainbowColor.BLUE, new HoeItem(Tiers.NETHERITE, 5, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS).fireResistant()));
        registerSickle(r, RainbowColor.PURPLE, new HoeItem(Tiers.NETHERITE, 6, -2.1F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS).fireResistant()));
    }

    private static void registerSickle(IForgeRegistry<Item> r, RainbowColor tier, HoeItem hoe) {
        arconiumSickles.put(tier, hoe);
        register(r, hoe, tier.getTierName() + ItemNames.SICKLE_SUFFIX);
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

    public static final Item getArconiumSickle(RainbowColor tier) { return arconiumSickles.get(tier); }

    public static final ColoredRoot getColoredRoot(RainbowColor tier) {
        return coloredRoots.get(tier);
    }
}