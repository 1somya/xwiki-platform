/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details, published at
 * http://www.gnu.org/copyleft/lesser.html or in lesser.txt in the
 * root folder of this distribution.
 *
 * Created by
 * User: Ludovic Dubost
 * Date: 9 d�c. 2003
 * Time: 13:47:20
 */
package com.xpn.xwiki.classes;

import com.xpn.xwiki.XWikiContext;

public class XWikiClassStringProperty extends XWikiClassProperty {

    public XWikiClassStringProperty() {
        setSize(30);
    }

    public int getSize() {
        return ((XWikiObjectNumberProperty)get("size")).getValue().intValue();
    }

    public void setSize(int size) {
        XWikiObjectNumberProperty property = new XWikiObjectNumberProperty();
        property.setValue(new Integer(size));
        put("size", property);
    }

    public XWikiObjectProperty fromString(String value) {
        XWikiObjectStringProperty property = new XWikiObjectStringProperty();
        property.setValue(value);
        property.setPropertyClass(this);
        return property;
    }

    public void displayHidden(StringBuffer buffer, String name, XWikiObject object, XWikiContext context) {
        super.displayHidden(buffer, name, object, context);    //To change body of overriden methods use Options | File Templates.
    }

    public void displaySearch(StringBuffer buffer, String name, XWikiObject object, XWikiContext context) {
        super.displaySearch(buffer, name, object, context);    //To change body of overriden methods use Options | File Templates.
    }

    public void displayView(StringBuffer buffer, String name, XWikiObject object, XWikiContext context) {
        super.displayView(buffer, name, object, context);    //To change body of overriden methods use Options | File Templates.
    }

    public void displayEdit(StringBuffer buffer, String name, XWikiObject object, XWikiContext context) {
        super.displayEdit(buffer, name, object, context);    //To change body of overriden methods use Options | File Templates.
    }
}
