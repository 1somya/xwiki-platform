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
package com.xpn.xwiki.wysiwyg.client.plugin.link.ui;

import java.util.EnumSet;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.link.LinkConfig;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.NavigationListener;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.NavigationListenerCollection;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.SourcesNavigationEvents;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.WizardStep;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.NavigationListener.NavigationDirection;

/**
 * Default implementation for the link configuration parameters, such as link labels, link tooltip, or opening the link
 * in a new window or not.
 * 
 * @version $Id$
 */
public class LinkConfigWizardStep implements WizardStep, SourcesNavigationEvents, KeyPressHandler
{
    /**
     * The default style of the link configuration dialog.
     */
    public static final String DEFAULT_STYLE_NAME = "xLinkConfig";

    /**
     * The link data to be edited by this wizard step.
     */
    private LinkConfig linkData;

    /**
     * Collection of {@link NavigationListener}s, to be notified by navigation events from this step. Used to handle
     * default buttons in this wizard step, by firing next event whenever enter key is hit in the step form.
     */
    private final NavigationListenerCollection navigationListeners = new NavigationListenerCollection();

    /**
     * The text box where the user will insert the text of the link to create.
     */
    private final TextBox labelTextBox = new TextBox();

    /**
     * The text box to get the link tooltip.
     */
    private final TextBox tooltipTextBox = new TextBox();

    /**
     * The checkbox to query about whether the link should be opened in a new window or not.
     */
    private CheckBox newWindowCheckBox;

    /**
     * The panel holding the input for the label of the built link.
     */
    private final Panel mainPanel = new FlowPanel();

    /**
     * Default constructor.
     */
    public LinkConfigWizardStep()
    {
        String infoLabelStyle = "xInfoLabel";
        String helpLabelStyle = "xHelpLabel";
        Label labelLabel = new Label(Strings.INSTANCE.linkLabelLabel());
        labelLabel.setStyleName(infoLabelStyle);
        labelLabel.addStyleDependentName("mandatory");
        Label helpLabelLabel = new Label(getLabelTextBoxTooltip());
        helpLabelLabel.setStyleName(helpLabelStyle);
        // on enter in the textbox, submit the form
        labelTextBox.addKeyPressHandler(this);
        labelTextBox.setTitle(getLabelTextBoxTooltip());
        tooltipTextBox.setTitle(getTooltipTextBoxTooltip());
        mainPanel.add(labelLabel);
        mainPanel.add(helpLabelLabel);
        mainPanel.add(getLabelTextBox());
        mainPanel.addStyleName(DEFAULT_STYLE_NAME);
        Label tooltipLabel = new Label(Strings.INSTANCE.linkTooltipLabel());
        tooltipLabel.setStyleName(infoLabelStyle);
        Label helpTooltipLabel = new Label(getTooltipTextBoxTooltip());
        helpTooltipLabel.setStyleName(helpLabelStyle);
        // on enter in the textbox, submit the form
        tooltipTextBox.addKeyPressHandler(this);
        mainPanel.add(tooltipLabel);
        mainPanel.add(helpTooltipLabel);
        mainPanel.add(tooltipTextBox);
        newWindowCheckBox = new CheckBox(Strings.INSTANCE.linkOpenInNewWindowLabel());
        // just add the style, because we need to be able to still detect this is a checkbox
        newWindowCheckBox.addStyleName(infoLabelStyle);
        Label helpNewWindowLabel = new Label(Strings.INSTANCE.linkOpenInNewWindowHelpLabel());
        helpNewWindowLabel.setStyleName(helpLabelStyle);
        mainPanel.add(newWindowCheckBox);
        mainPanel.add(helpNewWindowLabel);
    }

    /**
     * {@inheritDoc}
     */
    public void init(Object data, AsyncCallback< ? > cb)
    {
        // store the data received as parameter
        linkData = (LinkConfig) data;
        // set the link text box according to the received config data
        labelTextBox.setText(linkData.getLabelText());
        labelTextBox.setReadOnly(linkData.isReadOnlyLabel());
        tooltipTextBox.setText(linkData.getTooltip() == null ? "" : linkData.getTooltip());
        newWindowCheckBox.setValue(linkData.isOpenInNewWindow());
        cb.onSuccess(null);
    }

    /**
     * {@inheritDoc}
     */
    public Widget display()
    {
        return mainPanel;
    }

