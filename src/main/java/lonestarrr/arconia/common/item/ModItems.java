package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arconia.MOD_ID);

    public static final DeferredItem<Item> cloverStaff = ITEMS.register(ItemNames.CLOVER_STAFF, () -> new CloverStaff(defaultBuilder().stacksTo(1)));
    public static final DeferredItem<Item> fourLeafClover = ITEMS.registerSimpleItem(ItemNames.FOUR_LEAF_CLOVER);
    public static final DeferredItem<Item> threeLeafClover = ITEMS.registerSimpleItem(ItemNames.THREE_LEAF_CLOVER);
    public static final DeferredItem<Item> magicInABottle = ITEMS.register(ItemNames.MAGIC_IN_A_BOTTLE, () -> new MagicInABottle(defaultBuilder()));

    private static final Map<RainbowColor, DeferredItem<ArconiumEssence>> arconiumEssences = new HashMap<>();
    private static final Map<RainbowColor, DeferredItem<ColoredRoot>> coloredRoots = new HashMap<>();
    private static final Map<RainbowColor, DeferredItem<Item>> arconiumIngots = new HashMap<>();

    private static final Map<RainbowColor, DeferredItem<Item>> arconiumSickles = new HashMap<>();

    public static Item.Properties defaultBuilder() {
        return new Item.Properties();
    }

    static {
        // Register stand-alone items, not associated with a block - those go into ModBlocks
        final Item.Properties builder = defaultBuilder();

        for (RainbowColor tier: RainbowColor.values()) {
            arconiumEssences.put(tier, ITEMS.register(tier.getTierName() + ItemNames.ARCONIUM_ESSENCE_SUFFIX, () -> new ArconiumEssence(builder, tier)));
            coloredRoots.put(tier, ITEMS.register(tier.getTierName() + ItemNames.COLORED_TREE_ROOT_SUFFIX, () -> new ColoredRoot(builder, tier)));
            arconiumIngots.put(tier, ITEMS.registerSimpleItem(tier.getTierName() + ItemNames.ARCONIUM_INGOT_SUFFIX));
        }

        // Arconium sickles. Numerical values: base attack modifier, attack speed modifier.
        registerSickle(RainbowColor.RED, () -> new HoeItem(Tiers.WOOD, 4, -2.1F, builder));
        registerSickle(RainbowColor.ORANGE, () -> new HoeItem(Tiers.STONE, 4, -2.1F, builder));
        registerSickle(RainbowColor.YELLOW, () -> new HoeItem(Tiers.IRON, 4, -2.1F, builder));
        registerSickle(RainbowColor.GREEN, () -> new HoeItem(Tiers.GOLD, 7, -2.1F, builder));
        registerSickle(RainbowColor.LIGHT_BLUE, () -> new HoeItem(Tiers.DIAMOND, 5, -2.1F, builder));
        registerSickle(RainbowColor.BLUE, () -> new HoeItem(Tiers.NETHERITE, 5, -2.1F, builder.fireResistant()));
        registerSickle(RainbowColor.PURPLE, () -> new HoeItem(Tiers.NETHERITE, 6, -2.1F, builder.fireResistant()));
    }

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(cloverStaff.get());
            event.accept(magicInABottle.get());
            for (RainbowColor color: RainbowColor.values()) {
                // TODO acceptAll values()?
                event.accept(arconiumSickles.get(color).get());
            }
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(fourLeafClover.get());
            event.accept(threeLeafClover.get());
            for (RainbowColor color : RainbowColor.values()) {
                event.accept(arconiumEssences.get(color).get());
                event.accept(coloredRoots.get(color).get());
                event.accept(arconiumIngots.get(color).get());
            }
        }
    }
        private static void registerSickle(RainbowColor tier, Supplier<HoeItem> hoe) {
        arconiumSickles.put(tier, ITEMS.register(tier.getTierName() + ItemNames.SICKLE_SUFFIX, hoe));
    }

    public static DeferredItem<ArconiumEssence> getArconiumEssence(RainbowColor tier) {
        return arconiumEssences.get(tier);
    }

    public static DeferredItem<Item> getArconiumIngot(RainbowColor tier) {
        return arconiumIngots.get(tier);
    }

    public static DeferredItem<Item> getArconiumSickle(RainbowColor tier) { return arconiumSickles.get(tier); }

    public static DeferredItem<ColoredRoot> getColoredRoot(RainbowColor tier) {
        return coloredRoots.get(tier);
    }
}