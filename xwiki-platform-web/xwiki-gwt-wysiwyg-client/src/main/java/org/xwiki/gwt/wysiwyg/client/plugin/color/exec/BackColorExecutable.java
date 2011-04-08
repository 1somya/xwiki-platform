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
package org.xwiki.gwt.wysiwyg.client.plugin.color.exec;

import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.Command;
import org.xwiki.gwt.user.client.ui.rta.cmd.internal.DefaultExecutable;

/**
 * Changes the background color of the current selection. It executes the built-in command. We created this class solely
 * because in Mozilla the "backcolor" command sets the background color of the entire rich text area. The implementation
 * for Mozilla is in {@link HiliteColorExecutable}. We use deferred binding to load the proper class.
 * 
 * @version $Id$
 */
public class BackColorExecutable extends DefaultExecutable
{
    /**
     * Creates a new executable of this type.
     * 
     * @param rta the execution target
     */
    public BackColorExecutable(RichTextArea rta)
    {
        super(rta, Command.BACK_COLOR.toString());
    }
}
