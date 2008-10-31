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
package com.xpn.xwiki.wysiwyg.client.widget.rta.history.internal;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.dom.DOMUtils;
import com.xpn.xwiki.wysiwyg.client.dom.Document;
import com.xpn.xwiki.wysiwyg.client.dom.Range;
import com.xpn.xwiki.wysiwyg.client.dom.Selection;
import com.xpn.xwiki.wysiwyg.client.dom.Text;
import com.xpn.xwiki.wysiwyg.client.util.ShortcutKey;
import com.xpn.xwiki.wysiwyg.client.util.ShortcutKeyFactory;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.CommandListener;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.CommandManager;
import com.xpn.xwiki.wysiwyg.client.widget.rta.history.History;

/**
 * Default implementation for {@link History}.
 * 
 * @version $Id$
 */
public class DefaultHistory implements History, KeyboardListener, CommandListener
{
    /**
     * The rich text area for which we record the history. Actions taken on this rich text area trigger the update of
     * the history. Using the {@link History} interface the content of this rich text area can be reverted to a previous
     * version.
     */
    private final RichTextArea textArea;

    /**
     * The maximum number of entries the history can hold. While the history is full, each time we want to add a new
     * entry we have to remove the oldest one to make room.
     */
    private final int capacity;

    /**
     * The shortcut key that triggers the undo action.
     */
    private final ShortcutKey undoKey = ShortcutKeyFactory.createCtrlShortcutKey('Z');

    /**
     * The shortcut key that triggers the redo action.
     */
    private final ShortcutKey redoKey = ShortcutKeyFactory.createCtrlShortcutKey('Y');

    /**
     * The oldest stored history entry.
     */
    private Entry oldestEntry;

    /**
     * Points to the history entry storing the current version of the edited content.
     */
    private Entry currentEntry;

    /**
     * The previous keyboard action done by the user. The history is updated whenever the user changes the type of
     * keyboard action he does on the edited content.
     */
    private KeyboardAction previousKeyboardAction;