    /**
     * @return the mainPanel, to be used by subclasses to display the form defined by this wizard step.
     */
    public Panel getMainPanel()
    {
        return mainPanel;
    }

    /**
     * @return the labelTextBox
     */
    protected TextBox getLabelTextBox()
    {
        return labelTextBox;
    }

    /**
     * @return the {@link LinkConfig} configured by this wizard step
     */
    public LinkConfig getLinkData()
    {
        return linkData;
    }

    /**
     * @return the tooltip for label text box
     */
    protected String getLabelTextBoxTooltip()
    {
        return Strings.INSTANCE.linkConfigLabelTextBoxTooltip();
    }

    /**
     * @return the tooltip for the tooltip text box
     */
    protected String getTooltipTextBoxTooltip()
    {
        return Strings.INSTANCE.linkConfigTooltipTextBoxTooltip();
    }

    /**
     * @return the tooltipTextBox
     */
    public TextBox getTooltipTextBox()
    {
        return tooltipTextBox;
    }

    /**
     * @return the newWindowCheckBox
     */
    public CheckBox getNewWindowCheckBox()
    {
        return newWindowCheckBox;
    }

    /**
     * {@inheritDoc}<br/>
     * FIXME: this will go very wrong if this function validates (and saves) and subclasses don't save and validate, the
     * data in this superclass will be committed whereas the data in the subclasses not. This can potentially cause
     * trouble when trying to go to previous, we'd go back with partially submitted data in the result. Solution is to
     * skip the super call in the subclasses and do the validation only once, there: if everything passes, commit,
     * otherwise not. Another solution could be that super classes validate first their data and then, if it's fine,
     * validate this. They can roll back if this subclass doesn't validate.
     */
    public void onSubmit(AsyncCallback<Boolean> async)
    {
        // check the label input field
        if (this.labelTextBox.getText().trim().length() == 0) {
            Window.alert(Strings.INSTANCE.linkNoLabelError());
            // something is wrong, don't validate
            async.onSuccess(false);
        } else {
            // everything is fine, commit it in the link (the labels)
            if (!this.labelTextBox.getText().trim().equals(linkData.getLabelText().trim())) {
                linkData.setLabel(labelTextBox.getText().trim());
                linkData.setLabelText(labelTextBox.getText().trim());
            }
            // commit the tooltip value
            linkData.setTooltip(getTooltipTextBox().getText());
            // set the link to open in new window according to user input
            linkData.setOpenInNewWindow(getNewWindowCheckBox().getValue());
            async.onSuccess(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onCancel(AsyncCallback<Boolean> async)
    {
        // always passes with success
        async.onSuccess(true);
    }

    /**
     * {@inheritDoc}
     */
    public Object getResult()
    {
        // always return the (modified) linkData as result of this dialog
        return linkData;
    }

    /**
     * {@inheritDoc}
     */
    public String getNextStep()
    {
        // this is the last step in the wizard.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepTitle()
    {
        return Strings.INSTANCE.linkConfigTitle();
    }

    /**
     * {@inheritDoc}. Configure this as the last wizard step, by default, allowing to finish, cancel or go to previous
     * step if the navigation stack is not empty at this point.
     */
    public EnumSet<NavigationDirection> getValidDirections()
    {
        return EnumSet.of(NavigationDirection.FINISH, NavigationDirection.CANCEL, NavigationDirection.PREVIOUS);
    }

    /**
     * {@inheritDoc}
     */
    public String getDirectionName(NavigationDirection direction)
    {
        switch (direction) {
            case FINISH:
                return Strings.INSTANCE.linkCreateLinkButton();
            default:
                return null;
        }
    }

    /**
     * @return the default navigation direction, to be fired automatically when enter is hit in an input in the form of
     *         this configuration wizard step. To be overridden by subclasses to provide the specific direction to be
     *         followed.
     */
    public NavigationDirection getDefaultDirection()
    {
        return NavigationDirection.FINISH;
    }

    /**
     * {@inheritDoc}
     */
    public void addNavigationListener(NavigationListener listener)
    {
        navigationListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeNavigationListener(NavigationListener listener)
    {
        navigationListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see KeyPressHandler#onKeyPress(KeyPressEvent)
     */
    public void onKeyPress(KeyPressEvent event)
    {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            // fire the event for the default direction
            navigationListeners.fireNavigationEvent(getDefaultDirection());
        }
    }
}
