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
package com.xpn.xwiki.internal.model.reference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import org.junit.*;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link com.xpn.xwiki.internal.model.reference.CurrentStringEntityReferenceResolver}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
public class CurrentEntityReferenceResolverTest extends AbstractComponentTestCase
{
    private static final String CURRENT_WIKI = "currentwiki";

    private static final String CURRENT_SPACE = "currentspace";

    private static final String CURRENT_PAGE = "currentpage";

    private EntityReferenceResolver<String> resolver;

    private XWikiContext context;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.context = new XWikiContext();

        Execution execution = getComponentManager().lookup(Execution.class);
        execution.getContext().setProperty("xwikicontext", this.context);
        Utils.setComponentManager(getComponentManager());
        
        this.resolver = getComponentManager().lookup(EntityReferenceResolver.class, "current");
    }

    @org.junit.Test
    public void testResolveDocumentReferenceWhenNoContextDocument() throws Exception
    {
        EntityReference reference = resolver.resolve("", EntityType.DOCUMENT);
        Assert.assertEquals("xwiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("Main", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("WebHome", reference.getName());
    }

    @org.junit.Test
    public void testResolveDocumentReferenceWhenContextDocument() throws Exception
    {
        this.context.setDoc(new XWikiDocument(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE)));

        EntityReference reference = resolver.resolve("", EntityType.DOCUMENT);
        Assert.assertEquals(CURRENT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(CURRENT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(CURRENT_PAGE, reference.getName());
    }

    @org.junit.Test
    public void testResolveAttachmentReference() throws Exception
    {
        this.context.setDoc(new XWikiDocument(new DocumentReference(CURRENT_WIKI, CURRENT_SPACE, CURRENT_PAGE)));

        EntityReference reference = resolver.resolve("", EntityType.ATTACHMENT);
        Assert.assertEquals(CURRENT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(CURRENT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(CURRENT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("filename", reference.getName());
    }
}
