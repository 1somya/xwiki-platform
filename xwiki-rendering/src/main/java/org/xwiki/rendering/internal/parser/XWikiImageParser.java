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
package org.xwiki.rendering.internal.parser;

import org.xwiki.rendering.listener.DocumentImage;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.URLImage;
import org.xwiki.rendering.parser.ImageParser;

/**
 * Parses XWiki image definitions, using either a URL (pointing to an imagep or the following format:
 * <code>[wiki:Space.Page]^imageName</code> where <code>imageName</code> is the name of the image attachment
 * (for example "my.png").
 * 
 * @version $Id$
 * @since 1.7M3
 */
public class XWikiImageParser implements ImageParser
{
    /**
     * {@inheritDoc}
     * @see ImageParser#parse(String)
     */
    public Image parse(String imageLocation)
    {
        Image result;

        // TODO: Shouldn't we store a DocumentIdentity object instead in Image and make sure that it's never null
        // by using the current document when not specified?
        if (imageLocation.startsWith("http://")) {
            result = new URLImage(imageLocation);
        } else {
            String documentName = null;
            String attachmentName;
            int attachmentSeparatorPosition = imageLocation.indexOf("@");
            if (attachmentSeparatorPosition > -1) {
                documentName = imageLocation.substring(0, attachmentSeparatorPosition);
                attachmentName = imageLocation.substring(attachmentSeparatorPosition + 1);
            } else {
                attachmentName = imageLocation;
            }
            result = new DocumentImage(documentName, attachmentName); 
        }
        
        return result; 
    }
}
