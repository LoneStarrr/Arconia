package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Arconia.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerArconiumIngots();
        registerArconiumEssence();
        registerTreeRoots();
    }

    private void registerArconiumIngots() {
        // All ingots share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/arconium_ingot";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/arconium_ingot_white"));

        for (RainbowColor color: RainbowColor.values()) {
            Item item = ModItems.getArconiumIngot(color);
            String name = Registry.ITEM.getKey(item).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }

    private void registerArconiumEssence() {
        // All essences share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/arconium_essence";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/arconium_essence"));

        for (RainbowColor color: RainbowColor.values()) {
            Item item = ModItems.getArconiumEssence(color);
            String name = Registry.ITEM.getKey(item).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }

    private void registerTreeRoots() {
        // All tree roots share a single model. Layer0 is dynamically colored based on tier.
        final String modelName = "item/colored_tree_root";
        withExistingParent(modelName, GENERATED)
                .texture("layer0", prefix("item/colored_tree_root"));

        for (RainbowColor color: RainbowColor.values()) {
            Item item = ModItems.getColoredRoot(color);
            String name = Registry.ITEM.getKey(item).getPath();
            withExistingParent(name, prefix(modelName));
        }
    }
}
