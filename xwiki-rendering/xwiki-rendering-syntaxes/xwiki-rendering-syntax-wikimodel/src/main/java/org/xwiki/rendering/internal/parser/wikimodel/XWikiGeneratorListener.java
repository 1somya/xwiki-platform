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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import org.wikimodel.wem.IWemConstants;
import org.wikimodel.wem.IWemListener;
import org.wikimodel.wem.WikiFormat;
import org.wikimodel.wem.WikiParameter;
import org.wikimodel.wem.WikiParameters;
import org.wikimodel.wem.WikiReference;
import org.wikimodel.wem.WikiStyle;
import org.xwiki.rendering.listener.CompositeListener;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.parser.ImageParser;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.util.IdGenerator;

/**
 * Transforms WikiModel events into XWiki Rendering events.
 * 
 * @version $Id$
 * @since 2.1RC1
 */
public class XWikiGeneratorListener implements IWemListener
{
    /**
     * Listener(s) for the generated XWiki Events. Organized as a stack so that a buffering listener can hijack all
     * events for a while, for example. All generated events are sent to the top of the stack.
     */
    private Stack<Listener> listener = new Stack<Listener>();

    private StreamParser parser;

    private LinkParser linkParser;

    private ImageParser imageParser;

    private IdGenerator idGenerator;

    private PrintRendererFactory plainRendererFactory;

    private int documentDepth = 0;

    private WikiFormat lastEndFormat = null;

    /**
     * @see XWikiGeneratorListener
     * @see <a href="http://code.google.com/p/wikimodel/issues/detail?id=87">wikimodel issue 87</a>
     * @since 2.0M3
     */
    public XWikiGeneratorListener(StreamParser parser, Listener listener, LinkParser linkParser,
        ImageParser imageParser, PrintRendererFactory plainRendererFactory, IdGenerator idGenerator)
    {
        pushListener(listener);

        this.parser = parser;
        this.linkParser = linkParser;
        this.imageParser = imageParser;
        this.idGenerator = idGenerator != null ? idGenerator : new IdGenerator();
        this.plainRendererFactory = plainRendererFactory;
    }

    /**
     * Returns the 'default' listener to send xwiki events to, the top of the listeners stack.
     * 
     * @return the listener to send xwiki events to
     */
    public Listener getListener()
    {
        return this.listener.peek();
    }

    /**
     * Pushes a new listener in the listeners stack, thus making it the 'default' listener, to which all events are
     * sent.
     * 
     * @param listener the listener to add in the top of the stack
     * @return the listener pushed in the top of the stack
     */
    private Listener pushListener(Listener listener)
    {
        return this.listener.push(listener);
    }

    /**
     * Removes the listener from the top of the stack (the current 'default' listener).
     * 
     * @return the removed listener
     */
    private Listener popListener()
    {
        return this.listener.pop();
    }

    /**
     * Convert Wikimodel parameters to XWiki parameters format.
     * 
     * @param params the wikimodel parameters to convert
     * @return the parameters in XWiki format
     */
    private Map<String, String> convertParameters(WikiParameters params)
    {
        Map<String, String> xwikiParams;

        if (params.getSize() > 0) {
            xwikiParams = new LinkedHashMap<String, String>();
            for (WikiParameter wikiParameter : params.toList()) {
                xwikiParams.put(wikiParameter.getKey(), wikiParameter.getValue());
            }
        } else {
            xwikiParams = Listener.EMPTY_PARAMETERS;
        }

        return xwikiParams;
    }

    private Format convertFormat(WikiStyle style)
    {
        Format result;

        if (style == IWemConstants.STRONG) {
            result = Format.BOLD;
        } else if (style == IWemConstants.EM) {
            result = Format.ITALIC;
        } else if (style == IWemConstants.STRIKE) {
            result = Format.STRIKEDOUT;
        } else if (style == IWemConstants.INS) {
            result = Format.UNDERLINED;
        } else if (style == IWemConstants.SUP) {
            result = Format.SUPERSCRIPT;
        } else if (style == IWemConstants.SUB) {
            result = Format.SUBSCRIPT;
        } else if (style == IWemConstants.MONO) {
            result = Format.MONOSPACE;
        } else {
            result = Format.NONE;
        }

        return result;
    }

    private void flush()
    {
        flushInline();
    }

    private void flushInline()
    {
        flushFormat();
    }

    private void flushFormat()
    {
        if (this.lastEndFormat != null) {
            flushFormat(null);
            this.lastEndFormat = null;
        }
    }

    private void flushFormat(List<WikiStyle> xorStyles)
    {
        flushFormat(this.lastEndFormat.getStyles(), this.lastEndFormat.getParams(), xorStyles);
    }

