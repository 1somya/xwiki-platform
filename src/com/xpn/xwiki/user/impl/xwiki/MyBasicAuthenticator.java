/**
 * ===================================================================
 *
 * Copyright (c) 2003,2004 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details, published at
 * http://www.gnu.org/copyleft/gpl.html or in gpl.txt in the
 * root folder of this distribution.

 * Created by
 * User: Ludovic Dubost
 * Date: 3 f�vr. 2004
 * Time: 23:41:14
 */
package com.xpn.xwiki.user.impl.xwiki;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.securityfilter.authenticator.BasicAuthenticator;
import org.securityfilter.filter.SecurityRequestWrapper;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

public class MyBasicAuthenticator extends BasicAuthenticator {


    public boolean processLogin(SecurityRequestWrapper request, HttpServletResponse response) throws Exception {
        return processLogin(request, response, null);
    }

    public boolean processLogin(SecurityRequestWrapper request, HttpServletResponse response, XWikiContext context) throws Exception {
            // Always verify authentication
            String authorizationHeader = request.getHeader("Authorization");
            HttpSession session = request.getSession();
            if (authorizationHeader != null) {
                String decoded = decodeBasicAuthorizationString(authorizationHeader);
                String username = parseUsername(decoded);
                String password = parsePassword(decoded);

                Principal principal = authenticate(username, password, context);

                if (principal != null) {
                    // login successful
                    request.getSession().removeAttribute(LOGIN_ATTEMPTS);
                    request.setUserPrincipal(principal);
                } else {
                    // login failed
                    // show the basic authentication window again.
                    showLogin(request.getCurrentRequest(), response);
                    return true;
                }
            }
        return false;
    }

    /**
     * Parse the username out of the BASIC authorization header string.
     * @param decoded
     * @return username parsed out of decoded string
     */
    private String parseUsername(String decoded) {
        if (decoded == null) {
            return null;
        } else {
            int colon = decoded.indexOf(':');
            if (colon < 0) {
                return null;
            } else {
                return decoded.substring(0, colon).trim();
            }
        }
    }

    /**
     * Parse the password out of the decoded BASIC authorization header string.
     * @param decoded
     * @return password parsed out of decoded string
     */
    private String parsePassword(String decoded) {
        if (decoded == null) {
            return null;
        } else {
            int colon = decoded.indexOf(':');
            if (colon < 0) {
                return (null);
            } else {
                return decoded.substring(colon + 1).trim();
            }
        }
    }

    /**
     * Decode the BASIC authorization string.
     *
     * @param authorization
     * @return decoded string
     */
    private String decodeBasicAuthorizationString(String authorization) {
        if (authorization == null || !authorization.toLowerCase().startsWith("basic ")) {
            return null;
        } else {
            authorization = authorization.substring(6).trim();
            // Decode and parse the authorization credentials
            return new String(base64Helper.decodeBase64(authorization.getBytes()));
        }
    }

    protected Principal authenticate(String username, String password, XWikiContext context) throws XWikiException {
        return context.getWiki().getAuthService().authenticate(username, password, context);
    }
}
