package com.team34.model.event;

import com.team34.model.UIDManager;
import com.team34.model.chapter.ChapterListObject;
import com.team34.model.chapter.ChapterManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class manages all events and event order lists.
 * <p>
 * The events are stored in a HashMap, with UIDs (Long) as keys.
 * <p>
 * Event order lists are lists of event UID in a specific order. This allows switching
 * between different event orders and editing the order on a specific order list.
 *
 * @author Kasper S. Skott
 */
public class EventManager {

    private HashMap<Long, Event> events;
    private ArrayList<LinkedList<Long>> eventOrderLists;
    private boolean hasChanged;
    private ChapterManager chapterManager;

    /**
     * Constructs and initializes the EventManager. Creates a default event order list at index 0.
     */
    public EventManager(ChapterManager chapterManager) {
        hasChanged = false;
        events = new HashMap<Long, Event>();
        eventOrderLists = new ArrayList<>();
        eventOrderLists.add(new LinkedList<Long>());
        this.chapterManager = chapterManager;
    }

    /**
     * Constructs a new event and stores it. The new event UID is generated by the {@link UIDManager},
     * and is thereafter placed at the back of each event order list.
     * This will set {@link EventManager#hasChanged} to true, as data has been changed.
     *
     * @param name        the name of the event
     * @param description the description of the event
     * @return the UID of the new event
     * @author Hazem Elkhalil
     */
    public long newEvent(String name, String description, String color, ChapterListObject chapterListObject) {
        long uid = UIDManager.nextUID();
        Event event = new Event(name, description, chapterListObject, color);
        addEvent(uid, event);

        chapterManager.getChapter(chapterListObject.getUid()).getEvents().add(event);

        if (eventOrderLists.size() < 1)
            eventOrderLists.add(new LinkedList<>());

        for (LinkedList<Long> e : eventOrderLists)
            e.add(uid);

        return uid;
    }

    /**
     * Edits the data inside the event associated with the given UID.
     * This will set {@link EventManager#hasChanged} to true, as data has been changed.
     *
     * @param uid         the UID associated with the event to edit
     * @param name        the new name
     * @param description the new description
     * @return true if the event was successfully edited; false if the edit failed.
     */
    public boolean editEvent(long uid, String name, String description, ChapterListObject chapterListObject) {
        if (events.containsKey(uid)) {
            events.get(uid).setName(name);
            events.get(uid).setChapterListObject(chapterListObject);
            events.get(uid).setDescription(description);
            hasChanged = true;
            return true;
        }
        return false;
    }

    /**
     * Removes the event associated with the given UID.
     * Also removes the UID from each order list, and the UIDManager.
     * This will set {@link EventManager#hasChanged} to true, as data has been changed.
     *
     * @param uid the UID of the event to remove
     */
    public void removeEvent(long uid) {
        Event event = events.get(uid);
        chapterManager.getChapter(event.getChapterListObject().getUid()).getEvents().remove(event);
        events.remove(uid);
        UIDManager.removeUID(uid);
        for (LinkedList<Long> e : eventOrderLists)
            e.remove(uid);

        hasChanged = true;
    }

    /**
     * Creates an event, bypassing the UIDManager, and does not add the UID to
     * any event order list.
     * Note: This should only be used when loading a project, since events and
     * event order lists are loaded separately
     * This will set {@link EventManager#hasChanged} to true, as data has been changed.
     *
     * @param e the event to add.
     */
    public void addEvent(long uid, Event e) {
        events.put(uid, e);
        hasChanged = true;
    }

    public void addEvent(long uid, String name, String description, ChapterListObject chapterListObject) {
        if (chapterListObject != null) {
            events.put(uid, new Event(name, description, chapterListObject, chapterListObject.getColor()));
            hasChanged = true;
        } else {
            events.put(uid, new Event(name, description));
        }
    }

    /**
     * Returns a structure of data contained within the event, specified with the given UID.
     * The data returned is formatted like this:
     * <ul>
     *  <li>data[0] -- name
     *  <li>data[1] -- description
     * </ul>
     *
     * @param uid the UID of the event to get data from
     * @return an array with a constant size of 2
     */
    public Object[] getEventData(long uid) {
        Object[] data = new Object[4];
        Event event = events.get(uid);
        data[0] = event.getName();
        data[1] = event.getDescription();
        data[3] = event.getColor();

        return data;
    }

    /**
     * Returns a structure which contains all data within every event.
     * <p>
     * The data returned is formatted like this:
     * <ul>
     *  <li>data[i] -- array of event data
     *  <ul>
     *      <li>data[i][0] -- UID
     *      <li>data[i][1] -- name
     *      <li>data[i][2] -- description
     *  </ul>
     * </ul>
     * <p>
     * <p>
     * Example of contents:
     * <ul>
     *     <li>data[0][0] -- 1997L
     *     <li>data[0][1] -- "The Beginning"
     *     <li>data[0][2] -- "This is where it all began"
     *     <li>data[1][0] -- 2020L
     *     <li>data[1][1] -- "The End"
     *     <li>data[1][2] -- "This is where it all ended"
     * </ul>
     * <p>
     * <p>
     * Note: The data returned is unordered.
     *
     * @return a 2-dimensional array of event data
     */
    public Object[][] getEvents() {
        if (events.size() < 1)
            return null;

        Long[] uidOrder = events.keySet().toArray(new Long[events.size()]);
        Object[][] eventArray = new Object[uidOrder.length][4];

        for (int i = 0; i < uidOrder.length; i++) {
            long uid = uidOrder[i];
            Event eventRef = events.get(uid);
            eventArray[i][0] = uid;
            eventArray[i][1] = eventRef.getName();
            eventArray[i][2] = eventRef.getDescription();
            eventArray[i][3] = eventRef.getColor();
            eventArray[i][2] = eventRef.getChapterListObject();
        }
        return eventArray;
    }

