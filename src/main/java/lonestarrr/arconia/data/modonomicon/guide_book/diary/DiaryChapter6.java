package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryChapter6 extends EntryProvider {

  public DiaryChapter6(CategoryProviderBase parent) {
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
    this.pageTitle("Return");
    this.pageText(
        """
                        Dear diary. I have discovered all that I can, and I really need to be able to find my way back
                        to the nearest village. Tomorrow, I will start my travels to replenish.
                        I have definitely felt better. My supplies have run out, and I must be careful making my escape
                        to not get caught.
                        """);
    this.page("page2", () -> BookTextPageModel.create().withText(this.context().pageText()));
    this.pageText(
        """
                        I'm confident my notes are detailed enough so that I can reclaim all of the stuff the
                        Leprechauns have stolen. I can't trust any of the villagers with anything though,
                        so I will bury my notes here where they will be safe from prying eyes.
                        """);
  }

  @Override
  protected String entryName() {
    return "Return";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(Items.BOOK);
  }

  @Override
  protected String entryId() {
    return "diary_chapter6";
  }
}
