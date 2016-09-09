package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.converters.ConverterRegistry;
import nl.hsac.fitnesse.slim.converter.NumberedListConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixture to create and manipulate list values. Generated values can be stored in variables so the can
 * be passed as arguments to methods of other fixtures.
 * This fixture can be used using Slim's dynamic decision tables or using scripts (and scenarios).
 */
public class ListFixture extends SlimFixture {
    private final ArrayList<Object> list = new ArrayList<Object>();

    /**
     * Configures Slim to show lists as HTML numbered list (instead of comma separated between square brackets
     * (i.e. '[' and ']').
     */
    public void displayListsNumbered() {
        NumberedListConverter.register();
    }

    /**
     * Configures Slim to use standard coverters, i.e. go back to comma separated.
     */
    public void displayListsStandard() {
        ConverterRegistry.resetToStandardConverters();
    }

    /**
     * Adds new element to end of list.
     * @param value value to add.
     */
    public void add(Object value) {
        addTo(value, list);
    }

    /**
     * Sets value of element at index (0-based). If the current list has less elements it is extended to
     * have exactly index elements.
     * @param value value to store.
     * @param index 0-based index to add element.
     */
    public void setValueAt(Object value, int index) {
        setValueAtIn(value, index, list);
    }

    /**
     * Retrieves element at index (0-based).
     * @param index 0-based index of element to retrieve value of.
     * @return element at specified index.
     * @throws SlimFixtureException if the list does not have at least index elements.
     */
    public Object valueAt(int index) {
        return valueAtIn(index, list);
    }

    /**
     * @return new list containing current values.
     */
    public ArrayList<Object> copyList() {
        return copyList(list);
    }

    /**
     * @return number of elements in list.
     */
    public int size() {
        return sizeOf(list);
    }

    /**
     * Adds new element to end of list.
     * @param value value to add.
     * @param aList list to add to.
     */
    public void addTo(Object value, List aList) {
        Object cleanValue = cleanupValue(value);
        aList.add(cleanValue);
    }

    /**
     * Sets value of element at index (0-based). If the current list has less elements it is extended to
     * have exactly index elements.
     * @param value value to store.
     * @param index 0-based index to add element.
     * @param aList list to set element in.
     */
    public void setValueAtIn(Object value, int index, List aList) {
        Object cleanValue = cleanupValue(value);
        while (aList.size() <= index) {
            aList.add(null);
        }
        aList.set(index, cleanValue);
    }

    /**
     * Retrieves element at index (0-based).
     * @param index 0-based index of element to retrieve value of.
     * @param aList list to get element value from.
     * @return element at specified index.
     * @throws SlimFixtureException if the list does not have at least index elements.
     */
    public Object valueAtIn(int index, List aList) {
        if (aList.size() > index) {
            return aList.get(index);
        } else {
            throw new SlimFixtureException(false, "list only has " + aList.size() + " elements");
        }
    }

    /**
     * @param aList list to copy.
     * @return new list containing list's values.
     */
    public ArrayList<Object> copyList(List aList) {
        return new ArrayList<Object>(aList);
    }

    /**
     * @param aList list to get size of.
     * @return number of elements in list.
     */
    public int sizeOf(List aList) {
        return aList.size();
    }

    //// methods to support usage in dynamic decision tables

    /**
     * Called before next row is executed. (Clears all current values.)
     */
    public void reset() {
        list.clear();
    }

    /**
     * Sets a value.
     * @param index index (0-based) to set value for.
     * @param value value to be stored.
     */
    public void set(int index, Object value) {
        setValueAt(value, index);
    }

    /**
     * Retrieves value for output column.
     * @param headerName header of output column (without trailing '?').
     * @return new list containing current values.
     */
    public ArrayList<Object> get(String headerName) {
        return copyList();
    }

    //// end: methods to support usage in dynamic decision tables

}
