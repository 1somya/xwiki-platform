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
 *
 */

package com.xpn.xwiki.user.api;

import java.util.List;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public interface XWikiRightService
{
    /**
     * The Superadmin username.
     */
    public static final String SUPERADMIN_USER = "superadmin";
    
    /**
     * The Superadmin full name.
     */
    public static final String SUPERADMIN_USER_FULLNAME = "XWiki." + SUPERADMIN_USER;
    
    /**
     * The Guest full name.
     */
    public static final String GUEST_USER_FULLNAME = "XWiki.XWikiGuest";
    
    public boolean checkAccess(String action, XWikiDocument doc, XWikiContext context) throws XWikiException;

    public boolean hasAccessLevel(String right, String username, String docname, XWikiContext context)
        throws XWikiException;

    public boolean hasProgrammingRights(XWikiContext context);

    public boolean hasProgrammingRights(XWikiDocument doc, XWikiContext context);

    public boolean hasAdminRights(XWikiContext context);

    public List<String> listAllLevels(XWikiContext context) throws XWikiException;
}
