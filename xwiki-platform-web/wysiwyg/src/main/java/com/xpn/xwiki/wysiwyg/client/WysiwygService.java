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
package com.xpn.xwiki.wysiwyg.client;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;
import com.xpn.xwiki.wysiwyg.client.diff.Revision;
import com.xpn.xwiki.wysiwyg.client.plugin.link.LinkConfig;
import com.xpn.xwiki.wysiwyg.client.sync.SyncResult;
import com.xpn.xwiki.wysiwyg.client.util.Attachment;

/**
 * The service interface used on the server.
 * 
 * @version $Id$
 */
public interface WysiwygService extends RemoteService
{
    /**
     * Utility class for accessing the service stub.
     */
    public static final class Singleton
    {
        /**
         * The service stub.
         */
        private static WysiwygServiceAsync instance;

        /**
         * Private constructor because this is a utility class.
         */
        private Singleton()
        {
        }

        /**
         * @return The service stub.
         */
        public static synchronized WysiwygServiceAsync getInstance()
        {
            if (instance == null) {
                String serviceURL;
                try {
                    // Look in the global configuration object.
                    serviceURL = Dictionary.getDictionary("Wysiwyg").get("wysiwygServiceURL");
                } catch (MissingResourceException e) {
                    serviceURL = "/WysiwygService";
                }

                instance = (WysiwygServiceAsync) GWT.create(WysiwygService.class);
                ((ServiceDefTarget) instance).setServiceEntryPoint(serviceURL);

                // We cache the service calls.
                instance = new WysiwygServiceAsyncCacheProxy(instance);
            }
            return instance;
        }
    }

    /**
     * @param html The HTML fragment to be converted.
     * @param syntax The syntax of the result.
     * @return The result of converting the given HTML fragment to the specified syntax.
     */
    String fromHTML(String html, String syntax);

    /**
     * @param dirtyHTML The HTML fragment to be cleaned.
     * @return The result of cleaning the given HTML fragment.
     */
    String cleanHTML(String dirtyHTML);

    /**
     * Cleans dirty html content produced from an office application like MsWord, MsExcel, OpenOffice Writer etc. This
     * method is primarily utilized by the office importer wysiwyg plugin.
     * 
     * @param htmlPaste dirty html pasted by the user.
     * @param cleanerHint role hint for which cleaner to be used.
     * @param cleaningParams additional parameters to be used when cleaning.
     * @return The cleaned html content.
     */
    String cleanOfficeHTML(String htmlPaste, String cleanerHint, Map<String, String> cleaningParams);

    /**
     * Imports the most recent office attachment of the given wiki page into XHTML/1.0. This method should be invoked
     * right after the attachment has finished uploading. The resulting xhtml content will be returned from this method
     * while if there are non-textual content in the original office document, they will be attached to the wiki page
     * identified by pageName. Note that this method does not alter the content of the wiki page.
     * 
     * @param pageName the wiki page into which the office document is attached.
     * @param cleaningParams additional parameters for the import operation.
     * @return the xhtml result from the office importer.
     * @throws XWikiGWTException if the import operation fails.
     * @deprecated deprecated since 2.0.1, use {@link #officeToXHTML(Attachment, Map)} instead.
     */
    @Deprecated
    String officeToXHTML(String pageName, Map<String, String> cleaningParams) throws XWikiGWTException;

    /**
     * Imports the given office attachment into XHTML/1.0. This method returns the resulting xhtml content while if
     * there are non-textual content in the office attachment, they will be attached to the owner wiki page. Note that
     * this operation does not alter the content of the wiki page.
     * 
     * @param attachment office attachment to be imported into xhtml/1.0.
     * @param cleaningParams additional parameters for the import operation.
     * @return the xhtml result from the office importer.
     * @throws XWikiGWTException if the import operation fails.
     */
    String officeToXHTML(Attachment attachment, Map<String, String> cleaningParams) throws XWikiGWTException;

