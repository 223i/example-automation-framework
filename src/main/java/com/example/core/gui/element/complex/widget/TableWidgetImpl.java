package com.example.core.gui.element.complex.widget;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import com.example.core.enumeration.element.Locators;
import com.example.core.enumeration.element.Sortings;
import com.example.core.enumeration.element.TableRowCount;
import com.example.core.enumeration.element.TypeNames;
import com.example.core.enumeration.element.name.Columns;
import com.example.core.enumeration.element.name.Dropboxes;
import com.example.core.enumeration.element.name.Icons;
import com.example.core.exception.export.ElementNotPresentException;
import com.example.core.gui.element.base.WidgetImpl;
import com.example.core.gui.element.base.ComplexGUIElementImpl;
import com.example.core.gui.element.simple.DropboxImpl;
import com.example.core.gui.iface.base.ComplexGUIElement;
import com.example.core.gui.iface.complex.widget.Table;
import com.example.core.gui.iface.complex.widget.Row;
import com.example.core.util.ScreenshotMaker;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableWidgetImpl extends WidgetImpl implements Table {

    private  static final Logger LOG = LoggerFactory.getLogger(TableWidgetImpl.class);

    private final int tableNumber;

    public TableWidgetImpl(int tableNumber, ComplexGUIElement container) {
        super(Locators.TABLE, TypeNames.TABLE, String.valueOf(tableNumber), container);
        this.tableNumber = tableNumber;
    }

    //second constructor for reflection call, do not remove!
    protected TableWidgetImpl(String tableNumber, ComplexGUIElementImpl container) {
        this(Integer.parseInt(tableNumber), container);
    }

    @Override
    public int getTableNumber() {
        return tableNumber;
    }

    @Override
    public RowWidgetImpl row(int rowNumber){
        return new RowWidgetImpl(this, rowNumber);
    }

    @Override
    public Row rowWithColumnValue(Columns columnName, String value){
        int columnNumber = findZeroBasedColumnNumber(columnName);
        ElementsCollection tableRows = findAllRows();
        return tableRows.stream()
                .filter(tableRow -> tableRow.$$x(Locators.TABLE_COLUMN.value()).get(columnNumber).text().equals(value))
                .mapToInt(tableRows::indexOf)
                .mapToObj(index -> new RowWidgetImpl(this, index + 1))
                .findFirst()
                .orElseThrow(() -> new ElementNotPresentException("No rows with value '" + value +"' found in table " + this));
    }

    @Override
    @Step("Sort column {columnName} in {this} in {sorting} order")
    public void sortColumn(Columns columnName, Sortings sorting){
        try {
            SelenideElement columnToSort = getSelf().$x(Locators.TABLE_HEADER.forName(columnName.value()));
            if(!columnToSort.exists()){
                throw new ElementNotPresentException("No column '" + columnName + "' is present in " + this);
            }

            SelenideElement iconSortieren;
            if (sorting.equals(Sortings.ASCENDING)) {
                iconSortieren = columnToSort.$x(Locators.ICON.forName(Icons.ICON1.value()));
            } else {
                iconSortieren = columnToSort.$x(Locators.ICON.forName(Icons.ICON2.value()));
            }

            if(!iconSortieren.exists())    {
                throw new ElementNotPresentException("No sorting icon is present for " + columnName + " in " + this);
            }
            iconSortieren.click();

            String sortingName = sorting.name().toLowerCase();
            LOG.info("In {} column '{}' sorted in {} order.", this, columnName, sortingName);
            ScreenshotMaker.screenshot("in-" + this + "-column-" + columnName + "-" + sorting + "-sorted");
        } catch (ElementNotFound e) {
            throw new ElementNotPresentException(this, e);
        }
    }

    @Override
    public int getRowAmount(){
        return findAllRows().size();
    }

    @Override
    public void selectRowAmount(TableRowCount tableRowAmount){
        new DropboxImpl(Dropboxes.DROPBOX1.value(), this).selectValue(tableRowAmount);
    }

    @Override
    public boolean isEmpty(){
        return findAllRows().isEmpty();
    }

    private ElementsCollection findAllRows(){
        return getSelf().$$x(Locators.TABLE_ROW.forName("*"));
    }

    private int findZeroBasedColumnNumber(Columns columnName){
        ElementsCollection columnHeaders = getSelf().$$x(Locators.TABLE_HEADER.forName("")); //find all headers
        return columnHeaders.stream()
                .filter(header -> header.text().equals(columnName.value()))
                .mapToInt(columnHeaders::indexOf)
                .findFirst()
                .orElseThrow(() -> new ElementNotPresentException("No columns with name '" + columnName.value() +"' is present in " + this));
    }

    public static class RowWidgetImpl extends WidgetImpl implements Row {

        private final int rowNumber;
        private final Table table;

        private RowWidgetImpl(TableWidgetImpl parentTable, int rowNumber) {
            super(Locators.TABLE_ROW, TypeNames.ROW, String.valueOf(rowNumber), parentTable);
            this.rowNumber = rowNumber;
            this.table = parentTable;
        }

        @Override
        public int getOrderNumber() {
            return rowNumber;
        }

        @Override
        public Table getTable() {
            return table;
        }
    }
}
