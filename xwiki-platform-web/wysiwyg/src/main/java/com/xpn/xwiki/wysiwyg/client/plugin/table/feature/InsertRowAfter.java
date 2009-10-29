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
package com.xpn.xwiki.wysiwyg.client.plugin.table.feature;

import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;

import com.google.gwt.dom.client.TableCellElement;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.table.TablePlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.table.util.TableUtils;

/**
 * Feature allowing to insert a row bellow the currently edited row. After the insertion the caret is positioned in the
 * cell bellow the originally edited cell. It is disabled when the caret is positioned outside of a row. A row is a set
 * of cells aligned horizontally in a table.
 * 
 * @version $Id$
 */
public class InsertRowAfter extends AbstractTableFeature
{
    /**
     * Feature name.
     */
    public static final String NAME = "insertrowafter";

    /**
     * Initialize the feature. Table features needs to be aware of the plug-in (here the ClickListener) since they hold
     * their own PushButton.
     * 
     * @param plugin table plug-in.
     */
    public InsertRowAfter(TablePlugin plugin)
    {
        super(NAME, new Command(NAME), Strings.INSTANCE.insertRowAfter(), plugin);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#execute(RichTextArea, String)
     */
    public boolean execute(RichTextArea rta, String parameter)
    {
        TableCellElement currentCell =
            TableUtils.getInstance().getCell(TableUtils.getInstance().getCaretNode(rta.getDocument()));
        TableUtils.getInstance().insertRow(rta.getDocument(), false);
        TableUtils.getInstance().putCaretInNode(rta, TableUtils.getInstance().getNextCellInColumn(currentCell));
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#isEnabled(RichTextArea)
     */
    public boolean isEnabled(RichTextArea rta)
    {
        return super.isEnabled(rta)
            && TableUtils.getInstance().getRow(TableUtils.getInstance().getCaretNode(rta.getDocument())) != null;
    }
}
