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
package org.xwiki.gwt.user.client.ui.rta.internal;

import org.xwiki.gwt.dom.client.Element;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;

import com.google.gwt.dom.client.IFrameElement;

/**
 * Mozilla-specific implementation of rich-text editing.
 * 
 * @version $Id$
 */
public class RichTextAreaImplMozilla extends com.google.gwt.user.client.ui.impl.RichTextAreaImplMozilla
{
    /**
     * {@inheritDoc}<br/>
     * NOTE: Remove this method as soon as Issue 3156 is fixed. <br />
     * We also need this method to be able to hook simplification of the DOM tree storing meta data in elements.
     * 
     * @see com.google.gwt.user.client.ui.impl.RichTextAreaImplMozilla#setHTMLImpl(String)
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3156
     */
    protected void setHTMLImpl(String html)
    {
        if (elem.getPropertyBoolean(RichTextArea.DIRTY)) {
            elem.setPropertyBoolean(RichTextArea.DIRTY, false);
            ((Element) IFrameElement.as(elem).getContentDocument().getBody().cast()).xSetInnerHTML(html);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.impl.RichTextAreaImplMozilla#initElement()
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=3176
     */
    public native void initElement()
    /*-{
        var iframe = this.@com.google.gwt.user.client.ui.impl.RichTextAreaImpl::elem;
        if (!iframe[@org.xwiki.gwt.user.client.ui.rta.RichTextArea::LOADED]
            || iframe.contentWindow.document.designMode.toLowerCase() == 'on') return;

        iframe.contentWindow.document.designMode = 'on';
        try {
            // It seems that the following line of code fixes the Midas bug which prevents the user to delete any HTML
            // inserted through DOM API before any printable key has been pressed.
            iframe.contentWindow.document.execCommand('undo', false, null);
        } catch(e) {
            // Ignore: execCommand throws an exception if the iframe is hidden through CSS. This can happen when the
            // rich text area is loaded in background.
        }

        var outer = this;
        iframe.contentWindow.onunload = function() {
            iframe.contentWindow.onunload = null;
            iframe[@org.xwiki.gwt.user.client.ui.rta.RichTextArea::LOADED] = false;
            outer.@com.google.gwt.user.client.ui.impl.RichTextAreaImplStandard::uninitElement()()
        }

        this.@com.google.gwt.user.client.ui.impl.RichTextAreaImplStandard::initializing = true;
        this.@com.google.gwt.user.client.ui.impl.RichTextAreaImplStandard::onElementInitialized()();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.impl.RichTextAreaImplMozilla#hookEvents()
     */
    protected void hookEvents()
    {
        // JSNI doesn't support super.*
        // See http://code.google.com/p/google-web-toolkit/issues/detail?id=3507
        super.hookEvents();
        // Double click event is not caught by default.
        // See http://code.google.com/p/google-web-toolkit/issues/detail?id=3944
        hookCustomEvents();
    }

    /**
     * Hooks custom events.
     */
    protected native void hookCustomEvents()
    /*-{
        var elem = this.@com.google.gwt.user.client.ui.impl.RichTextAreaImpl::elem;
        elem.contentWindow.addEventListener('dblclick', elem.__gwt_handler, true);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see com.google.gwt.user.client.ui.impl.RichTextAreaImplMozilla#unhookEvents()
     */
    protected void unhookEvents()
    {
        // Double click event is not caught by default.
        // See http://code.google.com/p/google-web-toolkit/issues/detail?id=3944
        unhookCustomEvents();
        // JSNI doesn't support super.*
        // See http://code.google.com/p/google-web-toolkit/issues/detail?id=3507
        super.unhookEvents();
    }

    /**
     * Unhooks custom events.
     */
    protected native void unhookCustomEvents()
    /*-{
        var elem = this.@com.google.gwt.user.client.ui.impl.RichTextAreaImpl::elem;
        elem.contentWindow.removeEventListener('dblclick', elem.__gwt_handler, true);
    }-*/;
}
