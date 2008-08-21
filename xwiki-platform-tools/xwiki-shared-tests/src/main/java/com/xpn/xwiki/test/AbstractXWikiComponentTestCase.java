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
 *
 */
package com.xpn.xwiki.test;

import org.jmock.cglib.MockObjectTestCase;
import org.xwiki.component.manager.ComponentManager;

/**
 * Tests which needs to have XWiki Components set up should extend this class which makes the Component Manager
 * available.
 */
public abstract class AbstractXWikiComponentTestCase extends MockObjectTestCase
{
    private XWikiComponentInitializer initializer = new XWikiComponentInitializer();

    public AbstractXWikiComponentTestCase()
    {
        super();
    }

    public AbstractXWikiComponentTestCase(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        this.initializer.initialize();
    }

    protected void tearDown() throws Exception
    {
        this.initializer.shutdown();
    }
    
    /**
     * @return a configured Component Manager (which uses the plexus.xml file in the test resources directory) 
     *         which can then be put in the XWiki Context for testing.
     */
    public ComponentManager getComponentManager() throws Exception
    {
        return this.initializer.getComponentManager();
    }
}