    /**
     * @param syncedRevision The changes to this editor's content, since the last update.
     * @param pageName The page being edited.
     * @param version The version affected by syncedRevision.
     * @param syncReset resets the sync server for this page.
     * @return The result of synchronizing this editor with others editing the same page.
     * @throws XWikiGWTException when the synchronization fails
     */
    SyncResult syncEditorContent(Revision syncedRevision, String pageName, int version, boolean syncReset)
        throws XWikiGWTException;

    /**
     * Check if the current wiki is part of a multiwiki (i.e. this is a virtual wiki).
     * 
     * @return true if the current wiki is a multiwiki, and false in the other case
     */
    Boolean isMultiWiki();

    /**
     * @return a list containing the names of all wikis.
     */
    List<String> getVirtualWikiNames();

    /**
     * @param wikiName the name of the wiki to search for spaces. If this is <code>null</code>, the current wiki will be
     *            used.
     * @return a list of all spaces names in the specified wiki.
     */
    List<String> getSpaceNames(String wikiName);

    /**
     * @param wikiName the name of the wiki. Pass <code>null</code> if this should use the current wiki.
     * @param spaceName the name of the space
     * @return the list of the page names from a given space and a given wiki.
     */
    List<String> getPageNames(String wikiName, String spaceName);

    /**
     * @param start the start index of the list of pages to return
     * @param count the number of pages to return
     * @return the recently {@code count} modified pages of the current user, starting from position {@code start}
     * @throws XWikiGWTException if something goes wrong on the server
     */
    List<Document> getRecentlyModifiedPages(int start, int count) throws XWikiGWTException;

    /**
     * @param start the start index of the list of pages to return
     * @param count the number of pages to return
     * @param keyword the keyword to search the pages for
     * @return the {@code count} pages whose fullname or title match the keyword, starting from position {@code start}
     * @throws XWikiGWTException if something goes wrong on the server
     */
    List<Document> getMatchingPages(String keyword, int start, int count) throws XWikiGWTException;

    /**
     * Creates a page link (url, reference) from the given parameters. None of them are mandatory, if one misses, it is
     * replaced with a default value.
     * 
     * @param wikiName the name of the wiki to which to link
     * @param spaceName the name of the space of the page. If this parameter is missing, it is replaced with the space
     *            of the current document in the context.
     * @param pageName the name of the page to which to link to. If it's missing, it is replaced with "WebHome".
     * @param revision the value for the page revision to which to link to. If this is missing, the link is made to the
     *            latest revision, the default view action for the document.
     * @param anchor the name of the anchor type.
     * @return the data of the link to the document, containing link url and link reference information.
     */
    LinkConfig getPageLink(String wikiName, String spaceName, String pageName, String revision, String anchor);

    /**
     * Returns attachment information from the passed parameters, testing if the passed attachment exists. Note that the
     * {@code attachmentName} name will be cleaned to match the attachment names cleaning rules, and the attachment
     * reference and URL will be generated with the cleaned name. This function will be used as a method to test the
     * correct upload of a file to a page.
     * 
     * @param wikiName the name of the wiki of the page the file is attached to
     * @param spaceName the name of the space of the page the file is attached to
     * @param pageName the name of the page the file is attached to
     * @param attachmentName the uncleaned name of the attachment, which is to be cleaned on the server
     * @return an {@link Attachment} containing the reference and the URL of the attachment, or {@code null} in case the
     *         attachment was not found
     */
    Attachment getAttachment(String wikiName, String spaceName, String pageName, String attachmentName);

    /**
     * Returns all the image attachments from the referred page.
     * 
     * @param wikiName the name of the wiki to get images from
     * @param spaceName the name of the space to get image attachments from
     * @param pageName the name of the page to get image attachments from
     * @return list of the image attachments
     * @throws XWikiGWTException if something goes wrong on the server
     */
    List<Attachment> getImageAttachments(String wikiName, String spaceName, String pageName) throws XWikiGWTException;

    /**
     * Returns all the attachments from the referred page.
     * 
     * @param wikiName the name of the wiki to get attachments from
     * @param spaceName the name of the space to get attachments from
     * @param pageName the name of the page to get attachments from
     * @return list of the attachments
     * @throws XWikiGWTException if something goes wrong on the server
     */
    List<Attachment> getAttachments(String wikiName, String spaceName, String pageName) throws XWikiGWTException;
}
