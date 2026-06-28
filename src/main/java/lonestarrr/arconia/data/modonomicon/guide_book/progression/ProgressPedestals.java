package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import net.minecraft.util.Util;

public class ProgressPedestals extends EntryProvider {

  public ProgressPedestals(CategoryProviderBase parent) {
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
    this.pageTitle("Pedestals");
    this.pageText(
        """
        All strange, magical things in life somehow seem to involve a ritual performed in a
        circular fashion. One needs to utter some hard to pronounce wordsm and then smack things with a wand or staff.
        The Leprechauns clearly were not going to stray from this seemingly imposed doctrine, and invented
        a ritual of their own. Firstly, we're going to need several pedestals made from wood to make the circle.
        Eight will do. 
        """);
    this.page(
            "page2",
            () ->
                    BookCraftingRecipePageModel.create()
                            .withRecipeId1(ResourceLocationHelper.prefix("pedestal"))
                            .withTitle1("Pedestal"));

  }

  @Override
  protected String entryName() {
    return "Pedestals";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.pedestal.asItem());
  }

  @Override
  protected String entryId() {
    return "progress_pedestals";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("clover_staff"));
    this.lang().add(
            Util.makeDescriptionId("advancement", advancementCondition.getAdvancementId()) + ".title",
            "Make a clover staff"
    );

    return entry.withCondition(advancementCondition);

  }

}
