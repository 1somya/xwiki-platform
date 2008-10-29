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
package org.xwiki.rendering.internal.parser;

import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.parser.ParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parses the content of XWiki links.
 * 
 * The format is as follows:
 * <code>(link)(@interWikiAlias)?</code>, where:
 * <ul>
 *   <li><code>link</code>: The full link reference using the following syntax:
 *       <code>(reference)(#anchor)?(?queryString)?</code>, where:
 *       <ul>
 *         <li><code>reference</code>: The link reference. This can be either a URI in the form
 *             <code>protocol:path</code> (example: "http://xwiki.org", "mailto:john@smith.com) or
 *             a wiki page name (example: "wiki:Space.WebHome").</li>
 *         <li><code>anchor</code>: An optional anchor name pointing to an anchor defined in the
 *             referenced link. Note that in XWiki anchors are automatically created for titles.
 *             Example: "TableOfContentAnchor".</li>
 *         <li><code>queryString</code>: An optional query string for specifying parameters that
 *             will be used in the rendered URL. Example: "mydata1=5&mydata2=Hello".</li>
 *       </ul>
 *       The <code>link</code> element is mandatory.</li>
 *   <li><code>interWikiAlias</code>: An optional
 *       <a href="http://en.wikipedia.org/wiki/InterWiki">Inter Wiki</a> alias as defined in the
 *       InterWiki Map. Example: "wikipedia"</li>
 * </ul>
 * Examples of valid wiki links:
 * <ul>
 *   <li>Hello World</li>
 *   <li>http://myserver.com/HelloWorld</li>
 *   <li>HelloWorld#Anchor</li>
 *   <li>Hello World@Wikipedia</li>
 *   <li>mywiki:HelloWorld</li>
 *   <li>Hello World?param1=1&param2=2</li>
 * </ul>
 * 
 * @version $Id$
 * @since 1.5M2
 */
public class XWikiLinkParser implements LinkParser
{
    // Implementation note: We're not using regex in general in order to provide better error
    // messages when throwing exceptions. In addition regex makes the code less readable.
    // FWIW this is the kind of regex that would need to be used:
    //   private static final Pattern LINK_PATTERN = Pattern.compile(
    //      "(?:([^\\|>]*)[\\|>])?([^\\|>]*)(?:@([^\\|>]*))?(?:[\\|>](.*))?");
    //   private static final Pattern REFERENCE_PATTERN = Pattern.compile(
    //      "(mailto:.*|http:.*)|(?:([^?#]*)[?#]?)?(?:([^#]*)[#]?)?(.*)?");

    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://");

    private static final List<String> URI_PREFIXES = Arrays.asList("mailto", "image", "attach");
    
    public Link parse(String rawLink) throws ParseException
    {
        StringBuffer content = new StringBuffer(rawLink.trim());

        Link link = new Link();

        // Let's default the link to be a document link. If instead it's a link to a URI or to
        // an interwiki location it'll be overriden.
        link.setType(LinkType.DOCUMENT);

        // Parse the link reference itself.
        String uri = parseURI(content);
        if (uri != null) {
            link.setReference(uri);
            link.setType(LinkType.URI);
        } else {
            // Note: the order here is also very important.
            // We parse the query string early as it can contain our special delimiter characters
            // (like "."). Note: This means that "@" characters are forbidden in the query string...

            String interwikiAlias = parseElementAfterString(content, "@");
            if (interwikiAlias != null) {
                link.setInterWikiAlias(interwikiAlias);
                link.setType(LinkType.INTERWIKI);
            }

            link.setQueryString(parseElementAfterString(content, "?"));
        }

        link.setAnchor(parseElementAfterString(content, "#"));

        // What remains in the content buffer is the page name or the interwiki reference if any.
        // If the content is empty then it means no page was specified. This is allowed and in that
        // case when the link is rendered it'll be pointing to WebHome.

        // TODO: Check for invalid characters in a page

        if (link.getReference() == null) {
            link.setReference(content.toString());
        } else if (content.length() > 0) {
            throw new ParseException("Invalid link format [" + rawLink + "]");
        }

        return link;
    }

    /**
     * Find out the URI part of the full link. Supported URIs are "mailto:", "image:", "attach:" or any URL
     * in the form "protocol://".
     *
     * @param content the string to parse. This parameter will be modified by the method to remove
     *                the parsed content.
     * @return the parsed URI or null if no URI was specified
     * @throws ParseException if the URI is malformed
     */
    protected String parseURI(StringBuffer content) throws ParseException
    {
        String uri = null;

        // First, look for one of the known URI schemes
        int uriSchemeDelimiter = content.indexOf(":");
        if ((uriSchemeDelimiter > -1) && URI_PREFIXES.contains(content.substring(0, uriSchemeDelimiter))) {
            try {
                uri = new URI(content.toString()).toString();
            } catch (URISyntaxException e) {
                throw new ParseException("Invalid URI [" + content.toString() + "]", e);
            }
            content.setLength(0);
        } else {
            // Look for a URL pattern
            Matcher matcher = URL_SCHEME_PATTERN.matcher(content.toString());
            if (matcher.lookingAt()) {
                // If a URL is specified then virtual wiki aliases and spaces should not be allowed.
                try {
                    uri = new URL(content.toString()).toString();
                } catch (Exception e) {
                    throw new ParseException("Invalid URL format [" + content.toString() + "]", e);
                }
                content.setLength(0);
            }
        }

        return uri;
    }

    /**
     * Find out the element located to the right of the passed separator.
     *
     * @param content the string to parse. This parameter will be modified by the method to remove
     *                the parsed content.
     * @param separator the separator string to locate the element
     * @return the parsed element or null if the separator string wasn't found
     */
    protected String parseElementAfterString(StringBuffer content, String separator)
    {
        String element = null;

        int index = content.lastIndexOf(separator);
        if (index != -1) {
            element = content.substring(index + separator.length()).trim();
            content.delete(index, content.length());
        }

        return element;
    }
}
