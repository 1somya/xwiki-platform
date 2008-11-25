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
package org.xwiki.rendering.renderer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.listener.DocumentImage;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.ImageType;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.SectionLevel;
import org.xwiki.rendering.listener.URLImage;
import org.xwiki.rendering.listener.xml.XMLComment;
import org.xwiki.rendering.listener.xml.XMLElement;
import org.xwiki.rendering.listener.xml.XMLNode;
import org.xwiki.rendering.internal.renderer.XWikiSyntaxImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLIdGenerator;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLLinkRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLMacroRenderer;

/**
 * Generates XHTML from a {@link org.xwiki.rendering.block.XDOM} object being traversed.
 * 
 * @version $Id$
 * @since 1.5M2
 */
public class XHTMLRenderer extends AbstractPrintRenderer
{
    /**
     * A temporary service offering methods manipulating XWiki Documents that are needed to output the correct XHTML.
     * For example this is used to verify if a document exists when computing the HREF attribute for a link. It's
     * temporary because the current Document services have not yet been rewritten with the new architecture. This
     * bridge allows us to be independent of the XWiki Core module, thus preventing a cyclic dependency.
     */
    private DocumentAccessBridge documentAccessBridge;

    private RenderingConfiguration configuration;

    private XHTMLLinkRenderer linkRenderer;

    private XHTMLMacroRenderer macroRenderer;

    private XHTMLIdGenerator idGenerator;

    private XHTMLWikiPrinter xhtmlWikiPrinter;

    private XWikiSyntaxImageRenderer imageRenderer;
    
    /**
     * The temporary Printer used to redirect all outputs when computing the section title.
     * 
     * @see #originalPrinter
     */
    private WikiPrinter sectionTitlePrinter;
    
    private class DelegateWikiPrinter implements WikiPrinter
    {
        private WikiPrinter printer;

        private boolean hasContentBeenPrinted;

        public DelegateWikiPrinter(WikiPrinter printer)
        {
            this.printer = printer;
        }

        public void print(String text)
        {
            this.hasContentBeenPrinted = true;
            this.printer.print(text);
        }

        public void println(String text)
        {
            this.hasContentBeenPrinted = true;
            this.printer.println(text);
        }

        public boolean hasContentBeenPrinted()
        {
            return this.hasContentBeenPrinted;
        }
    }

