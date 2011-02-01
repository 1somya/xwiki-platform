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
package org.xwiki.rendering.block;

import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.util.IdGenerator;

import java.util.List;
import java.util.Collections;
import java.util.Map;

/**
 * Contains the full tree of {@link Block} that represent a XWiki Document's content.
 * 
 * @version $Id$
 * @since 1.5M2
 */
public class XDOM extends AbstractBlock
{
    /**
     * Constructs an empty XDOM. Useful for example when calling a macro that doesn't use the XDOM parameter passed to
     * it.
     */
    public static final XDOM EMPTY = new XDOM(Collections.<Block> emptyList());

    /**
     * Stateful id generator for this document. We store it in the XDOM because it is the only object which remains the
     * same between parsing, transformation and rendering, and we need to generate ids during parsing and during
     * transformation.
     */
    private IdGenerator idGenerator;

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param parameters the parameters to set
     * @see AbstractBlock#AbstractBlock(List)
     */
    public XDOM(List<Block> childBlocks, Map<String, String> parameters)
    {
        super(childBlocks, parameters);

        this.idGenerator = new IdGenerator();
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @see AbstractBlock#AbstractBlock(List)
     */
    public XDOM(List<Block> childBlocks)
    {
        super(childBlocks);

        this.idGenerator = new IdGenerator();
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param idGenerator a sateful id generator for this document
     */
    public XDOM(List<Block> childBlocks, IdGenerator idGenerator)
    {
        super(childBlocks);

        this.idGenerator = idGenerator;
    }

    /**
     * @return a stateful id generator for the whole document.
     */
    public IdGenerator getIdGenerator()
    {
        return this.idGenerator;
    }

    /**
     * @param idGenerator a stateful id generator for the whole document.
     * @since 2.1M1
     */
    public void setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.AbstractBlock#before(org.xwiki.rendering.listener.Listener)
     */
    public void before(Listener listener)
    {
        listener.beginDocument();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.AbstractBlock#after(org.xwiki.rendering.listener.Listener)
     */
    public void after(Listener listener)
    {
        listener.endDocument();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.AbstractBlock#clone()
     */
    @Override
    public XDOM clone()
    {
        return (XDOM) super.clone();
    }
}