    private void flushFormat(List<WikiStyle> formatStyles, List<WikiParameter> formatParameters,
        List<WikiStyle> xorStyles)
    {
        // Get the styles: the styles are wiki syntax styles (i.e. styles which have a wiki syntax such as bold,
        // italic ,etc). As opposed to format parameters which don't have any specific wiki syntax (they have a generic
        // wiki syntax such as (% a='b' %) for example in XWiki Syntax 2.0.
        // If there's any style or parameter defined, do something. The reason we need to check for this is because
        // wikimodel sends an empty begin/endFormat event before starting an inline block (such as a paragraph).
        if (formatStyles.size() > 0 || formatParameters.size() > 0) {
            // Generate nested FormatBlock blocks since XWiki uses nested Format blocks whereas Wikimodel doesn't.
            //
            // Simple Use Case: (% a='b' %)**//hello//**(%%)
            // WikiModel Events:
            // __beginFormat(params: a='b', styles = BOLD, ITALIC)
            // __onWord(hello)
            // __endFormat(params: a='b', styles = BOLD, ITALIC)
            // XWiki Blocks:
            // __FormatBLock(params: a='b', format = BOLD)
            // ____FormatBlock(format = ITALIC)
            //
            // More complex Use Case: **(% a='b' %)hello**world
            // WikiModel Events:
            // __beginFormat(params: a='b', styles = BOLD)
            // __onWord(hello)
            // __endFormat(params: a='b', styles = BOLD)
            // __beginFormat(params: a='b')
            // __onWord(world)
            // __endFormat(params: a='b')
            // XWiki Blocks:
            // __FormatBlock(params: a='b', format = BOLD)
            // ____WordBlock(hello)
            // __FormatBlock(params: a='b')
            // ____WordBlock(world)

            // TODO: We should instead have the following which would allow to simplify XWikiSyntaxChaining Renderer
            // which currently has to check if the next format has the same params as the previous format to decide
            // whether to print it or not.
            // __FormatBlock(params: a='b')
            // ____FormatBlock(format = BOLD)
            // ______WordBlock(hello)
            // ____WordBlock(world)

            Map<String, String> parameters;
            if (formatParameters.size() > 0) {
                parameters = convertParameters(new WikiParameters(formatParameters));
            } else {
                parameters = Listener.EMPTY_PARAMETERS;
            }

            if (formatStyles.size() > 0) {
                for (ListIterator<WikiStyle> it = formatStyles.listIterator(formatStyles.size()); it.hasPrevious();) {
                    WikiStyle style = it.previous();

                    // Exclude next format styles
                    if (xorStyles == null || !xorStyles.contains(style)) {
                        if (it.hasPrevious()) {
                            getListener().endFormat(convertFormat(style), Listener.EMPTY_PARAMETERS);
                        } else {
                            getListener().endFormat(convertFormat(style), parameters);
                        }
                    }
                }
            } else {
                getListener().endFormat(Format.NONE, parameters);
            }
        }

        this.lastEndFormat = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        getListener().beginDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginDefinitionList(org.wikimodel.wem.WikiParameters)
     */
    public void beginDefinitionList(WikiParameters params)
    {
        flushInline();

        getListener().beginDefinitionList(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        getListener().beginDefinitionTerm();
    }

    public void beginDocument()
    {
        beginDocument(WikiParameters.EMPTY);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginDocument(WikiParameters)
     */
    public void beginDocument(WikiParameters params)
    {
        if (this.documentDepth > 0) {
            getListener().beginGroup(convertParameters(params));
        }

        ++this.documentDepth;
    }

    /**
     * A format is a special formatting around an inline element, such as bold, italics, etc.
     */
    public void beginFormat(WikiFormat format)
    {
        List<WikiStyle> endFormatStyles = this.lastEndFormat != null ? this.lastEndFormat.getStyles() : null;
        List<WikiParameter> endFormatParameters = this.lastEndFormat != null ? this.lastEndFormat.getParams() : null;

        List<WikiStyle> formatStyles = format.getStyles();

        if (this.lastEndFormat != null) {
            // Exclude this format parameters and styles from previous end format
            flushFormat(endFormatStyles, endFormatParameters, formatStyles);
        }

        // Get the styles: the styles are wiki syntax styles (i.e. styles which have a wiki syntax such as bold, italic
        // ,etc).
        // As opposed to format parameters which don't have any specific wiki syntax (they have a generic wiki syntax
        // such as (% a='b' %) for example in XWiki Syntax 2.0.
        List<WikiStyle> styles = format.getStyles();

        // If there's any style or parameter defined, do something. The reason we need to check for this is because
        // wikimodel sends an empty begin/endFormat event before starting an inline block (such as a paragraph).
        if (styles.size() > 0 || format.getParams().size() > 0) {
            // Generate nested FormatBlock blocks since XWiki uses nested Format blocks whereas Wikimodel doesn't.
            //
            // Simple Use Case: (% a='b' %)**//hello//**(%%)
            // WikiModel Events:
            // __beginFormat(params: a='b', styles = BOLD, ITALIC)
            // __onWord(hello)
            // __endFormat(params: a='b', styles = BOLD, ITALIC)
            // XWiki Blocks:
            // __FormatBLock(params: a='b', format = BOLD)
            // ____FormatBlock(format = ITALIC)
            //
            // More complex Use Case: **(% a='b' %)hello**world
            // WikiModel Events:
            // __beginFormat(params: a='b', styles = BOLD)
            // __onWord(hello)
            // __endFormat(params: a='b', styles = BOLD)
            // __beginFormat(params: a='b')
            // __onWord(world)
            // __endFormat(params: a='b')
            // XWiki Blocks:
            // __FormatBlock(params: a='b', format = BOLD)
            // ____WordBlock(hello)
            // __FormatBlock(params: a='b')
            // ____WordBlock(world)

            // TODO: We should instead have the following which would allow to simplify XWikiSyntaxChaining Renderer
            // which currently has to check if the next format has the same params as the previous format to decide
            // whether to print it or not.
            // __FormatBlock(params: a='b')
            // ____FormatBlock(format = BOLD)
            // ______WordBlock(hello)
            // ____WordBlock(world)

            Map<String, String> parameters;
            if (format.getParams().size() > 0) {
                parameters = convertParameters(new WikiParameters(format.getParams()));
            } else {
                parameters = Listener.EMPTY_PARAMETERS;
            }

            if (styles.size() > 0) {
                boolean parametersConsumed = false;
                for (WikiStyle style : styles) {
                    // Exclude previous format styles
                    if (endFormatStyles == null || !endFormatStyles.contains(style)) {
                        if (!parametersConsumed) {
                            getListener().beginFormat(convertFormat(style), parameters);
                            parametersConsumed = true;
                        } else {
                            getListener().beginFormat(convertFormat(style), Listener.EMPTY_PARAMETERS);
                        }
                    }
                }
            } else {
                getListener().beginFormat(Format.NONE, parameters);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginSection(int, int, WikiParameters)
     */
    public void beginSection(int docLevel, int headerLevel, WikiParameters params)
    {
        if (headerLevel > 0) {
            getListener().beginSection(Listener.EMPTY_PARAMETERS);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginSectionContent(int, int, WikiParameters)
     */
    public void beginSectionContent(int docLevel, int headerLevel, WikiParameters params)
    {
        // TODO add support for it
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginHeader(int, WikiParameters)
     */
    public void beginHeader(int level, WikiParameters params)
    {
        // Heading needs to have an id generated from a plaintext representation of its content, so the header start
        // event will be sent at the end of the header, after reading the content inside and generating the id. 
        // For this:
        // buffer all events in a queue until the header ends, and also send them to a print renderer to generate the ID
        CompositeListener composite = new CompositeListener();
        composite.addListener(new QueueListener());
        composite.addListener(this.plainRendererFactory.createRenderer(new DefaultWikiPrinter()));

        // These 2 listeners will receive all events from now on until the header ends
        pushListener(composite);
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#beginInfoBlock(String, WikiParameters)
     */
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginList(WikiParameters, boolean)
     */
    public void beginList(WikiParameters params, boolean ordered)
    {
        flushInline();

        if (ordered) {
            getListener().beginList(ListType.NUMBERED, convertParameters(params));
        } else {
            getListener().beginList(ListType.BULLETED, convertParameters(params));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginListItem()
     */
    public void beginListItem()
    {
        getListener().beginListItem();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginParagraph(WikiParameters)
     */
    public void beginParagraph(WikiParameters params)
    {
        getListener().beginParagraph(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#beginPropertyBlock(String, boolean)
     */
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#beginPropertyInline(String)
     */
    public void beginPropertyInline(String str)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginQuotation(WikiParameters)
     */
    public void beginQuotation(WikiParameters params)
    {
        getListener().beginQuotation(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        getListener().beginQuotationLine();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginTable(WikiParameters)
     */
    public void beginTable(WikiParameters params)
    {
        getListener().beginTable(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginTableCell(boolean, WikiParameters)
     */
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        if (tableHead) {
            getListener().beginTableHeadCell(convertParameters(params));
        } else {
            getListener().beginTableCell(convertParameters(params));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#beginTableRow(WikiParameters)
     */
    public void beginTableRow(WikiParameters params)
    {
        getListener().beginTableRow(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        flushInline();

        getListener().endDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endDefinitionList(WikiParameters)
     */
    public void endDefinitionList(WikiParameters params)
    {
        getListener().endDefinitionList(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        flushInline();

        getListener().endDefinitionTerm();
    }

    public void endDocument()
    {
        endDocument(WikiParameters.EMPTY);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endDocument(WikiParameters)
     */
    public void endDocument(WikiParameters params)
    {
        flush();

        --this.documentDepth;

        if (this.documentDepth > 0) {
            getListener().endGroup(convertParameters(params));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#endFormat(WikiFormat)
     */
    public void endFormat(WikiFormat format)
    {
        if (format.getStyles().size() > 0 || format.getParams().size() > 0) {
            this.lastEndFormat = format;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endHeader(int, WikiParameters)
     */
    public void endHeader(int level, WikiParameters params)
    {
        // End all formats
        flushInline();

        CompositeListener composite = (CompositeListener) getListener();

        // Get the listener where events inside the header were buffered
        QueueListener queue = (QueueListener) composite.getListener(0);
        // and the listener in which the id was generated
        PrintRenderer renderer = (PrintRenderer) composite.getListener(1);

        // Restore the 'default' listener as it was at the beginning of the header
        popListener();

        HeaderLevel headerLevel = HeaderLevel.parseInt(level);
        // Generate the id from the content inside the header written to the renderer
        String id = this.idGenerator.generateUniqueId("H", renderer.getPrinter().toString());
        Map<String, String> parameters = convertParameters(params);

        // Generate the begin header event to the 'default' listener
        getListener().beginHeader(headerLevel, id, parameters);
        // Send all buffered events to the 'default' listener
        queue.consumeEvents(getListener());
        // Generate the end header event to the 'default' listener
        getListener().endHeader(headerLevel, id, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endSection(int, int, WikiParameters)
     */
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        if (headerLevel > 0) {
            getListener().endSection(Listener.EMPTY_PARAMETERS);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endSectionContent(int, int, WikiParameters)
     */
    public void endSectionContent(int docLevel, int headerLevel, WikiParameters params)
    {
        // TODO add support for it
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#endInfoBlock(String, WikiParameters)
     */
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endList(WikiParameters, boolean)
     */
    public void endList(WikiParameters params, boolean ordered)
    {
        if (ordered) {
            getListener().endList(ListType.NUMBERED, convertParameters(params));
        } else {
            getListener().endList(ListType.BULLETED, convertParameters(params));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endListItem()
     */
    public void endListItem()
    {
        flushInline();

        // Note: This means we support Paragraphs inside lists.
        getListener().endListItem();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endParagraph(WikiParameters)
     */
    public void endParagraph(WikiParameters params)
    {
        flushFormat();

        getListener().endParagraph(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#endPropertyBlock(String, boolean)
     */
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#endPropertyInline(String)
     */
    public void endPropertyInline(String inlineProperty)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endQuotation(WikiParameters)
     */
    public void endQuotation(WikiParameters params)
    {
        getListener().endQuotation(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        flushInline();

        getListener().endQuotationLine();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endTable(WikiParameters)
     */
    public void endTable(WikiParameters params)
    {
        getListener().endTable(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endTableCell(boolean, WikiParameters)
     */
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        flushInline();

        if (tableHead) {
            getListener().endTableHeadCell(convertParameters(params));
        } else {
            getListener().endTableCell(convertParameters(params));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#endTableRow(WikiParameters)
     */
    public void endTableRow(WikiParameters params)
    {
        getListener().endTableRow(convertParameters(params));
    }

    /**
     * Called by wikimodel when there are 2 or more empty lines between blocks. For example the following will generate
     * a call to <code>onEmptyLines(2)</code>:
     * <p>
     * <code><pre>
     * {{macro/}}
     * ... empty line 1...
     * ... empty line 2...
     * {{macro/}}
     * </pre></code>
     * 
     * @param count the number of empty lines separating the two blocks
     */
    public void onEmptyLines(int count)
    {
        getListener().onEmptyLines(count);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onEscape(String)
     */
    public void onEscape(String str)
    {
        // The WikiModel XWiki parser has been modified not to generate any onEscape event so do nothing here.
        // This is because we believe that WikiModel should not have an escape event since it's the
        // responsibility of Renderers to perform escaping as required.
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#onExtensionBlock(String, WikiParameters)
     */
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#onExtensionInline(String, WikiParameters)
     */
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onHorizontalLine(org.wikimodel.wem.WikiParameters)
     */
    public void onHorizontalLine(WikiParameters params)
    {
        getListener().onHorizontalLine(convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onLineBreak()
     */
    public void onLineBreak()
    {
        // Note that in XWiki we don't differentiate new lines and line breaks since it's the Renderers that decide
        // to generate new lines or line breaks depending on the context and the target syntax.
        onNewLine();
    }

    /**
     * A macro block was found and it's separated at least by one new line from the next block. If there's no new line
     * with the next block then wikimodel calls {@link #onMacroInline(String, org.wikimodel.wem.WikiParameters, String)}
     * instead.
     * <p>
     * In wikimodel block elements can be:
     * <ul>
     * <li>at the very beginning of the document (no "\n")</li>
     * <li>just after at least one "\n"</li>
     * </ul>
     */
    public void onMacroBlock(String macroName, WikiParameters params, String content)
    {
        getListener().onMacro(macroName, convertParameters(params), content, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onMacroInline(String, WikiParameters, String)
     */
    public void onMacroInline(String macroName, WikiParameters params, String content)
    {
        flushFormat();

        getListener().onMacro(macroName, convertParameters(params), content, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onLineBreak()
     */
    public void onNewLine()
    {
        flushFormat();

        // Note that in XWiki we don't differentiate new lines and line breaks since it's the Renderers that decide
        // to generate new lines or line breaks depending on the context and the target syntax.
        getListener().onNewLine();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Called when WikiModel finds an reference (link or image) such as a URI located directly in the text
     * (free-standing URI), as opposed to a link/image inside wiki link/image syntax delimiters.
     * </p>
     * 
     * @see org.wikimodel.wem.IWemListener#onLineBreak()
     */
    public void onReference(String reference)
    {
        onReference(reference, null, true, Listener.EMPTY_PARAMETERS);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onReference(String)
     */
    public void onReference(WikiReference reference)
    {
        onReference(reference.getLink(), reference.getLabel(), false, convertParameters(reference.getParameters()));
    }

    private void onReference(String reference, String label, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        flushFormat();

        // If there's no link parser defined, don't handle links and images...
        if (this.linkParser != null) {
            Link link = this.linkParser.parse(reference);

            if (link.getType() == LinkType.URI && link.getReference().startsWith("image:")) {
                String imageLocation = link.getReference().substring("image:".length());

                getListener().onImage(this.imageParser.parse(imageLocation), isFreeStandingURI, parameters);
            } else {
                getListener().beginLink(link, isFreeStandingURI, parameters);
                if (label != null) {
                    try {
                        // TODO: Use an inline parser. See http://jira.xwiki.org/jira/browse/XWIKI-2748
                        WikiModelParserUtils parserUtils = new WikiModelParserUtils();
                        parserUtils.parseInline(this.parser, label, getListener());
                    } catch (ParseException e) {
                        // TODO what should we do here ?
                    }
                }
                getListener().endLink(link, isFreeStandingURI, parameters);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListenerInline#onImage(java.lang.String)
     */
    public void onImage(String ref)
    {
        flushFormat();

        getListener().onImage(this.imageParser.parse(ref), true, Listener.EMPTY_PARAMETERS);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListenerInline#onImage(org.wikimodel.wem.WikiReference)
     */
    public void onImage(WikiReference ref)
    {
        flushFormat();

        getListener().onImage(this.imageParser.parse(ref.getLink()), false, convertParameters(ref.getParameters()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onSpace(String)
     */
    public void onSpace(String spaces)
    {
        flushFormat();

        // We want one space event per space.
        for (int i = 0; i < spaces.length(); i++) {
            getListener().onSpace();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onSpecialSymbol(String)
     */
    public void onSpecialSymbol(String symbol)
    {
        flushFormat();

        for (int i = 0; i < symbol.length(); i++) {
            getListener().onSpecialSymbol(symbol.charAt(i));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see IWemListener#onTableCaption(String)
     */
    public void onTableCaption(String str)
    {
        // Not used by XWiki Syntax 2.0
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onVerbatimBlock(String, WikiParameters)
     */
    public void onVerbatimBlock(String protectedString, WikiParameters params)
    {
        getListener().onVerbatim(protectedString, false, convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onVerbatimInline(String, WikiParameters)
     */
    public void onVerbatimInline(String protectedString, WikiParameters params)
    {
        flushFormat();

        getListener().onVerbatim(protectedString, true, convertParameters(params));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.wikimodel.wem.IWemListener#onWord(String)
     */
    public void onWord(String str)
    {
        flushFormat();

        getListener().onWord(str);
    }
}
