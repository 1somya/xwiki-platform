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

package com.xpn.xwiki.plugin.applicationmanager.doc;

import java.util.List;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.AbstractSuperClass;
import com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperDocument;
import com.xpn.xwiki.objects.classes.StaticListClass;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.applicationmanager.ApplicationManagerException;

/**
 * {@link SuperClass} implementation for XAppClasses.XWikiApplicationClass class.
 * 
 * @see SuperClass
 * @see AbstractSuperClass
 */
public class XWikiApplicationClass extends AbstractSuperClass
{
    /**
     * Space of class document.
     */
    private static final String CLASS_SPACE_PREFIX = "XApp";

    /**
     * Prefix of class document.
     */
    private static final String CLASS_PREFIX = "XWikiApplication";

    // ///

    /**
     * Name of field <code>appname</code> for the XWiki class XAppClasses.XWikiApplicationClass.
     * The name of the application.
     */
    public static final String FIELD_appname = "appname";

    /**
     * Pretty name of field <code>appname</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_appname = "Application Name";

    /**
     * Name of field <code>description</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass. The description of the application.
     */
    public static final String FIELD_description = "description";

    /**
     * Pretty name of field <code>description</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_description = "Description";

    /**
     * Name of field <code>version</code> for the XWiki class XAppClasses.XWikiApplicationClass.
     * The version of the application.
     */
    public static final String FIELD_appversion = "appversion";

    /**
     * Pretty name of field <code>version</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_appversion = "Application Version";

    /**
     * Name of field <code>dependencies</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass. The list of plugins on which application depends.
     */
    public static final String FIELD_dependencies = "dependencies";

    /**
     * Pretty name of field <code>dependencies</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_dependencies = "Dependencies";

    /**
     * Name of field <code>applications</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass. The list of other applications on which current
     * application depends.
     */
    public static final String FIELD_applications = "applications";

    /**
     * Pretty name of field <code>applications</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_applications = "Applications";

    /**
     * Name of field <code>documents</code> for the XWiki class XAppClasses.XWikiApplicationClass.
     * The list of documents application contains.
     */
    public static final String FIELD_documents = "documents";

    /**
     * Pretty name of field <code>documents</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_documents = "Documents";

    /**
     * Name of field <code>docstoinclude</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass. The list of document application contains that will be
     * included in place of copy from wiki template.
     */
    public static final String FIELD_docstoinclude = "docstoinclude";

    /**
     * Pretty name of field <code>docstoinclude</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_docstoinclude = "Documents to include";

    /**
     * Name of field <code>docstolink</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass. The list of document application contains that will be
     * linked in place of copy from wiki template.
     */
    public static final String FIELD_docstolink = "docstolink";

    /**
     * Pretty name of field <code>docstolink</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_docstolink = "Documents to link";

    /**
     * Name of field <code>translationdocs</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELD_translationdocs = "translationdocs";

    /**
     * Pretty name of field <code>translationdocs</code> for the XWiki class
     * XAppClasses.XWikiApplicationClass.
     */
    public static final String FIELDPN_translationdocs = "Translations documents";

    // ///

    /**
     * Unique instance of XWikiApplicationClass;
     */
    private static XWikiApplicationClass instance = null;

    /**
     * Return unique instance of XWikiApplicationClass and update documents for this context. It
     * also check if the corresponding Xwiki class/template/sheet exist in context's database and
     * create it if not.
     * 
     * @param context the XWiki context.
     * @return a unique instance of XWikiApplicationClass.
     * @throws XWikiException
     */
    public static XWikiApplicationClass getInstance(XWikiContext context) throws XWikiException
    {
        synchronized (XWikiApplicationClass.class) {
            if (instance == null)
                instance = new XWikiApplicationClass();
        }

        instance.check(context);

        return instance;
    }

