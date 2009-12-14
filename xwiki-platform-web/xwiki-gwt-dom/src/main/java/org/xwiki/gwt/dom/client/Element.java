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
package org.xwiki.gwt.dom.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

/**
 * Extends the element implementation provided by GWT to add useful methods. All of them should be removed as soon as
 * they make their way into GWT's API.
 * 
 * @version $Id$
 */
public class Element extends com.google.gwt.dom.client.Element
{
    /**
     * The text used in an element's meta data as a place holder for that element's outer HTML.
     */
    public static final String INNER_HTML_PLACEHOLDER = "org.xwiki.gwt.dom.client.Element#placeholder";

    /**
     * The name of the JavaScript property storing the reference to the meta data.<br/>
     * NOTE: We can't use the same name as for {@link #META_DATA_ATTR} because IE stores attribute values as JavaScript
     * properties of DOM element objects.
     */
    public static final String META_DATA_REF = "metaDataRef";

    /**
     * The name of the DOM attribute storing the HTML of the meta data. This HTML is used to recreate the meta data when
     * an element is cloned or copy&pasted.
     */
    public static final String META_DATA_ATTR = "metadata";

    /**
     * The {@code class} attribute is a space-separated list of CSS class names.
     */
    public static final String CLASS_NAME_SEPARATOR = "\\s+";

    /**
     * Default constructor. Needs to be protected because all instances are created from JavaScript.
     */
    protected Element()
    {
        super();
    }

    /**
     * Casts a {@link Node} to an instance of this type.
     * 
     * @param node the instance to be casted to this type.
     * @return the given object as an instance of {@link Element}.
     */
    public static Element as(Node node)
    {
        return (Element) com.google.gwt.dom.client.Element.as(node);
    }

    /**
     * @return The names of DOM attributes present on this element.
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3054
     */
    public final JsArrayString getAttributeNames()
    {
        return DOMUtils.getInstance().getAttributeNames(this);
    }

    /**
     * Returns the value of the specified CSS property for this element as it is computed by the browser before the
     * element is displayed. The CSS property doesn't have to be applied explicitly or directly on this element. It can
     * be inherited or assumed by default on this element.<br/>
     * NOTE: You have to pass the JavaScript name of the property and not its CSS name. The JavaScript name has camel
     * case style ({@code fontWeight}) and it is used like this {@code object.style.propertyJSName = value}. The CSS
     * name has dash style ({@code font-weight}) and it is used like this {@code propertyCSSName: value;}.
     * 
     * @param propertyName the script name of the CSS property whose value is returned.
     * @return the computed value of the specified CSS property for this element.
     */
    public final String getComputedStyleProperty(String propertyName)
    {
        return DOMUtils.getInstance().getComputedStyleProperty(this, propertyName);
    }

    /**
     * Set inner HTML in cross browser manner and notify the owner document.
     * 
     * @param html the html to set.
     * @see {@link DOMUtils#setInnerHTML(Element, String)}
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3146
     */
    public final void xSetInnerHTML(String html)
    {
        DOMUtils.getInstance().setInnerHTML(this, html);
        ((Document) getOwnerDocument()).fireInnerHTMLChange(this);
    }

    /**
     * @return the extended inner HTML of this element, which includes meta data.
     * @see #getInnerHTML()
     */
    public final String xGetInnerHTML()
    {
        return Element.as(cloneNode(true)).expandInnerMetaData().getInnerHTML();
    }

    /**
     * @return the extended outer HTML of this element, which includes meta data.
     * @see #getString()
     */
    public final String xGetString()
    {
        Node result = Element.as(cloneNode(true)).expandMetaData(true);
        return Element.is(result) ? Element.as(result).getString() : DocumentFragment.as(result).getInnerHTML();
    }

