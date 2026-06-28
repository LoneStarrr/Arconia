package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressRedArconiumIngot extends EntryProvider {

  public ProgressRedArconiumIngot(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page(
        "page1",
        () ->
            BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
    this.pageTitle("Red arconium ingot");
    this.pageText(
        """
        One of the first things I'll want to make using the red arconium essence is a magical red metal, which will
        prove very useful to unlock more treasure to steal, I mean, borrow. I can once
        again use the ritual to craft it, assuming the particular universe I'm in today didn't sneakily alter it.
        Either way, looking up the recipe for red arconium ingots should provide me with the exact ingredients and
        method needed.
        """);
    this.page(
        "page2",
        () ->
            BookSpotlightPageModel.create()
                .withItem(Ingredient.of(ModItems.getArconiumIngot(RainbowColor.RED))));
  }

  @Override
  protected String entryName() {
    return "Red arconium ingot";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getArconiumIngot(RainbowColor.RED));
  }

  @Override
  protected String entryId() {
    return "progress_red_arconium_ingot";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition =
        this.condition().advancement(ResourceLocationHelper.prefix("red_arconium_essence"));
    return entry.withCondition(advancementCondition);
  }
}
