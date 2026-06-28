package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryChapter5 extends EntryProvider {

  public DiaryChapter5(CategoryProviderBase parent) {
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
    this.pageTitle("Taking Notes");
    this.pageText(
        """
                         Dear diary. It's been several days since my discovery, and from what I can tell,
                         much of the stories about these so-called Leprechauns is true.
                         My mind is slowly going from lack of food though,
                         but I still have enough to survive for a few days more.
                        """);
    this.page("page2", () -> BookTextPageModel.create().withText(this.context().pageText()));
    this.pageText(
        """
                        Meanwhile, I am writing down as many details as I can in the back of my notebook so that I
                        can replicate the tricks I've witnessed and finally be able to settle down!
                        At least they are not on to me yet, thanks to my ability to be practically invisible.
                        Who knew that skill would one day prove to be valuable?
                        """);
  }

  @Override
  protected String entryName() {
    return "Taking Notes";
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
    return "diary_chapter5";
  }
}
