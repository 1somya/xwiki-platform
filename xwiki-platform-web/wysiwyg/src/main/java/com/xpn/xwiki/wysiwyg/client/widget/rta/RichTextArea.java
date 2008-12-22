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
 */
package com.xpn.xwiki.wysiwyg.client.widget.rta;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.xpn.xwiki.wysiwyg.client.dom.Document;
import com.xpn.xwiki.wysiwyg.client.dom.Event;
import com.xpn.xwiki.wysiwyg.client.util.ShortcutKey;
import com.xpn.xwiki.wysiwyg.client.util.ShortcutKeyFactory;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.CommandManager;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.internal.DefaultCommandManager;
import com.xpn.xwiki.wysiwyg.client.widget.rta.history.History;
import com.xpn.xwiki.wysiwyg.client.widget.rta.history.internal.DefaultHistory;
import com.xpn.xwiki.wysiwyg.client.widget.rta.internal.BehaviorAdjuster;

/**
 * Extends the rich text area provided by GWT to add support for advanced editing.
 * 
 * @version $Id$
 */
public class RichTextArea extends com.google.gwt.user.client.ui.RichTextArea implements SourcesChangeEvents, HasName
{
    /**
     * @see #setHTML(String)
     */
    public static final String DIRTY = "dirty";

    /**
     * The command manager that executes commands on this rich text area.
     */
    private final CommandManager cm;

    /**
     * The history of this rich text area.
     */
    private final History history;

    /**
     * Overwrites the default behavior of the rich text area when DOM events are triggered by user actions and that
     * default behavior is either incomplete, unnatural, browser specific or buggy. This custom behavior can still be
     * prevented from a listener by calling {@link Event#preventDefault()} on the {@link #getCurrentEvent()}.
     */
    private final BehaviorAdjuster adjuster = (BehaviorAdjuster) GWT.create(BehaviorAdjuster.class);

    /**
     * The list of listeners that are notified when the content of the rich text area changes. Change events are
     * triggered only when the content of the rich text area is changed using {@link #setHTML(String)} or
     * {@link #setText(String)}.
     */
    private final ChangeListenerCollection changeListeners = new ChangeListenerCollection();

    /**
     * The list of shortcut keys activated on this rich text area.
     */
    private final List<ShortcutKey> shortcutKeys = new ArrayList<ShortcutKey>();

    /**
     * The name of this rich text area. It could be used to submit the edited contents to the server.
     */
    private String name;

    /**
     * The current event triggered on this rich text area. We need to store it because DOM.eventGetCurrentEvent() and
     * Event.getCurrentEvent() return null for RichTextArea events.
     * 
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3133
     */
    private Event currentEvent;

    /**
     * Creates a new rich text area.
     */
    public RichTextArea()
    {
        cm = new DefaultCommandManager(this);
        history = new DefaultHistory(this, 10);
        adjuster.setTextArea(this);
    }

    /**
     * Custom constructor allowing us to inject a mock command manager and a mock history. It was mainly added to be
     * used in unit tests.
     * 
     * @param cm Custom command manager
     * @param history Custom history mechanism.
     */
    public RichTextArea(CommandManager cm, History history)
    {
        this.cm = cm;
        this.history = history;
        adjuster.setTextArea(this);
    }

    /**
     * Activates the given shortcut key on this text area. This way the default behavior of the browser is prevented,
     * and the caller of this method can associate its own behavior with the specified shortcut key.
     * 
     * @param shortcutKey the shortcut key to activate.
     */
    public void addShortcutKey(ShortcutKey shortcutKey)
    {
        if (!shortcutKeys.contains(shortcutKey)) {
            shortcutKeys.add(shortcutKey);
        }
    }

    /**
     * Deactivates the specified shortcut key for this rich text area.
     * 
     * @param shortcutKey The shortcut key to be deactivated.
     */
    public void removeShortcutKey(ShortcutKey shortcutKey)
    {
        shortcutKeys.remove(shortcutKey);
    }

    /**
     * {@inheritDoc}
     * 
     * @see SourcesChangeEvents#addChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener listener)
    {
        changeListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see SourcesChangeEvents#removeChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener)
    {
        changeListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HasName#setName(String)
     */
    public void setName(String name)
    {
        if (!name.equals(this.name)) {
            this.name = name;
            changeListeners.fireChange(this);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see HasName#getName()
     */
    public String getName()
    {
        return name;
    }

    /**
     * NOTE: If the current browser doesn't support rich text editing this method returns <code>null</code>. You should
     * test the returned value and fail save to an appropriate behavior!<br/>
     * The appropriate test would be: <code><pre>
     * if (rta.isAttached() && rta.getDocument() == null) {
     *   // The current browser doesn't support rich text editing.
     * }
     * </pre></code>
     * 
     * @return The DOM document being edited with this rich text area.
     */
    public Document getDocument()
    {
        if (getElement().getTagName().equalsIgnoreCase("iframe")) {
            return IFrameElement.as(getElement()).getContentDocument().cast();
        } else {
            return null;
        }
    }

    /**
     * @return the {@link CommandManager} associated with this instance.
     */
    public CommandManager getCommandManager()
    {
        return cm;
    }

    /**
     * @return The history of this rich text area.
     */
    public History getHistory()
    {
        return history;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.RichTextArea#setHTML(String)
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3147
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3156
     */
    public void setHTML(String html)
    {
        // We add a dirty attribute and set its value to true in order to be able to overcome the Issue 3156. Precisely,
        // we test this attribute in the setHTMLImpl to avoid overwriting the contents when setHTML haven't been called.
        getElement().setAttribute(DIRTY, String.valueOf(true));
        super.setHTML(html);
        changeListeners.fireChange(this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.RichTextArea#setText(String)
     */
    public void setText(String text)
    {
        super.setText(text);
        changeListeners.fireChange(this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.RichTextArea#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    public void onBrowserEvent(com.google.gwt.user.client.Event event)
    {
        // We need to preview the event due to a GWT bug.
        // @see http://code.google.com/p/google-web-toolkit/issues/detail?id=729
        if (!previewEvent(event)) {
            return;
        }
        currentEvent = event.cast();
        if (currentEvent.getTypeInt() == Event.ONKEYDOWN) {
            if (shortcutKeys.contains(ShortcutKeyFactory.createShortcutKey(currentEvent))) {
                currentEvent.xPreventDefault();
            }
        }
        super.onBrowserEvent(event);
        adjuster.onBrowserEvent();
        currentEvent = null;
    }

    /**
     * We need to call DOM.previewEvent because there is a bug in GWT that prevents PopupPanel from previewing events
     * generated in in-line frames like the one in behind of this rich text area.
     * 
     * @param event a handle to the event being previewed.
     * @return <code>false</code> to cancel the event.
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=729
     */
    private native boolean previewEvent(com.google.gwt.user.client.Event event)
    /*-{
        return @com.google.gwt.user.client.DOM::previewEvent(Lcom/google/gwt/user/client/Event;)(event);
    }-*/;

    /**
     * @return the current event triggered on this rich text area.
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3133
     */
    public Event getCurrentEvent()
    {
        return currentEvent;
    }
}
