/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.observation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;

/**
 * Default implementation of the {@link ObservationManager}.
 * 
 * @todo Check how threadsafe this implementation is...
 * @version $Id:$
 */
public class DefaultObservationManager implements ObservationManager
{
    /** Internal class for holding an (Event, EventListener) pair. Too bad there's no simple Pair class in the JDK. */
    protected static class RegisteredListener
    {
        /** The event for which this listener is registered. */
        private Event event;

        /** The Event Listener. */
        private EventListener listener;

        /**
         * Simple constructor, stores the passed Event and Listener.
         * 
         * @param e The Event.
         * @param l The Listener.
         */
        RegisteredListener(Event e, EventListener l)
        {
            this.event = e;
            this.listener = l;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#equals(Object)
         */
        @Override
        public String toString()
        {
            return "{" + this.event + ", " + this.listener + "}";
        }
    }

    /**
     * Registered listeners. The Map key is the Event class name and the value is a List with (Event, Listener) pairs.
     * 
     * @todo Should we allow event inheritance?
     */
    private Map<String, List<RegisteredListener>> listeners = new HashMap<String, List<RegisteredListener>>();

    /**
     * {@inheritDoc}
     * 
     * @see ObservationManager#addListener(Event,org.xwiki.observation.EventListener)
     */
    public void addListener(Event event, EventListener eventListener)
    {
        // Check if this is a new Event type not already registered
        List<RegisteredListener> eventListeners = this.listeners.get(event.getClass().getName());
        if (eventListeners == null) {
            eventListeners = new ArrayList<RegisteredListener>();
            this.listeners.put(event.getClass().getName(), eventListeners);
        }

        // Check to see if the event/listener pair is already there
        for (Iterator<RegisteredListener> it = eventListeners.iterator(); it.hasNext();) {
            RegisteredListener pair = it.next();
            if (pair.listener == eventListener) {
                return;
            }
        }

        // Add the event/listener pair
        eventListeners.add(new RegisteredListener(event, eventListener));
    }

    /**
     * {@inheritDoc}
     * 
     * @see ObservationManager#removeListener(org.xwiki.observation.event.Event, EventListener)
     */
    public void removeListener(Event event, EventListener eventListener)
    {
        if (event == null) {
            removeListener(eventListener);
            return;
        }

        // Find the event key for the specified event
        List<RegisteredListener> eventListeners = this.listeners.get(event.getClass().getName());
        if (eventListeners == null) {
            return;
        }

        // Remove the listener
        for (Iterator<RegisteredListener> it = eventListeners.iterator(); it.hasNext();) {
            RegisteredListener pair = it.next();
            if (pair.listener == eventListener) {
                it.remove();
                break;
            }
        }

        // Remove the event-type list if this was the last listener for this event type
        if (eventListeners.isEmpty()) {
            this.listeners.remove(event);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ObservationManager#removeListener(EventListener)
     */
    public void removeListener(EventListener eventListener)
    {
        // Loop over all registered events and remove the specified listener
        // Loop over a copy in case the removeListener() call wants to
        // remove the entire event from the map.
        for (List<RegisteredListener> list : this.listeners.values()) {
            for (RegisteredListener p : list) {
                if (p.listener == eventListener) {
                    // TODO This is not efficient. We already found the element to remove, why
                    // search for it again?
                    removeListener(p.event, eventListener);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ObservationManager#notify(org.xwiki.observation.event.Event, Object, Object)
     */
    public void notify(Event event, Object source, Object data)
    {
        // Find all event/listeners pairs for this event
        List<RegisteredListener> eventListeners = this.listeners.get(event.getClass().getName());
        if (eventListeners == null) {
            return;
        }

        // Loop over a copy of the list in case it is altered by a listener
        List<RegisteredListener> copy = new ArrayList<RegisteredListener>(eventListeners);
        for (RegisteredListener pair : copy) {
            if (pair.event.matches(event)) {
                pair.listener.onEvent(event, source, data);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ObservationManager#notify(org.xwiki.observation.event.Event, Object)
     */
    public void notify(Event event, Object source)
    {
        notify(event, source, null);
    }
}
