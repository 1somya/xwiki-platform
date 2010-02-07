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
package org.xwiki.gwt.wysiwyg.client.plugin.table;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.gwt.user.client.Config;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.wysiwyg.client.plugin.internal.AbstractPlugin;
import org.xwiki.gwt.wysiwyg.client.plugin.internal.FocusWidgetUIExtension;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.DeleteCol;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.DeleteRow;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.DeleteTable;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.InsertColAfter;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.InsertColBefore;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.InsertRowAfter;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.InsertRowBefore;
import org.xwiki.gwt.wysiwyg.client.plugin.table.feature.InsertTable;
import org.xwiki.gwt.wysiwyg.client.plugin.table.ui.TableMenuExtension;


/**
 * Plug-in allowing to manipulate tables in the WYSIWYG editor.
 * 
 * @version $Id$
 */
public class TablePlugin extends AbstractPlugin
{
    /**
     * List of table features (example : InsertTable, DeleteCol).
     */
    private final List<TableFeature> features = new ArrayList<TableFeature>();

    /**
     * The plug-in toolbar.
     */
    private final FocusWidgetUIExtension toolBarExtension = new FocusWidgetUIExtension("toolbar");

    /**
     * The menu extension of this plugin.
     */
    private TableMenuExtension menuExtension;

    /**
     * Make a feature available.
     * 
     * @param rta WYSIWYG RichTextArea.
     * @param feature feature to enable.
     */
    private void addFeature(RichTextArea rta, TableFeature feature)
    {
        rta.getCommandManager().registerCommand(feature.getCommand(), feature);
        features.add(feature);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#init(XRichTextArea, Config)
     */
    public void init(RichTextArea rta, Config config)
    {
        super.init(rta, config);

        addFeature(rta, new InsertTable(this));
        addFeature(rta, new InsertRowBefore(this));
        addFeature(rta, new InsertRowAfter(this));
        addFeature(rta, new DeleteRow(this));
        addFeature(rta, new InsertColBefore(this));
        addFeature(rta, new InsertColAfter(this));
        addFeature(rta, new DeleteCol(this));
        addFeature(rta, new DeleteTable(this));

        menuExtension = new TableMenuExtension(this);
        getUIExtensionList().add(menuExtension);

        // Disable the standard table editing features of Firefox since they don't take
        // table headings (th) into account.
        rta.getDocument().execCommand("enableInlineTableEditing", "false");

        getUIExtensionList().add(toolBarExtension);
    }

    /**
     * @return The list of the features exposed by the plugin.
     */
    public List<TableFeature> getFeatures()
    {
        return features;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#destroy()
     */
    public void destroy()
    {
        for (TableFeature feature : features) {
            feature.destroy();
        }
        features.clear();
        toolBarExtension.clearFeatures();
        super.destroy();
    }
}
