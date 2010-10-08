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
package org.xwiki.rendering;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.internal.configuration.DefaultRenderingConfiguration;
import org.xwiki.rendering.scaffolding.MockWikiModel;
import org.xwiki.rendering.scaffolding.RenderingTestSuite;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.ComponentManagerTestSetup;

/**
 * Rendering tests requiring a {@link WikiModel} implementation (ie tests that must have the notion of a wiki to run
 * fine).
 * 
 * @version $Id$
 * @since 2.0M1
 */
public class WikiRenderingTests extends TestCase
{
    public static junit.framework.Test suite() throws Exception
    {
        RenderingTestSuite suite = new RenderingTestSuite("Rendering tests requiring the wiki notion");

        // Links
        suite.addTestsFromResource("link/links1", false);
        suite.addTestsFromResource("link/links2", false);
        suite.addTestsFromResource("link/links3", false);
        suite.addTestsFromResource("link/links4", false);
        suite.addTestsFromResource("link/links5", false);
        suite.addTestsFromResource("link/links6", false);
        suite.addTestsFromResource("link/links7", false);
        suite.addTestsFromResource("link/links8", false);
        suite.addTestsFromResource("link/links9", false);
        suite.addTestsFromResource("link/links10", false);
        suite.addTestsFromResource("link/links11", false);
        suite.addTestsFromResource("link/links12", false);
        suite.addTestsFromResource("link/links13", false);
        suite.addTestsFromResource("link/links14", false);
        suite.addTestsFromResource("link/links15", false);
        suite.addTestsFromResource("link/links16", false);
        suite.addTestsFromResource("link/links17", false);
        suite.addTestsFromResource("link/links18", false);
        suite.addTestsFromResource("link/links19", false);
        suite.addTestsFromResource("link/links20", false);
        suite.addTestsFromResource("link/links21", false);
        suite.addTestsFromResource("link/links22", false);
        suite.addTestsFromResource("link/links23", false);
        suite.addTestsFromResource("link/links25", false);
        suite.addTestsFromResource("link/links26", false);
        suite.addTestsFromResource("link/links27", false);
        suite.addTestsFromResource("link/links28", false);
        suite.addTestsFromResource("link/links29", false);

        // Images
        suite.addTestsFromResource("image/image1", false);
        suite.addTestsFromResource("image/image2", false);
        suite.addTestsFromResource("image/image3", false);
        suite.addTestsFromResource("image/image4", false);
        suite.addTestsFromResource("image/image5", false);
        suite.addTestsFromResource("image/image6", false);
        suite.addTestsFromResource("image/image7", false);

        ComponentManagerTestSetup testSetup = new ComponentManagerTestSetup(suite);
        testSetup.addComponentDescriptor(MockWikiModel.getComponentDescriptor());

        // Add InterWiki Definition for links28 test
        DefaultRenderingConfiguration renderingConfiguration = 
            (DefaultRenderingConfiguration) testSetup.getComponentManager().lookup(RenderingConfiguration.class);
        renderingConfiguration.addInterWikiDefinition("knownalias", "http://server/common/url/");

        return testSetup;
    }
}
