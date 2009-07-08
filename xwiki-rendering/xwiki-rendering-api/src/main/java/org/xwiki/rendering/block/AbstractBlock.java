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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation for Block operations. All blocks should extend this class. Supports the notion of parameters which can
 * be added to a block (see {@link #setParameter(String, Object)} for more details).
 * 
 * @version $Id$
 * @since 1.5M2
 */
public abstract class AbstractBlock implements Block
{
    /**
     * Store parameters, see {@link #setParameter(String, Object)} for more explanations on what parameters are.
     */
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    /**
     * The Blocks this Block contains.
     */
    private List<Block> childrenBlocks = new ArrayList<Block>();

    /**
     * The Block containing this Block.
     */
    private Block parentBlock;

    /**
     * Empty constructor to construct an empty block.
     */
    public AbstractBlock()
    {
        // Nothing to do
    }

    /**
     * Construct a block with parameters.
     * 
     * @param parameters the parameters to set
     */
    public AbstractBlock(Map<String, String> parameters)
    {
        this.parameters.putAll(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#addChild(org.xwiki.rendering.block.Block)
     */
    public void addChild(Block blockToAdd)
    {
        insertChildAfter(blockToAdd, null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#addChildren(java.util.List)
     */
    public void addChildren(List< ? extends Block> blocksToAdd)
    {
        for (Block blockToAdd : blocksToAdd) {
            addChild(blockToAdd);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#insertChildBefore(org.xwiki.rendering.block.Block,
     *      org.xwiki.rendering.block.Block)
     */
    public void insertChildBefore(Block blockToInsert, Block nextBlock)
    {
        blockToInsert.setParent(this);

        if (nextBlock == null) {
            this.childrenBlocks.add(blockToInsert);
        } else {
            this.childrenBlocks.add(this.childrenBlocks.indexOf(nextBlock), blockToInsert);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#insertChildAfter(org.xwiki.rendering.block.Block,
     *      org.xwiki.rendering.block.Block)
     */
    public void insertChildAfter(Block blockToInsert, Block previousBlock)
    {
        blockToInsert.setParent(this);

        if (previousBlock == null) {
            this.childrenBlocks.add(blockToInsert);
        } else {
            this.childrenBlocks.add(this.childrenBlocks.indexOf(previousBlock) + 1, blockToInsert);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#replaceChild(Block, Block)
     */
    public void replaceChild(Block newBlock, Block oldBlock)
    {
        replaceChild(Collections.singletonList(newBlock), oldBlock);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#replaceChild(List, Block)
     */
    public void replaceChild(List<Block> newBlocks, Block oldBlock)
    {
        List<Block> blocks = getChildren();
        int pos = blocks.indexOf(oldBlock);
        for (Block block : newBlocks) {
            block.setParent(this);
        }
        blocks.addAll(pos, newBlocks);
        blocks.remove(pos + newBlocks.size());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getChildren()
     */
    public List<Block> getChildren()
    {
        return this.childrenBlocks;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getParent()
     */
    public Block getParent()
    {
        return this.parentBlock;
    }

    /**
     * @return all parameters
     */
    public Map<String, String> getParameters()
    {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * See {@link #setParameter(String, Object)} for detailed explanations on parameters.
     * 
     * @param name the name of the parameter to return
     * @return the parameter or null if the parameter doesn't exist
     */
    public String getParameter(String name)
    {
        return this.parameters.get(name);
    }

    /**
     * Set a parameter on the current block. See {@link #setParameter(String, Object)} for more details.
     * 
     * @param name the parameter's name
     * @param value the parameter's value
     */
    public void setParameter(String name, String value)
    {
        this.parameters.put(name, value);
    }

    /**
     * Set a parameter on the current block. A parameter is any semantic data associated with a block. It can be used
     * for various purposes and provide additional information to the renderers/listeners. For example you can pass
     * style information such as <code>style="color:red"</code> (in that example the name would be <code>style</code>
     * and the value <code>"color:red"</code>) to indicate that the current block should be displayed in red.
     * <p>
     * Note that there are currently no well-defined known parameter names and you'll need to check what the different
     * renderers/listeners support to know what to use.
     * </p>
     * 
     * @param name the parameter's name
     * @param value the parameter's value
     */
    public void setParameter(String name, Object value)
    {
        setParameter(name, ConvertUtils.convert(value));
    }

    /**
     * Set several parameters at once.
     * 
     * @param parameters the parameters to set
     * @see #setParameter(String, Object)
     * @since 1.7M2
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters.putAll(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#setParent(org.xwiki.rendering.block.Block)
     */
    public void setParent(Block parentBlock)
    {
        this.parentBlock = parentBlock;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getRoot()
     */
    public Block getRoot()
    {
        Block block = this;

        while (block.getParent() != null) {
            block = block.getParent();
        }

        return block;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getChildrenByType(java.lang.Class, boolean)
     */
    public <T extends Block> List<T> getChildrenByType(Class<T> blockClass, boolean recurse)
    {
        List<T> typedBlocks = new ArrayList<T>();
        for (Block block : getChildren()) {
            if (blockClass.isAssignableFrom(block.getClass())) {
                typedBlocks.add(blockClass.cast(block));
            }
            if (recurse && !block.getChildren().isEmpty()) {
                typedBlocks.addAll(block.getChildrenByType(blockClass, true));
            }
        }

        return typedBlocks;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getPreviousBlockByType(java.lang.Class, boolean)
     */
    public <T extends Block> T getPreviousBlockByType(Class<T> blockClass, boolean recurse)
    {
        if (getParent() == null) {
            return null;
        }

        List<Block> blocks = getParent().getChildren();
        int index = blocks.indexOf(this);

        for (int i = index - 1; i >= 0; --i) {
            Block previousBlock = blocks.get(i);
            if (blockClass.isAssignableFrom(previousBlock.getClass())) {
                return blockClass.cast(previousBlock);
            }
        }

        return getParent().getPreviousBlockByType(blockClass, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#getParentBlockByType(java.lang.Class)
     */
    public <T extends Block> T getParentBlockByType(Class<T> blockClass)
    {
        Block parent = getParent();

        if (parent == null || blockClass.isAssignableFrom(parent.getClass())) {
            return blockClass.cast(parent);
        }

        return parent.getParentBlockByType(blockClass);
    }

    /**
     * {@inheritDoc}
     * 
     * @see EqualsBuilder#reflectionEquals(Object, Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HashCodeBuilder#reflectionHashCode(Object)
     */
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#clone()
     */
    @Override
    public Block clone()
    {
        return clone(null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.block.Block#clone(org.xwiki.rendering.block.BlockFilter)
     * @since 1.8RC2
     */
    public Block clone(BlockFilter blockFilter)
    {
        Block block;
        try {
            block = (AbstractBlock) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen
            throw new RuntimeException("Failed to clone object", e);
        }

        ((AbstractBlock) block).parameters = new LinkedHashMap<String, String>(this.parameters);

        ((AbstractBlock) block).childrenBlocks = new ArrayList<Block>(this.childrenBlocks.size());
        for (Block childBlock : this.childrenBlocks) {
            if (blockFilter != null) {
                Block clonedChildBlocks = childBlock.clone(blockFilter);

                List<Block> filteredBlocks = blockFilter.filter(clonedChildBlocks);

                if (filteredBlocks.size() == 0) {
                    filteredBlocks = clonedChildBlocks.getChildren();
                }

                block.addChildren(filteredBlocks);
            } else {
                block.addChild(childBlock.clone());
            }
        }

        return block;
    }
}
