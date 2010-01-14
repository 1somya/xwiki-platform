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
package org.xwiki.url.internal;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.url.InvalidURLException;
import org.xwiki.url.XWikiDocumentURL;
import org.xwiki.url.XWikiURL;
import org.xwiki.url.XWikiURLFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transform a String representation of a URL into a XWiki URL.
 * 
 * @todo how do we support removing the view/ part of the url for example?
 * @todo how do we support hierarchical spaces?
 */
@Component
public class RegexXWikiURLFactory implements XWikiURLFactory<String>, Initializable
{
    // TODO: Move to a configuration parameter
    private String pattern = "(?:http[s]?://([a-zA-Z-]*)[a-zA-Z-.]*[:]?\\d*)?/\\w*/\\w*/(\\w*)/(\\w*)/?(\\w*)\\??(.*)";

    // TODO: Move to a configuration parameter
    private Map<String, String> regexMappings;
    
    private Pattern regexPattern;

    @Requirement("current/reference")
    private DocumentReferenceResolver<EntityReference> currentReferenceDocumentRefernceResolver; 

    /**
     * For performance reason compile the regex pattern.
     */
    public void initialize() throws InitializationException
    {
       this.regexPattern = Pattern.compile(this.pattern);

       this.regexMappings = new HashMap<String, String>();
       this.regexMappings.put("wiki", "1");
       this.regexMappings.put("action", "2");
       this.regexMappings.put("space", "3");
       this.regexMappings.put("page", "4");
       this.regexMappings.put("queryString", "5");
    }

    /**
     * Transform a String representation of a URL into a XWiki URL.
     * 
     * {@inheritDoc}
     * @see XWikiURLFactory#createURL(Object)
     */
    public XWikiURL createURL(String urlAsString) throws InvalidURLException
    {
        XWikiDocumentURL url;

        // Use a regex to parse the URL into its discrete parts:
        // <protocol>://<server>:<port>/<context>/<action>/<space>/<document>
        Matcher matcher = this.regexPattern.matcher(urlAsString);
        if (matcher.matches()) {

            EntityReference reference = null;

            // Find the wiki part in the URL
            String wiki = matcher.group(Integer.parseInt(this.regexMappings.get("wiki")));
            if (!StringUtils.isEmpty(wiki)) {
                reference = new EntityReference(wiki, EntityType.WIKI);
            }

            // Find the space part in the URL
            String space = matcher.group(Integer.parseInt(this.regexMappings.get("space")));
            if (!StringUtils.isEmpty(space)) {
                reference = new EntityReference(space, EntityType.SPACE, reference);
            }

            // Find the document part in the URL
            String page = matcher.group(Integer.parseInt(this.regexMappings.get("page")));
            if (!StringUtils.isEmpty(page)) {
                reference = new EntityReference(page, EntityType.DOCUMENT, reference);
            }

            url = new XWikiDocumentURL(this.currentReferenceDocumentRefernceResolver.resolve(reference));
            
            // Find the action part in the URL
            String action = matcher.group(Integer.parseInt(this.regexMappings.get("action")));
            url.setAction(action);
            
            // Find the query string if any and transform it into a parameter Map for easy access
            String queryString = matcher.group(Integer.parseInt(this.regexMappings.get("queryString")));
            if (queryString != null) {
                StringTokenizer st = new StringTokenizer(queryString, "&");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int pos = token.indexOf("=");
                    if (pos == -1) {
                        throw new InvalidURLException("Invalid Query String for token [" + token
                            + "]. Parameter values need an '=' sign between name and value.");
                    }
                    url.addParameter(token.substring(0, pos), token.substring(pos + 1));
                }
            }

        } else {
            throw new InvalidURLException("Failed to parse URL [" + urlAsString + "] using pattern ["
                + this.pattern + "]");
        }

        return url;
    }
}