    /**
     * Expands inner elements with meta data.
     * 
     * @return this element
     */
    public final Element expandInnerMetaData()
    {
        // Get all the inner elements with meta data.
        NodeList<com.google.gwt.dom.client.Element> elements = getElementsByTagName("*");
        List<Element> elementsWithMetaData = new ArrayList<Element>();
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.getItem(i);
            if (element.hasAttribute(META_DATA_ATTR)) {
                elementsWithMetaData.add(element);
            }
        }
        // Expand meta data. Don't iterate the node list directly because it is live and meta data can contain elements.
        for (Element element : elementsWithMetaData) {
            // Remove the cached reference to the meta data document fragment because it might be shared by clone nodes.
            // We could have cloned the meta data document fragment but this is not reliable with some DOM nodes like
            // embedded objects.
            element.removeProperty(META_DATA_REF);
            element.expandMetaData(false);
        }
        return this;
    }

    /**
     * Expands the meta data of this element and its descendants.
     * 
     * @param deep {@code true} to expand the inner elements with meta data, {@code false} otherwise
     * @return this element if it isn't replaced by its meta data, otherwise the document fragment resulted from
     *         expanding the meta data
     */
    public final Node expandMetaData(boolean deep)
    {
        DocumentFragment metaData = getMetaData();
        if (metaData == null) {
            return deep ? expandInnerMetaData() : this;
        }
        // Remove the meta data from the element.
        setMetaData(null);
        // We have to find the place holder inside the meta data, replace it with this element and then insert the meta
        // data where this element was previously located.
        // Let's find the place holder.
        Iterator<Node> iterator = ((Document) getOwnerDocument()).getIterator(metaData);
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (INNER_HTML_PLACEHOLDER.equals(node.getNodeValue())) {
                // Save the position of this element.
                Node hook = ((Document) getOwnerDocument()).createComment("");
                if (getParentNode() != null) {
                    getParentNode().replaceChild(hook, this);
                }
                // Replace the place holder with this element.
                node.getParentNode().replaceChild(this, node);
                // Insert the meta data at the right location.
                if (hook.getParentNode() != null) {
                    hook.getParentNode().replaceChild(metaData, hook);
                }
                if (deep) {
                    expandInnerMetaData();
                }
                return metaData;
            }
        }
        // We didn't find the place holder so the meta data will just replace this element.
        if (getParentNode() != null) {
            getParentNode().replaceChild(metaData, this);
        }
        return metaData;
    }

    /**
     * Places all the children of this element in a document fragment and returns it.<br/>
     * NOTE: The element will remain empty after this method call.
     * 
     * @return A document fragment containing all the descendants of this element.
     */
    public final DocumentFragment extractContents()
    {
        DocumentFragment contents = ((Document) getOwnerDocument()).createDocumentFragment();
        Node child = getFirstChild();
        while (child != null) {
            contents.appendChild(child);
            child = getFirstChild();
        }
        return contents;
    }

    /**
     * Replaces this element with its child nodes. In other words, all the child nodes of this element are moved to its
     * parent node and the element is removed from its parent.
     */
    public final void unwrap()
    {
        if (getParentNode() == null || getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
            return;
        }
        getParentNode().replaceChild(extractContents(), this);
    }

    /**
     * Wraps the passed node and takes its place in its parent. In other words, it adds the passed element as a child of
     * this element and replaces it in its parent.
     * 
     * @param node the node to wrap
     */
    public final void wrap(Node node)
    {
        if (node.getParentNode() == null) {
            return;
        }
        node.getParentNode().replaceChild(this, node);
        appendChild(node);
    }

    /**
     * @return the meta data associated with this element.
     */
    public final DocumentFragment getMetaData()
    {
        DocumentFragment metaData = (DocumentFragment) ((JavaScriptObject) cast()).get(META_DATA_REF);
        if (metaData == null) {
            // There's no saved reference to the meta data.
            // Test if this element has stored meta data.
            if (hasAttribute(META_DATA_ATTR)) {
                // This element could be the result of node cloning or copy&paste.
                // Let's update the cached meta data reference.
                Element container = Element.as(getOwnerDocument().createDivElement());
                // Set the inner HTML without notifying the listeners to prevent the meta data from being altered.
                DOMUtils.getInstance().setInnerHTML(container, xGetAttribute(META_DATA_ATTR));
                metaData = container.extractContents();
                ((JavaScriptObject) cast()).set(META_DATA_REF, metaData);
            }
        }
        return metaData;
    };

    /**
     * Sets the meta data of this element.
     * 
     * @param metaData a document fragment with additional information regarding this element.
     */
    public final void setMetaData(DocumentFragment metaData)
    {
        if (metaData != null) {
            // Save a reference to the meta data for fast retrieval.
            ((JavaScriptObject) cast()).set(META_DATA_REF, metaData);
            // We have to serialize the meta data and store it using a custom attribute to avoid loosing the meta data
            // over node cloning or copy&paste. The custom attribute used for storing the meta data should be filtered
            // when getting the outer HTML.
            setAttribute(META_DATA_ATTR, metaData.getInnerHTML());
        } else {
            removeProperty(META_DATA_REF);
            removeAttribute(META_DATA_ATTR);
        }
    };

    /**
     * @return {@code true} if HTML Strict DTD specifies that this element can have children, {@code false} otherwise
     */
    public final boolean canHaveChildren()
    {
        return DOMUtils.getInstance().canHaveChildren(this);
    }

    /**
     * Get the value for the specified attribute in cross browser manner.
     * 
     * @param name the name of the attribute
     * @return the value of the attribute
     * @see DOMUtils#getAttribute(Element, String)
     */
    public final String xGetAttribute(String name)
    {
        return DOMUtils.getInstance().getAttribute(this, name);
    }

    /**
     * Sets the value for the specified attribute in a cross browser manner.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    public final void xSetAttribute(String name, String value)
    {
        DOMUtils.getInstance().setAttribute(this, name, value);
    }

    /**
     * We need this method because {@link #getInnerText()} includes commented text in the output.
     * 
     * @return the text between the start and end tags of this element
     * @see #getInnerText()
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3275
     */
    public final String xGetInnerText()
    {
        return DOMUtils.getInstance().getInnerText(this);
    }

    /**
     * @param attrName a string representing the name of an attribute
     * @return true is this element has an attribute with the specified name, false otherwise
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=2852
     */
    public final boolean hasAttribute(String attrName)
    {
        return DOMUtils.getInstance().hasAttribute(this, attrName);
    }

    /**
     * @return {@code true} if this element has any attribute, {@code false} otherwise
     */
    public final boolean hasAttributes()
    {
        return DOMUtils.getInstance().hasAttributes(this);
    }

    /**
     * @param className a {@link String} representing the CSS class to look for
     * @return {@code true} if this element's {@code class} attribute contains the given class name, {@code false}
     *         otherwise
     */
    public final boolean hasClassName(String className)
    {
        if (className == null) {
            return false;
        }
        String trimmedClassName = className.trim();
        String[] classNames = getClassName().split(CLASS_NAME_SEPARATOR);
        for (int i = 0; i < classNames.length; i++) {
            if (classNames[i].equals(trimmedClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new CSS class name to this element's {@code class} attribute. The {@code class} attribute is a
     * space-separated list of CSS class names.
     * 
     * @param className a {@link String} representing the CSS class to be appended to this element's {@code class}
     *            attribute
     * @see #setClassName(String)
     */
    public final void addClassName(String className)
    {
        if (className != null && !hasClassName(className)) {
            String classNames = getClassName().trim();
            setClassName(classNames.length() == 0 ? className.trim() : classNames + " " + className.trim());
        }
    }

    /**
     * Removes a CSS class name form this element's {@code class} attribute. The {@code class} attribute is a
     * space-separated list of CSS class names.
     * 
     * @param className a {@link String} representing the CSS class to be removed from this element's {@code class}
     *            attribute
     * @see #setClassName(String)
     */
    public final void removeClassName(String className)
    {
        if (className == null) {
            return;
        }
        String trimmedClassName = className.trim();
        String[] classNames = getClassName().split(CLASS_NAME_SEPARATOR);
        StringBuffer newClassName = new StringBuffer();
        for (int i = 0; i < classNames.length; i++) {
            if (!classNames[i].equals(trimmedClassName)) {
                newClassName.append(" ");
                newClassName.append(classNames[i]);
            }
        }
        setClassName(newClassName.toString().trim());
    }

    /**
     * Ensures this element can be edited in design mode. This method is required because in some browsers you can't
     * place the caret inside elements that don't have any visible content and thus you cannot edit them.
     */
    public final void ensureEditable()
    {
        DOMUtils domUtils = DOMUtils.getInstance();
        if (domUtils.isInline(this) || getOffsetWidth() == 0) {
            return;
        }

        boolean editable = false;
        Node child = getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                editable = editable || child.getNodeValue().length() > 0;
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) child;
                editable = editable || element.getOffsetWidth() > 0 || domUtils.isOrContainsLineBreak(child);
                element.ensureEditable();
            }
            child = child.getNextSibling();
        }

        if (!editable) {
            domUtils.ensureBlockIsEditable(this);
        }
    }

    /**
     * Removes a property from this element.
     * 
     * @param propertyName the name of the property to be removed
     * @see #setPropertyBoolean(String, boolean)
     * @see #setPropertyDouble(String, double)
     * @see #setPropertyInt(String, int)
     * @see #setPropertyString(String, String)
     */
    public final void removeProperty(String propertyName)
    {
        DOMUtils.getInstance().removeProperty(this, propertyName);
    }
}
