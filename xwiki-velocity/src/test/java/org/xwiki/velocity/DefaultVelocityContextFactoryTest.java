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
package org.xwiki.velocity;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;

import com.xpn.xwiki.test.AbstractXWikiComponentTestCase;

/**
 * Unit tests for {@link DefaultVelocityContextFactory}.
 */
public class DefaultVelocityContextFactoryTest extends AbstractXWikiComponentTestCase
{
    private VelocityContextFactory factory;

    protected void setUp() throws Exception
    {
        this.factory = (VelocityContextFactory) getComponentManager().lookup(VelocityContextFactory.ROLE);
    }

    /**
     * Verify that we get different contexts when we call the createContext method but that
     * they contain the same references to the Velocity tools. Also tests that objects we
     * put in one context are not shared with other contexts.
     */
    public void testCreateDifferentContext() throws Exception
    {
        VelocityContext context1 = this.factory.createContext();
        context1.put("param", "value");

        VelocityContext context2= this.factory.createContext();
        assertNotSame(context1, context2);
        assertSame(context2.get("listtool"), context1.get("listtool"));
        assertNull(context2.get("param"));
    }

    public void testDefaultToolsPresent() throws Exception
    {
        // Verify for example that the List tool is present and working.
        VelocityContext context = this.factory.createContext();
        
        assertEquals("org.apache.velocity.tools.generic.ListTool", 
            context.get("listtool").getClass().getName());
        VelocityEngine manager = 
            (VelocityEngine) getComponentManager().lookup(VelocityEngine.ROLE);
        manager.initialize(new Properties());
        StringWriter writer = new StringWriter();
        manager.evaluate(context, writer, "mytemplate",
            "#set($list=[1, 2, 3])$listtool.get($list, 2)");
        assertEquals("3", writer.toString());
    }
   
}