    /**
     * @param printer the object to which to write the XHTML output to
     * @param documentAccessBridge see {@link #documentAccessBridge}
     * @param configuration the rendering configuration
     */
    public XHTMLRenderer(WikiPrinter printer, DocumentAccessBridge documentAccessBridge,
        RenderingConfiguration configuration)
    {
        super(printer);

        this.documentAccessBridge = documentAccessBridge;
        this.linkRenderer = new XHTMLLinkRenderer(documentAccessBridge, configuration);
        this.macroRenderer = new XHTMLMacroRenderer();
        this.configuration = configuration;
        this.xhtmlWikiPrinter = new XHTMLWikiPrinter(printer);
        this.imageRenderer = new XWikiSyntaxImageRenderer();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.AbstractPrintRenderer#pushPrinter(org.xwiki.rendering.renderer.WikiPrinter)
     */
    @Override
    protected void pushPrinter(WikiPrinter wikiPrinter)
    {
        super.pushPrinter(wikiPrinter);

        if (this.xhtmlWikiPrinter != null) {
            this.xhtmlWikiPrinter.setWikiPrinter(getPrinter());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.AbstractPrintRenderer#popPrinter()
     */
    @Override
    protected void popPrinter()
    {
        super.popPrinter();

        if (this.xhtmlWikiPrinter != null) {
            this.xhtmlWikiPrinter.setWikiPrinter(getPrinter());
        }
    }

    protected XHTMLWikiPrinter getXHTMLWikiPrinter()
    {
        return this.xhtmlWikiPrinter;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#beginDocument()
     */
    public void beginDocument()
    {
        // Use a new generator for each document being processed since the id generator is stateful and
        // remembers the generated ids.
        this.idGenerator = new XHTMLIdGenerator();
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#endDocument()
     */
    public void endDocument()
    {
        // Don't do anything
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#beginFormat(Format, Map)
     */
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        switch (format) {
            case BOLD:
                getXHTMLWikiPrinter().printXMLStartElement("strong");
                break;
            case ITALIC:
                getXHTMLWikiPrinter().printXMLStartElement("em");
                break;
            case STRIKEDOUT:
                getXHTMLWikiPrinter().printXMLStartElement("del");
                break;
            case UNDERLINED:
                getXHTMLWikiPrinter().printXMLStartElement("ins");
                break;
            case SUPERSCRIPT:
                getXHTMLWikiPrinter().printXMLStartElement("sup");
                break;
            case SUBSCRIPT:
                getXHTMLWikiPrinter().printXMLStartElement("sub");
                break;
            case MONOSPACE:
                getXHTMLWikiPrinter().printXMLStartElement("tt");
                break;
        }
        if (!parameters.isEmpty()) {
            getXHTMLWikiPrinter().printXMLStartElement("span", parameters);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#endFormat(Format, Map)
     */
    public void endFormat(Format format, Map<String, String> parameters)
    {
        if (!parameters.isEmpty()) {
            getXHTMLWikiPrinter().printXMLEndElement("span");
        }
        switch (format) {
            case BOLD:
                getXHTMLWikiPrinter().printXMLEndElement("strong");
                break;
            case ITALIC:
                getXHTMLWikiPrinter().printXMLEndElement("em");
                break;
            case STRIKEDOUT:
                getXHTMLWikiPrinter().printXMLEndElement("del");
                break;
            case UNDERLINED:
                getXHTMLWikiPrinter().printXMLEndElement("ins");
                break;
            case SUPERSCRIPT:
                getXHTMLWikiPrinter().printXMLEndElement("sup");
                break;
            case SUBSCRIPT:
                getXHTMLWikiPrinter().printXMLEndElement("sub");
                break;
            case MONOSPACE:
                getXHTMLWikiPrinter().printXMLEndElement("tt");
                break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#beginParagraph(java.util.Map)
     */
    public void beginParagraph(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("p", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#endParagraph(java.util.Map)
     */
    public void endParagraph(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("p");
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#onLineBreak()
     */
    public void onLineBreak()
    {
        getXHTMLWikiPrinter().printXMLElement("br");
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#onNewLine()
     */
    public void onNewLine()
    {
        // Voluntarily do nothing since we want the same behavior as HTML.
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#beginLink(Link, boolean, Map)
     */
    public void beginLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.linkRenderer.beginRender(getXHTMLWikiPrinter(), link, isFreeStandingURI, parameters);
        pushPrinter(new DelegateWikiPrinter(getPrinter()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see Renderer#endLink(Link, boolean, Map)
     */
    public void endLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        DelegateWikiPrinter printer = (DelegateWikiPrinter) getPrinter();
        popPrinter();
        this.linkRenderer.endRender(getXHTMLWikiPrinter(), link, isFreeStandingURI, !printer.hasContentBeenPrinted);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onInlineMacro(String, java.util.Map, String)
     */
    public void onInlineMacro(String name, Map<String, String> parameters, String content)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.render(getXHTMLWikiPrinter(), name, parameters, content);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onStandaloneMacro(String, java.util.Map, String)
     */
    public void onStandaloneMacro(String name, Map<String, String> parameters, String content)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.render(getXHTMLWikiPrinter(), name, parameters, content);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#beginSection(SectionLevel, Map)
     */
    public void beginSection(SectionLevel level, Map<String, String> parameters)
    {
        // Don't output anything yet since we need the section title to generate the unique XHTML id attribute.
        // Thus we're doing the output in the endSection() event.

        // Redirect all output to our writer
        this.sectionTitlePrinter = new DefaultWikiPrinter();
        pushPrinter(this.sectionTitlePrinter);
    }

    private void processBeginSection(SectionLevel level, String sectionTitle, Map<String, String> parameters)
    {
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        attributes.put("id", this.idGenerator.generateUniqueId(sectionTitle));
        attributes.putAll(parameters);

        int levelAsInt = level.getAsInt();
        getXHTMLWikiPrinter().printXMLStartElement("h" + levelAsInt, attributes);
        // We generate a span so that CSS rules have a hook to perform some magic that wouldn't work on just a H
        // element. Like some IE6 magic and others.
        getXHTMLWikiPrinter().printXMLStartElement("span");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#endSection(SectionLevel, Map)
     */
    public void endSection(SectionLevel level, Map<String, String> parameters)
    {
        String sectionTitle = this.sectionTitlePrinter.toString();
        popPrinter();
        processBeginSection(level, sectionTitle, parameters);
        print(sectionTitle);

        int levelAsInt = level.getAsInt();
        getXHTMLWikiPrinter().printXMLEndElement("span");
        getXHTMLWikiPrinter().printXMLEndElement("h" + levelAsInt);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onWord(String)
     */
    public void onWord(String word)
    {
        getXHTMLWikiPrinter().printXML(word);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onSpace()
     */
    public void onSpace()
    {
        // The XHTML printer will decide whether to print a normal space or a &nbsp;
        getXHTMLWikiPrinter().printSpace();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onSpecialSymbol(String)
     */
    public void onSpecialSymbol(char symbol)
    {
        getXHTMLWikiPrinter().printXML("" + symbol);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onEscape(String)
     */
    public void onEscape(String escapedString)
    {
        getXHTMLWikiPrinter().printXML(escapedString);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#beginList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        if (listType == ListType.BULLETED) {
            getXHTMLWikiPrinter().printXMLStartElement("ul", parameters);
        } else {
            getXHTMLWikiPrinter().printXMLStartElement("ol", parameters);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#beginListItem()
     */
    public void beginListItem()
    {
        getXHTMLWikiPrinter().printXMLStartElement("li");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#endList(org.xwiki.rendering.listener.ListType, java.util.Map)
     */
    public void endList(ListType listType, Map<String, String> parameters)
    {
        if (listType == ListType.BULLETED) {
            getXHTMLWikiPrinter().printXMLEndElement("ul");
        } else {
            getXHTMLWikiPrinter().printXMLEndElement("ol");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#endListItem()
     */
    public void endListItem()
    {
        getXHTMLWikiPrinter().printXMLEndElement("li");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#beginXMLNode(XMLNode)
     */
    public void beginXMLNode(XMLNode node)
    {
        switch (node.getNodeType()) {
            case CDATA:
                getXHTMLWikiPrinter().printXMLStartCData();
                break;
            case COMMENT:
                XMLComment commentNode = (XMLComment) node;
                getXHTMLWikiPrinter().printXMLComment(commentNode.getComment());
                break;
            case ELEMENT:
                XMLElement elementNode = (XMLElement) node;
                getXHTMLWikiPrinter().printXMLStartElement(elementNode.getName(), elementNode.getAttributes());
                break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#endXMLNode(XMLNode)
     */
    public void endXMLNode(XMLNode node)
    {
        switch (node.getNodeType()) {
            case CDATA:
                getXHTMLWikiPrinter().printXMLEndCData();
                break;
            case ELEMENT:
                XMLElement element = (XMLElement) node;
                getXHTMLWikiPrinter().printXMLEndElement(element.getName());
                break;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#beginMacroMarker(String, java.util.Map, String)
     */
    public void beginMacroMarker(String name, Map<String, String> parameters, String content)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.beginRender(getXHTMLWikiPrinter(), name, parameters, content);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#endMacroMarker(String, java.util.Map, String)
     */
    public void endMacroMarker(String name, Map<String, String> parameters, String content)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.endRender(getXHTMLWikiPrinter());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onId(String)
     */
    public void onId(String name)
    {
        // Note: We're using <a><a/> and not <a/> since some browsers do not support the <a/> syntax (FF3)
        // when the content type is set to HTML instead of XHTML.
        getXHTMLWikiPrinter().printXMLStartElement("a", new String[][] { {"id", name}, {"name", name}});
        getXHTMLWikiPrinter().printXMLEndElement("a");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onHorizontalLine(Map)
     */
    public void onHorizontalLine(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLElement("hr", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onVerbatimInline(String)
     */
    public void onVerbatimInline(String protectedString)
    {
        // Note: We generate a tt element rather than a pre element since pre elements cannot be located inside
        // paragraphs for example. There also no tag in XHTML that has a semantic of preserving inline content so
        // tt is the closed to pre for inline.
        // The class is what is expected by wikimodel to understand the tt as meaning a verbatim and not a Monospace
        // element.
        getXHTMLWikiPrinter().printXMLStartElement("tt", new String[][] {{"class", "wikimodel-verbatim"}});
        getXHTMLWikiPrinter().printXML(protectedString);
        getXHTMLWikiPrinter().printXMLEndElement("tt");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onVerbatimStandalone(String, Map)
     */
    public void onVerbatimStandalone(String protectedString, Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("pre", parameters);
        getXHTMLWikiPrinter().printXML(protectedString);
        getXHTMLWikiPrinter().printXMLEndElement("pre");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.Renderer#onEmptyLines(int)
     */
    public void onEmptyLines(int count)
    {
        // We need to use a special tag for empty lines since in XHTML the BR tag cannot be used outside of content
        // tags.
        // Note: We're using <div><div/> and not <div/> since some browsers do not support the <div/> syntax (FF3)
        // when the content type is set to HTML instead of XHTML.
        for (int i = 0; i < count; ++i) {
            getXHTMLWikiPrinter().printXMLStartElement("div", new String[][] {{"class", "wikimodel-emptyline"}});
            getXHTMLWikiPrinter().printXMLEndElement("div");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionList()
     */
    public void beginDefinitionList()
    {
        getXHTMLWikiPrinter().printXMLStartElement("dl");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionList()
     */
    public void endDefinitionList()
    {
        getXHTMLWikiPrinter().printXMLEndElement("dl");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        getXHTMLWikiPrinter().printXMLStartElement("dt");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        getXHTMLWikiPrinter().printXMLStartElement("dd");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        getXHTMLWikiPrinter().printXMLEndElement("dt");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        getXHTMLWikiPrinter().printXMLEndElement("dd");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginQuotation(java.util.Map)
     */
    public void beginQuotation(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("blockquote", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endQuotation(java.util.Map)
     */
    public void endQuotation(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("blockquote");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTable(java.util.Map)
     */
    public void beginTable(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("table", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableRow(java.util.Map)
     */
    public void beginTableRow(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("tr", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableCell(java.util.Map)
     */
    public void beginTableCell(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("td", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginTableHeadCell(java.util.Map)
     */
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement("th", parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTable(java.util.Map)
     */
    public void endTable(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("table");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableRow(java.util.Map)
     */
    public void endTableRow(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("tr");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableCell(java.util.Map)
     */
    public void endTableCell(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("td");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endTableHeadCell(java.util.Map)
     */
    public void endTableHeadCell(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement("th");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#onImage(org.xwiki.rendering.listener.Image, boolean, Map)
     * @since 1.7M2
     */
    public void onImage(Image image, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        // First we need to compute the image URL.
        String imageURL;
        if (image.getType() == ImageType.DOCUMENT) {
            DocumentImage documentImage = (DocumentImage) image;
             try {
                 imageURL = this.documentAccessBridge.getAttachmentURL(documentImage.getDocumentName(), 
                     documentImage.getAttachmentName());
             } catch (Exception e) {
                 // TODO: Handle exceptions in a better manner
                 throw new RuntimeException("Failed to get attachment URL for [" + image + "]", e);
             }
        } else {
            URLImage urlImage = (URLImage) image;
            imageURL = urlImage.getURL();
        }
        
        // Then add it as an attribute of the IMG element.
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        attributes.put("src", imageURL);
        
        // Add the class if we're on a freestanding uri
        if (isFreeStandingURI) {
            attributes.put("class", "wikimodel-freestanding");
        }
        
        // Add the other parameters as attributes
        attributes.putAll(parameters);

        // If not ALT attribute has been specified, add it since the XHTML specifications makes it mandatory.
        if (!parameters.containsKey("alt")) {
            attributes.put("alt", image.getName());
        }
        
        // And generate the XHTML IMG element. We need to save the image location in XML comment so that
        // it can be reconstructed later on when moving from XHTML to wiki syntax.
        getXHTMLWikiPrinter().printXMLComment("startimage:" + this.imageRenderer.renderImage(image));
        getXHTMLWikiPrinter().printXMLElement("img", attributes);
        getXHTMLWikiPrinter().printXMLComment("stopimage");
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#beginError(String, String)
     * @since 1.7M3
     */
    public void beginError(String message, String description)
    {
        getXHTMLWikiPrinter().printXMLStartElement("span", new String[][] {{"class", "xwikirenderingerror"}});
        getXHTMLWikiPrinter().printXML(message);
        getXHTMLWikiPrinter().printXMLComment("errordescription:" + description);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.Listener#endError(String, String)
     * @since 1.7M3
     */
    public void endError(String message, String description)
    {
        getXHTMLWikiPrinter().printXMLEndElement("span");
    }
}
