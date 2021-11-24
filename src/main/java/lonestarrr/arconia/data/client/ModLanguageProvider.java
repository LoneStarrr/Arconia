package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data gen some default en_US translations, mostly for rainbow-color blocks and items
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen, Arconia.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        registerRainbowBlocks();
        registerRainbowItems();
        registerModBlocks();
        registerModItems();
        registerAdvancements();
        registerMisc();
    }

    private void registerRainbowBlocks() {
        // Also register items for anything that has both a block and item representation
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModBlocks.getArconiumBlock(color), colorName + " Arconium Block");
            add(ModBlocks.getMoneyTreeLeaves(color), colorName + " Resource Tree Leaves");
            add(ModBlocks.getMoneyTreeSapling(color), colorName + " Resource Tree Sapling");
            add(ModBlocks.getResourceTreeRootBlock(color), colorName + " Tree Root Block");
            add(ModBlocks.getRainbowCrateBlock(color), colorName + " Rainbow Crate");
        }
    }

    private void registerRainbowItems() {
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModItems.getArconiumEssence(color), colorName + " Arconium Essence");
            add(ModItems.getArconiumIngot(color), colorName + " Arconium Ingot");
            add(ModItems.getColoredRoot(color), colorName + " Tree Root");
        }
    }

    private void registerModBlocks() {
        add(ModBlocks.clover, "Clover Plant");
        add(ModBlocks.pot, "Pot");
        add(ModBlocks.orb, "Orb");
        add(ModBlocks.hat, "Leprechaun Hat");
        add(ModBlocks.pedestal, "Pedestal");
        add(ModBlocks.centerPedestal, "Center Pedestal");
    }

    private void registerModItems() {
        add(ModItems.threeLeafClover, "Three Leaf Clover");
        add(ModItems.fourLeafClover, "Four Leaf Clover");
        add(ModItems.goldCoin, "Gold Coin");
        add(ModItems.cloverStaff, "Clover Staff");
        add(ModItems.magicInABottle, "Magic In A Bottle");
        add(ModItems.magicInABottle.getTranslationKey() + ".tooltip", "The Magic Luck-o-Meter indicates it's at %d%%");
    }

    private void registerAdvancements() {
        Stream.of(new String[][] {
                { "advancement.arconia.main.root.title", "Arconia" },
                { "advancement.arconia.main.root.desc", "Punch a defenseless clover plant" },
                { "advancement.arconia.main.four_leaf_clover.title", "Luck is on your side" },
                { "advancement.arconia.main.four_leaf_clover.desc", "Punch clover plants until you get lucky" },
                { "advancement.arconia.main.clover_staff.title", "You're a wizard now" },
                { "advancement.arconia.main.clover_staff.desc", "Make a clover staff to increase your luck punching clovers" },
                { "advancement.arconia.main.tree_root_block.title", "I'm rooting for you" },
                { "advancement.arconia.main.tree_root_block.desc", "I wonder what happens if you put this under one of those red trees" },
                { "advancement.arconia.main.pedestal.title", "Oh no, yet another altar" },
                { "advancement.arconia.main.pedestal.desc", "Setup an altar to perform colorful rituals" },
                { "advancement.arconia.main.red_root_of_essence.title", "Arconium?" },
                { "advancement.arconia.main.red_root_of_essence.desc", "Imbue magical properties onto a tree root using the altar" },
                { "advancement.arconia.main.red_arconium_ingot.title", "The first of many" },
                { "advancement.arconia.main.red_arconium_ingot.desc", "Craft your first arconium ingot" },
                { "advancement.arconia.main.pot_of_gold.title", "Rainbow not included" },
                { "advancement.arconia.main.pot_of_gold.desc", "Build a pot of gold" },
        }).forEach(adv -> add(adv[0], adv[1]));
    }

    private void registerMisc() {
        add("jei.arconia.recipe_category.altar", "Altar");
        // label in crate GUI
        add("container.arconia.rainbow_crate", "Rainbow Crate");
    }
}
