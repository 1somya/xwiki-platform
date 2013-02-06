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
package org.xwiki.wikistream.xml.internal.output;

import org.xwiki.wikistream.internal.output.OuputTarget;
import org.xwiki.wikistream.xml.internal.XMLParameters;

public class XMLOuputParameters extends XMLParameters
{
    private boolean format = true;

    private String encoding = "UTF-8";

    private OuputTarget target;

    public boolean isFormat()
    {
        return this.format;
    }

    public void setFormat(boolean format)
    {
        this.format = format;
    }

    public String getEncoding()
    {
        return this.encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public OuputTarget getTarget()
    {
        return this.target;
    }

    public void setTarget(OuputTarget target)
    {
        this.target = target;
    }
}
