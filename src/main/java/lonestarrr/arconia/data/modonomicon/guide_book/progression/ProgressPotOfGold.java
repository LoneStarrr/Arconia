package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import net.minecraft.world.item.Items;

public class ProgressPotOfGold extends EntryProvider {

  public ProgressPotOfGold(CategoryProviderBase parent) {
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
    this.pageTitle("Pot of Gold");
    this.pageText(
        """
        There's this well known myth that you can find a pot of gold at the end of the rainbow. Of course, once you
        actually set out to find it, it turns out to be a mirage. But as I discovered, there is some truth to it, and
        I know just how to turn it into reality.
        Instead of finding such a pot, however, we need to construct one ourselves.
        """);
    this.page(
            "page2",
            () ->
                    BookTextPageModel.create()
                            .withText(this.context().pageText()));
    this.pageText(
            """
            Luckily, studying the Leprechauns has revealed the secret to me. I have included instructions
            on how to build it from a collection of blocks on the next page.
            Once built, using the clover staff on it brings it to life. By itself, it doesn't do much yet,
            but I'll detail the rest later. First, however, let's build it.
            """);
    this.page(
            "page3",
            () -> BookMultiblockPageModel.create().withMultiblockId(this.modLoc("pot_of_gold")));
  }

  @Override
  protected String entryName() {
    return "Pot of Gold";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.CAULDRON);
  }

  @Override
  protected String entryId() {
    return "progress_pot_of_gold";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("clover_staff"));
    return entry.withCondition(advancementCondition);
    // TODO remove advancement titles in the book? Already datagenned elsewhere
  }

}
