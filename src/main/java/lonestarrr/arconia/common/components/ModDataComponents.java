package lonestarrr.arconia.common.components;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.MagicInABottle;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Arconia.MOD_ID);

    public static final Supplier<DataComponentType<MagicInABottle.MagicInABottleData>> MAGIC_IN_A_BOTTLE_DATA =
            DATA_COMPONENTS.registerComponentType("magic_in_a_bottle_data",
                    builder -> builder
                            .persistent(MagicInABottle.MagicInABottleData.CODEC)
                            .networkSynchronized(MagicInABottle.MagicInABottleData.STREAM_CODEC)
            );

    // TODO trying to create patchouli guide books which now uses data components, and I need something to serialize a ResourceLocation
    // This couldn't possibly work since Patchouli will .get() this on an itemstack with its own namespace. So really I just need to use the codec.
    public static final Supplier<DataComponentType<ResourceLocation>> BOOK = DATA_COMPONENTS.registerComponentType("patchouli_book", builder -> builder
            .persistent(ResourceLocation.CODEC)
            .networkSynchronized(ResourceLocation.STREAM_CODEC)
    );
}
