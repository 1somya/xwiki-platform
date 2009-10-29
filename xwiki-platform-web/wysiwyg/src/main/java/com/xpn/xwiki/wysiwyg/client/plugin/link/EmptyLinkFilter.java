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
package com.xpn.xwiki.wysiwyg.client.plugin.link;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.gwt.user.client.StringUtils;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;
import org.xwiki.gwt.user.client.ui.rta.cmd.CommandListener;
import org.xwiki.gwt.user.client.ui.rta.cmd.CommandManager;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * Client side empty link cleaner, to delete all links without content before the content is submitted. This is to
 * prevent the links which are not visible in the wysiwyg to get submitted as wysiwyg content.
 * 
 * @version $Id$
 */
public class EmptyLinkFilter implements CommandListener
{
    /**
     * The rich text area for which this command listener cleans the empty listener.
     */
    private RichTextArea rta;

    /**
     * Creates an empty link filter to handle links on the passed rich text area.
     * 
     * @param rta the {@link RichTextArea} to handle empty links for
     */
    public EmptyLinkFilter(RichTextArea rta)
    {
        this.rta = rta;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onBeforeCommand(CommandManager sender, Command command, String param)
    {
        // store the empty anchors in a separate list, to remove at the end since NodeList is an iterator, actually
        List<Element> emptyAnchors = new ArrayList<Element>();
        if (command.equals(new Command("submit"))) {
            NodeList<Element> anchorsList = rta.getDocument().getElementsByTagName("a");
            for (int i = 0; i < anchorsList.getLength(); i++) {
                Element anchor = anchorsList.getItem(i);
                // check if it has a href (not to remove named anchors by mistake) and it's void
                if (!StringUtils.isEmpty(anchor.getAttribute("href")) && anchor.getOffsetWidth() == 0) {
                    // should be removed
                    emptyAnchors.add(anchor);
                }
            }
        }
        // now actually remove them from the DOM
        for (Element anchor : emptyAnchors) {
            anchor.getParentElement().removeChild(anchor);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void onCommand(CommandManager sender, Command command, String param)
    {
        // nothing
    }
}
