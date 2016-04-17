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
package org.xwiki.model.internal.reference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.text.StringUtils;

/**
 * Generate a string representation of an entity reference (eg "wiki:space.page" for a document reference in the "wiki"
 * Wiki, the "space" Space and the "page" Page).
 * 
 * @version $Id$
 * @since 2.2M1
 */
@Component
@Singleton
public class DefaultStringEntityReferenceSerializer extends AbstractStringEntityReferenceSerializer
{
    @Inject
    private SymbolScheme symbolScheme;

    @Override
    protected void serializeEntityReference(EntityReference currentReference, StringBuilder representation,
        boolean isLastReference, Object... parameters)
    {
        EntityType currentType = currentReference.getType();
        EntityReference parentReference = currentReference.getParent();

        // Since the representation is being built from the root reference (i.e. from left to right), we need to add a
        // separator if some content has already been added to the representation string (i.e. if a higher level entity
        // type has already been processed).
        if (parentReference != null && representation.length() > 0) {
            // Get the separator to use between the previous type and the current type
            Character separator =
                getSymbolScheme().getSeparatorSymbols().get(currentType).get(parentReference.getType());
            if (separator != null) {
                representation.append(separator);
            } else {
                // The reference is invalid, the parent type is not an allowed type. Thus there's no valid separator
                // to separate the 2 types. Use the "???" character to show the user it's invalid.
                representation.append("???");
            }
        }

        // Escape characters that require escaping for the current type
        representation.append(StringUtils.replaceEach(currentReference.getName(),
            getSymbolScheme().getSymbolsRequiringEscapes(currentType),
            getSymbolScheme().getReplacementSymbols(currentType)));
    }

    protected SymbolScheme getSymbolScheme()
    {
        return this.symbolScheme;
    }
}
