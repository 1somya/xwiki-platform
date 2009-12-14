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

import org.apache.commons.lang.StringUtils;
import org.xwiki.model.DocumentName;
import org.xwiki.model.DocumentNameFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;

/**
 * Generate a Document Name from a raw string reference. The format is:
 * {@code wiki:space.page}. If wiki is not specified then the current wiki is returned and
 * if there's no current wiki then "xwiki" is used. If the space is not specified then the
 * current space is returned and if there's no current space then the "XWiki" space name is
 * used. If the page is not specified then "WebHome" is used. 
 * 
 * @version $Id$
 * @since 1.8.1
 * @todo move this implementation to the bridge module. We could almost move it if we are sure that that
 *       bridge.getCurrentDocumentName().getWiki() has always the same value as xwikicontext.getDatabase() 
 */
@Component
public class DefaultDocumentNameFactory implements DocumentNameFactory
{
    /**
     * Default space to use when the user has not specified any space and there's no current space set in the context.
     */
    private static final String DEFAULT_SPACE = "XWiki";

    /**
     * Default page name when the user has not specified the page name.
     */
    private static final String DEFAULT_PAGE = "WebHome";

    /**
     * Default wiki to use when the user has not specified any wiki and there's no current wiki set in the context. 
     */
    private static final String DEFAULT_WIKI = "xwiki";

    private static final String WIKI_SEPARATOR = ":";
    
    private static final String SPACE_SEPARATOR = ".";
    
    /** 
     * Execution context handler, needed for accessing the XWikiContext. 
     */
    @Requirement
    private Execution execution;
    
    /**
     * {@inheritDoc}
     * 
     * @see DocumentNameFactory#createDocumentName(String)
     * @since 2.2M1
     */
    public DocumentName createDocumentName(String reference)
    {
        String wiki;
        String space;
        String page;
        
        if (StringUtils.isBlank(reference)) {
            wiki = getDefaultWikiName();
            space = getDefaultSpaceName();
            page = getDefaultPageName();
        } else {

            // Step 1: Extract the wiki name
            
            // We allow the wiki separator in wiki names and thus we look for the last wiki sep in the reference.
            // TODO: Note that this was done to have the same behavior of XWikiDocument.setFullName() but it would
            //       seem better to me to allow the wiki sep in space names rather than in wiki names (since wiki 
            //       names are constrained by database schema names).
            int spaceSeparatorPosition;
            int wikiSeparatorPosition = reference.lastIndexOf(WIKI_SEPARATOR);
            if (wikiSeparatorPosition != -1) {
                wiki = reference.substring(0, wikiSeparatorPosition);
                if (wiki.length() == 0) {
                    wiki = getDefaultWikiName();
                }
                
                // We allow space sep in space names and thus we look for the last space sep in the reference.
                // TODO: Note that this was done to have the same behavior of XWikiDocument.setFullName() but it would
                //       seem better to me to allow space sep in pages names rather than in space names (since users
                //       want more liberty in page names and usually create pages in existing spaces).
                spaceSeparatorPosition = reference.lastIndexOf(SPACE_SEPARATOR);
                if (spaceSeparatorPosition <= wikiSeparatorPosition) {
                    spaceSeparatorPosition = -1;
                }
            } else {
                // No wiki separator, use default wiki.
                wiki = getDefaultWikiName();

                // We allow space sep in space names and thus we look for the last space sep in the reference.
                // TODO: Note that this was done to have the same behavior of XWikiDocument.setFullName() but it would
                //       seem better to me to allow space sep in pages names rather than in space names (since users
                //       want more liberty in page names and usually create pages in existing spaces).
                spaceSeparatorPosition = reference.lastIndexOf(SPACE_SEPARATOR);
            }
            
            // Step 2: Extract the space and page names
                
            if (spaceSeparatorPosition != -1) {
                space = reference.substring(wikiSeparatorPosition + WIKI_SEPARATOR.length(), 
                    spaceSeparatorPosition);
                if (space.length() == 0) {
                    space = getDefaultSpaceName();
                }
                
                // Make sure the space separator is not the last char of the reference
                if (spaceSeparatorPosition + SPACE_SEPARATOR.length() < reference.length()) {
                    page = reference.substring(spaceSeparatorPosition + SPACE_SEPARATOR.length());
                } else {
                    page = getDefaultPageName();
                }
            } else {
                // No space separator the whole substring is thus the page.
                space = getDefaultSpaceName();

                // Make sure the wiki separator is not the last char of the reference
                if (wikiSeparatorPosition == -1 
                    || wikiSeparatorPosition + WIKI_SEPARATOR.length() < reference.length())
                {
                    page = reference.substring(wikiSeparatorPosition + WIKI_SEPARATOR.length());
                } else {
                    page = getDefaultPageName();
                }
            }
        }
        
        return new DocumentName(wiki, space, page);
    }
    
    protected String getDefaultWikiName()
    {
        String wiki = getContext().getDatabase();
        if (wiki == null) {
            wiki = DEFAULT_WIKI;
        }
        return wiki;
    }

    protected String getDefaultPageName()
    {
        return DEFAULT_PAGE;
    }
    
    protected String getDefaultSpaceName()
    {
        String space;
        XWikiDocument currentDocument = getContext().getDoc();
        if (currentDocument == null) {
            space = DEFAULT_SPACE;
        } else {
            space = currentDocument.getSpace();
            if (space == null) {
                space = DEFAULT_SPACE;
            }
        }
        return space;
    }

    /**
     * @return the XWiki Context used to bridge with the old API
     */
    protected XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
