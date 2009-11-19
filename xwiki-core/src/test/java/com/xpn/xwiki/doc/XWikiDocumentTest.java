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
package com.xpn.xwiki.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.jmock.Mock;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.DocumentSection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.XWikiVersioningStoreInterface;
import com.xpn.xwiki.test.AbstractBridgedXWikiComponentTestCase;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.XWikiMessageTool;

/**
 * Unit tests for {@link XWikiDocument}.
 * 
 * @version $Id$
 */
public class XWikiDocumentTest extends AbstractBridgedXWikiComponentTestCase
{
    private static final String DOCWIKI = "Wiki";

    private static final String DOCSPACE = "Space";

    private static final String DOCNAME = "Page";

    private static final String DOCFULLNAME = DOCSPACE + "." + DOCNAME;

    private static final String CLASSNAME = DOCFULLNAME;

    private XWikiDocument document;

    private XWikiDocument translatedDocument;

    private Mock mockXWiki;

    private Mock mockXWikiRenderingEngine;

    private Mock mockXWikiVersioningStore;

    private Mock mockXWikiStoreInterface;

    private Mock mockXWikiMessageTool;

    private Mock mockXWikiRightService;

    private BaseClass baseClass;

    private BaseObject baseObject;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.document = new XWikiDocument(DOCWIKI, DOCSPACE, DOCNAME);
        this.document.setSyntaxId("xwiki/1.0");
        this.document.setLanguage("en");
        this.document.setDefaultLanguage("en");
        this.document.setNew(false);

        this.translatedDocument = new XWikiDocument();
        this.translatedDocument.setSyntaxId("xwiki/1.0");
        this.translatedDocument.setLanguage("fr");
        this.translatedDocument.setNew(false);

        getContext().put("isInRenderingEngine", true);

        this.mockXWiki = mock(XWiki.class);
        this.mockXWiki.stubs().method("Param").will(returnValue(null));

        this.mockXWikiRenderingEngine = mock(XWikiRenderingEngine.class);

        this.mockXWikiVersioningStore = mock(XWikiVersioningStoreInterface.class);
        this.mockXWikiVersioningStore.stubs().method("getXWikiDocumentArchive").will(returnValue(null));

        this.mockXWikiStoreInterface = mock(XWikiStoreInterface.class);
        this.document.setStore((XWikiStoreInterface) this.mockXWikiStoreInterface.proxy());

        this.mockXWikiMessageTool =
            mock(XWikiMessageTool.class, new Class[] {ResourceBundle.class, XWikiContext.class}, new Object[] {null,
            getContext()});
        this.mockXWikiMessageTool.stubs().method("get").will(returnValue("message"));

        this.mockXWikiRightService = mock(XWikiRightService.class);
        this.mockXWikiRightService.stubs().method("hasProgrammingRights").will(returnValue(true));

        this.mockXWiki.stubs().method("getRenderingEngine").will(returnValue(this.mockXWikiRenderingEngine.proxy()));
        this.mockXWiki.stubs().method("getVersioningStore").will(returnValue(this.mockXWikiVersioningStore.proxy()));
        this.mockXWiki.stubs().method("getStore").will(returnValue(this.mockXWikiStoreInterface.proxy()));
        this.mockXWiki.stubs().method("getDocument").will(returnValue(this.document));
        this.mockXWiki.stubs().method("getLanguagePreference").will(returnValue("en"));
        this.mockXWiki.stubs().method("getSectionEditingDepth").will(returnValue(2L));
        this.mockXWiki.stubs().method("getRightService").will(returnValue(this.mockXWikiRightService.proxy()));

        getContext().setWiki((XWiki) this.mockXWiki.proxy());
        getContext().put("msg", this.mockXWikiMessageTool.proxy());

        this.baseClass = this.document.getxWikiClass();
        this.baseClass.addTextField("string", "String", 30);
        this.baseClass.addTextAreaField("area", "Area", 10, 10);
        this.baseClass.addTextAreaField("puretextarea", "Pure text area", 10, 10);
        // set the text areas an non interpreted content
        ((TextAreaClass) this.baseClass.getField("puretextarea")).setContentType("puretext");
        this.baseClass.addPasswordField("passwd", "Password", 30);
        this.baseClass.addBooleanField("boolean", "Boolean", "yesno");
        this.baseClass.addNumberField("int", "Int", 10, "integer");
        this.baseClass.addStaticListField("stringlist", "StringList", "value1, value2");

