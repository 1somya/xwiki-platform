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
package org.xwiki.rendering.internal;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;

/**
 * Mock {@link DocumentAccessBridge} implementation used for testing, since we don't want to pull any dependency on the
 * Model/Skin/etc for the Rendering module's unit tests.
 * 
 * @version $Id: MockVelocityManager.java 10176 2008-06-09 16:11:28Z vmassol $
 * @since 1.6M1
 */
public class MockDocumentAccessBridge implements DocumentAccessBridge
{
    /**
     * Create and return a descriptor for this component.
     * 
     * @return the descriptor of the component.
     */
    public static ComponentDescriptor getComponentDescriptor()
    {
        DefaultComponentDescriptor componentDescriptor = new DefaultComponentDescriptor();

        componentDescriptor.setRole(DocumentAccessBridge.ROLE);
        componentDescriptor.setRoleHint("default");
        componentDescriptor.setImplementation(MockDocumentAccessBridge.class.getName());

        return componentDescriptor;
    }

    public String getDocumentContent(String documentName) throws Exception
    {
        return "Some content";
    }

    public String getDocumentContent(String documentName, String language) throws Exception
    {
        return "Some translated content";
    }

    public String getDocumentContentForDefaultLanguage(String documentName) throws Exception
    {
        return "Some content";
    }

    public boolean exists(String documentName) throws Exception
    {
        return documentName.equals("Space.ExistingPage");
    }

    public String getURL(String documentName, String action, String queryString, String anchor) throws Exception
    {
        String result = "/xwiki/bin/view/" + (documentName == null ? "currentdoc" : documentName.replace(".", "/"));
        if (anchor != null) {
            result = result + "#" + anchor;
        }
        if (queryString != null) {
            result = result + "?" + queryString;
        }
        return result;
    }

    public String getAttachmentURL(String documentName, String attachmentName) throws Exception
    {
        return "/xwiki/bin/download/" + (documentName == null ? "currentdoc" : documentName.replace(".", "/")) + "/"
            + attachmentName;
    }

    public String getProperty(String documentName, String className, int objectNumber, String propertyName)
        throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public String getProperty(String documentName, String className, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public String getProperty(String documentName, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public String getPropertyType(String className, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isPropertyCustomMapped(String className, String propertyName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public byte[] getAttachmentContent(String documentName, String attachmentName) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }
        
    public void setAttachmentContent(String documentName, String AttachmentName,
        byte[] attachmentData) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public void setDocumentContent(String documentName, String content, String editComment,
        boolean isMinorEdit) throws Exception
    {
        throw new RuntimeException("Not implemented");        
    }
    
    public void setDocumentSyntaxId(String documentName, String syntaxId) throws Exception
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isDocumentViewable(String documentName)
    {
        return true;
    }
    
    public boolean isDocumentEditable(String documentName)
    {
        return true;
    }

    public boolean hasProgrammingRights()
    {
        return true;
    }

    public String getCurrentUser()
    {
        throw new RuntimeException("Not implemented");
    }

    public String getDefaultEncoding()
    {
        throw new RuntimeException("Not implemented");
    }        
}
