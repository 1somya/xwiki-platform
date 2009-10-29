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
package com.xpn.xwiki.wysiwyg.client.plugin.submit.exec;

import org.xwiki.gwt.dom.client.Element;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Executable;

import com.google.gwt.dom.client.Document;

/**
 * Stores the value of the rich text area in a HTML form field before submitting it to the server.
 * 
 * @version $Id$
 */
public class SubmitExecutable implements Executable
{
    /**
     * The name of the property holding the submitted value.
     */
    private static final String VALUE = "value";

    /**
     * The form field identifier.
     */
    private final String fieldId;

    /**
     * Creates a new submit executable that bind a rich text area to the specified form field.
     * 
     * @param fieldId the form field identifier
     */
    public SubmitExecutable(String fieldId)
    {
        this.fieldId = fieldId;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#execute(RichTextArea, String)
     */
    public boolean execute(RichTextArea rta, String param)
    {
        Element field = (Element) Document.get().getElementById(fieldId);
        if (field != null) {
            field.setPropertyString(VALUE, rta.getHTML());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#getParameter(RichTextArea)
     */
    public String getParameter(RichTextArea rta)
    {
        Element field = (Element) Document.get().getElementById(fieldId);
        return field != null ? field.getPropertyString(VALUE) : null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#isEnabled(RichTextArea)
     */
    public boolean isEnabled(RichTextArea rta)
    {
        return isSupported(rta);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#isExecuted(RichTextArea)
     */
    public boolean isExecuted(RichTextArea rta)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Executable#isSupported(RichTextArea)
     */
    public boolean isSupported(RichTextArea rta)
    {
        return rta != null && fieldId != null && Document.get().getElementById(fieldId) != null;
    }
}
