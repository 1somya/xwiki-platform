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
package org.xwiki.rendering.scaffolding;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.model.AttachmentName;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.model.DocumentName;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Mock {@link DocumentAccessBridge} implementation used for testing, since we don't want to pull any dependency on the
 * Model/Skin/etc for the Rendering module's unit tests.
 * 
 * @version $Id$
 * @since 1.6M1
 */
public class MockDocumentAccessBridge implements DocumentAccessBridge
{
    /**
     * Create and return a descriptor for this component.
     * 
     * @return the descriptor of the component.
     */
    public static ComponentDescriptor<DocumentAccessBridge> getComponentDescriptor()
    {
        DefaultComponentDescriptor<DocumentAccessBridge> componentDescriptor =
            new DefaultComponentDescriptor<DocumentAccessBridge>();

        componentDescriptor.setRole(DocumentAccessBridge.class);
        componentDescriptor.setImplementation(MockDocumentAccessBridge.class);

        return componentDescriptor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDocumentContent(String)
     */
    public String getDocumentContent(String documentName) throws Exception
    {
        return "Some content";
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDocumentContent(String, String)
     */
    public String getDocumentContent(String documentName, String language) throws Exception
    {
        return "Some translated content";
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDocumentContentForDefaultLanguage(String)
     */
    public String getDocumentContentForDefaultLanguage(String documentName) throws Exception
    {
        return "Some content";
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#exists(String)
     */
    public boolean exists(String documentName)
    {
        return documentName.equals("Space.ExistingPage");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getURL(String, String, String, String)
     */
    public String getURL(String documentName, String action, String queryString, String anchor)
    {
        String result =
            "/xwiki/bin/view/" + (StringUtils.isBlank(documentName) ? "currentdoc" : documentName.replace(".", "/"));
        if (anchor != null) {
            result = result + "#" + anchor;
        }
        if (queryString != null) {
            result = result + "?" + queryString;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getAttachmentURL(String, String)
     */
    public String getAttachmentURL(String documentName, String attachmentName)
    {
        return "/xwiki/bin/download/" + (documentName == null ? "currentdoc" : documentName.replace(".", "/")) + "/"
            + attachmentName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getProperty(String, String, int, String)
     */
    public Object getProperty(String documentName, String className, int objectNumber, String propertyName)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getProperty(String, String, String)
     */
    public Object getProperty(String documentName, String className, String propertyName)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getProperty(String, String)
     */
    public Object getProperty(String documentName, String propertyName)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getProperties(String, String)
     */
    public List<Object> getProperties(String documentName, String className)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getPropertyType(String, String)
     */
    public String getPropertyType(String className, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#isPropertyCustomMapped(String, String)
     */
    public boolean isPropertyCustomMapped(String className, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(String documentName, String className, String propertyName, Object propertyValue)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @since 2.2M1
     */
    public InputStream getAttachmentContent(AttachmentName attachmentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @since 2.2M1
     */
    public List<AttachmentName> getAttachmentNames(DocumentName documentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getAttachmentContent(String, String)
     */
    public byte[] getAttachmentContent(String documentName, String attachmentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#setAttachmentContent(String, String, byte[])
     */
    public void setAttachmentContent(String documentName, String AttachmentName, byte[] attachmentData)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#setDocumentContent(String, String, String, boolean)
     */
    public void setDocumentContent(String documentName, String content, String editComment, boolean isMinorEdit)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.bridge.DocumentAccessBridge#getDocumentSyntaxId(java.lang.String)
     */
    public String getDocumentSyntaxId(String documentName) throws Exception
    {
        return new Syntax(SyntaxType.XWIKI, "2.0").toIdString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#setDocumentSyntaxId(String, String)
     */
    public void setDocumentSyntaxId(String documentName, String syntaxId) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#isDocumentViewable(String)
     */
    public boolean isDocumentViewable(String documentName)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#isDocumentEditable(String)
     */
    public boolean isDocumentEditable(String documentName)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see DocumentAccessBridge#isDocumentEditable(org.xwiki.model.DocumentName) 
     * @since 2.2M1
     */
    public boolean isDocumentEditable(DocumentName documentName)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#hasProgrammingRights()
     */
    public boolean hasProgrammingRights()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getCurrentUser()
     */
    public String getCurrentUser()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDefaultEncoding()
     */
    public String getDefaultEncoding()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDocument(String)
     */
    public DocumentModelBridge getDocument(String documentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.bridge.DocumentAccessBridge#getDocument(org.xwiki.model.DocumentName)
     * @since 2.2M1
     */
    public DocumentModelBridge getDocument(DocumentName documentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getDocumentName(String)
     * @since 2.2M1
     */
    public DocumentName getModelDocumentName(String documentName)
    {
        return new DocumentName("xwiki", "Space", "Page");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#popDocumentFromContext(Map)
     */
    public void popDocumentFromContext(Map<String, Object> backupObjects)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#pushDocumentInContext(Map, String)
     */
    public void pushDocumentInContext(Map<String, Object> backupObjects, String documentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getAttachmentURL(AttachmentName, boolean)
     * @since 2.2M1
     */
    public String getAttachmentURL(AttachmentName attachmentName, boolean isFullURL)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DocumentAccessBridge#getAttachmentURLs(DocumentName, boolean)
     * @since 2.2M1
     */
    public List<String> getAttachmentURLs(DocumentName documentName, boolean isFullURL) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @see DocumentAccessBridge#getCurrentWiki() 
     */
    public String getCurrentWiki()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.bridge.DocumentAccessBridge#getCurrentDocumentName()
     * @deprecated
     */
    public org.xwiki.bridge.DocumentName getCurrentDocumentName()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public DocumentModelBridge getDocument(org.xwiki.bridge.DocumentName documentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public org.xwiki.bridge.DocumentName getDocumentName(String documentName)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public InputStream getAttachmentContent(org.xwiki.bridge.AttachmentName attachmentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public List<org.xwiki.bridge.AttachmentName> getAttachments(org.xwiki.bridge.DocumentName documentName)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public String getAttachmentURL(org.xwiki.bridge.AttachmentName attachmentName, boolean isFullURL)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public List<String> getAttachmentURLs(org.xwiki.bridge.DocumentName documentName, boolean isFullURL)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }
}
