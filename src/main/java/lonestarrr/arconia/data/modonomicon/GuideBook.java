/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package lonestarrr.arconia.data.modonomicon;

import com.klikli_dev.modonomicon.api.datagen.ModonomiconLanguageProvider;
import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import lonestarrr.arconia.data.modonomicon.guide_book.MainCategory;
import net.minecraft.resources.Identifier;

public class GuideBook extends SingleBookSubProvider {

  public static final String ID = "guide_book";

  public GuideBook(String modid, ModonomiconLanguageProvider lang) {
    super(ID, modid, lang);
  }

  @Override
  protected BookModel additionalSetup(BookModel book) {
    // if we want to handle a second language in here we can add book-related (not category or entry
    // or page) translations here
    // this.add(this.lang("ru_ru"), this.context().bookName(), "Демонстрационная книга");
    // this.add(lang("ru_ru"), this.context().bookTooltip(), "Книга для демонстрации и тестирования
    // функций \"Модономикона\".");

    //    var commandEntryCommand =
    //        BookCommandModel.create(this.modLoc("test_command"), "/give @s minecraft:apple 1")
    //            .withSuccessMessage("modonomicon.command.test_command.success");
    //    this.add(commandEntryCommand.getSuccessMessage(), "You got an apple, because reading is
    // cool!");
    //
    //    var commandEntryLinkCommand =
    //        BookCommandModel.create(this.modLoc("test_command2"), "/give @s minecraft:wheat 1")
    //            .withSuccessMessage("modonomicon.command.test_command2.success")
    //            .withAllowedEntry("modonomicon:features/command");
    //    this.add(
    //        commandEntryLinkCommand.getSuccessMessage(), "You got wheat, because clicking is
    // cool!");

    return book.withModel(Identifier.parse("modonomicon:modonomicon_green"))
        .withTheme(
            theme ->
                theme.withLayout(
                    layout ->
                        layout
                            .withBookTextOffsetX(5)
                            .withBookTextOffsetY(0)
                            .withBookTextOffsetWidth(-5)))
        //        .withCommand(commandEntryCommand)
        //        .withCommand(commandEntryLinkCommand)
        .withAllowOpenBooksWithInvalidLinks(true);
  }

  @Override
  protected void registerDefaultMacros() {
    // currently no macros
  }

  @Override
  protected void generateCategories() {
    this.add(new MainCategory(this).generate());
    //    var formattingCategory = this.add(new FormattingCategory(this).generate());
    //
    //    var conditionalCategory =
    //        this.add(new ConditionalCategory(this).generate())
    //            .withCondition(
    //                this.condition()
    //                    .entryRead(this.modLoc(MainCategory.ID, ConditionRootEntry.ID)));
    //
    //    var indexModeCategory = this.add(new IndexModeCategory(this).generate());
  }

  @Override
  protected String bookName() {
    return "Arconia Notebook";
  }

  @Override
  protected String bookTooltip() {
    return "A traveller's notes on all things Arconia";
  }
}
