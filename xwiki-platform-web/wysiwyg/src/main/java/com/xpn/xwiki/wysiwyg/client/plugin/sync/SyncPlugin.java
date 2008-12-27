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
package com.xpn.xwiki.wysiwyg.client.plugin.sync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.NodeList;
import com.xpn.xwiki.wysiwyg.client.Wysiwyg;
import com.xpn.xwiki.wysiwyg.client.WysiwygService;
import com.xpn.xwiki.wysiwyg.client.dom.Range;
import com.xpn.xwiki.wysiwyg.client.dom.RangeFactory;
import com.xpn.xwiki.wysiwyg.client.dom.Document;
import com.xpn.xwiki.wysiwyg.client.dom.Selection;
import com.xpn.xwiki.wysiwyg.client.diff.Diff;
import com.xpn.xwiki.wysiwyg.client.diff.DifferentiationFailedException;
import com.xpn.xwiki.wysiwyg.client.diff.PatchFailedException;
import com.xpn.xwiki.wysiwyg.client.diff.Revision;
import com.xpn.xwiki.wysiwyg.client.diff.ToString;
import com.xpn.xwiki.wysiwyg.client.editor.Images;
import com.xpn.xwiki.wysiwyg.client.editor.Strings;
import com.xpn.xwiki.wysiwyg.client.editor.WysiwygEditorDebugger;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.AbstractPlugin;
import com.xpn.xwiki.wysiwyg.client.plugin.internal.FocusWidgetUIExtension;
import com.xpn.xwiki.wysiwyg.client.sync.SyncResult;
import com.xpn.xwiki.wysiwyg.client.sync.SyncTools;
import com.xpn.xwiki.wysiwyg.client.util.Config;
import com.xpn.xwiki.wysiwyg.client.util.Timer;
import com.xpn.xwiki.wysiwyg.client.util.TimerListener;
import com.xpn.xwiki.wysiwyg.client.widget.rta.RichTextArea;
import com.xpn.xwiki.gwt.api.client.dialog.MessageDialog;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;

import com.google.gwt.user.client.Random;

public class SyncPlugin extends AbstractPlugin implements ClickListener, TimerListener, AsyncCallback<SyncResult>
{
    public static final int DEFAULT_SYNC_DELAY = 3000;

    private PushButton sync;

    private Timer timer;

    private final FocusWidgetUIExtension toolBarExtension = new FocusWidgetUIExtension("toolbar");

    private String pageName;

    private int version = 0;

    private String initialContent;

    private String syncedContent;

    private Revision syncedRevision;

    private boolean syncInProgress = false;

    private int id;

    private boolean sendCursor = false;

