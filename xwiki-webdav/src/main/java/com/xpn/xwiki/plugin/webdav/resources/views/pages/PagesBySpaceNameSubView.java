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
package com.xpn.xwiki.plugin.webdav.resources.views.pages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.webdav.resources.XWikiDavResource;
import com.xpn.xwiki.plugin.webdav.resources.domain.DavPage;
import com.xpn.xwiki.plugin.webdav.resources.domain.DavTempFile;
import com.xpn.xwiki.plugin.webdav.resources.partial.AbstractDavView;
import com.xpn.xwiki.plugin.webdav.utils.XWikiDavUtils;

/**
 * This view groups all pages according to their space name.
 * 
 * @version $Id$
 */
public class PagesBySpaceNameSubView extends AbstractDavView
{
    /**
     * Logger instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PagesBySpaceNameSubView.class);

    /**
     * {@inheritDoc}
     */
    public void decode(Stack<XWikiDavResource> stack, String[] tokens, int next)
        throws DavException
    {
        if (next < tokens.length) {
            String nextToken = tokens[next];
            if (isTempResource(nextToken)) {
                super.decode(stack, tokens, next);
            } else if (nextToken.startsWith(XWikiDavUtils.VIRTUAL_DIRECTORY_PREFIX)
                && nextToken.endsWith(XWikiDavUtils.VIRTUAL_DIRECTORY_POSTFIX)) {
                PagesByFirstLettersSubView subView = new PagesByFirstLettersSubView();
                subView.init(this, nextToken.toUpperCase(), "/" + nextToken.toUpperCase());
                stack.push(subView);
                subView.decode(stack, tokens, next + 1);
            } else if (getContext().isCreateCollectionRequest() || getContext().exists(nextToken)) {
                DavPage page = new DavPage();
                page.init(this, this.name + "." + nextToken, "/" + nextToken);
                stack.push(page);
                page.decode(stack, tokens, next + 1);
            } else {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists()
    {
        try {
            List<String> spaces = getContext().getSpaces();
            if (spaces.contains(name)) {
                return true;
            }
        } catch (DavException ex) {
            LOG.error("Unexpected Error : ", ex);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public DavResourceIterator getMembers()
    {
        List<DavResource> children = new ArrayList<DavResource>();
        try {
            String sql = "where doc.web='" + this.name + "'";
            List<String> docNames = getContext().searchDocumentsNames(sql);
            Set<String> subViewNames = new HashSet<String>();
            int subViewNameLength = XWikiDavUtils.getSubViewNameLength(docNames.size());
            for (String docName : docNames) {
                if (getContext().hasAccess("view", docName)) {
                    int dot = docName.lastIndexOf('.');
                    String pageName = docName.substring(dot + 1);
                    if (subViewNameLength < pageName.length()) {
                        subViewNames.add(pageName.substring(0, subViewNameLength).toUpperCase());
                    } else {
                        // This is not good.
                        subViewNames.add(pageName.toUpperCase());
                    }
                }
            }
            for (String subViewName : subViewNames) {
                try {
                    String modName =
                        XWikiDavUtils.VIRTUAL_DIRECTORY_PREFIX + subViewName
                            + XWikiDavUtils.VIRTUAL_DIRECTORY_POSTFIX;
                    PagesByFirstLettersSubView subView = new PagesByFirstLettersSubView();
                    subView.init(this, modName, "/" + modName);
                    children.add(subView);
                } catch (DavException e) {
                    LOG.error("Unexpected Error : ", e);
                }
            }
        } catch (DavException ex) {
            LOG.error("Unexpected Error : ", ex);
        }
        // In-memory resources.
        for (DavResource sessionResource : getVirtualMembers()) {
            children.add(sessionResource);
        }
        return new DavResourceIteratorImpl(children);
    }

    /**
     * {@inheritDoc}
     */
    public void addMember(DavResource resource, InputContext inputContext) throws DavException
    {
        if (resource instanceof DavTempFile) {
            addTempResource((DavTempFile) resource, inputContext);
        } else if (resource instanceof DavPage) {
            String pName = ((DavPage) resource).getDisplayName();
            if (getContext().hasAccess("edit", pName)) {
                XWikiDocument childDoc = getContext().getDocument(pName);
                childDoc.setContent("This page was created thorugh xwiki-webdav interface.");
                getContext().saveDocument(childDoc);
            }
        } else {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeMember(DavResource member) throws DavException
    {
        if (member instanceof DavTempFile) {
            removeTempResource((DavTempFile) member);
        } else if (member instanceof DavPage) {
            String pName = ((DavPage) member).getDisplayName();
            getContext().checkAccess("delete", pName);
            XWikiDocument childDoc = getContext().getDocument(pName);
            if (!childDoc.isNew()) {
                getContext().deleteDocument(childDoc);
            }
        } else {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void move(DavResource destination) throws DavException
    {
        // We only support rename operation for the moment.
        if (destination instanceof PagesBySpaceNameSubView) {
            PagesBySpaceNameSubView dSpace = (PagesBySpaceNameSubView) destination;
            if (!dSpace.exists()) {
                // Now check whether this is a rename operation.
                if (getCollection().equals(dSpace.getCollection())) {
                    String sql = "where doc.web='" + this.name + "'";
                    List<String> docNames = getContext().searchDocumentsNames(sql);
                    // To rename an entire space, user should have edit rights on all the
                    // documents in the current space and delete rights on all the documents that
                    // will be replaced (if they exist).
                    for (String docName : docNames) {
                        String newDocName = dSpace.getDisplayName() + "." + docName;
                        getContext().checkAccess("edit", docName);
                        getContext().checkAccess("overwrite", newDocName);
                    }
                    for (String docName : docNames) {
                        XWikiDocument doc = getContext().getDocument(docName);
                        String newDocName = dSpace.getDisplayName() + "." + doc.getName();
                        getContext().renameDocument(doc, newDocName);
                    }
                } else {
                    // Actual moves (perhaps from one view to another) is not
                    // allowed.
                    throw new DavException(DavServletResponse.SC_BAD_REQUEST);
                }
            } else {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST);
            }
        } else {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
    }
}
