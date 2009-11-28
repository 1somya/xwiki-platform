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
package com.xpn.xwiki.wysiwyg.server.plugin.importer;

import java.io.StringReader;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xwiki.bridge.AttachmentName;
import org.xwiki.bridge.AttachmentNameFactory;
import org.xwiki.bridge.DocumentNameSerializer;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.officeimporter.OfficeImporter;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

import com.xpn.xwiki.wysiwyg.client.plugin.importer.ImportService;
import com.xpn.xwiki.wysiwyg.client.util.Attachment;

/**
 * XWiki specific implementation of {@link ImportService}.
 * 
 * @version $Id$
 */
public class XWikiImportService implements ImportService
{
    /**
     * Default XWiki logger to report errors correctly.
     */
    private static final Log LOG = LogFactory.getLog(XWikiImportService.class);

    /**
     * The component used to import office documents.
     */
    @Requirement
    private OfficeImporter officeImporter;

    /**
     * The component used to serialize {@link org.xwiki.bridge.DocumentName} instances. This component is needed only
     * because OfficeImporter component uses String instead of {@link org.xwiki.bridge.DocumentName}.
     */
    @Requirement
    private DocumentNameSerializer documentNameSerializer;

    /**
     * The component used to parse attachment references.
     */
    @Requirement
    private AttachmentNameFactory attachmentNameFactory;

    /**
     * The component manager. We need it because we have to access some components dynamically.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * {@inheritDoc}
     * 
     * @see ImportService#cleanOfficeHTML(String, String, Map)
     */
    public String cleanOfficeHTML(String htmlPaste, String cleanerHint, Map<String, String> cleaningParams)
    {
        try {
            HTMLCleaner cleaner = componentManager.lookup(HTMLCleaner.class, cleanerHint);
            HTMLCleanerConfiguration configuration = cleaner.getDefaultConfiguration();
            configuration.setParameters(cleaningParams);
            Document cleanedDocument = cleaner.clean(new StringReader(htmlPaste), configuration);
            HTMLUtils.stripHTMLEnvelope(cleanedDocument);
            return HTMLUtils.toString(cleanedDocument, true, true);
        } catch (Exception e) {
            LOG.error("Exception while cleaning office HTML content.", e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ImportService#officeToXHTML(Attachment, Map)
     */
    public String officeToXHTML(Attachment attachment, Map<String, String> cleaningParams)
    {
        try {
            AttachmentName attachmentName = attachmentNameFactory.createAttachmentName(attachment.getReference());
            // OfficeImporter should be improved to use DocumentName instead of String. This will remove the need for a
            // DocumentNameSerializer.
            return officeImporter.importAttachment(documentNameSerializer.serialize(attachmentName.getDocumentName()),
                attachmentName.getFileName(), cleaningParams);
        } catch (Exception e) {
            LOG.error("Exception while importing office document.", e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
