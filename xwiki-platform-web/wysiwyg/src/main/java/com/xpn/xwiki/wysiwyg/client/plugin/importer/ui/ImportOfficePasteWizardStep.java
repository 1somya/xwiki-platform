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
package com.xpn.xwiki.wysiwyg.client.plugin.importer.ui;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.wysiwyg.client.WysiwygService;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.WizardStep;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.NavigationListener.NavigationDirection;

/**
 * Wizard step responsible for importing copy-pasted office content.
 * 
 * @version $Id$
 * @since 2.0.1
 */
public class ImportOfficePasteWizardStep implements WizardStep
{
    /**
     * Main UI of this wizard.
     */
    private Panel mainPanel;

    /**
     * The text area where the user can paste his content.
     */
    private RichTextArea textArea;

    /**
     * Storage for the result of the import operation.
     */
    private Object result;

    /**
     * Checkbox allowing the user to select whether he wants to filter out office styles or not.
     */
    private CheckBox filterStylesCheckBox;

    /**
     * Creates an instance of {@link ImportOfficePasteWizardStep}.
     */
    public ImportOfficePasteWizardStep()
    {
        mainPanel = new FlowPanel();

        // Info label.
        Panel infoLabel = new FlowPanel();
        infoLabel.setStyleName("xInfoLabel");
        infoLabel.add(new InlineLabel(Strings.INSTANCE.importOfficePasteInfoLabel()));
        InlineLabel mandatoryLabel = new InlineLabel(Strings.INSTANCE.mandatory());
        mandatoryLabel.addStyleName("xMandatory");
        infoLabel.add(mandatoryLabel);
        mainPanel.add(infoLabel);

        // Help label.
        Label helpLabel = new Label(Strings.INSTANCE.importOfficePasteHelpLabel());
        helpLabel.setStyleName("xHelpLabel");
        mainPanel.add(helpLabel);

        // Text area panel.
        textArea = new RichTextArea();
        textArea.addStyleName("xImportOfficeContentEditor");
        mainPanel.add(textArea);

        // Filter styles check box.
        this.filterStylesCheckBox = new CheckBox(Strings.INSTANCE.importOfficeContentFilterStylesCheckBoxLabel());
        mainPanel.add(filterStylesCheckBox);
    }

    /**
     * {@inheritDoc}
     */
    public Widget display()
    {
        return this.mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    public void init(Object data, AsyncCallback< ? > cb)
    {
        textArea.setHTML("");
        textArea.setFocus(true);
        cb.onSuccess(null);
    }

    /**
     * {@inheritDoc}
     */
    public EnumSet<NavigationDirection> getValidDirections()
    {
        return EnumSet.of(NavigationDirection.CANCEL, NavigationDirection.FINISH);
    }

    /**
     * {@inheritDoc}
     */
    public String getDirectionName(NavigationDirection direction)
    {
        if (direction == NavigationDirection.FINISH) {
            return Strings.INSTANCE.importWizardImportButtonCaption();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getNextStep()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getResult()
    {
        return this.result;
    }

    /**
     * Sets the result of this wizard step.
     * 
     * @param result the result.
     */
    private void setResult(Object result)
    {
        this.result = result;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepTitle()
    {
        return Strings.INSTANCE.importOfficePasteWizardStepTitle();
    }

    /**
     * {@inheritDoc}
     */
    public void onCancel()
    {
        textArea.setHTML("");
    }

    /**
     * {@inheritDoc}
     */
    public void onSubmit(final AsyncCallback<Boolean> async)
    {
        String officeHTML = textArea.getHTML();
        if (officeHTML.trim().equals("")) {
            async.onSuccess(false);
        } else {
            WysiwygService.Singleton.getInstance().cleanOfficeHTML(officeHTML, "wysiwyg", getHTMLCleaningParams(),
                new AsyncCallback<String>()
                {
                    public void onSuccess(String result)
                    {
                        setResult(result);
                        async.onSuccess(true);
                    }

                    public void onFailure(Throwable thrown)
                    {
                        async.onFailure(thrown);
                    }
                });
        }
    }

    /**
     * Prepares the cleaning parameters map.
     * 
     * @return a {@link Map} with cleaning parameters for office importer.
     */
    protected Map<String, String> getHTMLCleaningParams()
    {
        Map<String, String> params = new HashMap<String, String>();
        if (filterStylesCheckBox.getValue()) {
            params.put("filterStyles", "strict");
        }
        // For Office2007: Office2007 generates an xhtml document (when copied) which has attributes and tags of
        // several namespaces. But the document itself doesn't contain the namespace definitions, which causes
        // the HTMLCleaner (the DomSerializer) to fail while performing it's operations. As a workaround we
        // force HTMLCleaner to avoid parsing of namespace information.
        params.put("namespacesAware", Boolean.toString(false));
        return params;
    }
}
