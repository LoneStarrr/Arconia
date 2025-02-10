package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.stream.Stream;

/**
 * Data gen some default en_US translations, mostly for rainbow-color blocks and items
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Arconia.MOD_ID, locale);
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
        // Also registers items for anything that has both a block and item representation
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModBlocks.getArconiumBlock(color).get(), colorName + " Arconium Block");
            add(ModBlocks.getArconiumTreeLeaves(color).get(), colorName + " Arconium Tree Leaves");
            add(ModBlocks.getArconiumTreeSapling(color).get(), colorName + " Arconium Tree Sapling");
            add(ModBlocks.getRainbowGrassBlock(color).get(), colorName + " Rainbow Grass Block");
        }
    }

    private void registerRainbowItems() {
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModItems.getArconiumEssence(color).get(), colorName + " Arconium Essence");
            add(ModItems.getArconiumIngot(color).get(), colorName + " Arconium Ingot");
            add(ModItems.getColoredRoot(color).get(), colorName + " Tree Root");
            add(ModItems.getArconiumSickle(color).get(), colorName + " Arconium Sickle");
        }
    }

    private void registerModBlocks() {
        add(ModBlocks.clover.get(), "Clover Plant");
        add(ModBlocks.hat.get(), "Leprechaun Hat");
        add(ModBlocks.worldBuilder.get(), "World Builder");
        add(ModBlocks.pedestal.get(), "Pedestal");
        add(ModBlocks.centerPedestal.get(), "Center Pedestal");
    }

    private void registerModItems() {
        add(ModItems.threeLeafClover.get(), "Three Leaf Clover");
        add(ModItems.fourLeafClover.get(), "Four Leaf Clover");
        add(ModItems.cloverStaff.get(), "Clover Staff");
        add(ModItems.magicInABottle.get(), "Magic In A Bottle");
        add(ModItems.magicInABottle.get().getDescriptionId() + ".tooltip", "The Magic Luck-o-Meter indicates it's at %d%%");
    }

    private void registerAdvancements() {
        Stream.of(new String[][] {
                { "advancement.arconia.main.root.title", "Arconia" },
                { "advancement.arconia.main.root.desc", "Dig up dirt and find some memoirs" },
                { "advancement.arconia.main.four_leaf_clover.title", "Get lucky" },
                { "advancement.arconia.main.four_leaf_clover.desc", "Find a four-leaf clover" },
                { "advancement.arconia.main.red_tree_root.title", "Arconium Tree Root" },
                { "advancement.arconia.main.red_tree_root.desc", "Harvest arconium trees for some roots" },
                { "advancement.arconia.main.clover_staff.title", "You're a wizard now" },
                { "advancement.arconia.main.clover_staff.desc", "Make a clover staff" },
                { "advancement.arconia.main.red_arconium_sickle.title", "Leaf breaker 9000" },
                { "advancement.arconia.main.red_arconium_sickle.desc", "Craft a red arconium sickle" },
                { "advancement.arconia.main.pedestal.title", "All mods need these" },
                { "advancement.arconia.main.pedestal.desc", "Craft some pedestals" },
                { "advancement.arconia.main.center_pedestal.title", "Let the rituals begin" },
                { "advancement.arconia.main.center_pedestal.desc", "Craft a center pedestal to perform rituals" },
                { "advancement.arconia.main.red_root_of_essence.title", "Arconium?" },
                { "advancement.arconia.main.red_root_of_essence.desc", "Imbue magical properties onto a tree root using the altar" },
                { "advancement.arconia.main.pot_of_gold.title", "This has pot-ential" },
                { "advancement.arconia.main.pot_of_gold.desc", "Build a pot of gold" },
                { "advancement.arconia.main.hat.title", "I've hat it with these puns" },
                { "advancement.arconia.main.hat.desc", "Craft a leprechaun hat" },
                { "advancement.arconia.main.red_arconium_essence.title", "Arconium Essence" },
                { "advancement.arconia.main.red_arconium_essence.desc", "Get your first red arconium essence" },
                { "advancement.arconia.main.red_arconium_ingot.title", "Arconium Ingots" },
                { "advancement.arconia.main.red_arconium_ingot.desc", "Craft your first arconium ingot" },
                { "advancement.arconia.main.orange_arconium_tree_sapling.title", "Orange you glad" },
                { "advancement.arconia.main.orange_arconium_tree_sapling.desc", "Harvest your first orange arconium sapling" },
                { "advancement.arconia.main.orange_arconium_ingot.title", "Not so juicy" },
                { "advancement.arconia.main.orange_arconium_ingot.desc", "Orange arconium ingots!" },
                { "advancement.arconia.main.yellow_arconium_ingot.title", "Nautical Nonsense" },
                { "advancement.arconia.main.yellow_arconium_ingot.desc", "Yellow arconium ingots!" },
                { "advancement.arconia.main.green_arconium_ingot.title", "It's not easy being green" },
                { "advancement.arconia.main.green_arconium_ingot.desc", "Green arconium ingots!" },
                { "advancement.arconia.main.light_blue_arconium_ingot.title", "You smurfed another one!" },
                { "advancement.arconia.main.light_blue_arconium_ingot.desc", "Light blue arconium ingots!" },
                { "advancement.arconia.main.blue_arconium_ingot.title", "Da Ba Dee" },
                { "advancement.arconia.main.blue_arconium_ingot.desc", "Blue arconium ingots!" },
                { "advancement.arconia.main.purple_arconium_ingot.title", "ROYGBIV" },
                { "advancement.arconia.main.purple_arconium_ingot.desc", "Obtain the last, purple arconium ingot!" },
        }).forEach(adv -> add(adv[0], adv[1]));
    }

    private void registerMisc() {
        add("jei.arconia.recipe_category.altar", "Pedestal Ritual");
        add("jei.arconia.recipe_category.enchanted_root", "Pot of Gold");
        // clover staff messages
        add("arconia.item.cloverstaff.selectpot.success", "Stored coordinate of the pot of gold at %s");
        add("arconia.item.cloverstaff.selectpot.failed", "Invalid pot of gold multiblock structure?");
        // pot resource setting / unsetting messages
        add("arconia.block.pot_multiblock.set_resource_empty", "The root needs to be enchanted through the pedestal ritual first");
        add("arconia.block.pot_multiblock.set_resource_full", "The pot cannot generate any more resources");
        add("arconia.block.pot_multiblock.set_resource_success", "More riches will be collected by the pot");

        // pedestal ritual messages
        add("arconia.block.center_pedestal.ritual_start_failed", "Failed to start ritual. Perhaps some items are missing?");
        // Pot multiblock
        add("block.arconia.pot_multiblock_secondary", "Pot of Gold");
        add("block.arconia.pot_multiblock_primary", "Pot of Gold");
        add("arconia.block.pot_multiblock.no_tier", "The pot is at the slowest tier");
        add("arconia.block.pot_multiblock.show_tier", "The pot's detected tier is %s");
        // world builder
        add("arconia.block.world_builder.in_progress", "The world builder is a little busy right now, please try again later");
        add("arconia.block.world_builder.start_build", "Starting the builder");
        add("arconia.block.world_builder.no_blocks_found", "The builder didn't find any blocks to convert");

        // Guide book
        add("arconia.guide_book.landing_text", "A collection of notes on my discovery of the secrets to the Leprechaun's wealth, and how to help myself to some of that.");
        add("arconia.guide_book.name", "Arconia Notebook");
    }
}
