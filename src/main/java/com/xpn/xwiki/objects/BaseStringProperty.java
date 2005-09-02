/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
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
 *
 * Created by
 * User: Ludovic Dubost
 * Date: 9 d�c. 2003
 * Time: 13:51:00
 */
package com.xpn.xwiki.objects;



public class BaseStringProperty extends BaseProperty {
    private String value;


    public BaseStringProperty() {
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = (String)value;
    }

    public String toText() {
        String value = (String)getValue();
        if (value!=null)
         return value;
        else
         return "";
    }

    public boolean equals(Object obj) {
        if (!super.equals(obj))
         return false;

       if ((getValue()==null)
            && (((BaseStringProperty)obj).getValue()==null))
         return true;

       return getValue().equals(((BaseStringProperty)obj).getValue());
    }

    public Object clone() {
        BaseStringProperty property = (BaseStringProperty) super.clone();
        property.setValue(getValue());
        return property;
    }
}
