package lonestarrr.arconia.data;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.advancements.PotOfGoldTrigger;
import lonestarrr.arconia.common.advancements.TouchGrassTrigger;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.components.ModDataComponents;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.PatchouliHelper;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.Optional;
import java.util.function.Consumer;

public class AdvancementSubProvider implements AdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        ItemStack guideBook = PatchouliHelper.createGuideBook();
        // This can't be the best way to get an item with a single component, CAN IT?
        AdvancementHolder root = Advancement.Builder.advancement()
                .addCriterion("guide_book", InventoryChangeTrigger.TriggerInstance
                        .hasItems(
                                ItemPredicate.Builder.item()
                                        .of(guideBook.getItem())
                                        .hasComponents(
                                                DataComponentPredicate.allOf(
                                                        (guideBook.getComponents().filter(ct -> ct.equals(PatchouliHelper.patchouliGuideBookComponent())))
                                                )
                                        )
                        )
                )
                .display(
                        guideBook,
                        Component.translatable("advancement.arconia.main.root.title"),
                        Component.translatable("advancement.arconia.main.root.desc"),
                        ResourceLocation.withDefaultNamespace("textures/block/stripped_oak_log.png"),
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("root"), existingFileHelper);

        AdvancementHolder redRoot = Advancement.Builder.advancement()
                .addCriterion("red_tree_root", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getColoredRoot(RainbowColor.RED)))
                .parent(root)
                .display(
                        ModItems.getColoredRoot(RainbowColor.RED),
                        Component.translatable("advancement.arconia.main.red_tree_root.title"),
                        Component.translatable("advancement.arconia.main.red_tree_root.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("red_tree_root"), existingFileHelper);

        AdvancementHolder pedestal = Advancement.Builder.advancement()
                .addCriterion("pedestal", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModBlocks.pedestal.asItem()))
                .parent(root)
                .display(
                        ModBlocks.pedestal.asItem(),
                        Component.translatable("advancement.arconia.main.pedestal.title"),
                        Component.translatable("advancement.arconia.main.pedestal.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("pedestal"), existingFileHelper);

        AdvancementHolder centerPedestal = Advancement.Builder.advancement()
                .addCriterion("center_pedestal", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModBlocks.centerPedestal.asItem()))
                .parent(root)
                .display(
                        ModBlocks.centerPedestal.asItem(),
                        Component.translatable("advancement.arconia.main.center_pedestal.title"),
                        Component.translatable("advancement.arconia.main.center_pedestal.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("center_pedestal"), existingFileHelper);

        AdvancementHolder fourLeafClover = Advancement.Builder.advancement()
                .addCriterion("four_leaf_clover", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.fourLeafClover))
                .parent(root)
                .display(
                        ModItems.fourLeafClover,
                        Component.translatable("advancement.arconia.main.four_leaf_clover.title"),
                        Component.translatable("advancement.arconia.main.four_leaf_clover.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("four_leaf_clover"), existingFileHelper);

        AdvancementHolder cloverStaff = Advancement.Builder.advancement()
                .addCriterion("clover_staff", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.cloverStaff))
                .parent(fourLeafClover)
                .display(
                        ModItems.cloverStaff,
                        Component.translatable("advancement.arconia.main.clover_staff.title"),
                        Component.translatable("advancement.arconia.main.clover_staff.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("clover_staff"), existingFileHelper);


        // This can't be the best way to get an item with a single component, CAN IT?
        ItemStack coloredRoot = ColoredRoot.getColoredRootWithResource(RainbowColor.RED, new ItemStack(ModItems.getArconiumEssence(RainbowColor.RED).asItem()));
        AdvancementHolder redRootOfEssence = Advancement.Builder.advancement()
                .addCriterion("red_root_of_essence", InventoryChangeTrigger.TriggerInstance
                        .hasItems(
                                ItemPredicate.Builder.item()
                                        .of(coloredRoot.getItem())
                                        .hasComponents(
                                                DataComponentPredicate.allOf(
                                                        (coloredRoot.getComponents().filter(ct -> ct.equals(DataComponents.CONTAINER)))
                                                )
                                        )
                        )
                )
                .parent(centerPedestal)
                .display(
                        coloredRoot,
                        Component.translatable("advancement.arconia.main.red_root_of_essence.title"),
                        Component.translatable("advancement.arconia.main.red_root_of_essence.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("red_root_of_essence"), existingFileHelper);

        AdvancementHolder redEssence = Advancement.Builder.advancement()
                .addCriterion("red_arconium_essence", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumEssence(RainbowColor.RED)))
                .parent(redRootOfEssence)
                .display(
                        ModItems.getArconiumEssence(RainbowColor.RED),
                        Component.translatable("advancement.arconia.main.red_arconium_essence.title"),
                        Component.translatable("advancement.arconia.main.red_arconium_essence.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("red_arconium_essence"), existingFileHelper);

        AdvancementHolder potOfGold = Advancement.Builder.advancement()
                .addCriterion("pot_of_gold", ModCriteriaTriggers.CREATE_POT_OF_GOLD_TRIGGER.get().createCriterion(new PotOfGoldTrigger.TriggerInstance(Optional.empty())))
                .parent(cloverStaff)
                .display(
                        Items.GOLD_BLOCK,
                        Component.translatable("advancement.arconia.main.pot_of_gold.title"),
                        Component.translatable("advancement.arconia.main.pot_of_gold.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("pot_of_gold"), existingFileHelper);

        AdvancementHolder touchGrass = Advancement.Builder.advancement()
                .addCriterion("touch_grass", ModCriteriaTriggers.TOUCH_GRASS_TRIGGER.get().createCriterion(new TouchGrassTrigger.TriggerInstance(Optional.empty())))
                .parent(redEssence)
                .display(
                        ModBlocks.getRainbowGrassBlock(RainbowColor.RED).asItem(),
                        Component.translatable("advancement.arconia.main.touch_grass.title"),
                        Component.translatable("advancement.arconia.main.touch_grass.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("touch_grass"), existingFileHelper);

        AdvancementHolder redIngot = Advancement.Builder.advancement()
                .addCriterion("red_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.RED)))
                .parent(redEssence)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.RED),
                        Component.translatable("advancement.arconia.main.red_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.red_arconium_ingot.desc"),
                        null,
                        AdvancementType.GOAL,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("red_arconium_ingot"), existingFileHelper);

        AdvancementHolder redSickle = Advancement.Builder.advancement()
                .addCriterion("red_arconium_sickle", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumSickle(RainbowColor.RED)))
                .parent(redIngot)
                .display(
                        ModItems.getArconiumSickle(RainbowColor.RED),
                        Component.translatable("advancement.arconia.main.red_arconium_sickle.title"),
                        Component.translatable("advancement.arconia.main.red_arconium_sickle.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("red_arconium_sickle"), existingFileHelper);

        AdvancementHolder orangeSapling = Advancement.Builder.advancement()
                .addCriterion("orange_arconium_tree_sapling", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModBlocks.getArconiumTreeSapling(RainbowColor.ORANGE).asItem()))
                .parent(redSickle)
                .display(
                        ModBlocks.getArconiumTreeSapling(RainbowColor.ORANGE).asItem(),
                        Component.translatable("advancement.arconia.main.orange_arconium_tree_sapling.title"),
                        Component.translatable("advancement.arconia.main.orange_arconium_tree_sapling.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("orange_arconium_tree_sapling"), existingFileHelper);

        AdvancementHolder orangeIngot = Advancement.Builder.advancement()
                .addCriterion("orange_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.ORANGE)))
                .parent(orangeSapling)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.ORANGE),
                        Component.translatable("advancement.arconia.main.orange_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.orange_arconium_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("orange_arconium_ingot"), existingFileHelper);

        AdvancementHolder yellowIngot = Advancement.Builder.advancement()
                .addCriterion("yellow_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.YELLOW)))
                .parent(orangeIngot)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.YELLOW),
                        Component.translatable("advancement.arconia.main.yellow_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.yellow_arconium_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("yellow_arconium_ingot"), existingFileHelper);

        AdvancementHolder greenIngot = Advancement.Builder.advancement()
                .addCriterion("green_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.GREEN)))
                .parent(yellowIngot)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.GREEN),
                        Component.translatable("advancement.arconia.main.green_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.green_arconium_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("green_arconium_ingot"), existingFileHelper);

        AdvancementHolder lightBlueIngot = Advancement.Builder.advancement()
                .addCriterion("light_blue_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.LIGHT_BLUE)))
                .parent(greenIngot)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.LIGHT_BLUE),
                        Component.translatable("advancement.arconia.main.light_blue_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.light_blue_arconium_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("light_blue_arconium_ingot"), existingFileHelper);

        AdvancementHolder blueIngot = Advancement.Builder.advancement()
                .addCriterion("blue_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.BLUE)))
                .parent(lightBlueIngot)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.BLUE),
                        Component.translatable("advancement.arconia.main.blue_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.blue_arconium_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("blue_arconium_ingot"), existingFileHelper);

        AdvancementHolder purpleIngot = Advancement.Builder.advancement()
                .addCriterion("purple_arconium_ingot", InventoryChangeTrigger.TriggerInstance
                        .hasItems(ModItems.getArconiumIngot(RainbowColor.PURPLE)))
                .parent(blueIngot)
                .display(
                        ModItems.getArconiumIngot(RainbowColor.PURPLE),
                        Component.translatable("advancement.arconia.main.purple_arconium_ingot.title"),
                        Component.translatable("advancement.arconia.main.purple_arconium_ingot.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,true,false
                )
                .save(saver, ResourceLocationHelper.prefix("purple_arconium_ingot"), existingFileHelper);

    }
}
