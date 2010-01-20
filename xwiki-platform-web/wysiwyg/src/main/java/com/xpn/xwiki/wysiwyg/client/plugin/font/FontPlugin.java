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
package com.xpn.xwiki.wysiwyg.client.plugin.font;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.gwt.dom.client.Style;
import org.xwiki.gwt.user.client.Config;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;
import org.xwiki.gwt.user.client.ui.rta.cmd.internal.InlineStyleExecutable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.AbstractStatefulPlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.FocusWidgetUIExtension;

/**
 * Plug-in for manipulating the font size and font family used while editing.
 * 
 * @version $Id$
 */
public class FontPlugin extends AbstractStatefulPlugin implements ChangeHandler
{
    /**
     * The list of default font names.
     */
    private static final String DEFAULT_FONT_NAMES =
        "andale mono,arial,arial black,book antiqua,comic sans ms"
            + ",courier new,georgia,helvetica,impact,symbol,tahoma,terminal"
            + ",times new roman,trebuchet ms,verdana,webdings,wingdings";

    /**
     * The list of default font sizes.
     */
    private static final String DEFAULT_FONT_SIZES = "8pt,10pt,12pt,14pt,18pt,24pt,36pt";

    /**
     * The association between pickers and the commands that are executed on change events.
     */
    private final Map<Picker, Command> pickers = new HashMap<Picker, Command>();

    /**
     * User interface extension for the editor tool bar.
     */
    private final FocusWidgetUIExtension toolBarExtension = new FocusWidgetUIExtension("toolbar");

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStatefulPlugin#init(RichTextArea, Config)
     */
    public void init(RichTextArea textArea, Config config)
    {
        super.init(textArea, config);

        // Register custom executables.
        getTextArea().getCommandManager().registerCommand(Command.FONT_NAME,
            new InlineStyleExecutable(textArea, Style.FONT_FAMILY));
        getTextArea().getCommandManager().registerCommand(Command.FONT_SIZE,
            new InlineStyleExecutable(textArea, Style.FONT_SIZE));

        addFeature("fontname", Command.FONT_NAME, new FontFamilyPicker(), Strings.INSTANCE.font(), "fontNames",
            DEFAULT_FONT_NAMES);
        addFeature("fontsize", Command.FONT_SIZE, new FontSizePicker(), Strings.INSTANCE.fontSize(), "fontSizes",
            DEFAULT_FONT_SIZES);

        if (toolBarExtension.getFeatures().length > 0) {
            registerTextAreaHandlers();
            getUIExtensionList().add(toolBarExtension);
        }
    }

    /**
     * Makes the specified feature available to be used on the tool bar.
     * 
     * @param name the feature name
     * @param command the rich text area command that is executed by this feature
     * @param picker the widget to be placed on the tool bar
     * @param title the tool tip used on the tool bar widget
     * @param parameter the configuration parameter that holds the list of possible values for this feature
     * @param defaultValues the default list of possible values for this list
     */
    private void addFeature(String name, Command command, ListBox picker, String title, String parameter,
        String defaultValues)
    {
        if (getTextArea().getCommandManager().isSupported(command)) {
            picker.setTitle(title);
            saveRegistration(picker.addChangeHandler(this));

            String[] values = getConfig().getParameter(parameter, defaultValues).split("\\s*,\\s*");
            for (int i = 0; i < values.length; i++) {
                picker.addItem(values[i]);
            }

            toolBarExtension.addFeature(name, picker);
            pickers.put((Picker) picker, command);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStatefulPlugin#destroy()
     */
    public void destroy()
    {
        for (Picker picker : pickers.keySet()) {
            ((Widget) picker).removeFromParent();
        }
        pickers.clear();

        toolBarExtension.clearFeatures();

        super.destroy();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ChangeHandler#onChange(ChangeEvent)
     */
    public void onChange(ChangeEvent event)
    {
        Command command = pickers.get(event.getSource());
        if (command != null && ((FocusWidget) event.getSource()).isEnabled()) {
            getTextArea().setFocus(true);
            getTextArea().getCommandManager().execute(command, ((Picker) event.getSource()).getSelectedValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStatefulPlugin#update()
     */
    public void update()
    {
        for (Map.Entry<Picker, Command> entry : pickers.entrySet()) {
            if (((FocusWidget) entry.getKey()).isEnabled()) {
                entry.getKey().setSelectedValue(getTextArea().getCommandManager().getStringValue(entry.getValue()));
            }
        }
    }
}
