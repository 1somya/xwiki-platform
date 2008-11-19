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
package com.xpn.xwiki.render;

import javax.script.ScriptContext;

import org.xwiki.context.Execution;
import org.xwiki.rendering.parser.SyntaxFactory;
import org.xwiki.script.ScriptContextInitializer;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Context;
import com.xpn.xwiki.api.XWiki;
import com.xpn.xwiki.web.Utils;

public class XWikiScriptContextInitializer implements ScriptContextInitializer
{
    private Execution execution;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.script.ScriptContextInitializer#initialize(javax.script.ScriptContext)
     */
    public void initialize(ScriptContext scriptContext)
    {
        // Bridge. To be removed later.
        if (scriptContext.getAttribute("util") == null) {
            XWikiContext xcontext = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");

            // Put the Util API in the Script context.
            scriptContext.setAttribute("util", new com.xpn.xwiki.api.Util(xcontext.getWiki(), xcontext),
                ScriptContext.GLOBAL_SCOPE);

            // We put the com.xpn.xwiki.api.XWiki object into the context and not the com.xpn.xwiki.XWiki one which is
            // for internal use only. In this manner we control what the user can access.
            scriptContext.setAttribute("xwiki", new XWiki(xcontext.getWiki(), xcontext), ScriptContext.GLOBAL_SCOPE);

            scriptContext.setAttribute("request", xcontext.getRequest(), ScriptContext.GLOBAL_SCOPE);
            scriptContext.setAttribute("response", xcontext.getResponse(), ScriptContext.GLOBAL_SCOPE);

            // We put the com.xpn.xwiki.api.Context object into the context and not the com.xpn.xwiki.XWikiContext one
            // which is for internal use only. In this manner we control what the user can access.
            scriptContext.setAttribute("context", new Context(xcontext), ScriptContext.GLOBAL_SCOPE);

            // Make the Syntax Factory component available from Script.
            // TODO: We need to decide how we want to expose components in general and how to protect users from
            // "dangerous" apis.
            scriptContext.setAttribute("syntaxFactory", Utils.getComponent(SyntaxFactory.ROLE),
                ScriptContext.GLOBAL_SCOPE);

            // Ugly hack. The MessageTool object is created in xwiki.prepareResources(). It's also put in the
            // Script context there. However if we create a new Script context we need to populate it with
            // the message tool. This needs to be refactored to be made clean.
            Object msg = xcontext.get("msg");
            if (msg != null) {
                if (scriptContext.getAttribute("msg") == null) {
                    scriptContext.setAttribute("msg", msg, ScriptContext.GLOBAL_SCOPE);
                }
            }

            // Save the Script Context in the XWiki context so that users can access the objects we've put in it
            // (xwiki, request, response, etc).
            xcontext.put("scriptContext", scriptContext);
        }
    }
}
