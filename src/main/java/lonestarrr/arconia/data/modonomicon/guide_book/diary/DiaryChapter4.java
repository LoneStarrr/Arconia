package lonestarrr.arconia.data.modonomicon.guide_book.diary;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import net.minecraft.world.item.Items;

public class DiaryChapter4 extends EntryProvider {

  public DiaryChapter4(CategoryProviderBase parent) {
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
    this.pageTitle("Discovery");
    this.pageText(
        """
        Dear diary. The noises came back last night, and this time I was able to discover a bit more.
        Remember those stories from over-imaginative travellers at taverns around the world?
        About the little green Leprechauns hoarding their riches, only to have their stashes disclosed
        by giant multicolored arches in the sky?
        """);
    this.page("page2", () -> BookTextPageModel.create().withText(this.context().pageText()));
    this.pageText(
        """
            Yeah, they might have been on to something, because what I just witnessed struck an uncanny resemblance
            to those tales. I am safe for now, but I have to be careful not to make any noises.
            """);
  }

  @Override
  protected String entryName() {
    return "Discovery";
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
    return "diary_chapter4";
  }
}
