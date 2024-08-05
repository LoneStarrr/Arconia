package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ColoredRoot;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Arconia.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerArconiumIngots();
        registerArconiumEssence();
        registerArconiumSickles();
        registerTreeRoots();
    }

    private void registerArconiumIngots() {
        // All ingots share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/arconium_ingot";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/arconium_ingot_white"));

        for (RainbowColor color: RainbowColor.values()) {
            Supplier<Item> item = ModItems.getArconiumIngot(color);
            String name = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }

    private void registerArconiumEssence() {
        // All essences share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/arconium_essence";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/arconium_essence"));

        for (RainbowColor color: RainbowColor.values()) {
            Supplier<Item> item = ModItems.getArconiumEssence(color);
            String name = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }

    private void registerArconiumSickles() {
        // All hoes share a single model. Layer1 is dynamically colored based on tier.
        final String modelName = "item/arconium_sickle";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/sickle_handle"))
                .texture("layer1", prefix("item/sickle_head"));

        for (RainbowColor color: RainbowColor.values()) {
            Supplier<Item> item = ModItems.getArconiumSickle(color);
            String name = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }

    private void registerTreeRoots() {
        // All tree roots share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/colored_tree_root";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/colored_tree_root"));

        for (RainbowColor color: RainbowColor.values()) {
            Supplier<ColoredRoot> item = ModItems.getColoredRoot(color);
            String name = BuiltInRegistries.ITEM.getKey(item.get()).getPath();

            withExistingParent(name, prefix(modelName));
        }
    }
}
