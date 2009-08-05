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
package org.xwiki.rendering.internal.parser.wikimodel;

import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ImageParser;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.parser.ParseException;
import org.wikimodel.wem.IWikiParser;

import java.io.Reader;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.rendering.renderer.BlockRenderer;

/**
 * Common code for all WikiModel-based parsers.
 * 
 * @version $Id$
 * @since 1.5M2
 */
public abstract class AbstractWikiModelParser extends AbstractLogEnabled implements Parser
{
    /**
     * Used by the XDOMGeneratorListener to generate unique header ids.
     */
    @Requirement("plain/1.0")
    protected BlockRenderer plainTextBlockRenderer;

    /**
     * @return the WikiModel parser instance to use to parse input content.
     * @throws ParseException when there's a problem creating an instance of the parser to use
     */
    public abstract IWikiParser createWikiModelParser() throws ParseException;

    /**
     * @return the parser to use when parsing links. We need to parse links to transform a link reference passed as
     *         a raw string by WikiModel into a {@link org.xwiki.rendering.listener.Link} object.
     */
    public abstract LinkParser getLinkParser();

    /**
     * @return the parser to use when parsing image references (eg "Space.Doc@image.png" in XWiki Syntax 2.0). 
     *         We transform a raw image reference into a {@link org.xwiki.rendering.listener.Image} object.
     */
    public abstract ImageParser getImageParser();
    
    /**
     * @return the syntax parser to use for parsing link labels, since wikimodel does not support wiki syntax 
     *         in links and they need to be handled in the XDOMGeneratorListener. By default, the link label 
     *         parser is the same one as the source parser (this), but you should overwrite this method if you
     *         need to use a special parser.
     * @see XDOMGeneratorListener
     * @see <a href="http://code.google.com/p/wikimodel/issues/detail?id=87">wikimodel issue 87</a>
     * TODO: Remove this method when the parser will not need to be passed to the XDOMGeneratorListener anymore.
     */
    protected Parser getLinkLabelParser() 
    {
        return this;
    }

    /**
     * {@inheritDoc}
     * @see Parser#parse(Reader)
     */
    public XDOM parse(Reader source) throws ParseException
    {
        IWikiParser parser = createWikiModelParser();

        // We pass the LinkParser corresponding to the syntax.
        XDOMGeneratorListener listener = new XDOMGeneratorListener(this.getLinkLabelParser(), getLinkParser(),
            getImageParser(), this.plainTextBlockRenderer);

        try {
            parser.parse(source, listener);
        } catch (Exception e) {
            throw new ParseException("Failed to parse input source", e);
        }
        return listener.getXDOM();
    }
}
