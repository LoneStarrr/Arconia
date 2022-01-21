package lonestarrr.arconia.common.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.util.INBTSerializable;
import lonestarrr.arconia.common.Arconia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * All the colors of the Minecraft rainbow roughly matching dye colors - used by various blocks and items
 */
public enum RainbowColor {
    RED(1, "red", "Red", 0xFF0000),
    ORANGE(2, "orange", "Orange", 0xFF7F00),
    YELLOW(3, "yellow", "Yellow", 0xFFFF00),
    GREEN(4, "green", "Green", 0x00FF00),
    LIGHT_BLUE(5, "light_blue", "Light Blue", 0x00FFFF),
    BLUE(6, "blue", "Blue", 0x0000FF),
    PURPLE(7, "purple", "Purple", 0x8B00FF),
    MAGENTA(8, "magenta", "Magenta", 0xFF00FF),
    // Pink's not actually a rainbow color, it is a mix of 2 wavelengths. Sorry for breaking the realism of the game :P
//    PINK(9, "pink", "Pink", 0xFFB1DC);
    PINK(9, "pink", "Pink", 0xFF1493);

    private int tier;
    private int colorValue;
    private String tierName;
    private String unlocalizedName;

    private RainbowColor(int tier, String name, String unlocalizedName, int colorValue) {
        this.tier = tier;
        this.tierName = name;
        this.unlocalizedName = unlocalizedName;
        this.colorValue = colorValue;
    }

    @Nonnull
    public String getTierName() {
        return this.tierName;
    }

    @Nonnull
    /**
     * @return Color RGB value associated with the tier
     */
    public int getColorValue() {
        return this.colorValue;
    }

    /**
     *
     * @return Progression tier this color is associated (1..)
     */
    public int getTier() {
        return tier;
    }

    public static Stream<RainbowColor> stream() {
        return Stream.of(RainbowColor.values());
    }
    /**
     *
     * @return The next tier, or null if this is the maximum tier
     */
    @Nullable
    public RainbowColor getNextTier() {
        if (tier == RainbowColor.PINK.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() + 1];
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }
}