    public ObservableList<String> getEvents2() {
        if (events.size() < 1)
            return null;

        Long[] uidOrder = events.keySet().toArray(new Long[events.size()]);

        for (int i = 0; i < uidOrder.length; i++) {
            long uid = uidOrder[i];
            Event eventRef = events.get(uid);
        }
        return null;
    }

    public ArrayList EventListChar() {
        ObservableList<Object> list = FXCollections.observableList(Collections.singletonList(eventOrderLists));

        ArrayList<Event> events = new ArrayList<>();

        return events;
    }

    /**
     * Returns an array of UIDs, specifying the order of events according
     * to the list at the specified index. Provides boundary-checking.
     *
     * @param eventOrderList the index to the event order list to use
     * @return the order of event UIDs
     */
    public Long[] getEventOrder(int eventOrderList) {
        if (eventOrderLists == null)
            return null;
        if (eventOrderList >= eventOrderLists.size() || eventOrderList < 0)
            return null;

        return eventOrderLists.get(eventOrderList).toArray(
                new Long[eventOrderLists.get(eventOrderList).size()]
        );
    }


    /**
     * Return the @Event object by the UID
     *
     * @param uid the eventobjet uid
     * @return event
     * @author Hazem Elkhalil
     */
    public Event getEvent(Long uid) {
        return this.events.get(uid);
    }

    /**
     * Returns the index of a given event in a given event order list, else returns -1.
     *
     * @param uid event
     * @return event index
     * @author Jim Andersson
     */
    public int getEventIndex(int eventOrderList, long uid) {
        Long[] events = getEventOrder(eventOrderList);

        for (int i = 0; i < events.length; i++) {
            if (events[i].equals(uid))
                return i;
        }
        return -1;
    }

    /**
     * Swap the place of two events on the timeline.
     *
     * @param orderList The order list on which the two events appear.
     * @param index1    index of event on the list
     * @param index2    index of event on the list
     * @author Jim Andersson
     */
    public void swapEvent(int orderList, int index1, int index2) {
        //TODO swap places between two events in the specified order list.
        Long[] order = getEventOrder(orderList);

        Long temp = order[index2];
        order[index2] = order[index1];
        order[index1] = temp;

        for (int i = 0; i < eventOrderLists.get(orderList).size(); i++) {
            eventOrderLists.get(orderList).set(i, order[i]);
        }
        hasChanged = true;
    }

    /**
     * Move event from one spot on the timeline to another.
     *
     * @param orderList The event list on which the given event appears
     * @param fromIndex The dragged event which has been selected through MouseEvent
     *                  (see {@link com.team34.view.timeline.Timeline})
     * @param toIndex   The event on which the dragged event is released
     * @author Jim Andersson
     */
    public void moveEvent(int orderList, int fromIndex, int toIndex) {
        // TODO move to (insert at) specified location.

        Long[] order = getEventOrder(orderList);

        // When the event is moved in the right direction on the timeline,
        // the events inside the scope of the loop are shifted to the left.
        if (fromIndex < toIndex) {
            for (int i = fromIndex; i < toIndex; i++) {
                Long temp = order[i];
                order[i] = order[i + 1];
                order[i + 1] = temp;
            }
        }

        // When the event is moved in the left direction on the timeline,
        // the events inside the scope of the loop are shifted to the right.
        if (toIndex < fromIndex) {
            for (int i = fromIndex; i > toIndex; i--) {
                Long temp = order[i];
                order[i] = order[i - 1];
                order[i - 1] = temp;
            }
        }

        for (int i = 0; i < eventOrderLists.get(orderList).size(); i++) {
            eventOrderLists.get(orderList).set(i, order[i]);
        }
        hasChanged = true;
    }

    /**
     * Adds an event order list.
     * This should only be used when loading a project.
     *
     * @param orderList the event order list to add
     */
    public void addOrderList(LinkedList<Long> orderList) {
        eventOrderLists.add(orderList);
    }

    /**
     * Removes all events and event order lists, and sets {@link EventManager#hasChanged} to false.
     */
    public void clear() {
        events.clear();
        eventOrderLists.clear();
        hasChanged = false;
    }

    /**
     * Returns whether the data inside this class has changed
     *
     * @return the value of {@link EventManager#hasChanged}
     */
    public boolean hasChanged() {
        return hasChanged;
    }

    /**
     * Sets {@link EventManager#hasChanged} to false.
     */
    public void resetChanges() {
        hasChanged = false;
    }

    public Event getEvent(String eventName) {
        for (Event value : events.values()) {
            if (value.getName().equalsIgnoreCase(eventName))
                return value;
        }
        return null;
    }
}
