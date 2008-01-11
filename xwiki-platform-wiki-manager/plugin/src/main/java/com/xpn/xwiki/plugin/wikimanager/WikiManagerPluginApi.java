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

package com.xpn.xwiki.plugin.wikimanager;

import com.xpn.xwiki.plugin.applicationmanager.core.api.XWikiExceptionApi;
import com.xpn.xwiki.plugin.applicationmanager.core.plugin.XWikiPluginMessageTool;
import com.xpn.xwiki.plugin.PluginApi;
import com.xpn.xwiki.plugin.wikimanager.doc.Wiki;
import com.xpn.xwiki.plugin.wikimanager.doc.XWikiServer;
import com.xpn.xwiki.plugin.wikimanager.doc.XWikiServerClass;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * API for managing wikis (create wiki, delete wiki, create wiki from template, etc).
 * 
 * @version $Id: $
 * @see com.xpn.xwiki.plugin.wikimanager.WikiManagerPlugin
 */
public class WikiManagerPluginApi extends PluginApi
{
    /**
     * Field name of the last error code inserted in context.
     */
    public static final String CONTEXT_LASTERRORCODE = "lasterrorcode";

    /**
     * Field name of the last api exception inserted in context.
     */
    public static final String CONTEXT_LASTEXCEPTION = "lastexception";

    /**
     * Logging tool.
     */
    protected static final Log LOG = LogFactory.getLog(WikiManagerPluginApi.class);

    /**
     * The default WikiManager managed exception.
     */
    private XWikiExceptionApi defaultException;

    /**
     * The plugin internationalization service.
     */
    private XWikiPluginMessageTool messageTool;

    /**
     * Create an instance of the Wiki Manager plugin user api.
     * 
     * @param plugin the entry point of the Wiki Manager plugin.
     * @param context the XWiki context.
     */
    public WikiManagerPluginApi(WikiManagerPlugin plugin, XWikiContext context)
    {
        super(plugin, context);

        this.defaultException =
            new XWikiExceptionApi(WikiManagerException.getDefaultException(), this.context);

        // Message Tool
        Locale locale = (Locale) context.get("locale");
        this.messageTool = new WikiManagerMessageTool(locale, context);

        context.put(WikiManager.MESSAGETOOL_CONTEXT_KEY, this.messageTool);
    }

    /**
     * @return the default plugin api exception.
     */
    public XWikiExceptionApi getDefaultException()
    {
        return this.defaultException;
    }

