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
package org.xwiki.wikistream.type;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Combination of supported wikis and their export types.
 * 
 * @version $Id$
 */
public class WikiStreamType
{
    public static final WikiStreamType MEDIAWIKI_XML = new WikiStreamType(WikiType.MEDIAWIKI, "XML");

    public static final WikiStreamType CONFLUENCE_XML = new WikiStreamType(WikiType.CONFLUENCE, "XML");

    public static final WikiStreamType XWIKI_XAR = new WikiStreamType(WikiType.XWIKI, "XAR");

    /**
     * Generic WIKI XML Syntax
     */
    public static final WikiStreamType WIKI_XML = new WikiStreamType(WikiType.WIKI, "XML");

    /**
     * Wiki type.
     */
    private WikiType type;

    /**
     * Export data format.
     */
    private String dataFormat;

    /**
     * @param type the type of Wiki
     * @param dataFormat the export data format
     */
    public WikiStreamType(WikiType type, String dataFormat)
    {
        this.type = type;
        this.dataFormat = dataFormat;
    }

    /**
     * @return the wiki
     */
    public WikiType getType()
    {
        return type;
    }

    /**
     * @return the export data format
     */
    public String getDataFormat()
    {
        return dataFormat;
    }

    public String toIdString()
    {
        return getType().getId() + "/" + getDataFormat().toLowerCase();
    }

    @Override
    public String toString()
    {
        return getType().toString() + " " + getDataFormat();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getType()).append(getDataFormat()).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result;

        if (this == object) {
            result = true;
        } else {
            if (object instanceof WikiStreamType) {
                result =
                    ObjectUtils.equals(getType(), ((WikiStreamType) object).getType())
                        && ObjectUtils.equals(getDataFormat(), ((WikiStreamType) object).getDataFormat());
            } else {
                result = false;
            }
        }

        return result;
    }
}
