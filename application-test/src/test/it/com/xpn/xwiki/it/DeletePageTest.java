/*
 * Copyright 2007, XpertNet SARL, and individual contributors as indicated
 * by the contributors.txt.
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
package com.xpn.xwiki.it;

import com.xpn.xwiki.it.framework.AbstractXWikiTestCase;
import com.xpn.xwiki.it.framework.AlbatrossSkinExecutor;
import com.xpn.xwiki.it.framework.XWikiTestSuite;
import junit.framework.Test;

/**
 * Verify deletion of pages.
 *
 * @version $Id: $
 */
public class DeletePageTest extends AbstractXWikiTestCase
{
    public static Test suite()
    {
        XWikiTestSuite suite = new XWikiTestSuite("Verify deletion of pages");
        suite.addTestSuite(DeletePageTest.class, AlbatrossSkinExecutor.class);
        return suite;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();

        open("/xwiki/bin/edit/Test/DeleteTest?editor=wiki");
        setFieldValue("content", "some content");
        clickEditSaveAndView();
    }

    public void testDeleteOkWhenConfirming()
    {
        clickDeletePage();
        clickLinkWithLocator("//input[@value='yes']");

        assertTextPresent("The document has been deleted.");
    }

    /**
     * Verify that we can skip the delete result page if we pass a xredirect parameter to a page
     * we want to be redirected to. Note that the confirm=1 parameter is also required as
     * otherwise the redirect will have no effect. This is possibly a bug.
     */
    public void testDeletePageCanSkipConfirmationAndDoARedirect()
    {
        open("/xwiki/bin/delete/Test/DeleteTest?confirm=1&xredirect=/xwiki/bin/view/Main/");
        assertPage("Main", "WebHome");
    }
}
