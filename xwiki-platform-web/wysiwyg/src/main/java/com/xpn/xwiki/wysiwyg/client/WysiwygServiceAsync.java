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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xpn.xwiki.wysiwyg.client.diff.Revision;
import com.xpn.xwiki.wysiwyg.client.plugin.image.ImageConfig;
import com.xpn.xwiki.wysiwyg.client.sync.SyncResult;

/**
 * Service interface used on the client. It should have all the methods from {@link WysiwygService} with an additional
 * {@link AsyncCallback} parameter. This is specific to GWT's architecture.
 * 
 * @version $Id$
 */
public interface WysiwygServiceAsync
{
    /**
     * Makes a request to the server to convert the given HTML fragment to the specified syntax.
     * 
     * @param html The HTML fragment to be converted.
     * @param syntax The syntax of the result.
     * @param async The callback to be used for notifying the caller after receiving the response from the server.
     */
    void fromHTML(String html, String syntax, AsyncCallback<String> async);

    /**
     * Makes a request to the server to convert the given text from the specified syntax to HTML.
     * 
     * @param source The text to be converted.
     * @param syntax The syntax of the given text.
     * @param async The callback to be used for notifying the caller after receiving the response from the server.
     */
    void toHTML(String source, String syntax, AsyncCallback<String> async);

    /**
     * Makes a request to the server to clean the given HTML fragment.
     * 
     * @param dirtyHTML The HTML fragment to be cleaned.
     * @param async The callback to be used for notifying the caller after receiving the response from the server.
     */
    void cleanHTML(String dirtyHTML, AsyncCallback<String> async);

    /**
     * Synchronizes this editor with others that edit the same page.
     * 
     * @param syncedRevision The changes to this editor's content, since the last update.
     * @param pageName The page being edited.
     * @param version The version affected by syncedRevision.
     * @param async The callback to be used for notifying the caller after receiving the response from the server.
     */
    void syncEditorContent(Revision syncedRevision, String pageName, int version, AsyncCallback<SyncResult> async);

    /**
     * Check if the current wiki is part of a multiwiki (i.e. this is a virtual wiki).
     * 
     * @param async object used for asynchronous communication between server and client
     */
    void isMultiWiki(AsyncCallback<Boolean> async);

    /**
     * Returns a list containing the names of all wikis.
     * 
     * @param async async object used for asynchronous communication between server and client
     */
    void getVirtualWikiNames(AsyncCallback<List<String>> async);

    /**
     * Returns a list of all spaces names in the specified wiki.
     * 
     * @param wikiName the name of the wiki to search for spaces. If this is <code>null</code>, the current wiki will be
     *            used.
     * @param async object used for asynchronous communication between server and client
     */
    void getSpaceNames(String wikiName, AsyncCallback<List<String>> async);

    /**
     * Returns the list of the page names from a given space and a given wiki.
     * 
     * @param wikiName the name of the wiki. Pass <code>null</code> if this should use the current wiki.
     * @param spaceName the name of the space
     * @param async object used for asynchronous communication between server and client
     */
    void getPageNames(String wikiName, String spaceName, AsyncCallback<List<String>> async);

    /**
     * Creates a page url from the given parameters. None of them are mandatory, if one misses, it is replaced with a
     * default value.
     * 
     * @param wikiName the name of the wiki to which to link
     * @param spaceName the name of the space of the page. If this parameter is missing, it is replaced with the space
     *            of the current document in the context.
     * @param pageName the name of the page to which to link to. If it's missing, it is replaced with "WebHome".
     * @param revision the value for the page revision to which to link to. If this is missing, the link is made to the
     *            latest revision, the default view action for the document.
     * @param anchor the name of the anchor type.
     * @param async object used for asynchronous communication between server and client.
     */
    void createPageURL(String wikiName, String spaceName, String pageName, String revision, String anchor,
        AsyncCallback<String> async);

    /**
     * Returns all the image attachments from the page referred by its parameters. It can either get all the pictures in
     * a page or in a space or in a wiki, depending on the values of its parameters. A null means a missing parameter on
     * that position.
     * 
     * @param wikiName the name of the wiki to get images from
     * @param spaceName the name of the space to get image attachments from
     * @param pageName the name of the page to get image attachments from
     * @param async object used for asynchronous communication between server and client. 
     */
    void getImageAttachments(String wikiName, String spaceName, String pageName,
        AsyncCallback<List<ImageConfig>> async);
}
