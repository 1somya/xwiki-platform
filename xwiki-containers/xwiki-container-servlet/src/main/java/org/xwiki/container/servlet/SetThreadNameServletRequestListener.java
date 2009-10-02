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

package org.xwiki.container.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Make threads names created by the application server more meaningful.
 * 
 * @todo When it will be possible it would be better to do this a component like a RequestInitializer component to work
 *       for any kind of container. Right now component can't really access the initial URL.
 * @version $Id$
 * @since 2.0M3
 */
public class SetThreadNameServletRequestListener implements ServletRequestListener
{
    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent sre)
    {
        ServletRequest servletRequest = sre.getServletRequest();

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            String threadName = httpServletRequest.getRequestURL().toString();

            if (httpServletRequest.getQueryString() != null) {
                threadName += "?" + httpServletRequest.getQueryString();
            }

            Thread.currentThread().setName(threadName);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent sre)
    {
        // Nothing to do here
    }
}