    /**
     * Starts to record the history of the given rich text area. At each moment the number of history entries stored is
     * at most the specified capacity.
     * 
     * @param textArea the rich text area for which to record the history.
     * @param capacity the maximum number of history entries that can be stored.
     */
    public DefaultHistory(RichTextArea textArea, int capacity)
    {
        assert (capacity > 1);
        this.capacity = capacity;

        this.textArea = textArea;
        textArea.addShortcutKey(undoKey);
        textArea.addShortcutKey(redoKey);
        textArea.addKeyboardListener(this);
        textArea.getCommandManager().addCommandListener(this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see History#canRedo()
     */
    public boolean canRedo()
    {
        return currentEntry != null && currentEntry.getNextEntry() != null && !isDirty();
    }

    /**
     * {@inheritDoc}
     * 
     * @see History#canUndo()
     */
    public boolean canUndo()
    {
        return currentEntry != null && (currentEntry.getPreviousEntry() != null || isDirty());
    }

    /**
     * {@inheritDoc}
     * 
     * @see History#redo()
     */
    public void redo()
    {
        if (canRedo()) {
            load(currentEntry.getNextEntry());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see History#undo()
     */
    public void undo()
    {
        if (canUndo()) {
            if (!canRedo()) {
                save();
            }
            load(currentEntry.getPreviousEntry());
        }
    }

    /**
     * NOTE: the number of stored entries is computed each time because the history length can drop significantly if the
     * user reverts and continues editing.
     * 
     * @return true if the number of history entries stored is equal or exceeds the {@link #capacity}.
     */
    private boolean isFull()
    {
        int entryCount = 0;
        Entry entry = oldestEntry;
        while (entry != null) {
            entryCount++;
            entry = entry.getNextEntry();
        }
        return entryCount >= capacity;
    }

    /**
     * @return true if there are no history entries stored.
     */
    private boolean isEmpty()
    {
        return oldestEntry == null;
    }

    /**
     * @return true if the user is doing an edit action on the current version of the edited content. The stored HTML
     *         content in the current history entry should be different from the one in the text area.
     */
    private boolean isDirty()
    {
        return currentEntry != null && !currentEntry.getContent().equals(textArea.getHTML());
    }

    /**
     * @param entry the history entry to load in the rich text area.
     */
    private void load(Entry entry)
    {
        currentEntry = entry;

        textArea.setHTML(entry.getContent());
        Document doc = textArea.getDocument();

        Range range = doc.createRange();
        range.setStart(getNode(doc, entry.getStartPath()), entry.getStartPath().get(0));
        range.setEnd(getNode(doc, entry.getEndPath()), entry.getEndPath().get(0));

        Selection selection = doc.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
    }

    /**
     * @param node a DOM node.
     * @param offset the offset inside the given node. It represents the number of characters in case of text node and
     *            the number of child nodes otherwise.
     * @return the path from the given node to root of the DOM tree, where each token in the path represents the
     *         normalized index of the node at that level.
     */
    private static List<Integer> getPath(Node node, int offset)
    {
        List<Integer> path = new ArrayList<Integer>();
        if (node.getNodeType() == Node.TEXT_NODE) {
            path.add(Text.as(node).getOffset() + offset);
        } else if (offset == node.getChildNodes().getLength()) {
            path.add(DOMUtils.getInstance().getNormalizedChildCount(node));
        } else {
            path.add(DOMUtils.getInstance().getNormalizedNodeIndex(node.getChildNodes().getItem(offset)));
        }
        Node ancestor = node;
        while (ancestor.getParentNode() != null) {
            path.add(DOMUtils.getInstance().getNormalizedNodeIndex(ancestor));
            ancestor = ancestor.getParentNode();
        }
        return path;
    }

    /**
     * @param doc a DOM document
     * @param path a DOM path. Each token in the path is a node index.
     * @return the node at the end of the given path in the specified DOM tree.
     */
    private static Node getNode(Document doc, List<Integer> path)
    {
        Node node = doc;
        for (int i = path.size() - 1; i > 0; i--) {
            node = node.getChildNodes().getItem(path.get(i));
        }
        return node;
    }

    /**
     * Saves the current state of the underlying rich text area.
     */
    private void save()
    {
        if (!isEmpty() && !isDirty()) {
            return;
        }

        Selection selection = textArea.getDocument().getSelection();
        Range range = selection.getRangeAt(0);

        List<Integer> startPath = getPath(range.getStartContainer(), range.getStartOffset());
        List<Integer> endPath = getPath(range.getEndContainer(), range.getEndOffset());

        Entry newestEntry = new Entry(textArea.getHTML(), startPath, endPath);
        if (currentEntry != null) {
            currentEntry.setNextEntry(newestEntry);
        }
        newestEntry.setPreviousEntry(currentEntry);
        currentEntry = newestEntry;
        if (oldestEntry == null) {
            oldestEntry = currentEntry;
        }

        if (isFull()) {
            oldestEntry = oldestEntry.getNextEntry();
            oldestEntry.setPreviousEntry(null);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyboardListener#onKeyDown(Widget, char, int)
     */
    public void onKeyDown(Widget sender, char keyCode, int modifiers)
    {
        if (sender == textArea && (modifiers & KeyboardListener.MODIFIER_CTRL) == 0) {
            KeyboardAction currentKeyboardAction = KeyboardAction.valueOf(keyCode, modifiers);
            if (isEmpty() || currentKeyboardAction != previousKeyboardAction) {
                save();
            }
            previousKeyboardAction = currentKeyboardAction;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyboardListener#onKeyPress(Widget, char, int)
     */
    public void onKeyPress(Widget sender, char keyCode, int modifiers)
    {
        // ignore
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyboardListener#onKeyUp(Widget, char, int)
     */
    public void onKeyUp(Widget sender, char keyCode, int modifiers)
    {
        if (sender == textArea) {
            if ((modifiers & KeyboardListener.MODIFIER_CTRL) != 0) {
                if (keyCode == undoKey.getKeyCode()) {
                    undo();
                } else if (keyCode == redoKey.getKeyCode()) {
                    redo();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see CommandListener#onCommand(CommandManager, Command, String)
     */
    public void onCommand(CommandManager sender, Command command, String param)
    {
        if (sender == textArea.getCommandManager()) {
            if (command != Command.UNDO && command != Command.REDO) {
                save();
                previousKeyboardAction = null;
            }
        }
    }
}