    private XWikiApplicationClass()
    {
        super(CLASS_SPACE_PREFIX, CLASS_PREFIX);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.AbstractSuperClass#updateBaseClass(com.xpn.xwiki.objects.classes.BaseClass)
     */
    protected boolean updateBaseClass(BaseClass baseClass)
    {
        boolean needsUpdate = super.updateBaseClass(baseClass);

        needsUpdate |= baseClass.addTextField(FIELD_appname, FIELDPN_appname, 30);
        needsUpdate |= baseClass.addTextAreaField(FIELD_description, FIELDPN_description, 40, 5);
        needsUpdate |= baseClass.addTextField(FIELD_appversion, FIELDPN_appversion, 30);

        StaticListClass slc;

        if (baseClass.addStaticListField(FIELD_dependencies, FIELDPN_dependencies, 80, true, "",
            "input")) {
            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_dependencies);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        if (baseClass.addStaticListField(FIELD_applications, FIELDPN_applications, 80, true, "",
            "input")) {
            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_applications);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        if (baseClass.addStaticListField(FIELD_documents, FIELDPN_documents, 80, true, "",
            "input")) {

            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_documents);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        if (baseClass.addStaticListField(FIELD_docstoinclude, FIELDPN_docstoinclude, 80, true,
            "", "input")) {

            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_docstoinclude);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        if (baseClass.addStaticListField(FIELD_docstolink, FIELDPN_docstolink, 80, true, "",
            "input")) {

            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_docstolink);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        if (baseClass.addStaticListField(FIELD_translationdocs, FIELDPN_translationdocs, 80,
            true, "", "input")) {

            // TODO : move into BaseClass.addStaticListField with "separators' parameter when/if
            // http://jira.xwiki.org/jira/browse/XWIKI-1683 is applied in XWiki Core and when this
            // starts depending on that version where it's applied.
            slc = (StaticListClass) baseClass.getField(FIELD_translationdocs);
            slc.setSeparators("|");
            slc.setSeparator("|");

            needsUpdate = true;
        }

        return needsUpdate;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.AbstractSuperClass#updateClassTemplateDocument(com.xpn.xwiki.doc.XWikiDocument)
     */
    protected boolean updateClassTemplateDocument(XWikiDocument doc)
    {
        boolean needsUpdate = false;

        if ((getClassSpacePrefix() + "Manager.WebHome").equals(doc.getParent())) {
            doc.setParent(getClassSpacePrefix() + "Manager.WebHome");
            needsUpdate = true;
        }

        if ("1.0".equals(doc.getStringValue(getClassFullName(), FIELD_appversion))) {
            doc.setStringValue(getClassFullName(), FIELD_appversion, "1.0");
            needsUpdate = true;
        }

        return needsUpdate;
    }

    /**
     * Get the XWiki document descriptor of containing XAppClasses.XWikiApplication XWiki object
     * with "appname" field equals to <code>appName</code>.
     * 
     * @param appName the name of the application.
     * @param context the XWiki context.
     * @param validate indicate if it return new XWikiDocument or throw exception if application
     *            descriptor does not exist.
     * @return the XWikiDocument representing application descriptor.
     * @throws XWikiException
     * @see #getApplication(String, XWikiContext, boolean)
     */
    private XWikiDocument getApplicationDocument(String appName, XWikiContext context,
        boolean validate) throws XWikiException
    {
        XWiki xwiki = context.getWiki();

        List listApp =
            searchItemDocumentsByField(FIELD_appname, appName, StringProperty.class
                .getSimpleName(), context);

        if (listApp.size() == 0) {
            if (validate)
                throw new ApplicationManagerException(ApplicationManagerException.ERROR_APPLICATIONMANAGER_DOES_NOT_EXIST,
                    appName + " application does not exist");
            else
                return xwiki.getDocument(getItemDocumentDefaultFullName(appName, context),
                    context);
        }

        return (XWikiDocument) listApp.get(0);
    }

    /**
     * Get the XWiki document descriptor of containing XAppClasses.XWikiApplication XWiki object
     * with "appname" field equals to <code>appName</code>.
     * 
     * @param appName the name of the application.
     * @param context the XWiki context.
     * @param validate indicate if it return new XWikiDocument or throw exception if application
     *            descriptor does not exist.
     * @return the XWikiDocument representing application descriptor.
     * @throws XWikiException
     * @see #getApplicationDocument(String, XWikiContext, boolean)
     */
    public XWikiApplication getApplication(String appName, XWikiContext context, boolean validate)
        throws XWikiException
    {
        return (XWikiApplication) newSuperDocument(getApplicationDocument(appName, context,
            validate), context);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override abstract method using {@link XWikiApplication} as {@link XWikiApplication}
     * 
     * @see com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.AbstractSuperClass#newSuperDocument(com.xpn.xwiki.doc.XWikiDocument,
     *      com.xpn.xwiki.XWikiContext)
     */
    public SuperDocument newSuperDocument(XWikiDocument doc, XWikiContext context)
    {
        return (SuperDocument) doc.newDocument(XWikiApplication.class.getName(), context);
    }
}
