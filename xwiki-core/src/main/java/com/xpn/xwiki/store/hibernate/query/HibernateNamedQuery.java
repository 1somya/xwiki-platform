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
package com.xpn.xwiki.store.hibernate.query;

import org.hibernate.Query;
import org.hibernate.Session;


/**
 * Query implementation for named queries in Hibernate Store.
 * @version $Id$
 * @since 1.6M1
 */
public class HibernateNamedQuery extends HqlQuery
{
    /**
     * Hint for this component.
     */
    public static String hint = "hibernateNamed";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Query createQuery(Session session)
    {
        return session.getNamedQuery(getStatement());
    }
}
