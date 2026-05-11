package lonestarrr.arconia.common.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.ItemNames;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arconia.MOD_ID);

  public static final DeferredItem<CloverStaff> cloverStaff =
      ITEMS.registerItem(ItemNames.CLOVER_STAFF, props -> new CloverStaff(props.stacksTo(1)));
  public static final DeferredItem<Item> fourLeafClover =
      ITEMS.registerSimpleItem(ItemNames.FOUR_LEAF_CLOVER);
  public static final DeferredItem<Item> threeLeafClover =
      ITEMS.registerSimpleItem(ItemNames.THREE_LEAF_CLOVER);
  public static final DeferredItem<MagicInABottle> magicInABottle =
      ITEMS.registerItem(ItemNames.MAGIC_IN_A_BOTTLE, MagicInABottle::new);

  private static final Map<RainbowColor, DeferredItem<ArconiumEssence>> arconiumEssences =
      new HashMap<>();
  private static final Map<RainbowColor, DeferredItem<ColoredBranch>> coloredBranches =
      new HashMap<>();
  private static final Map<RainbowColor, DeferredItem<Item>> arconiumIngots = new HashMap<>();

  private static final Map<RainbowColor, DeferredItem<HoeItem>> arconiumSickles = new HashMap<>();

  public static Item.Properties defaultBuilder() {
    return new Item.Properties();
  }

  static {
    for (RainbowColor tier : RainbowColor.values()) {
      arconiumEssences.put(
          tier,
          ITEMS.registerItem(
              tier.getTierName() + ItemNames.ARCONIUM_ESSENCE_SUFFIX,
              props -> new ArconiumEssence(props, tier)));
      coloredBranches.put(
          tier,
          ITEMS.registerItem(
              tier.getTierName() + ItemNames.COLORED_TREE_BRANCH_SUFFIX,
              props -> new ColoredBranch(props, tier)));
      arconiumIngots.put(
          tier, ITEMS.registerSimpleItem(tier.getTierName() + ItemNames.ARCONIUM_INGOT_SUFFIX));
    }

    // Arconium sickles. Numerical values: base attack modifier, attack speed modifier.
    registerSickle(RainbowColor.RED, props -> new HoeItem(ToolMaterial.WOOD, 4F, -2.1F, props));
    registerSickle(RainbowColor.ORANGE, props -> new HoeItem(ToolMaterial.STONE, 4F, -2.1F, props));
    registerSickle(RainbowColor.YELLOW, props -> new HoeItem(ToolMaterial.IRON, 4F, -2.1F, props));
    registerSickle(RainbowColor.GREEN, props -> new HoeItem(ToolMaterial.GOLD, 7F, -2.1F, props));
    registerSickle(
        RainbowColor.LIGHT_BLUE, props -> new HoeItem(ToolMaterial.DIAMOND, 5F, -2.1F, props));
    registerSickle(
        RainbowColor.BLUE,
        props -> new HoeItem(ToolMaterial.NETHERITE, 5F, -2.1F, props.fireResistant()));
    registerSickle(
        RainbowColor.PURPLE,
        props -> new HoeItem(ToolMaterial.NETHERITE, 6F, -2.1F, props.fireResistant()));
  }

  public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      event.accept(cloverStaff.get());
      event.accept(magicInABottle.get());
      for (RainbowColor color : RainbowColor.values()) {
        event.accept(arconiumSickles.get(color).get());
      }
    } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
      event.accept(fourLeafClover.get());
      event.accept(threeLeafClover.get());
      for (RainbowColor color : RainbowColor.values()) {
        event.accept(arconiumEssences.get(color).get());
        event.accept(coloredBranches.get(color).get());
        event.accept(arconiumIngots.get(color).get());
      }
    }
  }

  private static void registerSickle(
      RainbowColor tier, Function<Item.Properties, HoeItem> factory) {
    arconiumSickles.put(
        tier, ITEMS.registerItem(tier.getTierName() + ItemNames.SICKLE_SUFFIX, factory));
  }

  public static DeferredItem<ArconiumEssence> getArconiumEssence(RainbowColor tier) {
    return arconiumEssences.get(tier);
  }

  public static DeferredItem<Item> getArconiumIngot(RainbowColor tier) {
    return arconiumIngots.get(tier);
  }

  public static DeferredItem<HoeItem> getArconiumSickle(RainbowColor tier) {
    return arconiumSickles.get(tier);
  }

  public static DeferredItem<ColoredBranch> getColoredBranch(RainbowColor tier) {
    return coloredBranches.get(tier);
  }
}
