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
package org.xwiki.rendering.renderer;

/**
 * Printer using a {@link StringBuffer} as the underlying output target.
 *
 * @version $Id: $
 * @since 1.6M1
 */
public class DefaultWikiPrinter implements WikiPrinter
{
    private StringBuffer buffer;

    public DefaultWikiPrinter()
    {
        this(new StringBuffer());
    }

    public DefaultWikiPrinter(StringBuffer buffer)
    {
        this.buffer = buffer;
    }

    public StringBuffer getBuffer()
    {
        return this.buffer;
    }

    /**
     * @return a new line symbols
     */
    protected String getEOL()
    {
        return "\n";
    }

    /**
     * {@inheritDoc}
     * @see WikiPrinter#print(String) 
     */
    public void print(String text)
    {
        getBuffer().append(text);
    }

    /**
     * {@inheritDoc}
     * @see WikiPrinter#println(String)
     */
    public void println(String text)
    {
        getBuffer().append(text).append(getEOL());
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getBuffer().toString();
    }
}
