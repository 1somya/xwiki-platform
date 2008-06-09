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
 *
 */
package org.xwiki.velocity;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * Initialize a Velocity Engine and make Velocity services available.
 *
 * @version $Id$
 */
public interface VelocityEngine
{
    /**
     * This component's role, used when code needs to look it up.
     */
    String ROLE = VelocityEngine.class.getName();

    /**
     * Initializes the Velocity engine by setting its configuration both from the component's
     * configuration and from the passed properties. This method must be called before any other
     * method from this class can be executed.
     *
     * @param properties the properties that will override the static properties defined in the
     * component's configuration
     * @throws XWikiVelocityException in case of error
     */
    void initialize(Properties properties) throws XWikiVelocityException;

    /**
     * Renders the input string using the context into the output writer.
     *
     * @param context the Velocity context to use in rendering the input string
     * @param out the writer in which to render the output
     * @param templateName the string to be used as the template name for log messages in case of
     *        error
     * @param source the input string containing the VTL to be rendered
     * @return true if successful, false otherwise. If false, see the Velocity runtime log
     * @throws XWikiVelocityException in case of error
     */
    boolean evaluate(Context context, Writer out, String templateName, String source)
        throws XWikiVelocityException;

    /**
     * Renders the input string using the context into the output writer.
     *
     * @param context the Velocity context to use in rendering the input string
     * @param out the writer in which to render the output
     * @param templateName the string to be used as the template name for log messages in case of
     *        error
     * @param source the input containing the VTL to be rendered, as a Reader
     * @return true if successful, false otherwise. If false, see the Velocity runtime log
     * @throws XWikiVelocityException in case of error
     */
    boolean evaluate(Context context, Writer out, String templateName, Reader source)
        throws XWikiVelocityException;
}
