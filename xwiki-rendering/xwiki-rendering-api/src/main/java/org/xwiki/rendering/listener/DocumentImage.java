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
package org.xwiki.rendering.listener;

/**
 * Image located in a wiki Document.
 * 
 * @version $Id$
 * @since 1.7RC1
 */
public class DocumentImage extends AbstractImage
{
    /**
     * @see #getAttachmentReference() 
     */
    private String attachmentReference;

    /**
     * @param attachmentReference see {@link #getAttachmentReference()}
     * @since 2.5RC1
     */
    public DocumentImage(String attachmentReference)
    {
        this.attachmentReference = attachmentReference;
    }

    /**
     * @return the reference to the attachment containing the image
     * @since 2.5RC1
     */
    public String getAttachmentReference()
    {
        return this.attachmentReference;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Image#getType()
     */
    public ImageType getType()
    {
        return ImageType.DOCUMENT;
    }

    /**
     * {@inheritDoc}
     *
     * @see Image#getReference()
     */
    public String getReference()
    {
        return getAttachmentReference();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "reference = [" + getAttachmentReference() + "]";
    }
}
