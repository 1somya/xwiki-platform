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
package com.xpn.xwiki.wysiwyg.client.plugin.importer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PushButton;
import com.xpn.xwiki.wysiwyg.client.Wysiwyg;
import com.xpn.xwiki.wysiwyg.client.editor.Images;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.importer.ui.ImporterDialog;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.AbstractPlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.FocusWidgetUIExtension;
import com.xpn.xwiki.wysiwyg.client.util.Config;
import com.xpn.xwiki.wysiwyg.client.widget.CompositeDialogBox;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;

/**
 * Office Importer wysiwyg plugin.
 * 
 * @version $Id$
 */
public class ImporterPlugin extends AbstractPlugin implements ClickHandler, CloseHandler<CompositeDialogBox>
{
    /**
     * Import button placed on the tool bar.
     */
    private PushButton importPushButton;

    /**
     * Importer dialog used to communicate with the user.
     */
    private ImporterDialog importerDialog;

    /**
     * The toolbar extension used to add the link buttons to the toolbar.
     */
    private final FocusWidgetUIExtension toolBarExtension = new FocusWidgetUIExtension("toolbar");

    /**
     * {@inheritDoc}
     */
    public void init(Wysiwyg wysiwyg, RichTextArea textArea, Config config)
    {
        super.init(wysiwyg, textArea, config);

        if (getTextArea().getCommandManager().isSupported(Command.INSERT_HTML)) {
            importPushButton = new PushButton(Images.INSTANCE.importer().createImage());
            saveRegistration(importPushButton.addClickHandler(this));
            importPushButton.setTitle(Strings.INSTANCE.importerToolTip());
            toolBarExtension.addFeature("importer", importPushButton);
        }

        if (toolBarExtension.getFeatures().length > 0) {
            getUIExtensionList().add(toolBarExtension);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void destroy()
    {
        if (importPushButton != null) {
            importPushButton.removeFromParent();
            importPushButton = null;
        }
        if (importerDialog != null) {
            importerDialog.hide();
            importerDialog.removeFromParent();
            importerDialog = null;
        }
        if (toolBarExtension.getFeatures().length > 0) {
            toolBarExtension.clearFeatures();
        }
        super.destroy();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ClickHandler#onClick(ClickEvent)
     */
    public void onClick(ClickEvent event)
    {
        if (event.getSource() == importPushButton) {
            getImporterDialog().center();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see CloseHandler#onClose(CloseEvent)
     */
    public void onClose(CloseEvent<CompositeDialogBox> event)
    {
        getTextArea().setFocus(true);
        if (importerDialog.getResult() != null) {
            getTextArea().getCommandManager().execute(Command.INSERT_HTML, importerDialog.getResult());
        }
    }

    /**
     * @return The importer dialog instance.
     */
    private ImporterDialog getImporterDialog()
    {
        if (null == importerDialog) {            
            importerDialog = new ImporterDialog(getConfig());
            saveRegistration(importerDialog.addCloseHandler(this));
        }
        return importerDialog;
    }
}
