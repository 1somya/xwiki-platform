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
package com.xpn.xwiki.wysiwyg.client.plugin.macro;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.xpn.xwiki.wysiwyg.client.Wysiwyg;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.AbstractPlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.macro.exec.CollapseExecutable;
import com.xpn.xwiki.wysiwyg.client.plugin.macro.exec.InsertExecutable;
import com.xpn.xwiki.wysiwyg.client.plugin.macro.exec.RefreshExecutable;
import com.xpn.xwiki.wysiwyg.client.plugin.macro.ui.EditMacroDialog;
import com.xpn.xwiki.wysiwyg.client.plugin.macro.ui.SelectMacroDialog;
import com.xpn.xwiki.wysiwyg.client.util.Config;
import com.xpn.xwiki.wysiwyg.client.widget.CompositeDialogBox;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;

/**
 * WYSIWYG editor plug-in for inserting macros and for editing macro parameters.
 * 
 * @version $Id$
 */
public class MacroPlugin extends AbstractPlugin implements CloseHandler<CompositeDialogBox>
{
    /**
     * Rich text area command for refreshing macro output.
     */
    public static final Command REFRESH = new Command("macroRefresh");

    /**
     * Rich text area command for collapsing all the macros.
     */
    public static final Command COLLAPSE = new Command("macroCollapseAll");

    /**
     * Rich text area command for expanding all the macros.
     */
    public static final Command EXPAND = new Command("macroExpandAll");

    /**
     * Rich text area command for inserting a macro in place of the current selection.
     */
    public static final Command INSERT = new Command("macroInsert");

    /**
     * The dialog used for editing macro parameters and content.
     */
    private EditMacroDialog editDialog;

    /**
     * The dialog used for selecting one of the available macros before insertion.
     */
    private SelectMacroDialog selectDialog;

    /**
     * Hides macro meta data and displays macro output in a read only text box.
     */
    private MacroDisplayer displayer;

    /**
     * Controls the currently selected macros.
     */
    private MacroSelector selector;

    /**
     * Provides a user interface extension to allow users to manipulate macros using the top-level menu of the WYSIWYG
     * editor.
     */
    private MacroMenuExtension menuExtension;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#init(Wysiwyg, RichTextArea, Config)
     */
    public void init(Wysiwyg wysiwyg, RichTextArea textArea, Config config)
    {
        super.init(wysiwyg, textArea, config);

        displayer = GWT.create(MacroDisplayer.class);
        displayer.setTextArea(getTextArea());
        selector = new MacroSelector(displayer);

        getTextArea().getCommandManager().registerCommand(REFRESH,
            new RefreshExecutable(getConfig().getParameter("syntax", "xhtml/1.0")));
        getTextArea().getCommandManager().registerCommand(COLLAPSE, new CollapseExecutable(selector, true));
        getTextArea().getCommandManager().registerCommand(EXPAND, new CollapseExecutable(selector, false));
        getTextArea().getCommandManager().registerCommand(INSERT, new InsertExecutable(selector));

        menuExtension = new MacroMenuExtension(this);
        getUIExtensionList().add(menuExtension.getExtension());
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#destroy()
     */
    public void destroy()
    {
        if (editDialog != null) {
            editDialog.hide();
            editDialog.removeFromParent();
            editDialog = null;
        }

        if (selectDialog != null) {
            selectDialog.hide();
            selectDialog.removeFromParent();
            selectDialog = null;
        }

        menuExtension.destroy();

        getTextArea().getCommandManager().unregisterCommand(REFRESH);
        getTextArea().getCommandManager().unregisterCommand(COLLAPSE);
        getTextArea().getCommandManager().unregisterCommand(EXPAND);
        getTextArea().getCommandManager().unregisterCommand(INSERT);

        selector.destroy();
        selector = null;

        displayer.destroy();
        displayer = null;

        super.destroy();
    }

    /**
     * @return the macro selector
     */
    public MacroSelector getSelector()
    {
        return selector;
    }

    /**
     * Shows the edit macro dialog.
     */
    public void edit()
    {
        edit(true);
    }

    /**
     * Either shows the edit macro dialog or applies user changes, depending in the given flag.
     * 
     * @param show whether to show the edit macro dialog or apply the changes made using the dialog
     */
    private void edit(boolean show)
    {
        if (show) {
            getEditDialog().setMacroCall(new MacroCall(getTextArea().getCommandManager().getStringValue(INSERT)));
            getEditDialog().center();
        } else {
            getTextArea().setFocus(true);
            if (!getEditDialog().isCanceled()) {
                getTextArea().getCommandManager().execute(INSERT, getEditDialog().getMacroCall().toString());
            }
        }
    }

    /**
     * Shows the insert macro dialog.
     */
    public void insert()
    {
        insert(true);
    }

    /**
     * Either shows the select macro dialog or opens the edit macro dialog for the selected macro, depending in the
     * given flag.
     * 
     * @param show whether to show the select macro dialog or the edit macro dialog for the selected macro
     */
    private void insert(boolean show)
    {
        if (show) {
            getSelectDialog().center();
        } else {
            if (getSelectDialog().isCanceled()) {
                getTextArea().setFocus(true);
            } else {
                MacroCall macroCall = new MacroCall();
                macroCall.setName(getSelectDialog().getSelectedMacro());
                getEditDialog().setMacroCall(macroCall);
                getEditDialog().center();
            }
        }
    }

    /**
     * We use this method in order to lazy load the edit dialog.
     * 
     * @return the dialog used for editing macro parameters and content
     */
    private EditMacroDialog getEditDialog()
    {
        if (editDialog == null) {
            editDialog = new EditMacroDialog(getConfig());
            saveRegistration(editDialog.addCloseHandler(this));
        }
        return editDialog;
    }

    /**
     * We use this method in order to lazy load the select dialog.
     * 
     * @return the dialog used for selecting one of the available macros before insertion
     */
    private SelectMacroDialog getSelectDialog()
    {
        if (selectDialog == null) {
            selectDialog = new SelectMacroDialog(getConfig());
            saveRegistration(selectDialog.addCloseHandler(this));
        }
        return selectDialog;
    }

    /**
     * {@inheritDoc}
     * 
     * @see CloseHandler#onClose(CloseEvent)
     */
    public void onClose(CloseEvent<CompositeDialogBox> event)
    {
        if (event.getTarget() == getEditDialog() && !event.isAutoClosed()) {
            edit(false);
        } else if (event.getTarget() == getSelectDialog() && !event.isAutoClosed()) {
            insert(false);
        }
    }
}
