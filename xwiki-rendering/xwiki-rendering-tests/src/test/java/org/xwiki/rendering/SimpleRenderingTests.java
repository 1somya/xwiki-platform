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

import junit.framework.Test;
import junit.framework.TestCase;

import org.xwiki.rendering.scaffolding.RenderingTestSuite;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.ComponentManagerTestSetup;

/**
 * Rendering tests not requiring a {@link WikiModel} implementation (ie tests that don't need the notion
 * of Wiki to run).
 * 
 * @version $Id$
 * @since 2.0M1
 */
public class SimpleRenderingTests extends TestCase
{
    public static Test suite() throws Exception
    {
        RenderingTestSuite suite = new RenderingTestSuite("Rendering tests not requiring the wiki notion");

        // Embedded documents
        suite.addTestsFromResource("group/group1", false);
        suite.addTestsFromResource("group/group2", false);
        suite.addTestsFromResource("group/group3", false);
        suite.addTestsFromResource("group/group4", false);
        suite.addTestsFromResource("group/group5", false);
        suite.addTestsFromResource("group/group6", false);
        suite.addTestsFromResource("group/group7", false);
        suite.addTestsFromResource("group/group8", false);
        suite.addTestsFromResource("group/group9", false);
        suite.addTestsFromResource("group/group10", false);
        suite.addTestsFromResource("group/group11", false);

        // Text formatting
        suite.addTestsFromResource("bold/bold1", false);
        suite.addTestsFromResource("bold/bold2", false);
        suite.addTestsFromResource("bold/bold3", false);
        suite.addTestsFromResource("bold/bold4", false);
        suite.addTestsFromResource("bold/bold5", false);
        suite.addTestsFromResource("bold/bold6", false);
        suite.addTestsFromResource("bold/bold7", false);
        suite.addTestsFromResource("bold/bold8", false);
        suite.addTestsFromResource("bold/bold9", false);
        suite.addTestsFromResource("italic/italic1", false);
        suite.addTestsFromResource("italic/italic2", false);
        suite.addTestsFromResource("italic/italic3", false);
        suite.addTestsFromResource("italic/italic4", false);
        suite.addTestsFromResource("italic/italic5", false);
        suite.addTestsFromResource("italic/italic6", false);
        suite.addTestsFromResource("italic/italic7", false);
        suite.addTestsFromResource("italic/italic8", false);
        suite.addTestsFromResource("underline/underline1", false);
        suite.addTestsFromResource("underline/underline2", false);
        suite.addTestsFromResource("strikedout/strikedout1", false);
        suite.addTestsFromResource("strikedout/strikedout2", false);
        suite.addTestsFromResource("strikedout/strikedout3", false);
        suite.addTestsFromResource("strikedout/strikedout4", false);
        suite.addTestsFromResource("superscript/superscript1", false);
        suite.addTestsFromResource("superscript/superscript2", false);
        suite.addTestsFromResource("subscript/subscript1", false);
        suite.addTestsFromResource("subscript/subscript2", false);
        suite.addTestsFromResource("monospace/monospace1", false);
        suite.addTestsFromResource("monospace/monospace2", false);
        suite.addTestsFromResource("verbatim/verbatim1", false);
        suite.addTestsFromResource("verbatim/verbatim2", false);
        suite.addTestsFromResource("verbatim/verbatim3", false);
        suite.addTestsFromResource("verbatim/verbatim4", false);
        suite.addTestsFromResource("verbatim/verbatim5", false);
        suite.addTestsFromResource("verbatim/verbatim6", false);
        suite.addTestsFromResource("verbatim/verbatim7", false);
        suite.addTestsFromResource("verbatim/verbatim8", false);

        // Paragraphs
        suite.addTestsFromResource("paragraph/paragraph1", false);
        suite.addTestsFromResource("paragraph/paragraph2", false);
        suite.addTestsFromResource("paragraph/paragraph3", false);
        suite.addTestsFromResource("paragraph/paragraph4", false);
        suite.addTestsFromResource("paragraph/paragraph5", false);
        suite.addTestsFromResource("paragraph/paragraph6", false);
        suite.addTestsFromResource("paragraph/paragraph7", false);
        suite.addTestsFromResource("paragraph/paragraph8", false);
        suite.addTestsFromResource("paragraph/paragraph9", false);
        suite.addTestsFromResource("paragraph/paragraph10", false);

        // Tables
        suite.addTestsFromResource("table/table1", false);
        suite.addTestsFromResource("table/table2", false);
        suite.addTestsFromResource("table/table3", false);
        suite.addTestsFromResource("table/table4", false);
        suite.addTestsFromResource("table/table5", false);
        suite.addTestsFromResource("table/table6", false);
        suite.addTestsFromResource("table/table7", false);

        // Macros
        suite.addTestsFromResource("macros/macro1", false);
        suite.addTestsFromResource("macros/macro2", false);
        suite.addTestsFromResource("macros/macro3", false);
        suite.addTestsFromResource("macros/macro4", false);
        suite.addTestsFromResource("macros/macro5", false);
        suite.addTestsFromResource("macros/macro6", true);
        suite.addTestsFromResource("macros/macro7", true);
        suite.addTestsFromResource("macros/macro8", true);
        suite.addTestsFromResource("macros/macro9", true);
        suite.addTestsFromResource("macros/macro10", true);
        suite.addTestsFromResource("macros/macro11", true);
        suite.addTestsFromResource("macros/macro12", true);
        suite.addTestsFromResource("macros/macro13", true);
        suite.addTestsFromResource("macros/macro14", true);
        suite.addTestsFromResource("macros/macro15", false);

        // Lists
        suite.addTestsFromResource("list/list1", false);
        suite.addTestsFromResource("list/list2", false);
        suite.addTestsFromResource("list/list3", false);
        suite.addTestsFromResource("list/list4", false);
        suite.addTestsFromResource("list/list5", false);
        suite.addTestsFromResource("list/list6", false);
        suite.addTestsFromResource("list/list7", false);
        suite.addTestsFromResource("list/list8", false);
        suite.addTestsFromResource("list/list9", false);
        suite.addTestsFromResource("list/list10", false);
        suite.addTestsFromResource("list/list11", false);
        suite.addTestsFromResource("list/list12", false);
        suite.addTestsFromResource("list/list13", false);
        suite.addTestsFromResource("list/list14", false);
        suite.addTestsFromResource("list/definitionlist1", false);
        suite.addTestsFromResource("list/definitionlist2", false);
        suite.addTestsFromResource("list/definitionlist3", false);
        suite.addTestsFromResource("list/definitionlist4", false);
        suite.addTestsFromResource("list/definitionlist5", false);
        suite.addTestsFromResource("list/definitionlist6", false);
        suite.addTestsFromResource("list/definitionlist7", false);

        // Sections
        suite.addTestsFromResource("section/section1", false);
        suite.addTestsFromResource("section/section2", false);
        suite.addTestsFromResource("section/section3", true);
        suite.addTestsFromResource("section/section4", false);
        suite.addTestsFromResource("section/section5", false);
        suite.addTestsFromResource("section/section6", false);
        suite.addTestsFromResource("section/section7", false);
        suite.addTestsFromResource("section/section8", false);
        suite.addTestsFromResource("section/section9", false);
        suite.addTestsFromResource("section/section10", false);

        // Escaping
        suite.addTestsFromResource("escape/escape1", false);
        suite.addTestsFromResource("escape/escape2", false);
        suite.addTestsFromResource("escape/escape3", false);
        suite.addTestsFromResource("escape/escape4", false);
        suite.addTestsFromResource("escape/escape5", false);
        suite.addTestsFromResource("escape/escape6", false);
        suite.addTestsFromResource("escape/escape7", false);
        suite.addTestsFromResource("escape/escape8", false);
        suite.addTestsFromResource("escape/escape9", false);
        suite.addTestsFromResource("escape/escape10", false);
        suite.addTestsFromResource("escape/escape11", false);
        suite.addTestsFromResource("escape/escape12", false);
        suite.addTestsFromResource("escape/escape13", false);
        suite.addTestsFromResource("escape/escape14", false);
        suite.addTestsFromResource("escape/escape15", false);
        suite.addTestsFromResource("escape/escape16", false);
        suite.addTestsFromResource("escape/escape17", false);
        suite.addTestsFromResource("escape/escape18", false);
        suite.addTestsFromResource("escape/escape19", false);

        // Other
        suite.addTestsFromResource("horizontalline/horizontalline1", false);
        suite.addTestsFromResource("horizontalline/horizontalline2", false);
        suite.addTestsFromResource("horizontalline/horizontalline3", false);
        suite.addTestsFromResource("horizontalline/horizontalline4", false);
        suite.addTestsFromResource("horizontalline/horizontalline5", false);
        suite.addTestsFromResource("quote/quote1", false);
        suite.addTestsFromResource("quote/quote2", false);
        suite.addTestsFromResource("quote/quote3", false);
        suite.addTestsFromResource("quote/quote4", false);
        suite.addTestsFromResource("misc/misc1", false);
        suite.addTestsFromResource("misc/misc2", false);
        suite.addTestsFromResource("misc/misc3", false);
        suite.addTestsFromResource("misc/misc4", false);
        suite.addTestsFromResource("misc/misc5", false);
        suite.addTestsFromResource("misc/misc6", false);
        suite.addTestsFromResource("misc/misc7", false);
        suite.addTestsFromResource("encoding/encoding1", false);

        suite.addTestsFromResource("escape/escape1", false);
        suite.addTestsFromResource("escape/escape2", false);
        suite.addTestsFromResource("escape/escape3", false);
        suite.addTestsFromResource("escape/escape4", false);
        suite.addTestsFromResource("escape/escape5", false);
        suite.addTestsFromResource("escape/escape6", false);
        suite.addTestsFromResource("escape/escape7", false);
        suite.addTestsFromResource("escape/escape8", false);
        suite.addTestsFromResource("escape/escape9", false);
        suite.addTestsFromResource("escape/escape10", false);
        suite.addTestsFromResource("escape/escape11", false);
        suite.addTestsFromResource("escape/escape12", false);
        suite.addTestsFromResource("escape/escape13", false);
        suite.addTestsFromResource("escape/escape14", false);
        suite.addTestsFromResource("escape/escape15", false);
        suite.addTestsFromResource("escape/escape16", false);
        suite.addTestsFromResource("escape/escape17", false);
        suite.addTestsFromResource("escape/escape18", false);
        suite.addTestsFromResource("escape/escape19", false);

        return new ComponentManagerTestSetup(suite);
    }
}
