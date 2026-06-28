package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;

public class ProgressRedSickle extends EntryProvider {

  public ProgressRedSickle(CategoryProviderBase parent) {
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
    this.pageTitle("Red Sickle");
    this.pageText(
            """
                    Normally, when one chops a red arconium tree, it will drop saplings to grow another red tree.
                    However, I learned from the Leprechauns that you can get them to drop saplings for a different color using
                    the magic powers of a hand-crafted sickle.
                    It must be made from arconium ingots to give it this special ability. The colors are not random; there is a
                    clear progression in colors, lining up with the colors in a rainbow.
                    """);
    this.page(
            "page2",
            () ->
                    BookCraftingRecipePageModel.create()
                            .withRecipeId1(ResourceLocationHelper.prefix("red_arconium_sickle")));
  }

  @Override
  protected String entryName() {
    return "Sickle";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getArconiumSickle(RainbowColor.RED));
  }

  @Override
  protected String entryId() {
    return "progress_red_arconium_sickle";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("red_arconium_ingot"));
    return entry.withCondition(advancementCondition);
  }

}