    /**
     * @return the plugin internationalization service.
     */
    public XWikiMessageTool getMessageTool()
    {
        return this.messageTool;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Wikis management

    /**
     * Create a new wiki from template.
     * 
     * @param wikiname the name of the new wiki.
     * @param templateWiki the name of the wiki from where to copy document to the new wiki.
     * @param pkgName the name of the attached XAR file to import in the new wiki.
     * @param wikiXObjectDocument a wiki descriptor document from which the new wiki descriptor
     *            document will be created.
     * @param failOnExist if true throw exception when wiki already exist. If false overwrite
     *            existing wiki.
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_XWIKINOTVIRTUAL}: xwiki is not in virtual
     *         mode.</li>
     *         <li>{@link WikiManagerException#ERROR_XWIKI_USERDOESNOTEXIST}: provided user does
     *         not exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKINAMEFORBIDDEN}: provided wiki name
     *         can't be used to create new wiki.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIALREADYEXISTS}: wiki descriptor
     *         already exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_UPDATEDATABASE}: error occurred when
     *         updating database.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEDOESNOTEXISTS}: attached package
     *         does not exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEIMPORT}: package loading failed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEINSTALL}: loaded package insertion
     *         into database failed.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int createNewWiki(String wikiname, String templateWiki, String pkgName,
        XWikiServer wikiXObjectDocument, boolean failOnExist) throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        wikiXObjectDocument.setWikiName(wikiname);

        String realTemplateWikiName =
            templateWiki == null || templateWiki.trim().length() == 0 ? null : templateWiki;

        String realPkgName = pkgName == null || pkgName.trim().length() == 0 ? null : pkgName;

        try {
            String comment;

            if (realTemplateWikiName != null) {
                comment = WikiManagerMessageTool.COMMENT_CREATEWIKIFROMTEMPLATE;
            } else if (realPkgName != null) {
                comment = WikiManagerMessageTool.COMMENT_CREATEWIKIFROMPACKAGE;
            } else {
                comment = WikiManagerMessageTool.COMMENT_CREATEEMPTYWIKI;
            }

            WikiManager.getInstance().createNewWiki(wikiXObjectDocument, failOnExist,
                realTemplateWikiName, realPkgName, comment, this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKICREATION, wikiXObjectDocument
                .toString()), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Delete wiki descriptor document from database.
     * 
     * @param wikiName the name of the wiki to delete.
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int deleteWiki(String wikiName) throws XWikiException
    {
        return deleteWiki(wikiName, 0);
    }

    /**
     * Delete wiki descriptor document from database.
     * 
     * @param wikiName the name of the wiki to delete.
     * @param objectId the id of the XWiki object included in the document to manage.
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int deleteWiki(String wikiName, int objectId) throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            WikiManager.getInstance().deleteWiki(wikiName, objectId, this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIDELETION, wikiName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Get {@link Wiki} with provided name.
     * 
     * @param wikiName the name of the wiki.
     * @return the {@link Wiki} object.
     * @throws XWikiException error when getting document from wiki name.
     */
    public Wiki getWikiFromName(String wikiName) throws XWikiException
    {
        Wiki doc = null;

        try {
            doc = WikiManager.getInstance().getWikiFromName(wikiName, context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIGET, wikiName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return doc;
    }
    
    /**
     * @return the list of all {@link Wiki}.
     * @throws XWikiException error when getting wiki documents descriptors.
     */
    public List getAllWikis() throws XWikiException
    {
        List wikiList = Collections.EMPTY_LIST;

        try {
            wikiList = WikiManager.getInstance().getAllWikis(context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIGETALL), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return wikiList;
    }

    /**
     * Get {@link Wiki} described by document with provided full name.
     * 
     * @param documentFullName the full name of the wiki document descriptor.
     * @return the {@link Wiki} object.
     * @throws XWikiException error when getting document.
     */
    public Wiki getWikiFromDocumentName(String documentFullName) throws XWikiException
    {
        Wiki doc = null;

        try {
            doc = WikiManager.getInstance().getWikiFromDocumentName(documentFullName, context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIGET, documentFullName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return doc;
    }

    /**
     * Get wiki descriptor document corresponding to provided wiki name.
     * 
     * @param wikiName the name of the wiki.
     * @return null if there is an error and add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public XWikiServer getWikiDocument(String wikiName) throws XWikiException
    {
        return getWikiDocument(wikiName, 0);
    }

    /**
     * Get wiki descriptor document corresponding to provided wiki name.
     * 
     * @param wikiName the name of the wiki.
     * @param objectId the id of the XWiki object included in the document to manage.
     * @return null if there is an error and add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public XWikiServer getWikiDocument(String wikiName, int objectId) throws XWikiException
    {
        XWikiServer doc = null;

        try {
            doc = WikiManager.getInstance().getWikiAlias(wikiName, objectId, true, this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIALIASGET, wikiName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return doc;
    }

    /**
     * Get the list of all wiki descriptor documents.
     * 
     * @return the list {@link XWikiServer}.
     * @throws XWikiException critical error in xwiki engine.
     */
    public List getWikiDocumentList() throws XWikiException
    {
        List listDocument = Collections.EMPTY_LIST;

        try {
            listDocument = WikiManager.getInstance().getWikiAliasList(this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKIALIASGETALL), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return listDocument;
    }

    /**
     * Create an empty not saved {@link XWikiServer}.
     * 
     * @return an empty not saved {@link XWikiServer}.
     * @throws XWikiException critical error in xwiki engine.
     */
    public XWikiServer createWikiDocument() throws XWikiException
    {
        return (XWikiServer) XWikiServerClass.getInstance(context).newXObjectDocument(context);
    }

    /**
     * Check if a Server of the given name exists in the master Wiki by checking if the
     * "XWiki.XWikiServer{serverName}" document is new.
     * 
     * @param wikiName the name of the server to be checked
     * @return true if server exists, false otherwise
     */
    public boolean isWikiExist(String wikiName)
    {
        return isWikiExist(wikiName, 0);
    }

    /**
     * Check if a Server of the given name exists in the master Wiki by checking if the
     * "XWiki.XWikiServer{serverName}" document is new.
     * 
     * @param wikiName the name of the server to be checked
     * @param objectId the id of the XWiki object included in the document to manage.
     * @return true if server exists, false otherwise
     */
    public boolean isWikiExist(String wikiName, int objectId)
    {
        return WikiManager.getInstance().isWikiAliasExist(wikiName, objectId, this.context);
    }

    /**
     * Change the {@link XWikiServerClass} "visibility" field of a wiki descriptor document.
     * 
     * @param wikiName the name of the wiki descriptor.
     * @param visibility the new value of "visibility" field. Can be "public", "private" or
     *            "template".
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int setWikiVisibility(String wikiName, String visibility) throws XWikiException
    {
        return setWikiVisibility(wikiName, 0, visibility);
    }

    /**
     * Change the {@link XWikiServerClass} "visibility" field of a wiki descriptor document.
     * 
     * @param wikiName the name of the wiki descriptor.
     * @param objectId the id of the XWiki object included in the document to manage.
     * @param visibility the new value of "visibility" field. Can be "public", "private" or
     *            "template".
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int setWikiVisibility(String wikiName, int objectId, String visibility)
        throws XWikiException
    {
        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            XWikiServer wikiDoc =
                WikiManager.getInstance().getWikiAlias(wikiName, objectId, true, this.context);
            wikiDoc.setVisibility(visibility);
            wikiDoc.save();
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKISETVISIBILITY, wikiName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));

            returncode = e.getCode();
        }

        return returncode;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Template management

    /**
     * Create a new xiki with {@link XWikiServerClass} "visibility" field set to "template".
     * 
     * @param templateName the name of the new wiki template to create.
     * @param templateDescription the description of the new wiki template to create.
     * @param packageName the name of the attached XAR file to import in the new wiki.
     * @return If there is error, it add error code in context {@link #CONTEXT_LASTERRORCODE} field
     *         and exception in context's {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_XWIKINOTVIRTUAL}: xwiki is not in virtual
     *         mode.</li>
     *         <li>{@link WikiManagerException#ERROR_XWIKI_USERDOESNOTEXIST}: provided user does
     *         not exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKINAMEFORBIDDEN}: provided wiki name
     *         can't be used to create new wiki.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIALREADYEXISTS}: wiki descriptor
     *         already exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_UPDATEDATABASE}: error occurred when
     *         updating database.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEDOESNOTEXISTS}: attached package
     *         does not exists.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEIMPORT}: package loading failed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_PACKAGEINSTALL}: loaded package insertion
     *         into database failed.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public int createWikiTemplate(String templateName, String templateDescription,
        String packageName) throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        XWikiServer wikiXObjectDocument =
            (XWikiServer) XWikiServerClass.getInstance(context).newXObjectDocument(context);
        wikiXObjectDocument.setWikiName(templateName);
        wikiXObjectDocument.setDescription(templateDescription);

        wikiXObjectDocument.setServer(templateName + ".template.local");

        wikiXObjectDocument.setState(XWikiServerClass.FIELDL_STATE_ACTIVE);
        wikiXObjectDocument.setOwner(this.context.getUser());

        try {
            WikiManager.getInstance().createWikiTemplate(
                wikiXObjectDocument,
                packageName,
                this.messageTool.get(WikiManagerMessageTool.COMMENT_CREATEWIKITEMPLATE,
                    new String[] {templateName, packageName}), this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKICREATION, wikiXObjectDocument
                .toString()), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Get wiki descriptor document corresponding to provided wiki name with
     * {@link XWikiServerClass} "visibility" field set to "template".
     * 
     * @param wikiName the name of the wiki template.
     * @return null if there is an error and add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public XWikiServer getWikiTemplateDocument(String wikiName) throws XWikiException
    {
        return getWikiTemplateDocument(wikiName, 0);
    }

    /**
     * Get wiki descriptor document corresponding to provided wiki name with
     * {@link XWikiServerClass} "visibility" field set to "template".
     * 
     * @param wikiName the name of the wiki template.
     * @param objectId the id of the XWiki object included in the document to manage.
     * @return null if there is an error and add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li>{@link XWikiExceptionApi#ERROR_NOERROR}: methods succeed.</li>
     *         <li>{@link WikiManagerException#ERROR_WM_WIKIDOESNOTEXISTS}: wiki to delete does
     *         not exists.</li>
     *         </ul>
     * @throws XWikiException critical error in xwiki engine.
     */
    public XWikiServer getWikiTemplateDocument(String wikiName, int objectId)
        throws XWikiException
    {
        XWikiServer doc = null;

        try {
            doc =
                WikiManager.getInstance().getWikiTemplateAlias(wikiName, objectId, this.context,
                    true);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKITEMPLATEGET, wikiName), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return doc;
    }

    /**
     * @return all the template wiki. Wiki with "visibility" field equals to "template".
     * @throws XWikiException critical error in xwiki engine.
     */
    public List getWikiTemplateList() throws XWikiException
    {
        List listDocument = new ArrayList();

        try {
            return WikiManager.getInstance().getWikiTemplateAliasList(this.context);
        } catch (WikiManagerException e) {
            LOG.error(messageTool.get(WikiManagerMessageTool.LOG_WIKITEMPLATEGETALL), e);

            this.context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            this.context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, this.context));
        }

        return listDocument;
    }
}
