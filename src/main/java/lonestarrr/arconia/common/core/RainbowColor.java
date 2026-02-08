package lonestarrr.arconia.common.core;

import net.minecraft.util.FastColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * All the colors of the Minecraft rainbow roughly matching dye colors - used by various blocks and items
 */
public enum RainbowColor implements StringRepresentable {
    RED(1, "red", "Red", FastColor.ARGB32.color(0xFF, 0, 0)),
    ORANGE(2, "orange", "Orange", FastColor.ARGB32.color(0xFF, 0x7F, 0)),
    YELLOW(3, "yellow", "Yellow", FastColor.ARGB32.color(0xFF, 0xFF, 0)),
    GREEN(4, "green", "Green", FastColor.ARGB32.color(0, 0xFF, 0)),
    LIGHT_BLUE(5, "light_blue", "Light Blue", FastColor.ARGB32.color(0, 0xFF, 0xFF)),
    BLUE(6, "blue", "Blue", FastColor.ARGB32.color(0, 0, 0xFF)),
    PURPLE(7, "purple", "Purple", FastColor.ARGB32.color(0x80, 0, 0x80));

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

    public static RainbowColor byTier(int tier) {
        // TODO use a codec for this probably, and do this smarter?
        for (RainbowColor color: RainbowColor.values()) {
            if (color.getTier() == tier) {
                return color;
            }
        }
        return null;
    }

    @Nonnull
    public String getTierName() {
        return this.tierName;
    }

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
        if (tier == RainbowColor.PURPLE.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() + 1];
    }

    public RainbowColor getPreviousTier() {
        if (tier == RainbowColor.RED.tier) {
            return null;
        }
        return RainbowColor.values()[this.ordinal() - 1];
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.tierName;
    }

    public String toString() {
        return this.tierName;
    }

    /**
     * @return Unlocalized name for default language data generation
     */
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public static Item woolByTier(RainbowColor tier) {
        return switch (tier) {
            case RED -> Items.RED_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
        };
    }

    public static Item dyeByTier(RainbowColor tier) {
        return switch (tier) {
            case RED -> Items.RED_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case GREEN -> Items.GREEN_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case PURPLE -> Items.PURPLE_DYE;
        };
    }

    public static MapColor getMapColor(RainbowColor tier) {
        return switch (tier) {
            case RainbowColor.RED -> MapColor.COLOR_RED;
            case RainbowColor.ORANGE -> MapColor.COLOR_ORANGE;
            case RainbowColor.YELLOW -> MapColor.COLOR_YELLOW;
            case RainbowColor.GREEN -> MapColor.COLOR_GREEN;
            case RainbowColor.LIGHT_BLUE -> MapColor.COLOR_LIGHT_BLUE;
            case RainbowColor.BLUE -> MapColor.COLOR_BLUE;
            case RainbowColor.PURPLE -> MapColor.COLOR_PURPLE;
        };
    }
}
