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

package com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.stub.CustomStub;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.XWikiHibernateVersioningStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.XWikiVersioningStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * Unit tests for
 * {@link com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.AbstractSuperClass}.
 * 
 * @version $Id: $
 */
public class TestAbstractSuperClassTest extends MockObjectTestCase
{
    private XWikiContext context;

    private XWiki xwiki;

    private Mock mockXWikiStore;

    private Mock mockXWikiVersioningStore;

    private Map documents = new HashMap();

    protected void setUp() throws XWikiException
    {
        this.context = new XWikiContext();
        this.xwiki = new XWiki(new XWikiConfig(), this.context);

        // //////////////////////////////////////////////////
        // XWikiHibernateStore

        this.mockXWikiStore =
            mock(XWikiHibernateStore.class, new Class[] {XWiki.class, XWikiContext.class},
                new Object[] {this.xwiki, this.context});
        this.mockXWikiStore.stubs().method("loadXWikiDoc").will(
            new CustomStub("Implements XWikiStoreInterface.loadXWikiDoc")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    XWikiDocument shallowDoc = (XWikiDocument) invocation.parameterValues.get(0);

                    if (documents.containsKey(shallowDoc.getFullName())) {
                        return documents.get(shallowDoc.getFullName());
                    } else {
                        return shallowDoc;
                    }
                }
            });
        this.mockXWikiStore.stubs().method("saveXWikiDoc").will(
            new CustomStub("Implements XWikiStoreInterface.saveXWikiDoc")
            {
                public Object invoke(Invocation invocation) throws Throwable
                {
                    XWikiDocument document = (XWikiDocument) invocation.parameterValues.get(0);

                    document.setNew(false);
                    document.setStore((XWikiStoreInterface) mockXWikiStore.proxy());
                    documents.put(document.getFullName(), document);

                    return null;
                }
            });
        this.mockXWikiStore.stubs().method("getTranslationList").will(
            returnValue(Collections.EMPTY_LIST));

        this.mockXWikiVersioningStore =
            mock(XWikiHibernateVersioningStore.class, new Class[] {XWiki.class,
            XWikiContext.class}, new Object[] {this.xwiki, this.context});
        this.mockXWikiVersioningStore.stubs().method("getXWikiDocumentArchive").will(
            returnValue(null));
        this.mockXWikiVersioningStore.stubs().method("resetRCSArchive").will(returnValue(null));

        this.xwiki.setStore((XWikiStoreInterface) mockXWikiStore.proxy());
        this.xwiki.setVersioningStore((XWikiVersioningStoreInterface) mockXWikiVersioningStore
            .proxy());

        // ////////////////////////////////////////////////////////////////////////////////
        // XWikiRightService

        this.xwiki.setRightService(new XWikiRightService()
        {
            public boolean checkAccess(String action, XWikiDocument doc, XWikiContext context)
                throws XWikiException
            {
                return true;
            }

            public boolean hasAccessLevel(String right, String username, String docname,
                XWikiContext context) throws XWikiException
            {
                return true;
            }

            public boolean hasAdminRights(XWikiContext context)
            {
                return true;
            }

            public boolean hasProgrammingRights(XWikiContext context)
            {
                return true;
            }

            public boolean hasProgrammingRights(XWikiDocument doc, XWikiContext context)
            {
                return true;
            }

            public List listAllLevels(XWikiContext context) throws XWikiException
            {
                return Collections.EMPTY_LIST;
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////////////////:
    // Tests

    private static final String CLASS_SPACE_PREFIX = "Space";

    private static final String CLASS_PREFIX = "Prefix";

    private static final String CLASS_NAME =
        CLASS_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASS_SUFFIX;

    private static final String CLASSSHEET_NAME =
        CLASS_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASSSHEET_SUFFIX;

    private static final String CLASSTEMPLATE_NAME =
        CLASS_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASSTEMPLATE_SUFFIX;

    private static final String DISPATCH_CLASS_SPACE =
        CLASS_SPACE_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASS_SPACE_SUFFIX;

    private static final String DISPATCH_CLASS_FULLNAME = DISPATCH_CLASS_SPACE + "." + CLASS_NAME;

    private static final String DISPATCH_CLASSSHEET_SPACE =
        CLASS_SPACE_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASSSHEET_SPACE_SUFFIX;

    private static final String DISPATCH_CLASSSHEET_FULLNAME =
        DISPATCH_CLASSSHEET_SPACE + "." + CLASSSHEET_NAME;

    private static final String DISPATCH_CLASSTEMPLATE_SPACE =
        CLASS_SPACE_PREFIX
            + com.xpn.xwiki.plugin.applicationmanager.core.doc.objects.classes.SuperClass.XWIKI_CLASSTEMPLATE_SPACE_SUFFIX;

    private static final String DISPATCH_CLASSTEMPLATE_FULLNAME =
        DISPATCH_CLASSTEMPLATE_SPACE + "." + CLASSTEMPLATE_NAME;

    private static final String NODISPATCH_CLASS_SPACE = CLASS_SPACE_PREFIX;

    private static final String NODISPATCH_CLASS_FULLNAME =
        NODISPATCH_CLASS_SPACE + "." + CLASS_NAME;

    private static final String NODISPATCH_CLASSSHEET_SPACE = CLASS_SPACE_PREFIX;

    private static final String NODISPATCH_CLASSSHEET_FULLNAME =
        NODISPATCH_CLASSSHEET_SPACE + "." + CLASSSHEET_NAME;

    private static final String NODISPATCH_CLASSTEMPLATE_SPACE = CLASS_SPACE_PREFIX;

    private static final String NODISPATCH_CLASSTEMPLATE_FULLNAME =
        NODISPATCH_CLASSTEMPLATE_SPACE + "." + CLASSTEMPLATE_NAME;

    private static final String DEFAULT_ITEM_NAME = "item";

    private static final String DEFAULT_ITEMDOCUMENT_NAME = CLASS_PREFIX + "Item";

    private static final String DISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME =
        CLASS_SPACE_PREFIX + "." + DEFAULT_ITEMDOCUMENT_NAME;

    private static final String NODISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME =
        CLASS_SPACE_PREFIX + "." + DEFAULT_ITEMDOCUMENT_NAME;

    /**
     * Name of field <code>string</code>.
     */
    public static final String FIELD_string = "string";

    /**
     * Pretty name of field <code>string</code>.
     */
    public static final String FIELDPN_string = "String";

    /**
     * Name of field <code>stringlist</code>.
     */
    public static final String FIELD_stringlist = "stringlist";

    /**
     * Pretty name of field <code>stringlist</code>.
     */
    public static final String FIELDPN_stringlist = "String List";
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with no parameters.
     */
    public static final String WHERECLAUSE_null = ", BaseObject as obj where doc.fullName=obj.name and obj.className=? and obj.name<>?";
    
    public static final String[] WHERECLAUSE_doc0 = {"df0", null, "dv0"};
    public static final String[] WHERECLAUSE_doc1 = {"df1", null, "dv1"};
    public static final String[] WHERECLAUSE_obj0 = {"of0", "String", "ov0"};
    public static final String[] WHERECLAUSE_obj1 = {"of1", "Int", "ov1"};
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with doc filter.
     */
    public static final String WHERECLAUSE_doc = ", BaseObject as obj where doc.fullName=obj.name and obj.className=? and obj.name<>? and lower(doc.df0)=?";
    public static final String[][] WHERECLAUSE_PARAM_doc = {WHERECLAUSE_doc0}; 

    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with more than one docs filters.
     */
    public static final String WHERECLAUSE_doc_multi = ", BaseObject as obj where doc.fullName=obj.name and obj.className=? and obj.name<>? and lower(doc.df0)=? and lower(doc.df1)=?";
    public static final String[][] WHERECLAUSE_PARAM_doc_multi = {WHERECLAUSE_doc0, WHERECLAUSE_doc1};
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with object filter.
     */
    public static final String WHERECLAUSE_obj = ", BaseObject as obj, String as field0 where doc.fullName=obj.name and obj.className=? and obj.name<>? and obj.id=field0.id.id and field0.name=? and lower(field0.value)=?";
    public static final String[][] WHERECLAUSE_PARAM_obj = {WHERECLAUSE_obj0};
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with more than one objects filters.
     */
    public static final String WHERECLAUSE_obj_multi = ", BaseObject as obj, String as field0, Int as field1 where doc.fullName=obj.name and obj.className=? and obj.name<>? and obj.id=field0.id.id and field0.name=? and lower(field0.value)=? and obj.id=field1.id.id and field1.name=? and lower(field1.value)=?";
    public static final String[][] WHERECLAUSE_PARAM_obj_multi = {WHERECLAUSE_obj0, WHERECLAUSE_obj1};
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with object and doc filter.
     */
    public static final String WHERECLAUSE_objdoc = ", BaseObject as obj, String as field1 where doc.fullName=obj.name and obj.className=? and obj.name<>? and lower(doc.df0)=? and obj.id=field1.id.id and field1.name=? and lower(field1.value)=?";
    public static final String[][] WHERECLAUSE_PARAM_objdoc = {WHERECLAUSE_doc0, WHERECLAUSE_obj0};
    
    /**
     * Result of {@link AbstractSuperClass#createWhereClause(String[][], List)} with more than one objects and docs filters.
     */
    public static final String WHERECLAUSE_objdoc_multi = ", BaseObject as obj, String as field2, Int as field3 where doc.fullName=obj.name and obj.className=? and obj.name<>? and lower(doc.df0)=? and lower(doc.df1)=? and obj.id=field2.id.id and field2.name=? and lower(field2.value)=? and obj.id=field3.id.id and field3.name=? and lower(field3.value)=?";
    public static final String[][] WHERECLAUSE_PARAM_objdoc_multi = {WHERECLAUSE_doc0, WHERECLAUSE_doc1, WHERECLAUSE_obj0, WHERECLAUSE_obj1};
    
    static abstract public class TestSuperClass extends AbstractSuperClass
    {
        /**
         * Default constructor for XWikiApplicationClass.
         */
        protected TestSuperClass(String spaceprefix, String prefix, boolean dispatch)
        {
            super(spaceprefix, prefix, dispatch);
        }

        protected boolean updateBaseClass(BaseClass baseClass)
        {
            boolean needsUpdate = super.updateBaseClass(baseClass);

            needsUpdate |= baseClass.addTextField(FIELD_string, FIELDPN_string, 30);
            needsUpdate |= baseClass.addTextField(FIELD_stringlist, FIELDPN_stringlist, 80);

            return needsUpdate;
        }
    }

    static public class DispatchSuperClass extends TestSuperClass
    {
        /**
         * Unique instance of XWikiApplicationClass;
         */
        private static DispatchSuperClass instance = null;

        /**
         * Return unique instance of XWikiApplicationClass and update documents for this context.
         * 
         * @param context Context.
         * @return XWikiApplicationClass Instance of XWikiApplicationClass.
         * @throws XWikiException
         */
        public static DispatchSuperClass getInstance(XWikiContext context) throws XWikiException
        {
            // if (instance == null)
            instance = new DispatchSuperClass();

            instance.check(context);

            return instance;
        }

        /**
         * Default constructor for XWikiApplicationClass.
         */
        private DispatchSuperClass()
        {
            super(CLASS_SPACE_PREFIX, CLASS_PREFIX, true);
        }
    }

    static public class NoDispatchSuperClass extends TestSuperClass
    {
        /**
         * Unique instance of XWikiApplicationClass;
         */
        private static NoDispatchSuperClass instance = null;

        /**
         * Return unique instance of XWikiApplicationClass and update documents for this context.
         * 
         * @param context Context.
         * @return XWikiApplicationClass Instance of XWikiApplicationClass.
         * @throws XWikiException
         */
        public static NoDispatchSuperClass getInstance(XWikiContext context)
            throws XWikiException
        {
            // if (instance == null)
            instance = new NoDispatchSuperClass();

            instance.check(context);

            return instance;
        }

        /**
         * Default constructor for XWikiApplicationClass.
         */
        private NoDispatchSuperClass()
        {
            super(CLASS_SPACE_PREFIX, CLASS_PREFIX, false);
        }
    }

    public void testInitSuperClassDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        SuperClass sclass = DispatchSuperClass.getInstance(context);

        assertEquals(CLASS_SPACE_PREFIX, sclass.getClassSpacePrefix());
        assertEquals(CLASS_PREFIX, sclass.getClassPrefix());

        assertEquals(CLASS_NAME, sclass.getClassName());
        assertEquals(CLASSSHEET_NAME, sclass.getClassSheetName());
        assertEquals(CLASSTEMPLATE_NAME, sclass.getClassTemplateName());

        assertEquals(DISPATCH_CLASS_SPACE, sclass.getClassSpace());
        assertEquals(DISPATCH_CLASS_FULLNAME, sclass.getClassFullName());
        assertEquals(DISPATCH_CLASSSHEET_SPACE, sclass.getClassSheetSpace());
        assertEquals(DISPATCH_CLASSSHEET_FULLNAME, sclass.getClassSheetFullName());
        assertEquals(DISPATCH_CLASSTEMPLATE_SPACE, sclass.getClassTemplateSpace());
        assertEquals(DISPATCH_CLASSTEMPLATE_FULLNAME, sclass.getClassTemplateFullName());
    }

    public void testInitSuperClassNoDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        SuperClass sclass = NoDispatchSuperClass.getInstance(context);

        assertEquals(CLASS_SPACE_PREFIX, sclass.getClassSpacePrefix());
        assertEquals(CLASS_PREFIX, sclass.getClassPrefix());

        assertEquals(CLASS_NAME, sclass.getClassName());
        assertEquals(CLASSSHEET_NAME, sclass.getClassSheetName());
        assertEquals(CLASSTEMPLATE_NAME, sclass.getClassTemplateName());

        assertEquals(NODISPATCH_CLASS_SPACE, sclass.getClassSpace());
        assertEquals(NODISPATCH_CLASS_FULLNAME, sclass.getClassFullName());
        assertEquals(NODISPATCH_CLASSSHEET_SPACE, sclass.getClassSheetSpace());
        assertEquals(NODISPATCH_CLASSSHEET_FULLNAME, sclass.getClassSheetFullName());
        assertEquals(NODISPATCH_CLASSTEMPLATE_SPACE, sclass.getClassTemplateSpace());
        assertEquals(NODISPATCH_CLASSTEMPLATE_FULLNAME, sclass.getClassTemplateFullName());
    }

    private void ptestCkeck(SuperClass sclass) throws XWikiException
    {
        XWikiDocument doc = xwiki.getDocument(sclass.getClassFullName(), context);

        assertFalse(doc.isNew());

        BaseClass baseclass = doc.getxWikiClass();

        assertEquals(sclass.getClassFullName(), baseclass.getName());

        PropertyInterface prop = baseclass.getField(FIELD_string);

        assertNotNull(prop);

        prop = baseclass.getField(FIELD_stringlist);

        assertNotNull(prop);

        // ///

        XWikiDocument docSheet = xwiki.getDocument(sclass.getClassSheetFullName(), context);

        assertFalse(docSheet.isNew());

        // ///

        XWikiDocument docTemplate = xwiki.getDocument(sclass.getClassTemplateFullName(), context);

        assertFalse(docTemplate.isNew());

        BaseObject baseobject = docTemplate.getObject(sclass.getClassFullName());

        assertNotNull(baseobject);
    }

    public void testCkeckDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestCkeck(NoDispatchSuperClass.getInstance(context));
    }

    public void testCkeckNoDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestCkeck(NoDispatchSuperClass.getInstance(context));
    }

    private void ptestGetClassDocument(SuperClass sclass) throws XWikiException
    {
        XWikiDocument doc = xwiki.getDocument(sclass.getClassFullName(), context);
        Document docFromClass = sclass.getClassDocument(context);

        assertFalse(docFromClass.isNew());
        assertEquals(doc.getFullName(), docFromClass.getFullName());
    }

    public void testGetClassDocumentDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassDocument(DispatchSuperClass.getInstance(context));
    }

    public void testGetClassDocumentNoDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassDocument(NoDispatchSuperClass.getInstance(context));
    }

    private void ptestGetClassSheetDocument(SuperClass sclass) throws XWikiException
    {
        XWikiDocument doc = xwiki.getDocument(sclass.getClassSheetFullName(), context);
        Document docFromClass = sclass.getClassSheetDocument(context);

        assertFalse(docFromClass.isNew());
        assertEquals(doc.getFullName(), docFromClass.getFullName());
    }

    public void testGetClassSheetDocumentDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassSheetDocument(DispatchSuperClass.getInstance(context));
    }

    public void testGetClassSheetDocumentNoDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassSheetDocument(NoDispatchSuperClass.getInstance(context));
    }

    private void ptestGetClassTemplateDocument(SuperClass sclass) throws XWikiException
    {
        XWikiDocument doc = xwiki.getDocument(sclass.getClassTemplateFullName(), context);
        Document docFromClass = sclass.getClassTemplateDocument(context);

        assertFalse(docFromClass.isNew());
        assertEquals(doc.getFullName(), docFromClass.getFullName());
    }

    public void testGetClassTemplateDocumentDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassTemplateDocument(DispatchSuperClass.getInstance(context));
    }

    public void testGetClassTemplateDocumentNoDispatch() throws XWikiException
    {
        documents.clear();

        // ///

        ptestGetClassTemplateDocument(NoDispatchSuperClass.getInstance(context));
    }

    public void testGetItemDefaultNameDisptach() throws XWikiException
    {
        assertEquals(DEFAULT_ITEM_NAME, DispatchSuperClass.getInstance(context)
            .getItemDefaultName(DISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME));
    }

    public void testGetItemDefaultNameNoDispatch() throws XWikiException
    {
        assertEquals(DEFAULT_ITEM_NAME, NoDispatchSuperClass.getInstance(context)
            .getItemDefaultName(NODISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME));
    }

    public void testGetItemDocumentDefaultNameDispatch() throws XWikiException
    {
        assertEquals(DEFAULT_ITEMDOCUMENT_NAME, DispatchSuperClass.getInstance(context)
            .getItemDocumentDefaultName(DEFAULT_ITEM_NAME, context));
        assertEquals(DEFAULT_ITEMDOCUMENT_NAME, DispatchSuperClass.getInstance(context)
            .getItemDocumentDefaultName(DEFAULT_ITEM_NAME + " ", context));
        assertEquals(DEFAULT_ITEMDOCUMENT_NAME, DispatchSuperClass.getInstance(context)
            .getItemDocumentDefaultName(DEFAULT_ITEM_NAME + "\t", context));
        assertEquals(DEFAULT_ITEMDOCUMENT_NAME, DispatchSuperClass.getInstance(context)
            .getItemDocumentDefaultName(DEFAULT_ITEM_NAME + ".", context));
    }

    public void testGetItemDocumentDefaultNameNoDispatch() throws XWikiException
    {
        assertEquals(DEFAULT_ITEMDOCUMENT_NAME, NoDispatchSuperClass.getInstance(context)
            .getItemDocumentDefaultName(DEFAULT_ITEM_NAME, context));
    }

    public void testGetItemDocumentDefaultFullNameDispatch() throws XWikiException
    {
        assertEquals(DISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME, DispatchSuperClass.getInstance(
            context).getItemDocumentDefaultFullName(DEFAULT_ITEM_NAME, context));
    }

    public void testGetItemDocumentDefaultFullNameNoDispatch() throws XWikiException
    {
        assertEquals(NODISPATCH_DEFAULT_ITEMDOCUMENT_FULLNAME, NoDispatchSuperClass.getInstance(
            context).getItemDocumentDefaultFullName(DEFAULT_ITEM_NAME, context));
    }

    public void testIsInstanceNoDispatch() throws XWikiException
    {
        assertTrue(NoDispatchSuperClass.getInstance(context).isInstance(
            NoDispatchSuperClass.getInstance(context).newSuperDocument(context).getDocumentApi()));
        assertFalse(NoDispatchSuperClass.getInstance(context).isInstance(new XWikiDocument()));
    }

    public void testIsInstanceDispatch() throws XWikiException
    {
        assertTrue(DispatchSuperClass.getInstance(context).isInstance(
            DispatchSuperClass.getInstance(context).newSuperDocument(context).getDocumentApi()));
        assertFalse(DispatchSuperClass.getInstance(context).isInstance(new XWikiDocument()));
    }
    
    public void testCreateWhereClause_null() throws XWikiException
    {
        List list = new ArrayList();
        
        String where = DispatchSuperClass.getInstance(context).createWhereClause(null, list);
        
        assertEquals(WHERECLAUSE_null, where);
    }
    
    public void testCreateWhereClause_nothing() throws XWikiException
    {
        List list = new ArrayList();
        String[][] fieldDescriptors = new String[][]{};
        
        String where = DispatchSuperClass.getInstance(context).createWhereClause(fieldDescriptors, list);
        
        assertEquals(WHERECLAUSE_null, where);
    }
    
    public void testCreateWhereClause_doc() throws XWikiException
    {
        List list = new ArrayList();
        
        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_doc, list);
        
        assertEquals(WHERECLAUSE_doc, where);
    }
    
    public void testCreateWhereClause_doc_multi() throws XWikiException
    {
        List list = new ArrayList();
        
        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_doc_multi, list);
        
        assertEquals(WHERECLAUSE_doc_multi, where);
    }
    
    public void testCreateWhereClause_obj() throws XWikiException
    {
        List list = new ArrayList();

        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_obj, list);
        
        assertEquals(WHERECLAUSE_obj, where);
    }
    
    public void testCreateWhereClause_obj_multi() throws XWikiException
    {
        List list = new ArrayList();

        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_obj_multi, list);
        
        assertEquals(WHERECLAUSE_obj_multi, where);
    }
    
    public void testCreateWhereClause_objdoc() throws XWikiException
    {
        List list = new ArrayList();

        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_objdoc, list);
        
        assertEquals(WHERECLAUSE_objdoc, where);
    }
    
    public void testCreateWhereClause_objdoc_multi() throws XWikiException
    {
        List list = new ArrayList();

        String where = DispatchSuperClass.getInstance(context).createWhereClause(WHERECLAUSE_PARAM_objdoc_multi, list);
        
        assertEquals(WHERECLAUSE_objdoc_multi, where);
    }
}
