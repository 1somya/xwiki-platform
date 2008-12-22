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

import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.user.client.ui.PushButton;
import com.xpn.xwiki.wysiwyg.client.editor.Images;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.table.TablePlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.table.util.TableUtils;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;

/**
 * Feature allowing to insert a column at the left of an existing column. After the insertion the caret is positioned in
 * the cell at the left of the originally edited cell. It is disabled when the caret is positioned outside of a column.
 * A column is a set of cells aligned vertically in a table.
 * 
 * @version $Id$
 */
public class InsertColBefore extends AbstractTableFeature
{
    /**
     * Feature name.
     */
    private static final String NAME = "insertcolbefore";

    /**
     * Initialize the feature. Table features needs to be aware of the plug-in (here the ClickListener) since they hold
     * their own PushButton.
     * 
     * @param plugin table plug-in.
     */
    public InsertColBefore(TablePlugin plugin)
    {
        super(NAME, new Command(NAME), new PushButton(Images.INSTANCE.insertColBefore().createImage(), plugin),
            Strings.INSTANCE.insertColBefore(), plugin);
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
        TableUtils.getInstance().insertCol(rta.getDocument(), true);
        TableUtils.getInstance().putCaretInNode(rta, TableUtils.getInstance().getPreviousCellInRow(currentCell));
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
            && TableUtils.getInstance().getCell(TableUtils.getInstance().getCaretNode(rta.getDocument())) != null;
    }
}
