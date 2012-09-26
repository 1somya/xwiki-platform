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
package com.xpn.xwiki.objects.classes;

import org.xwiki.component.annotation.Role;

/**
 * Provide a class that should be initialized at startup and when creating new wiki.
 * <p>
 * The role hint should be the local reference of the document in which the class is supposed to be saved so that I can
 * easily be found.
 * 
 * @version $Id$
 */
@Role
public interface XClassProvider
{
    /**
     * @return the standard class for the current context, null if there is none
     */
    BaseClass getXClass();
}
