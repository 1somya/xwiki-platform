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
package com.xpn.xwiki.wysiwyg.client.plugin.color;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.Wysiwyg;
import com.xpn.xwiki.wysiwyg.client.editor.Images;
import com.xpn.xwiki.wysiwyg.client.editor.RichTextEditor;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.AbstractPlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.FocusWidgetUIExtension;
import com.xpn.xwiki.wysiwyg.client.util.Config;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;

/**
 * {@link RichTextEditor} plug-in for controlling the text color and the background color. It installs two push buttons
 * on the tool bar, each opening a color picker dialog, which is synchronized with the text area.
 */
public class ColorPlugin extends AbstractPlugin implements ClickListener, PopupListener
{
    private PushButton backColor;

    private ColorPicker backColorPicker;

    private PushButton foreColor;

    private ColorPicker foreColorPicker;

    private final FocusWidgetUIExtension toolBarExtension = new FocusWidgetUIExtension("toolbar");

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#init(Wysiwyg, RichTextArea, Config)
     */
    public void init(Wysiwyg wysiwyg, RichTextArea textArea, Config config)
    {
        super.init(wysiwyg, textArea, config);

        if (getTextArea().getCommandManager().isSupported(Command.FORE_COLOR)) {
            foreColor = new PushButton(Images.INSTANCE.foreColor().createImage(), this);
            foreColor.setTitle(Strings.INSTANCE.foreColor());
            toolBarExtension.addFeature("forecolor", foreColor);

            foreColorPicker = new ColorPicker();
            foreColorPicker.addPopupListener(this);
        }

        if (getTextArea().getCommandManager().isSupported(Command.BACK_COLOR)) {
            backColor = new PushButton(Images.INSTANCE.backColor().createImage(), this);
            backColor.setTitle(Strings.INSTANCE.backColor());
            toolBarExtension.addFeature("backcolor", backColor);

            backColorPicker = new ColorPicker();
            backColorPicker.addPopupListener(this);
        }

        if (toolBarExtension.getFeatures().length > 0) {
            getTextArea().addClickListener(this);
            getUIExtensionList().add(toolBarExtension);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#destroy()
     */
    public void destroy()
    {
        if (foreColor != null) {
            foreColor.removeFromParent();
            foreColor.removeClickListener(this);
            foreColor = null;

            foreColorPicker.hide();
            foreColorPicker.removeFromParent();
            foreColorPicker.removePopupListener(this);
            foreColorPicker = null;
        }

        if (backColor != null) {
            backColor.removeFromParent();
            backColor.removeClickListener(this);
            backColor = null;

            backColorPicker.hide();
            backColorPicker.removeFromParent();
            backColorPicker.removePopupListener(this);
            backColorPicker = null;
        }

        if (toolBarExtension.getFeatures().length > 0) {
            getTextArea().removeClickListener(this);
            toolBarExtension.clearFeatures();
        }

        super.destroy();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ClickListener#onClick(Widget)
     */
    public void onClick(Widget sender)
    {
        if (sender == foreColor) {
            onForeColor(true);
        } else if (sender == backColor) {
            onBackColor(true);
        } else {
            foreColorPicker.hide();
            backColorPicker.hide();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see PopupListener#onPopupClosed(PopupPanel, boolean)
     */
    public void onPopupClosed(PopupPanel sender, boolean autoHide)
    {
        if (sender == foreColorPicker && !autoHide) {
            onForeColor(false);
        } else if (sender == backColorPicker && !autoHide) {
            onBackColor(false);
        }
    }

    public void onForeColor(boolean show)
    {
        if (show) {
            String color = getTextArea().getCommandManager().getStringValue(Command.FORE_COLOR);
            foreColorPicker.setColor(color);

            int left = foreColor.getAbsoluteLeft();
            int top = foreColor.getAbsoluteTop() + foreColor.getOffsetHeight();
            foreColorPicker.setPopupPosition(left, top);

            foreColorPicker.show();
        } else {
            String color = foreColorPicker.getColor();
            if (color != null) {
                getTextArea().getCommandManager().execute(Command.FORE_COLOR, color);
            }
        }
    }

    public void onBackColor(boolean show)
    {
        if (show) {
            String color = getTextArea().getCommandManager().getStringValue(Command.BACK_COLOR);
            backColorPicker.setColor(color);

            int left = backColor.getAbsoluteLeft();
            int top = backColor.getAbsoluteTop() + backColor.getOffsetHeight();
            backColorPicker.setPopupPosition(left, top);

            backColorPicker.show();
        } else {
            String color = backColorPicker.getColor();
            if (color != null) {
                getTextArea().getCommandManager().execute(Command.BACK_COLOR, color);
            }
        }
    }
}
