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
package com.xpn.xwiki.wysiwyg.client.syntax.rule;

import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;

import com.xpn.xwiki.wysiwyg.client.syntax.ValidationRule;

/**
 * Validation rule for disabling the indent and outdent features when the selection is outside a list item. These two
 * features behave differently according to the context in which they are called. For the moment we use them only to
 * indent and outdent list items. We'll drop this constraint when we'll add support for block-quotes in the editor.
 * 
 * @version $Id$
 */
public class DisableIndentOutsideList implements ValidationRule
{
    /**
     * {@inheritDoc}
     * 
     * @see ValidationRule#areValid(SubmittableRichTextArea)
     */
    public boolean areValid(RichTextArea textArea)
    {
        return textArea.getCommandManager().isExecuted(Command.INSERT_UNORDERED_LIST)
            || textArea.getCommandManager().isExecuted(Command.INSERT_ORDERED_LIST);
    }

    /**
     * {@inheritDoc}
     * 
     * @see ValidationRule#getFeatures()
     */
    public String[] getFeatures()
    {
        return new String[] {"indent", "outdent"};
    }
}
