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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link com.xpn.xwiki.doc.CurrentEntityReferenceFactory}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
public class CompactEntityReferenceSerializerTest extends AbstractComponentTestCase
{
    private EntityReferenceSerializer serializer;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.serializer = getComponentManager().lookup(EntityReferenceSerializer.class, "compact");
    }

    @org.junit.Test
    public void testSerializeWhenNoContext() throws Exception
    {
        DocumentReference reference = new DocumentReference("wiki", "space", "page");
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));
    }

    @org.junit.Test
    public void testSerializeWhenNoContextDocument() throws Exception
    {
        DocumentReference reference = new DocumentReference("wiki", "space", "page");
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));
    }

    @org.junit.Test
    public void testSerializeDocumentReferenceWhenContextDocument() throws Exception
    {
        DocumentReference reference = new DocumentReference("wiki", "space", "page");
        XWikiContext xcontext = setUpXWikiContext();

        xcontext.setDoc(new XWikiDocument("wiki", "space", "page"));
        Assert.assertEquals("page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("wiki", "space", "otherpage"));
        Assert.assertEquals("page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("wiki", "otherspace", "otherpage"));
        Assert.assertEquals("space.page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("otherwiki", "otherspace", "otherpage"));
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("wiki", "otherspace", "page"));
        Assert.assertEquals("space.page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("otherwiki", "otherspace", "page"));
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("otherwiki", "space", "page"));
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("otherwiki", "space", "otherpage"));
        Assert.assertEquals("wiki:space.page", this.serializer.serialize(reference));
    }

    @org.junit.Test
    public void testSerializeAttachmentReferenceWhenContextDocument() throws Exception
    {
        AttachmentReference reference = new AttachmentReference("filename",
            new DocumentReference("wiki", "space", "page"));
        XWikiContext xcontext = setUpXWikiContext();

        xcontext.setDoc(new XWikiDocument("wiki", "space", "page"));
        Assert.assertEquals("filename", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("wiki", "space", "otherpage"));
        Assert.assertEquals("page@filename", this.serializer.serialize(reference));

        xcontext.setDoc(new XWikiDocument("otherwiki", "space", "page"));
        Assert.assertEquals("wiki:space.page@filename", this.serializer.serialize(reference));
    }

    private XWikiContext setUpXWikiContext() throws Exception
    {
        XWikiContext xcontext = new XWikiContext();

        Execution execution = getComponentManager().lookup(Execution.class);
        execution.getContext().setProperty("xwikicontext", xcontext);
        Utils.setComponentManager(getComponentManager());

        return xcontext;
    }
}