    private boolean maintainCursor = true;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#init(Wysiwyg, RichTextArea, Config)
     */
    public void init(Wysiwyg wysiwyg, RichTextArea textArea, Config config)
    {
        super.init(wysiwyg, textArea, config);

        // init the plugin id
        id = Math.abs(Random.nextInt());

        pageName = config.getParameter("syncPage");
        if (pageName == null) {
            return;
        }

        sync = new PushButton(Images.INSTANCE.sync().createImage(), this);
        sync.setTitle(Strings.INSTANCE.sync());

        toolBarExtension.addFeature("sync", sync);
        getUIExtensionList().add(toolBarExtension);

        initialContent = getTextArea().getHTML();
        if (initialContent == null) {
            initialContent = "";
        }

        timer = new Timer();
        timer.addTimerListener(this);
        timer.scheduleRepeating(wysiwyg.getParamAsInt("sync_delay", DEFAULT_SYNC_DELAY));
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractPlugin#destroy()
     */
    public void destroy()
    {
        sync.removeFromParent();
        sync.removeClickListener(this);
        sync = null;

        toolBarExtension.clearFeatures();

        timer.removeTimerListener(this);
        timer.cancel();
        timer = null;

        super.destroy();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ClickListener#onClick(Widget)
     */
    public void onClick(Widget sender)
    {
        if (sender == sync) {
            onSync();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see TimerListener#onElapsed(Timer)
     */
    public void onElapsed(Timer sender)
    {
        if (sender == timer) {
            onSync();
        }
    }

    public synchronized void onSync()
    {
        if (syncInProgress) {
            return;
        }
        syncInProgress = true;

        // Compute our revision
        syncedRevision = null;
        if (version!=0) {

            if (sendCursor)
                insertCursor(getTextArea().getDocument());
            syncedContent = getTextArea().getHTML();
            if (sendCursor)
                removeCursor(getTextArea().getDocument());
            if ((version>0) && !initialContent.equals(syncedContent)) {
                try {
                    syncedRevision =
                            Diff.diff(ToString.stringToArray(initialContent), ToString.stringToArray(syncedContent));
                } catch (DifferentiationFailedException e) {
                    showError(e, null);
                }
            }
        } else {
            syncedContent = "";
        }

        // Commit our revision and, at the same time, checkout the latest revision
        try {
            WysiwygService.Singleton.getInstance().syncEditorContent(syncedRevision, pageName, version, this);
        } catch (XWikiGWTException e) {
            showError(e, null);
        }
    }

    private void insertCursor(Document doc) {
        try {
            int color = id - 10 * (int) Math.floor(id / 10);
            // insertCursor(element, id, color);
            SpanElement cursorNode = doc.createSpanElement();
            cursorNode.setId("cursor-" + id);
            cursorNode.setClassName("cursor cursor-" + color);
            cursorNode.setAttribute("style", "background-color: #" + color + ";");
            Range range = doc.getSelection().getRangeAt(0);
            try {
                if (range!=null) {
                    if (range.getStartContainer().equals(doc)
                            &&range.getEndContainer().equals(doc)
                            &&range.getStartOffset()==0
                            &&range.getEndOffset()==0) {
                        debugMessage("Cursor at start.. let's not handle it");
                    } else {
                        debugMessage("Start container: " + range.getStartContainer());
                        debugMessage("Start offset: " + range.getStartOffset());
                        debugMessage("End container: " + range.getEndContainer());
                        debugMessage("End offset: " + range.getEndOffset());
                        range.surroundContents(cursorNode);
                        debugMessage("surrounding range ok");
                    }
                }
            } catch (Exception e) {
                try {
                    debugMessage("Exception: " + e.getMessage());
                    debugMessage("error surrounding range");
                    if (range!=null) {
                        debugMessage("Start container: " + range.getStartContainer());
                        debugMessage("Start offset: " + range.getStartOffset());
                        debugMessage("End container: " + range.getEndContainer());
                        debugMessage("End offset: " + range.getEndOffset());
                        try {
                            debugMessage("Range content: " + range.cloneContents().getInnerHTML());
                        } catch (Exception e3) {
                        }
                    }
                    Selection selection = doc.getSelection();
                    if (selection!=null) {
                        debugMessage("Selection range count: " + selection.getRangeCount());
                    }
                } catch (Exception e2) {
                    debugMessage("Exception: " + e2.getMessage());
                }
            }

        } catch (Exception e) {
            debugMessage("Uncaught exception in insertCursor: " + e.getMessage());
        }

    }

    private void removeCursor(Document doc) {
        try {
            Node cursorNode = null;
            NodeList list = doc.getElementsByTagName("span");
            for (int i=0;i<list.getLength();i++) {
                Element element = (Element) list.getItem(i);
                if (element.getId().equals("cursor-" + id))
                    cursorNode = element;
            }
            if (cursorNode!=null) {
                debugMessage("found cursor element");
                Node firstNode = null;
                Node lastNode = null;
                Node pNode = cursorNode;
                NodeList childs = cursorNode.getChildNodes();
                // readd all childs of the cursor to the left of the cursor node
                int nb = childs.getLength();
                for (int i=nb-1;i>=0;i--) {
                    Node node = childs.getItem(i);
                    if (i==0)
                        firstNode = node;
                    else if (i==nb-1)
                        lastNode = node;
                    // we want to insert the node in it's parent before the cursor Node
                    cursorNode.removeChild(node);
                    cursorNode.getParentNode().insertBefore(node, pNode);
                    pNode = node;
                }
                // remove the cursor node itself
                Node previousNode = cursorNode.getPreviousSibling();
                debugMessage("removing cursor node");
                cursorNode.getParentNode().removeChild(cursorNode);

                debugMessage("creating new range");
                Range range = RangeFactory.INSTANCE.createRange(doc);

                if (firstNode!=null) {
                    debugMessage("set range with first node and last node");
                    range.setStartBefore(firstNode);
                    range.setEndAfter(lastNode);
                } else if (previousNode!=null){
                    debugMessage("set range with previous node");
                    Node nextNode = previousNode.getNextSibling();
                    if (nextNode!=null) {
                        range.setStart(nextNode, 0);
                    } else {
                        range.setStartAfter(previousNode);
                        range.setEndAfter(previousNode);
                        range.collapse(true);
                    }
                }

                doc.getSelection().removeAllRanges();
                doc.getSelection().addRange(range);
                getTextArea().setFocus(true);
            } else {
                debugMessage("could not find cursor element");
            }
        } catch (Exception e) {
            debugMessage("Uncaught exception in insertCursor: " + e.getMessage());
        }

    }

    public void debugMessage(String text) {
        WysiwygEditorDebugger debugger = getWysiwyg().getWysiwygEditorDebugger();
        if (debugger!=null)
         debugger.debugMessage(text);
    }


    /**
     * {@inheritDoc}
     * 
     * @see AsyncCallback#onFailure(Throwable)
     */
    public synchronized void onFailure(Throwable caught)
    {
        getWysiwyg().showError(caught);
   }

    /**
     * {@inheritDoc}
     * 
     * @see AsyncCallback#onSuccess(Object)
     */
    public synchronized void onSuccess(SyncResult result)
    {
        // If result is null we have nothing to do
        if (result == null) {
            syncInProgress = false;
            return;
        }

        SyncResult syncResult = result;
        Revision newRevision = syncResult.getRevision();

        try {
            if (newRevision != null) {
                // We don't have the latest version
                // We need to take local changes that might have occured
                if (maintainCursor && (version!=0))
                 insertCursor(getTextArea().getDocument());
                String localContent = getTextArea().getHTML();

                String newHTMLContent =
                    ToString.arrayToString(newRevision.patch(ToString.stringToArray(initialContent)));
                String futureInitialContent = newHTMLContent;

                if ((version!=0) && !localContent.equals(initialContent)) {
                    try {
                        // we need to rework the path to take into account the local content
                        Revision localRevision = Diff.diff(ToString.stringToArray(initialContent), ToString.stringToArray(localContent));
                        Revision localRevision2 = SyncTools.relocateRevision(localRevision, newRevision);
                        newHTMLContent =
                                ToString.arrayToString(localRevision2.patch(ToString.stringToArray(newHTMLContent)));
                    } catch (Exception e) {
                        debugMessage("Exception while applying local revision: " + e.getMessage());
                    }
                }
                initialContent = futureInitialContent;

                // TODO improve by working on an cloned Document that is updated in one call in the textarea
                getTextArea().setHTML(newHTMLContent);
                // we should have retrieved the cursor so we need to remove it
                if ((maintainCursor||sendCursor)&& (version!=0))
                 removeCursor(getTextArea().getDocument());
            } else {
                // We have the latest version
                initialContent = syncedContent;
            }
            version = syncResult.getVersion();
            // normal ending let's reset the syncInProgress
            syncInProgress = false;
        } catch (Throwable e) {
           showError(e, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                syncInProgress = false;
            }

            public void onSuccess(Object o) {
                syncInProgress = false;
            }
           });
        }
    }


    /**
     *
     * @param title
     * @param message
     */
    public void showDialog(String title, String message, AsyncCallback cb) {
        MessageDialog messageDialog = new MessageDialog(getWysiwyg(), title, Dialog.BUTTON_NEXT);
        if (cb!=null)
        messageDialog.setAsyncCallback(cb);
        messageDialog.setMessage(message, new String[0]);

    }
    
    public void showError(Throwable caught, AsyncCallback cb) {
        if (caught instanceof XWikiGWTException) {
            XWikiGWTException exp = ((XWikiGWTException)caught);
            if (exp.getCode()== 9002) {
                // This is a login error
                showDialog(getWysiwyg().getTranslation("appname"), getWysiwyg().getTranslation("login_first"), cb);
            }
            else if (exp.getCode()== 9001) {
                // This is a right error
                showDialog(getWysiwyg().getTranslation("appname"), getWysiwyg().getTranslation("missing_rights"), cb);
            } else
                showError("" + exp.getCode(), exp.getFullMessage(), cb);
        }
        else {
            if (caught!=null)
                caught.printStackTrace();
            showError("", (caught==null) ? "" : caught.toString(), cb);
        }
    }

    public void showError(String text, AsyncCallback cb) {
        showError("", text, cb);
    }

    public void showError(String code, String text, AsyncCallback cb) {
        String[] args = new String[1];
        args[0] = code;
        String message = getWysiwyg().getTranslation("errorwithcode", args) + "\r\n\r\n" + text;
        showDialog(getWysiwyg().getTranslation("appname"), message, cb);
    }

}
