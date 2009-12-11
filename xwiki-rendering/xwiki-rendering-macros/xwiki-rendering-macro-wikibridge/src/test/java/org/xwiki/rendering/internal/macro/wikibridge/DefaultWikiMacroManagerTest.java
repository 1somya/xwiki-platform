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
package org.xwiki.rendering.internal.macro.wikibridge;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.rendering.internal.macro.wikibridge.DefaultWikiMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.wikibridge.WikiMacroDescriptor;
import org.xwiki.rendering.macro.wikibridge.WikiMacroException;
import org.xwiki.rendering.macro.wikibridge.WikiMacroManager;
import org.xwiki.rendering.macro.wikibridge.WikiMacroParameterDescriptor;
import org.xwiki.rendering.macro.wikibridge.WikiMacroVisibility;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.macro.wikibridge.DefaultWikiMacroManager}.
 * 
 * @version $Id: DefaultWikiMacroManagerTest.java 25429 2009-12-03 15:04:48Z vmassol $
 * @since 2.0M2
 */
public class DefaultWikiMacroManagerTest extends AbstractComponentTestCase
{
    /**
     * The {@link org.xwiki.rendering.macro.wikibridge.WikiMacroManager} component.
     */
    private WikiMacroManager wikiMacroManager;

    /**
     * The {@link MacroManager} component.
     */
    private MacroManager macroManager;

    private Mockery mockery = new Mockery();

    private DocumentAccessBridge mockDocumentAccessBridge;

    @Override protected void registerComponents() throws Exception
    {
        super.registerComponents();

        // Document Access Bridge Mock
        this.mockDocumentAccessBridge = mockery.mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptorDAB =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptorDAB.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptorDAB, this.mockDocumentAccessBridge);

        this.macroManager = getComponentManager().lookup(MacroManager.class);
        this.wikiMacroManager = getComponentManager().lookup(WikiMacroManager.class);
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenGlobalVisibilityAndAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.GLOBAL);

        // Simulate a user with programming rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).getCurrentDocumentName(); will(returnValue(wikiMacro.getDocumentName()));
            allowing(mockDocumentAccessBridge).getCurrentUser(); will(returnValue("dummy"));
            allowing(mockDocumentAccessBridge).hasProgrammingRights(); will(returnValue(true));
        }});

        Assert.assertFalse(wikiMacroManager.hasWikiMacro(wikiMacro.getDocumentName()));

        wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
        Assert.assertTrue(wikiMacroManager.hasWikiMacro(wikiMacro.getDocumentName()));

        Macro< ? > registeredMacro = macroManager.getMacro(new MacroId("testwikimacro"));
        Assert.assertEquals(0, registeredMacro.compareTo(wikiMacro));

        wikiMacroManager.unregisterWikiMacro(wikiMacro.getDocumentName());
        Assert.assertFalse(wikiMacroManager.hasWikiMacro(wikiMacro.getDocumentName()));

        Assert.assertFalse(macroManager.exists(new MacroId("testwikimacro")));
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenWikiVisibilityAndAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.WIKI);

        // Simulate a user with programming rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).getCurrentDocumentName(); will(returnValue(wikiMacro.getDocumentName()));
            allowing(mockDocumentAccessBridge).getCurrentUser(); will(returnValue("dummy"));
            allowing(mockDocumentAccessBridge).isDocumentEditable(wikiMacro.getDocumentName()); will(returnValue(true));
        }});

        wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
        Assert.assertTrue(wikiMacroManager.hasWikiMacro(wikiMacro.getDocumentName()));

        Macro< ? > registeredMacro = macroManager.getMacro(new MacroId("testwikimacro"));
        Assert.assertEquals(0, registeredMacro.compareTo(wikiMacro));
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenUserVisibilityAndAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.USER);

        // Simulate a user with programming rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).getCurrentDocumentName(); will(returnValue(wikiMacro.getDocumentName()));
            allowing(mockDocumentAccessBridge).getCurrentUser(); will(returnValue("dummy"));
            allowing(mockDocumentAccessBridge).isDocumentEditable(wikiMacro.getDocumentName()); will(returnValue(true));
        }});

        wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
        Assert.assertTrue(wikiMacroManager.hasWikiMacro(wikiMacro.getDocumentName()));

        Macro< ? > registeredMacro = macroManager.getMacro(new MacroId("testwikimacro"));
        Assert.assertEquals(0, registeredMacro.compareTo(wikiMacro));
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenGlobalVisibilityAndNotAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.GLOBAL);

        // Simulate a user without programming rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).hasProgrammingRights(); will(returnValue(false));
        }});

        try {
            wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
            Assert.fail("Should have raised an exception here");
        } catch (WikiMacroException e) {
            Assert.assertEquals("Unable to register macro [testwikimacro] in [wiki = [xwiki], space = [Main], "
                + "page = [TestWikiMacro]] for visibility [GLOBAL] due to insufficient privileges", e.getMessage());
        }
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenWikiVisibilityAndNotAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.WIKI);

        // Simulate a user without edit rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).isDocumentEditable(wikiMacro.getDocumentName());
                will(returnValue(false));
        }});

        try {
            wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
            Assert.fail("Should have raised an exception here");
        } catch (WikiMacroException e) {
            Assert.assertEquals("Unable to register macro [testwikimacro] in [wiki = [xwiki], space = [Main], "
                + "page = [TestWikiMacro]] for visibility [WIKI] due to insufficient privileges", e.getMessage());
        }
    }

    @org.junit.Test
    public void testRegisterWikiMacroWhenUserVisibilityAndNotAllowed() throws Exception
    {
        final DefaultWikiMacro wikiMacro = generateWikiMacro(WikiMacroVisibility.USER);

        // Simulate a user without edit rights
        mockery.checking(new Expectations() {{
            allowing(mockDocumentAccessBridge).isDocumentEditable(wikiMacro.getDocumentName());
                will(returnValue(false));
        }});

        try {
            wikiMacroManager.registerWikiMacro(wikiMacro.getDocumentName(), wikiMacro);
            Assert.fail("Should have raised an exception here");
        } catch (WikiMacroException e) {
            Assert.assertEquals("Unable to register macro [testwikimacro] in [wiki = [xwiki], space = [Main], "
                + "page = [TestWikiMacro]] for visibility [USER] due to insufficient privileges", e.getMessage());
        }
    }

    private DefaultWikiMacro generateWikiMacro(WikiMacroVisibility visibility) throws Exception
    {
        DocumentName wikiMacroDocName = new DocumentName("xwiki", "Main", "TestWikiMacro");

        WikiMacroDescriptor descriptor = new WikiMacroDescriptor("Test Wiki Macro", "Description", "Test",
            visibility, new DefaultContentDescriptor(), new ArrayList<WikiMacroParameterDescriptor>());
        DefaultWikiMacro wikiMacro = new DefaultWikiMacro(wikiMacroDocName, "testwikimacro", true, descriptor,
            "== Test ==", "xwiki/2.0", getComponentManager());

        return wikiMacro;
    }
}
