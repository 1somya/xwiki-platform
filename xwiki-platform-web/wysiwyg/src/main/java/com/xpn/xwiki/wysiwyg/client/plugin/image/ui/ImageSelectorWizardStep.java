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
package com.xpn.xwiki.wysiwyg.client.plugin.image.ui;

import java.util.Arrays;
import java.util.List;

import org.xwiki.gwt.user.client.StringUtils;
import org.xwiki.gwt.user.client.ui.wizard.WizardStep;

import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.plugin.image.ImageConfig;
import com.xpn.xwiki.wysiwyg.client.util.ResourceName;
import com.xpn.xwiki.wysiwyg.client.widget.wizard.util.AbstractSelectorAggregatorWizardStep;

/**
 * Wizard step to select the image to insert, by aggregating a current page selector step and an "all pages" selector
 * step.
 * 
 * @version $Id$
 */
public class ImageSelectorWizardStep extends AbstractSelectorAggregatorWizardStep<ImageConfig>
{
    /**
     * Builds an image selector wizard step for the currently edited resource.
     * 
     * @param editedResource the currently edited resource.
     */
    public ImageSelectorWizardStep(ResourceName editedResource)
    {
        super(editedResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRequiredStep()
    {
        if (StringUtils.isEmpty(getData().getReference())) {
            // no reference set, default with current page
            return Strings.INSTANCE.selectorSelectFromCurrentPage();
        }

        // check if the edited attachment is in the current page
        ResourceName resource = new ResourceName(getData().getReference(), true);
        // check match on current page
        if (resource.matchesUpToPage(getEditedResource())) {
            return Strings.INSTANCE.selectorSelectFromCurrentPage();
        } else {
            return Strings.INSTANCE.selectorSelectFromAllPages();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WizardStep getStepInstance(String name)
    {
        if (name.equals(Strings.INSTANCE.selectorSelectFromCurrentPage())) {
            return new CurrentPageImageSelectorWizardStep(getEditedResource());
        }
        if (name.equals(Strings.INSTANCE.selectorSelectFromAllPages())) {
            // create an explorer which does not show wiki selector, by default
            return new ImagesExplorerWizardStep(getEditedResource(), false);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getStepNames()
    {
        return Arrays.asList(Strings.INSTANCE.selectorSelectFromCurrentPage(), Strings.INSTANCE
            .selectorSelectFromAllPages());
    }

    /**
     * {@inheritDoc}
     */
    public String getStepTitle()
    {
        return Strings.INSTANCE.imageSelectImageTitle();
    }
}
