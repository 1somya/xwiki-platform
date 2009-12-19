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
package org.xwiki.model.internal;

import org.xwiki.model.AttachmentName;
import org.xwiki.model.AttachmentNameSerializer;
import org.xwiki.model.DocumentNameSerializer;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Generate a fully qualified attachment reference string (ie of the form {@code wiki:space.page@filename}
 * out of a {@link AttachmentName}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
@Component
public class DefaultAttachmentNameSerializer implements AttachmentNameSerializer
{
    /**
     * Serializer to transform a DocumentName Object into a string. 
     */
    @Requirement
    private DocumentNameSerializer documentNameSerializer;
    
    /**
     * {@inheritDoc}
     * @see AttachmentNameSerializer#serialize(AttachmentName)
     */
    public String serialize(AttachmentName attachmentName)
    {
        // A valid AttachmentName must not have any null value and thus we don't need to check for nulls here.
        // It's the responsibility of creators of AttachmentName factories to ensure it's valid.
        StringBuffer result = new StringBuffer();

        result.append(this.documentNameSerializer.serialize(attachmentName.getDocumentName()));
        result.append(DefaultAttachmentNameFactory.FILENAME_SEPARATOR);
        result.append(attachmentName.getFileName());
        
        return result.toString();
    }
}
