package lonestarrr.arconia.data.client;

import com.google.gson.JsonElement;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static lonestarrr.arconia.common.core.helper.ResourceLocationHelper.prefix;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Arconia.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerArconiumIngots();
    }

    private void registerArconiumIngots() {
        // All ingots share a single model. Layer0 is dynamically colored based on tier.
        for (RainbowColor color: RainbowColor.values()) {
            Item item = ModItems.getArconiumIngot(color);
            String name = Registry.ITEM.getKey(item).getPath();
            withExistingParent(name, GENERATED)
                    .texture("layer0", prefix("item/arconium_ingot_" + color.getTierName()));
        }
    }
}
