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
package org.xwiki.xml.internal.html;

import junit.framework.TestCase;

import org.htmlcleaner.JDomSerializer;
import org.jdom.Document;
import org.xwiki.xml.XMLUtils;
import org.xwiki.xml.internal.html.DefaultHTMLCleaner;

/**
 * Unit tests for {@link org.xwiki.xml.internal.html.DefaultHTMLCleaner}.
 *
 * @version $Id: $
 * @since 1.6M1
 */
public class DefaultHTMLCleanerTest extends TestCase
{
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
    
    private static final String HEADER_FULL = HEADER + "<html><head /><body>";

    private static final String FOOTER = "</body></html>\n";

    private DefaultHTMLCleaner cleaner;

    protected void setUp() throws Exception
    {
        this.cleaner = new DefaultHTMLCleaner();
        this.cleaner.initialize();
    }

    public void testSpecialCharacters()
    {
        // TODO: We still have a problem I think in that if there are characters such as "&" or quote in the source
        // text they are not escaped. This is because we have use "false" in DefaultHTMLCleaner here:
        //     Document document = new JDomSerializer(this.cleanerProperties, false).createJDom(cleanedNode);
        // See the problem described here: http://sourceforge.net/forum/forum.php?thread_id=2243880&forum_id=637246
        assertHTML("<p>&quot;&amp;**notbold**&lt;notag&gt;</p>", "<p>&quot;&amp;**notbold**&lt;notag&gt;</p>");
    }

    public void testCloseUnbalancedTags()
    {
        assertHTML("<hr /><p>hello</p>", "<hr><p>hello");
    }

    public void testConversionsFromHTML()
    {
        assertHTML("this <strong>is</strong> bold", "this <b>is</b> bold");
        assertHTML("<em>italic</em>", "<i>italic</i>");
        assertHTML("<del>strike</del>", "<strike>strike</strike>");
        assertHTML("<del>strike</del>", "<s>strike</s>");
        assertHTML("<ins>strike</ins>", "<u>strike</u>");
        assertHTML("<p style=\"text-align:center\">center</p>", "<center>center</center>");
        assertHTML("<span style=\"color:red;font-family=arial;font-size=3pt;\">This is some text!</span>",
            "<font face=\"arial\" size=\"3\" color=\"red\">This is some text!</font>");
    }

    public void testCleanNonXHTMLLists()
    {
        assertHTML("<ul><li>item1<ul><li>item2</li></ul></li></ul>", "<ul><li>item1</li><ul><li>item2</li></ul></ul>");
        assertHTML("<ul><li>item1<ul><li>item2<ul><li>item3</li></ul></li></ul></li></ul>",
            "<ul><li>item1</li><ul><li>item2</li><ul><li>item3</li></ul></ul></ul>");
        assertHTML("<ul><li><ul><li>item</li></ul></li></ul>", "<ul><ul><li>item</li></ul></ul>");
        assertHTML("<ul> <li><ul><li>item</li></ul></li></ul>", "<ul> <ul><li>item</li></ul></ul>");
        assertHTML("<ul><li>item1<ol><li>item2</li></ol></li></ul>", "<ul><li>item1</li><ol><li>item2</li></ol></ul>");
        assertHTML("<ol><li>item1<ol><li>item2<ol><li>item3</li></ol></li></ol></li></ol>",
            "<ol><li>item1</li><ol><li>item2</li><ol><li>item3</li></ol></ol></ol>");
        assertHTML("<ol><li><ol><li>item</li></ol></li></ol>", "<ol><ol><li>item</li></ol></ol>");
        assertHTML("<ul><li>item1<ul><li><ul><li>item2</li></ul></li><li>item3</li></ul></li></ul>", 
            "<ul><li>item1</li><ul><ul><li>item2</li></ul><li>item3</li></ul></ul>");
    }

    public void testPruneTags()
    {
        assertHTML("<p>hello</p>", "<script>whatever</script><p>hello</p>");
    }

    private void assertHTML(String expected, String actual)
    {
        assertEquals(HEADER_FULL + expected + FOOTER, XMLUtils.toString(this.cleaner.clean(actual)));
    }
}
