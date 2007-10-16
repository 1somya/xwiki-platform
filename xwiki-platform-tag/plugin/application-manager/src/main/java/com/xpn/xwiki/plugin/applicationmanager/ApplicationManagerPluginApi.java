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

package com.xpn.xwiki.plugin.applicationmanager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.xpn.xwiki.plugin.applicationmanager.core.api.XWikiExceptionApi;
import com.xpn.xwiki.plugin.applicationmanager.core.plugin.XWikiPluginMessageTool;
import com.xpn.xwiki.plugin.PluginApi;
import com.xpn.xwiki.plugin.applicationmanager.doc.XWikiApplication;
import com.xpn.xwiki.plugin.applicationmanager.doc.XWikiApplicationClass;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Plugin for managing applications: installation, export, creation. The plugin uses the concept of
 * an Application Descriptor describing an application (its version, the documents it contains, the
 * translations, etc).
 * 
 * @version $Id: $
 * @see com.xpn.xwiki.plugin.applicationmanager.ApplicationManagerPlugin
 */
public class ApplicationManagerPluginApi extends PluginApi
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
     * The logging tool.
     */
    protected static final Log LOG = LogFactory.getLog(ApplicationManagerPluginApi.class);

    /**
     * Quote string.
     */
    private static final String QUOTE_STRING = "\"";

    /**
     * The default ApplicationManager managed exception.
     */
    private XWikiExceptionApi defaultException;

    /**
     * The plugin internationalization service.
     */
    private XWikiPluginMessageTool messageTool;

    /**
     * Create an instance of the Application Manager plugin user api.
     * 
     * @param plugin the entry point of the Application Manager plugin.
     * @param context the XWiki context.
     */
    public ApplicationManagerPluginApi(ApplicationManagerPlugin plugin, XWikiContext context)
    {
        super(plugin, context);

        // Default Exception
        defaultException =
            new XWikiExceptionApi(ApplicationManagerException.getDefaultException(), context);

        // Message Tool
        Locale locale = (Locale) context.get("locale");
        ResourceBundle bundle =
            ResourceBundle.getBundle(getPlugin().getName() + "/ApplicationResources", locale);
        this.messageTool = new XWikiPluginMessageTool(bundle, context);
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
    // Applications management

    /**
     * Create empty application document.
     * 
     * @return an empty application descriptor document.
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public XWikiApplication createApplicationDocument() throws XWikiException
    {
        return (XWikiApplication) XWikiApplicationClass.getInstance(context).newSuperDocument(
            context);
    }

    /**
     * Create a new application descriptor base on provided application descriptor.
     * 
     * @param appSuperDocument the user application descriptor from which new descriptor will be
     *            created.
     * @param failOnExist if true fail if the application descriptor to create already exists.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : method succeed with no error.</li>
     *         <li> {@link XWikiException#ERROR_XWIKI_ACCESS_DENIED} : context's user don't have
     *         rights to do this action.</li>
     *         <li>
     *         {@link ApplicationManagerException#ERROR_AM_APPDOCALREADYEXISTS} :
     *         application descriptor already exists.</li>
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public int createApplication(XWikiApplication appSuperDocument, boolean failOnExist)
        throws XWikiException
    {
        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        // TODO : check rights

        try {
            ApplicationManager.getInstance().createApplication(
                appSuperDocument,
                failOnExist,
                this.messageTool.get("applicationmanager.plugin.createapplication.comment",
                    appSuperDocument.toString()), context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to create application " + QUOTE_STRING + appSuperDocument
                + QUOTE_STRING, e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Delete an application descriptor document.
     * 
     * @param appName the name of the application.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link XWikiException#ERROR_XWIKI_ACCESS_DENIED} : context's user don't have
     *         rights to do this action.
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public int deleteApplication(String appName) throws XWikiException
    {
        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        // TODO : check rights

        try {
            ApplicationManager.getInstance().deleteApplication(appName, context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to delete application " + QUOTE_STRING + appName + QUOTE_STRING, e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Get all applications descriptors documents.
     * 
     * @return a list of XWikiApplication.
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public List getApplicationDocumentList() throws XWikiException
    {
        List listDocument = Collections.EMPTY_LIST;

        try {
            listDocument = ApplicationManager.getInstance().getApplicationList(this.context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to get all applications documents", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));
        }

        return listDocument;
    }

    /**
     * Get the application descriptor document of the provided application name.
     * 
     * @param appName the name of the application.
     * @return the application descriptor document. If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field. Error codes can be :
     *         <ul>
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public XWikiApplication getApplicationDocument(String appName) throws XWikiException
    {
        XWikiApplication app = null;

        try {
            app = ApplicationManager.getInstance().getApplication(appName, context, true);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to get application document from application name", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));
        }

        return app;
    }

    /**
     * Export an application into XAR using Packaging plugin.
     * 
     * @param appName the name of the application to export.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     * @throws IOException all error that does not caused by user of this method.
     */
    public int exportApplicationXAR(String appName) throws XWikiException, IOException
    {
        return exportApplicationXAR(appName, true, false);
    }

    /**
     * Export an application into XAR using Packaging plugin.
     * 
     * @param appName the name of the application.
     * @param recurse if true include all dependencies applications into XAR.
     * @param withDocHistory if true export with documents history.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     * @throws IOException all error that does not caused by user of this method.
     */
    public int exportApplicationXAR(String appName, boolean recurse, boolean withDocHistory)
        throws XWikiException, IOException
    {
        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            ApplicationManager.getInstance().exportApplicationXAR(appName, recurse,
                withDocHistory, context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to export application in a XAR package", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Import attached application XAR into current wiki and do all actions needed to installation
     * an application. See {@link #reloadApplication(String)} for more.
     * 
     * @param packageName the name of the attached XAR file to import.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link XWikiException#ERROR_XWIKI_ACCESS_DENIED} : context's user don't have
     *         rights to do this action.
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public int importApplication(String packageName) throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            ApplicationManager.getInstance().importApplication(
                context.getDoc(),
                packageName,
                this.messageTool.get("applicationmanager.plugin.importapplication.comment",
                    packageName), context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to import applications from XAR package", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Reload xwiki application. It means :
     * <ul>
     * <li> update XWikiPreferences with application translation documents.
     * </ul>
     * 
     * @param appName the name of the application to reload.
     * @return error code . If there is error, it add error code in context
     *         {@link #CONTEXT_LASTERRORCODE} field and exception in context's
     *         {@link #CONTEXT_LASTEXCEPTION} field.
     *         <p>
     *         Error codes can be :
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link XWikiException#ERROR_XWIKI_ACCESS_DENIED} : context's user don't have
     *         rights to do this action.
     *         <li> {@link ApplicationManagerException#ERROR_AM_DOESNOTEXIST} :
     *         provided application does not exist.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public int reloadApplication(String appName) throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            XWikiApplication app =
                ApplicationManager.getInstance().getApplication(appName, context, true);
            ApplicationManager.getInstance().reloadApplication(
                app,
                this.messageTool.get("applicationmanager.plugin.reloadapplication.comment", app
                    .getAppName()), context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to reload application", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Reload all xwiki applications. It means : - update XWikiPreferences with each application
     * translation documents
     * 
     * @return error code.
     *         <ul>
     *         <li> {@link XWikiExceptionApi#ERROR_NOERROR} : action finished with no error.
     *         <li> {@link XWikiException#ERROR_XWIKI_ACCESS_DENIED} : context's user don't have
     *         rights to do this action.
     *         </ul>
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public int reloadAllApplications() throws XWikiException
    {
        if (!hasAdminRights()) {
            return XWikiException.ERROR_XWIKI_ACCESS_DENIED;
        }

        int returncode = XWikiExceptionApi.ERROR_NOERROR;

        try {
            ApplicationManager.getInstance().reloadAllApplications(
                this.messageTool.get("applicationmanager.plugin.reloadallapplications.comment"),
                context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to reload all applications", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));

            returncode = e.getCode();
        }

        return returncode;
    }

    /**
     * Get the current wiki root application.
     * 
     * @return the root application descriptor document. If can't find root application return null.
     * @throws XWikiException all error that does not caused by user of this method.
     */
    public XWikiApplication getRootApplication() throws XWikiException
    {
        XWikiApplication app = null;

        try {
            app = ApplicationManager.getInstance().getRootApplication(context);
        } catch (ApplicationManagerException e) {
            LOG.error("Try to get root application document", e);

            context.put(CONTEXT_LASTERRORCODE, new Integer(e.getCode()));
            context.put(CONTEXT_LASTEXCEPTION, new XWikiExceptionApi(e, context));
        }

        return app;
    }
}
