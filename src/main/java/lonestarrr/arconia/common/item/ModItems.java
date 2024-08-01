package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Arconia.MOD_ID);
    public static RegistryObject<Item> goldCoin;
    public static RegistryObject<Item> cloverStaff;
    public static RegistryObject<Item> fourLeafClover;
    public static RegistryObject<Item> threeLeafClover;
    public static RegistryObject<Item> magicInABottle;

    private static final Map<RainbowColor, RegistryObject<Item>> arconiumEssences = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<ColoredRoot>> coloredRoots = new HashMap<>();
    private static final Map<RainbowColor, RegistryObject<Item>> arconiumIngots = new HashMap<>();

    private static final Map<RainbowColor, RegistryObject<Item>> arconiumSickles = new HashMap<>();

    public static Item.Properties defaultBuilder() {
        return new Item.Properties();
    }

    static {
        // Register stand-alone items, not associated with a block - those go into ModBlocks
        Item.Properties builder;

        builder = defaultBuilder();

        goldCoin = ITEMS.register(ItemNames.GOLD_COIN, () -> new Item(defaultBuilder()));
        cloverStaff = ITEMS.register(ItemNames.CLOVER_STAFF, () -> new CloverStaff(defaultBuilder().stacksTo(1)));
        fourLeafClover = ITEMS.register(ItemNames.FOUR_LEAF_CLOVER, () -> new Item(defaultBuilder()));
        threeLeafClover = ITEMS.register(ItemNames.THREE_LEAF_CLOVER, () -> new Item(defaultBuilder()));
        magicInABottle = ITEMS.register(ItemNames.MAGIC_IN_A_BOTTLE, () -> new MagicInABottle(defaultBuilder()));

        for (RainbowColor tier: RainbowColor.values()) {
            arconiumEssences.put(tier, ITEMS.register(tier.getTierName() + ItemNames.ARCONIUM_ESSENCE_SUFFIX, () -> new Item(builder)));
            coloredRoots.put(tier, ITEMS.register(tier.getTierName() + ItemNames.COLORED_TREE_ROOT_SUFFIX, () -> new ColoredRoot(builder, tier)));
            arconiumIngots.put(tier, ITEMS.register(tier.getTierName() + ItemNames.ARCONIUM_INGOT_SUFFIX, () -> new Item(builder)));
        }

        // Arconium sickles. Numerical values: base attack modifier, attack speed modifier.
        registerSickle(RainbowColor.RED, () -> new HoeItem(Tiers.WOOD, 4, -2.1F, (new Item.Properties())));
        registerSickle(RainbowColor.ORANGE, () -> new HoeItem(Tiers.STONE, 4, -2.1F, (new Item.Properties())));
        registerSickle(RainbowColor.YELLOW, () -> new HoeItem(Tiers.IRON, 4, -2.1F, (new Item.Properties())));
        registerSickle(RainbowColor.GREEN, () -> new HoeItem(Tiers.GOLD, 7, -2.1F, (new Item.Properties())));
        registerSickle(RainbowColor.LIGHT_BLUE, () -> new HoeItem(Tiers.DIAMOND, 5, -2.1F, (new Item.Properties())));
        registerSickle(RainbowColor.BLUE, () -> new HoeItem(Tiers.NETHERITE, 5, -2.1F, (new Item.Properties()).fireResistant()));
        registerSickle(RainbowColor.PURPLE, () -> new HoeItem(Tiers.NETHERITE, 6, -2.1F, (new Item.Properties()).fireResistant()));
    }

    private static void registerSickle(RainbowColor tier, Supplier<HoeItem> hoe) {
        arconiumSickles.put(tier, ITEMS.register(tier.getTierName() + ItemNames.SICKLE_SUFFIX, hoe));
    }

    public static final RegistryObject<Item> getArconiumEssence(RainbowColor tier) {
        return arconiumEssences.get(tier);
    }

    public static final RegistryObject<Item> getArconiumIngot(RainbowColor tier) {
        return arconiumIngots.get(tier);
    }

    public static final RegistryObject<Item> getArconiumSickle(RainbowColor tier) { return arconiumSickles.get(tier); }

    public static final RegistryObject<ColoredRoot> getColoredRoot(RainbowColor tier) {
        return coloredRoots.get(tier);
    }
}