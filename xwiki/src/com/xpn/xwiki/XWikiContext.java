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
 * Date: 4 d�c. 2003
 * Time: 22:47:17
 */
package com.xpn.xwiki;

import com.xpn.xwiki.util.Util;

import java.util.Hashtable;

public class XWikiContext extends Hashtable {

   private XWiki wiki;

   public XWikiContext(XWiki wiki) {
       this.wiki = wiki;
   }

   public XWiki getWiki() {
       return wiki;
   }

   public Util getUtil() {
       Util util = (Util) this.get("util");
       if (util==null) {
           util = new Util();
           this.put("util", util);
       }
       return util;
    }


}