        this.mockXWiki.stubs().method("getClass").will(returnValue(this.baseClass));

        this.baseObject = this.document.newObject(CLASSNAME, getContext());
        this.baseObject.setStringValue("string", "string");
        this.baseObject.setLargeStringValue("area", "area");
        this.baseObject.setStringValue("passwd", "passwd");
        this.baseObject.setIntValue("boolean", 1);
        this.baseObject.setIntValue("int", 42);
        this.baseObject.setStringListValue("stringlist", Arrays.asList("VALUE1", "VALUE2"));

        this.mockXWikiStoreInterface.stubs().method("search").will(returnValue(new ArrayList<XWikiDocument>()));
    }

    public void testGetDisplayTitleWhenNoTitleAndNoContent()
    {
        this.document.setContent("Some content");

        assertEquals("Page", this.document.getDisplayTitle(getContext()));
    }

    public void testGetDisplayWhenTitleExists()
    {
        this.document.setContent("Some content");
        this.document.setTitle("Title");
        this.mockXWikiRenderingEngine.expects(once()).method("interpretText").with(eq("Title"), ANYTHING, ANYTHING).will(
            returnValue("Title"));

        assertEquals("Title", this.document.getDisplayTitle(getContext()));
    }

    public void testGetDisplayWhenNoTitleButSectionExists()
    {
        this.document.setContent("Some content\n1 Title");
        this.mockXWikiRenderingEngine.expects(once()).method("interpretText").with(eq("Title"), ANYTHING, ANYTHING).will(
            returnValue("Title"));

        assertEquals("Title", this.document.getDisplayTitle(getContext()));
    }

    public void testMinorMajorVersions()
    {
        // there is no version in doc yet, so 1.1
        assertEquals("1.1", this.document.getVersion());

        this.document.setMinorEdit(false);
        this.document.incrementVersion();
        // no version => incrementVersion sets 1.1
        assertEquals("1.1", this.document.getVersion());

        this.document.setMinorEdit(false);
        this.document.incrementVersion();
        // increment major version
        assertEquals("2.1", this.document.getVersion());

        this.document.setMinorEdit(true);
        this.document.incrementVersion();
        // increment minor version
        assertEquals("2.2", this.document.getVersion());
    }

    public void testAuthorAfterDocumentCopy() throws XWikiException
    {
        String author = "Albatross";
        this.document.setAuthor(author);
        XWikiDocument copy = this.document.copyDocument(this.document.getName() + " Copy", getContext());

        assertTrue(author.equals(copy.getAuthor()));
    }

    public void testCreatorAfterDocumentCopy() throws XWikiException
    {
        String creator = "Condor";
        this.document.setCreator(creator);
        XWikiDocument copy = this.document.copyDocument(this.document.getName() + " Copy", getContext());

        assertTrue(creator.equals(copy.getCreator()));
    }

    public void testCreationDateAfterDocumentCopy() throws XWikiException, InterruptedException
    {
        Date sourceCreationDate = this.document.getCreationDate();
        Thread.sleep(1000);
        XWikiDocument copy = this.document.copyDocument(this.document.getName() + " Copy", getContext());

        assertTrue(copy.getCreationDate().equals(sourceCreationDate));
    }

    public void testToStringReturnsFullName()
    {
        assertEquals("Space.Page", this.document.toString());
        assertEquals("Main.WebHome", new XWikiDocument().toString());
    }

    public void testCloneSaveVersions()
    {
        XWikiDocument doc1 = new XWikiDocument("qwe", "qwe");
        XWikiDocument doc2 = (XWikiDocument) doc1.clone();
        doc1.incrementVersion();
        doc2.incrementVersion();
        assertEquals(doc1.getVersion(), doc2.getVersion());
    }

    public void testAddObject() throws XWikiException
    {
        XWikiDocument doc = new XWikiDocument("test", "document");
        this.mockXWiki.stubs().method("getClass").will(returnValue(new BaseClass()));
        BaseObject object = BaseClass.newCustomClassInstance("XWiki.XWikiUsers", getContext());
        doc.addObject("XWiki.XWikiUsers", object);
        assertEquals("XWikiDocument.addObject does not set the object's name", doc.getFullName(), object.getName());
    }

    public void testObjectNumbersAfterXMLRoundrip() throws XWikiException
    {
        String classname = XWikiConstant.TAG_CLASS;
        BaseClass tagClass = new BaseClass();
        tagClass.setName(classname);
        tagClass.addStaticListField(XWikiConstant.TAG_CLASS_PROP_TAGS, "Tags", 30, true, "", "checkbox");

        XWikiDocument doc = new XWikiDocument("test", "document");
        this.mockXWiki.stubs().method("getClass").will(returnValue(tagClass));
        this.mockXWiki.stubs().method("getEncoding").will(returnValue("iso-8859-1"));

        BaseObject object = BaseClass.newCustomClassInstance(classname, getContext());
        doc.addObject(classname, object);

        object = BaseClass.newCustomClassInstance(classname, getContext());
        doc.addObject(classname, object);

        object = BaseClass.newCustomClassInstance(classname, getContext());
        doc.addObject(classname, object);

        doc.getObjects(classname).set(1, null);

        String docXML = doc.toXML(getContext());
        XWikiDocument docFromXML = new XWikiDocument();
        docFromXML.fromXML(docXML);

        Vector<BaseObject> objects = doc.getObjects(classname);
        Vector<BaseObject> objectsFromXML = docFromXML.getObjects(classname);

        assertNotNull(objects);
        assertNotNull(objectsFromXML);

        assertTrue(objects.size() == objectsFromXML.size());

        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i) == null) {
                assertNull(objectsFromXML.get(i));
            } else {
                assertTrue(objects.get(i).getNumber() == objectsFromXML.get(i).getNumber());
            }
        }
    }

    public void testGetUniqueLinkedPages10()
    {
        XWikiDocument contextDocument = new XWikiDocument("contextdocspace", "contextdocpage");
        getContext().setDoc(contextDocument);

        this.mockXWiki.stubs().method("exists").will(returnValue(true));

        this.document.setContent("[TargetPage][TargetLabel>TargetPage][TargetSpace.TargetPage]"
            + "[TargetLabel>TargetSpace.TargetPage?param=value#anchor][http://externallink][mailto:mailto][label>]");

        Set<String> linkedPages = this.document.getUniqueLinkedPages(getContext());

        assertEquals(new HashSet<String>(Arrays.asList("Space.TargetPage", "TargetSpace.TargetPage")),
            new HashSet<String>(linkedPages));
    }

    public void testGetUniqueLinkedPages()
    {
        XWikiDocument contextDocument = new XWikiDocument("contextdocspace", "contextdocpage");
        getContext().setDoc(contextDocument);

        this.document.setContent("[[TargetPage]][[TargetLabel>>TargetPage]][[TargetSpace.TargetPage]]"
            + "[[TargetLabel>>TargetSpace.TargetPage?param=value#anchor]][[http://externallink]][[mailto:mailto]]"
            + "[[]][[#anchor]][[?param=value]][[targetwiki:TargetSpace.TargetPage]]");
        this.document.setSyntaxId("xwiki/2.0");

        Set<String> linkedPages = this.document.getUniqueLinkedPages(getContext());

        assertEquals(new LinkedHashSet<String>(Arrays.asList("Space.TargetPage", "TargetSpace.TargetPage",
            "Space.WebHome", "targetwiki:TargetSpace.TargetPage")), linkedPages);
    }

    public void testGetSections10() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "1 header 1\nheader 1 content\n"
            + "1.1 header 2\nheader 2 content");

        List<DocumentSection> headers = this.document.getSections();

        assertEquals(2, headers.size());

        DocumentSection header1 = headers.get(0);
        DocumentSection header2 = headers.get(1);

        assertEquals("header 1", header1.getSectionTitle());
        assertEquals(23, header1.getSectionIndex());
        assertEquals(1, header1.getSectionNumber());
        assertEquals("1", header1.getSectionLevel());
        assertEquals("header 2", header2.getSectionTitle());
        assertEquals(51, header2.getSectionIndex());
        assertEquals(2, header2.getSectionNumber());
        assertEquals("1.1", header2.getSectionLevel());
    }

    public void testGetSections() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "= header 1=\nheader 1 content\n"
            + "== header 2==\nheader 2 content");
        this.document.setSyntaxId("xwiki/2.0");

        List<DocumentSection> headers = this.document.getSections();

        assertEquals(2, headers.size());

        DocumentSection header1 = headers.get(0);
        DocumentSection header2 = headers.get(1);

        assertEquals("header 1", header1.getSectionTitle());
        assertEquals(-1, header1.getSectionIndex());
        assertEquals(1, header1.getSectionNumber());
        assertEquals("1", header1.getSectionLevel());
        assertEquals("header 2", header2.getSectionTitle());
        assertEquals(-1, header2.getSectionIndex());
        assertEquals(2, header2.getSectionNumber());
        assertEquals("1.1", header2.getSectionLevel());
    }

    public void testGetDocumentSection10() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "1 header 1\nheader 1 content\n"
            + "1.1 header 2\nheader 2 content");

        DocumentSection header1 = this.document.getDocumentSection(1);
        DocumentSection header2 = this.document.getDocumentSection(2);

        assertEquals("header 1", header1.getSectionTitle());
        assertEquals(23, header1.getSectionIndex());
        assertEquals(1, header1.getSectionNumber());
        assertEquals("1", header1.getSectionLevel());
        assertEquals("header 2", header2.getSectionTitle());
        assertEquals(51, header2.getSectionIndex());
        assertEquals(2, header2.getSectionNumber());
        assertEquals("1.1", header2.getSectionLevel());
    }

    public void testGetDocumentSection() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "= header 1=\nheader 1 content\n"
            + "== header 2==\nheader 2 content");
        this.document.setSyntaxId("xwiki/2.0");

        DocumentSection header1 = this.document.getDocumentSection(1);
        DocumentSection header2 = this.document.getDocumentSection(2);

        assertEquals("header 1", header1.getSectionTitle());
        assertEquals(-1, header1.getSectionIndex());
        assertEquals(1, header1.getSectionNumber());
        assertEquals("1", header1.getSectionLevel());
        assertEquals("header 2", header2.getSectionTitle());
        assertEquals(-1, header2.getSectionIndex());
        assertEquals(2, header2.getSectionNumber());
        assertEquals("1.1", header2.getSectionLevel());
    }

    public void testGetContentOfSection10() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "1 header 1\nheader 1 content\n"
            + "1.1 header 2\nheader 2 content");

        String content1 = this.document.getContentOfSection(1);
        String content2 = this.document.getContentOfSection(2);

        assertEquals("1 header 1\nheader 1 content\n1.1 header 2\nheader 2 content", content1);
        assertEquals("1.1 header 2\nheader 2 content", content2);
    }

    public void testGetContentOfSection() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "= header 1=\nheader 1 content\n"
            + "== header 2==\nheader 2 content\n" + "=== header 3===\nheader 3 content\n"
            + "== header 4==\nheader 4 content");
        this.document.setSyntaxId("xwiki/2.0");

        String content1 = this.document.getContentOfSection(1);
        String content2 = this.document.getContentOfSection(2);
        String content3 = this.document.getContentOfSection(3);

        assertEquals("= header 1 =\n\nheader 1 content\n\n== header 2 ==\n\nheader 2 content\n\n"
            + "=== header 3 ===\n\nheader 3 content\n\n== header 4 ==\n\nheader 4 content", content1);
        assertEquals("== header 2 ==\n\nheader 2 content\n\n=== header 3 ===\n\nheader 3 content", content2);
        assertEquals("== header 4 ==\n\nheader 4 content", content3);

        // Validate that third level header is not skipped anymore
        this.mockXWiki.stubs().method("getSectionEditingDepth").will(returnValue(3L));

        content3 = this.document.getContentOfSection(3);
        String content4 = this.document.getContentOfSection(4);

        assertEquals("=== header 3 ===\n\nheader 3 content", content3);
        assertEquals("== header 4 ==\n\nheader 4 content", content4);
    }

    public void testSectionSplit10() throws XWikiException
    {
        List<DocumentSection> sections;
        // Simple test
        this.document.setContent("1 Section 1\n" + "Content of first section\n" + "1.1 Subsection 2\n"
            + "Content of second section\n" + "1 Section 3\n" + "Content of section 3");
        sections = this.document.getSections();
        assertEquals(3, sections.size());
        assertEquals("Section 1", sections.get(0).getSectionTitle());
        assertEquals("1 Section 1\n" + "Content of first section\n" + "1.1 Subsection 2\n"
            + "Content of second section\n", this.document.getContentOfSection(1));
        assertEquals("1.1", sections.get(1).getSectionLevel());
        assertEquals("1.1 Subsection 2\nContent of second section\n", this.document.getContentOfSection(2));
        assertEquals(3, sections.get(2).getSectionNumber());
        assertEquals(80, sections.get(2).getSectionIndex());
        assertEquals("1 Section 3\nContent of section 3", this.document.getContentOfSection(3));
        // Test comments don't break the section editing
        this.document.setContent("1 Section 1\n" + "Content of first section\n" + "## 1.1 Subsection 2\n"
            + "Content of second section\n" + "1 Section 3\n" + "Content of section 3");
        sections = this.document.getSections();
        assertEquals(2, sections.size());
        assertEquals("Section 1", sections.get(0).getSectionTitle());
        assertEquals("1", sections.get(1).getSectionLevel());
        assertEquals(2, sections.get(1).getSectionNumber());
        assertEquals(83, sections.get(1).getSectionIndex());
        // Test spaces are ignored
        this.document.setContent("1 Section 1\n" + "Content of first section\n" + "   1.1    Subsection 2  \n"
            + "Content of second section\n" + "1 Section 3\n" + "Content of section 3");
        sections = this.document.getSections();
        assertEquals(3, sections.size());
        assertEquals("Subsection 2  ", sections.get(1).getSectionTitle());
        assertEquals("1.1", sections.get(1).getSectionLevel());
        // Test lower headings are ignored
        this.document.setContent("1 Section 1\n" + "Content of first section\n" + "1.1.1 Lower subsection\n"
            + "This content is not important\n" + "   1.1    Subsection 2  \n" + "Content of second section\n"
            + "1 Section 3\n" + "Content of section 3");
        sections = this.document.getSections();
        assertEquals(3, sections.size());
        assertEquals("Section 1", sections.get(0).getSectionTitle());
        assertEquals("Subsection 2  ", sections.get(1).getSectionTitle());
        assertEquals("1.1", sections.get(1).getSectionLevel());
        // Test blank lines are preserved
        this.document.setContent("\n\n1 Section 1\n\n\n" + "Content of first section\n\n\n"
            + "   1.1    Subsection 2  \n\n" + "Content of second section\n" + "1 Section 3\n" + "Content of section 3");
        sections = this.document.getSections();
        assertEquals(3, sections.size());
        assertEquals(2, sections.get(0).getSectionIndex());
        assertEquals("Subsection 2  ", sections.get(1).getSectionTitle());
        assertEquals(43, sections.get(1).getSectionIndex());
    }

    public void testUpdateDocumentSection10() throws XWikiException
    {
        List<DocumentSection> sections;
        // Fill the document
        this.document.setContent("1 Section 1\n" + "Content of first section\n" + "1.1 Subsection 2\n"
            + "Content of second section\n" + "1 Section 3\n" + "Content of section 3");
        String content = this.document.updateDocumentSection(3, "1 Section 3\n" + "Modified content of section 3");
        assertEquals("1 Section 1\n" + "Content of first section\n" + "1.1 Subsection 2\n"
            + "Content of second section\n" + "1 Section 3\n" + "Modified content of section 3", content);
        this.document.setContent(content);
        sections = this.document.getSections();
        assertEquals(3, sections.size());
        assertEquals("Section 1", sections.get(0).getSectionTitle());
        assertEquals("1 Section 1\n" + "Content of first section\n" + "1.1 Subsection 2\n"
            + "Content of second section\n", this.document.getContentOfSection(1));
        assertEquals("1.1", sections.get(1).getSectionLevel());
        assertEquals("1.1 Subsection 2\nContent of second section\n", this.document.getContentOfSection(2));
        assertEquals(3, sections.get(2).getSectionNumber());
        assertEquals(80, sections.get(2).getSectionIndex());
        assertEquals("1 Section 3\nModified content of section 3", this.document.getContentOfSection(3));
    }

    public void testUpdateDocumentSection() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "= header 1=\nheader 1 content\n"
            + "== header 2==\nheader 2 content");
        this.document.setSyntaxId("xwiki/2.0");

        // Modify section content
        String content1 = this.document.updateDocumentSection(2, "== header 2==\nmodified header 2 content");

        assertEquals(
            "content not in section\n\n= header 1 =\n\nheader 1 content\n\n== header 2 ==\n\nmodified header 2 content",
            content1);

        String content2 =
            this.document.updateDocumentSection(1,
                "= header 1 =\n\nmodified also header 1 content\n\n== header 2 ==\n\nheader 2 content");

        assertEquals(
            "content not in section\n\n= header 1 =\n\nmodified also header 1 content\n\n== header 2 ==\n\nheader 2 content",
            content2);

        // Remove a section
        String content3 = this.document.updateDocumentSection(2, "");

        assertEquals("content not in section\n\n= header 1 =\n\nheader 1 content", content3);
    }

    public void testDisplay10()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/1.0"));

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "{pre}<input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/>{/pre}",
            this.document.display("string", "edit", getContext()));

        this.mockXWikiRenderingEngine.expects(once()).method("renderText").with(eq("area"), ANYTHING, ANYTHING).will(
            returnValue("area"));

        assertEquals("area", this.document.display("area", "view", getContext()));
    }

    public void testDisplay()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/2.0"));

        this.document.setSyntaxId("xwiki/2.0");

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "{{html clean=\"false\" wiki=\"false\"}}<pre><input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/></pre>{{/html}}",
            this.document.display("string", "edit", getContext()));

        assertEquals("{{html clean=\"false\" wiki=\"false\"}}<p>area</p>{{/html}}", this.document.display("area",
            "view", getContext()));
    }

    public void testDisplay1020()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/1.0"));

        XWikiDocument doc10 = new XWikiDocument();
        doc10.setSyntaxId("xwiki/1.0");
        getContext().setDoc(doc10);

        this.document.setSyntaxId("xwiki/2.0");

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "{pre}<input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/>{/pre}",
            this.document.display("string", "edit", getContext()));

        assertEquals("<p>area</p>", this.document.display("area", "view", getContext()));
    }

    public void testDisplay2010()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/2.0"));

        XWikiDocument doc10 = new XWikiDocument();
        doc10.setSyntaxId("xwiki/2.0");
        getContext().setDoc(doc10);

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "{{html clean=\"false\" wiki=\"false\"}}<pre><input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/></pre>{{/html}}",
            this.document.display("string", "edit", getContext()));

        this.mockXWikiRenderingEngine.expects(once()).method("renderText").with(eq("area"), ANYTHING, ANYTHING).will(
            returnValue("area"));

        assertEquals("area", this.document.display("area", "view", getContext()));
    }

    public void testDisplayTemplate10()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/1.0"));

        getContext().put("isInRenderingEngine", false);

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "{pre}<input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/>{/pre}",
            this.document.display("string", "edit", getContext()));

        this.mockXWikiRenderingEngine.expects(once()).method("renderText").with(eq("area"), ANYTHING, ANYTHING).will(
            returnValue("area"));

        assertEquals("area", this.document.display("area", "view", getContext()));
    }

    public void testDisplayTemplate20()
    {
        this.mockXWiki.stubs().method("getCurrentContentSyntaxId").will(returnValue("xwiki/2.0"));

        getContext().put("isInRenderingEngine", false);

        this.document.setSyntaxId("xwiki/2.0");

        assertEquals("string", this.document.display("string", "view", getContext()));
        assertEquals(
            "<pre><input size='30' id='Space.Page_0_string' value='string' name='Space.Page_0_string' type='text'/></pre>",
            this.document.display("string", "edit", getContext()));

        assertEquals("<p>area</p>", this.document.display("area", "view", getContext()));
    }

    public void testConvertSyntax() throws XWikiException
    {
        this.document.setContent("content not in section\n" + "1 header 1\nheader 1 content\n"
            + "1.1 header 2\nheader 2 content");
        this.baseObject.setLargeStringValue("area", "object content not in section\n"
            + "1 object header 1\nobject header 1 content\n" + "1.1 object header 2\nobject header 2 content");
        this.baseObject.setLargeStringValue("puretextarea", "object content not in section\n"
            + "1 object header 1\nobject header 1 content\n" + "1.1 object header 2\nobject header 2 content");

        this.document.convertSyntax("xwiki/2.0", getContext());

        assertEquals("content not in section\n\n" + "= header 1 =\n\nheader 1 content\n\n"
            + "== header 2 ==\n\nheader 2 content", this.document.getContent());
        assertEquals("object content not in section\n\n" + "= object header 1 =\n\nobject header 1 content\n\n"
            + "== object header 2 ==\n\nobject header 2 content", this.baseObject.getStringValue("area"));
        assertEquals("object content not in section\n" + "1 object header 1\nobject header 1 content\n"
            + "1.1 object header 2\nobject header 2 content", this.baseObject.getStringValue("puretextarea"));
        assertEquals("xwiki/2.0", this.document.getSyntaxId());
    }

    public void testGetRenderedContent10() throws XWikiException
    {
        this.document.setContent("*bold*");
        this.document.setSyntaxId("xwiki/1.0");

        this.mockXWikiRenderingEngine.expects(once()).method("renderDocument").will(returnValue("<b>bold</b>"));

        assertEquals("<b>bold</b>", this.document.getRenderedContent(getContext()));

        this.translatedDocument = new XWikiDocument();
        this.translatedDocument.setContent("~italic~");
        this.translatedDocument.setSyntaxId("xwiki/2.0");
        this.translatedDocument.setNew(false);

        this.mockXWiki.stubs().method("getLanguagePreference").will(returnValue("fr"));
        this.mockXWikiStoreInterface.stubs().method("loadXWikiDoc").will(returnValue(this.translatedDocument));
        this.mockXWikiRenderingEngine.expects(once()).method("renderDocument").will(returnValue("<i>italic</i>"));

        assertEquals("<i>italic</i>", this.document.getRenderedContent(getContext()));
    }

    public void testGetRenderedContent() throws XWikiException
    {
        this.document.setContent("**bold**");
        this.document.setSyntaxId("xwiki/2.0");

        assertEquals("<p><strong>bold</strong></p>", this.document.getRenderedContent(getContext()));

        this.translatedDocument = new XWikiDocument();
        this.translatedDocument.setContent("//italic//");
        this.translatedDocument.setSyntaxId("xwiki/1.0");
        this.translatedDocument.setNew(false);

        this.mockXWiki.stubs().method("getLanguagePreference").will(returnValue("fr"));
        this.mockXWikiStoreInterface.stubs().method("loadXWikiDoc").will(returnValue(this.translatedDocument));

        assertEquals("<p><em>italic</em></p>", this.document.getRenderedContent(getContext()));
    }

    public void testGetRenderedContentWithSourceSyntax() throws XWikiException
    {
        this.document.setSyntaxId("xwiki/1.0");

        assertEquals("<p><strong>bold</strong></p>", this.document.getRenderedContent("**bold**", "xwiki/2.0",
            getContext()));
    }

    public void testRename() throws XWikiException
    {
        // Possible ways to write parents, include documents, or make links:
        // "name"  -----means-----> DOCWIKI+":"+DOCSPACE+"."+input
        // "space.name" -means----> DOCWIKI+":"+input
        // "database:name" -means-> input.replace(":",":"+DOCSPACE+".") (Not likely to happen much, but it works so it must be supported throughout)
        // "database:space.name" (no change)
        XWikiDocument doc1 = new XWikiDocument(DOCWIKI, DOCSPACE, "Page1");
        doc1.setContent("[[" + DOCWIKI + ":" + DOCSPACE + "." + DOCNAME + "]] [[someName>>" 
                             + DOCSPACE + "." + DOCNAME + "]] [["
                             + DOCNAME + "]] [["
                             + DOCWIKI + ":" + DOCNAME + "]]");
        doc1.setSyntaxId("xwiki/2.0");
        XWikiDocument doc2 = new XWikiDocument("newwikiname", DOCSPACE, "Page2");
        doc2.setContent("[[" + DOCWIKI + ":" + DOCSPACE + "." + DOCNAME + "]] [["
                             + DOCWIKI + ":" + DOCNAME + "]]");
        doc2.setSyntaxId("xwiki/2.0");
        XWikiDocument doc3 = new XWikiDocument("newwikiname", "newspace", "Page3");
        doc3.setContent("[[" + DOCWIKI + ":" + DOCSPACE + "." + DOCNAME + "]]");
        doc3.setSyntaxId("xwiki/2.0");

        // Test to make sure it also drags children along.
        XWikiDocument doc4 = new XWikiDocument(DOCWIKI, DOCSPACE, "Page4");
        doc4.setParent(DOCSPACE + "." + DOCNAME);
        XWikiDocument doc5 = new XWikiDocument("newwikiname", DOCSPACE, "Page5");
        doc5.setParent(DOCWIKI + ":" + DOCNAME);
        XWikiDocument doc6 = new XWikiDocument("newwikiname", "newspace", "Page6");
        doc6.setParent(DOCWIKI + ":" + DOCSPACE + "." + DOCNAME);

        this.mockXWiki.stubs().method("copyDocument").will(returnValue(true));
        this.mockXWiki.stubs().method("getDocument").with(eq("1"), ANYTHING).will(returnValue(doc1));
        this.mockXWiki.stubs().method("getDocument").with(eq("2"), ANYTHING).will(returnValue(doc2));
        this.mockXWiki.stubs().method("getDocument").with(eq("3"), ANYTHING).will(returnValue(doc3));
        this.mockXWiki.stubs().method("getDocument").with(eq("4"), ANYTHING).will(returnValue(doc4));
        this.mockXWiki.stubs().method("getDocument").with(eq("5"), ANYTHING).will(returnValue(doc5));
        this.mockXWiki.stubs().method("getDocument").with(eq("6"), ANYTHING).will(returnValue(doc6));
        this.mockXWiki.stubs().method("saveDocument").isVoid();
        this.mockXWiki.stubs().method("deleteDocument").isVoid();

        this.document.rename("newwikiname:newspace.newpage", Arrays.asList("1", "2", "3"), 
            Arrays.asList("4", "5", "6"), getContext());

        // Test links
        assertEquals("[[newwikiname:newspace.newpage]] "
                   + "[[someName>>newwikiname:newspace.newpage]] "
                   + "[[newwikiname:newspace.newpage]] "
                   + "[[newwikiname:newspace.newpage]]", doc1.getContent());
        assertEquals("[[newspace.newpage]] "
                   + "[[newspace.newpage]]", doc2.getContent());
        assertEquals("[[newspace.newpage]]", doc3.getContent());

        // Test parents
        assertEquals("newwikiname:newspace.newpage", doc4.getParent());
        assertEquals("newwikiname:newspace.newpage", doc5.getParent());
        assertEquals("newwikiname:newspace.newpage", doc6.getParent());
    }

    /**
     * Normally the xobject vector has the Nth object on the Nth position, but in case an object gets misplaced, trying
     * to remove it should indeed remove that object, and no other.
     */
    public void testRemovingObjectWithWrongObjectVector()
    {
        // Setup: Create a document and two xobjects
        XWikiDocument doc = new XWikiDocument();
        BaseObject o1 = new BaseObject(), o2 = new BaseObject();
        o1.setClassName(CLASSNAME);
        o2.setClassName(CLASSNAME);

        // First test: put the second xobject on the third position
        // addObject creates the object vector and configures the objects
        doc.addObject(CLASSNAME, o1);
        doc.addObject(CLASSNAME, o2);
        // Mess up the object vector
        Vector<BaseObject> objects = doc.getObjects(CLASSNAME);
        objects.setSize(3);
        objects.set(1, null);
        objects.set(2, o2);
        // Call the tested method
        doc.removeObject(o2);
        // Check the correct behavior:
        assertTrue(objects.contains(o1));
        assertFalse(objects.contains(o2));
        assertNull(objects.get(1));
        assertNull(objects.get(2));

        // Second test: swap the two objects, so that the first object is in the position the second should have
        // Start over, re-adding the two objects
        doc = new XWikiDocument();
        doc.addObject(CLASSNAME, o1);
        doc.addObject(CLASSNAME, o2);
        // Swap the two objects
        objects = doc.getObjects(CLASSNAME);
        objects.set(0, o2);
        objects.set(1, o1);
        // Call the tested method
        doc.removeObject(o2);
        // Check the correct behavior
        assertTrue(objects.contains(o1));
        assertFalse(objects.contains(o2));
    }

    public void testCopyDocument() throws XWikiException
    {
        XWikiDocument doc = new XWikiDocument();
        BaseObject o = new BaseObject();
        o.setClassName(CLASSNAME);
        doc.addObject(CLASSNAME, o);

        XWikiDocument newDoc = doc.copyDocument("newdoc", getContext());
        BaseObject newO = newDoc.getObject(CLASSNAME);

        assertNotSame(o, newDoc.getObject(CLASSNAME));
        assertFalse(newO.getGuid().equals(o.getGuid()));
    }
}
