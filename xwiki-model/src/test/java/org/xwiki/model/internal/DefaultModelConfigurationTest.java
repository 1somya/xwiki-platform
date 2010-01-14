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
package org.xwiki.model.internal;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelConfiguration;

/**
 * Unit tests for {@link DefaultModelConfiguration}.
 *
 * @version $Id$
 * @since 2.2M1
 */
public class DefaultModelConfigurationTest
{
    private Mockery mockery = new Mockery();

    private ModelConfiguration configuration;

    private ConfigurationSource mockSource;

    @Before
    public void setUp() throws Exception
    {
        this.mockSource = this.mockery.mock(ConfigurationSource.class);
        this.configuration = new DefaultModelConfiguration();

        final ComponentManager mockCM = this.mockery.mock(ComponentManager.class);
        ReflectionUtils.setFieldValue(this.configuration, "componentManager", mockCM);
        this.mockery.checking(new Expectations() {{
            allowing(mockCM).lookup(ConfigurationSource.class, "xwikiproperties"); will(returnValue(mockSource));
        }});
    }

    @Test
    public void testGetDefaultReferenceNameWhenDefinedInConfiguration()
    {
        this.mockery.checking(new Expectations() {{
            oneOf(mockSource).getProperty(with(equal("model.reference.default.wiki")), with(any(String.class)));
                will(returnValue("defaultWiki"));
            oneOf(mockSource).getProperty(with(equal("model.reference.default.document")), with(any(String.class)));
                will(returnValue("defaultDocument"));
            oneOf(mockSource).getProperty(with(equal("model.reference.default.space")), with(any(String.class)));
                will(returnValue("defaultSpace"));
            oneOf(mockSource).getProperty(with(equal("model.reference.default.attachment")), with(any(String.class)));
                will(returnValue("defaultFilename"));
        }});
        
        Assert.assertEquals("defaultWiki", this.configuration.getDefaultReferenceName(EntityType.WIKI));
        Assert.assertEquals("defaultDocument", this.configuration.getDefaultReferenceName(EntityType.DOCUMENT));
        Assert.assertEquals("defaultSpace", this.configuration.getDefaultReferenceName(EntityType.SPACE));
        Assert.assertEquals("defaultFilename", this.configuration.getDefaultReferenceName(EntityType.ATTACHMENT));
    }

    @Test
    public void testGetDefaultReferenceNameWhenNotDefinedInConfiguration()
    {
        this.mockery.checking(new Expectations() {{
            oneOf(mockSource).getProperty("model.reference.default.wiki", "xwiki"); will(returnValue("xwiki"));
            oneOf(mockSource).getProperty("model.reference.default.document", "WebHome"); will(returnValue("WebHome"));
            oneOf(mockSource).getProperty("model.reference.default.space", "Main"); will(returnValue("Main"));
            oneOf(mockSource).getProperty("model.reference.default.attachment", "filename");
                will(returnValue("filename"));
        }});

        Assert.assertEquals("xwiki", this.configuration.getDefaultReferenceName(EntityType.WIKI));
        Assert.assertEquals("WebHome", this.configuration.getDefaultReferenceName(EntityType.DOCUMENT));
        Assert.assertEquals("Main", this.configuration.getDefaultReferenceName(EntityType.SPACE));
        Assert.assertEquals("filename", this.configuration.getDefaultReferenceName(EntityType.ATTACHMENT));
    }
}
