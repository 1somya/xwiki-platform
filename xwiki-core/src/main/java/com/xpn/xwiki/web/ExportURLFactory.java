package com.xpn.xwiki.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.util.Util;

/**
 * Handle URL generation in rendered wiki pages. This implementation makes sure URL will be local URL for exported
 * content (like skin, attachment and pages).
 * 
 * @version $Id$
 */
public class ExportURLFactory extends XWikiServletURLFactory
{
    /**
     * Logging tool.
     */
    protected static final Log LOG = LogFactory.getLog(ExportURLFactory.class);

    /**
     * Pages for which to convert URL to local.
     */
    protected Set<String> exportedPages = new HashSet<String>();

    /**
     * Directory where to export attachment.
     */
    protected File exportDir;

    /**
     * Names of skins needed by rendered page(s).
     */
    private Set<String> neededSkins = new HashSet<String>();

    /**
     * ExportURLFactory constructor.
     */
    public ExportURLFactory()
    {
    }

    /**
     * @return the list skins names used.
     */
    public Collection<String> getNeededSkins()
    {
        return this.neededSkins;
    }

    /**
     * Init the url factory.
     * 
     * @param exportedPages the pages that will be exported.
     * @param exportDir the directory where to copy exported objects (attachments).
     * @param context the XWiki context.
     */
    public void init(Collection<String> exportedPages, File exportDir, XWikiContext context)
    {
        super.init(context);

        if (exportDir != null) {
            this.exportDir = exportDir;
        }

        if (exportedPages != null) {
            XWikiDocument doc = new XWikiDocument();

            for (String pageName : exportedPages) {
                doc.setDatabase(null);
                doc.setSpace(null);
                doc.setName(null);

                doc.setFullName(pageName);

                String absolutePageName = "";

                if (doc.getDatabase() != null) {
                    absolutePageName += doc.getDatabase().toLowerCase();
                } else {
                    absolutePageName += context.getDatabase().toLowerCase();
                }

                absolutePageName += XWikiDocument.DB_SPACE_SEP;

                absolutePageName += doc.getFullName();

                this.exportedPages.add(absolutePageName);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#createSkinURL(java.lang.String, java.lang.String,
     *      com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createSkinURL(String filename, String skin, XWikiContext context)
    {
        try {
            getNeededSkins().add(skin);

            StringBuffer newpath = new StringBuffer();

            newpath.append("file://");

            newpath.append("skins/");
            newpath.append(skin);

            addFileName(newpath, filename, false, context);

            return new URL(newpath.toString());
        } catch (Exception e) {
            LOG.error("Failed to create skin URL", e);
        }

        return super.createSkinURL(filename, skin, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#createSkinURL(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createSkinURL(String filename, String web, String name, String xwikidb, XWikiContext context)
    {
        if (!"skins".equals(web)) {
            return super.createSkinURL(filename, web, name, xwikidb, context);
        }

        try {
            getNeededSkins().add(name);

            StringBuffer newpath = new StringBuffer();

            newpath.append("file://");

            newpath.append("skins/");
            newpath.append(name);

            addFileName(newpath, filename, false, context);

            return new URL(newpath.toString());
        } catch (Exception e) {
            LOG.error("Failed to create skin URL", e);
        }

        return super.createSkinURL(filename, web, name, xwikidb, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#createResourceURL(java.lang.String, boolean,
     *      com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createResourceURL(String filename, boolean forceSkinAction, XWikiContext context)
    {
        try {
            StringBuffer newpath = new StringBuffer();

            newpath.append("file://");

            newpath.append("resources");

            addFileName(newpath, filename, false, context);

            return new URL(newpath.toString());
        } catch (Exception e) {
            LOG.error("Failed to create skin URL", e);
        }

        return super.createResourceURL(filename, forceSkinAction, context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#createURL(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createURL(String web, String name, String action, String querystring, String anchor, String xwikidb,
        XWikiContext context)
    {
        String wikiname = xwikidb == null ? context.getDatabase().toLowerCase() : xwikidb.toLowerCase();

        try {
            if (this.exportedPages.contains(wikiname + XWikiDocument.DB_SPACE_SEP + web + XWikiDocument.SPACE_NAME_SEP
                + name)
                && "view".equals(action) && context.getLinksAction() == null) {
                StringBuffer newpath = new StringBuffer();

                newpath.append("file://");

                newpath.append(wikiname);
                newpath.append(".");
                newpath.append(web);
                newpath.append(".");
                newpath.append(name);

                if ((anchor != null) && (!anchor.equals(""))) {
                    newpath.append("#");
                    newpath.append(anchor);
                }

                newpath.append(".html");

                return new URL(newpath.toString());
            }
        } catch (Exception e) {
            LOG.error("Failed to create page URL", e);
        }

        return super.createURL(web, name, action, querystring, anchor, xwikidb, context);
    }

    /**
     * Generate an url targeting attachment in provided wiki page.
     * 
     * @param filename the name of the attachment.
     * @param space the space of the page containing the attachment.
     * @param name the name of the page containing the attachment.
     * @param xwikidb the wiki of the page containing the attachment.
     * @param context the XWiki context.
     * @return the generated url.
     * @throws XWikiException error when retrieving document attachment.
     * @throws IOException error when retrieving document attachment.
     * @throws URISyntaxException when retrieving document attachment.
     */
    private URL createAttachmentURL(String filename, String space, String name, String xwikidb, XWikiContext context)
        throws XWikiException, IOException, URISyntaxException
    {
        String db = (xwikidb == null ? context.getDatabase() : xwikidb);
        String path = "attachment/" + db + "." + space + "." + name + "." + filename;

        File tempdir = this.exportDir;
        File file = new File(tempdir, path);
        if (!file.exists()) {
            XWikiDocument doc =
                    context.getWiki().getDocument(
                        db + XWikiDocument.DB_SPACE_SEP + space + XWikiDocument.SPACE_NAME_SEP + name, context);
            XWikiAttachment attachment = doc.getAttachment(filename);
            byte[] data = attachment.getContent(context);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        }

        return new URI("file://" + path.replace(" ", "%20")).toURL();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#createAttachmentURL(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createAttachmentURL(String filename, String web, String name, String action, String querystring,
        String xwikidb, XWikiContext context)
    {
        try {
            return createAttachmentURL(filename, web, name, xwikidb, context);
        } catch (Exception e) {
            LOG.error("Failed to create attachement URL", e);

            return super.createAttachmentURL(filename, web, name, action, null, xwikidb, context);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiDefaultURLFactory#createAttachmentRevisionURL(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public URL createAttachmentRevisionURL(String filename, String web, String name, String revision, String xwikidb,
        XWikiContext context)
    {
        try {
            return createAttachmentURL(filename, web, name, xwikidb, context);
        } catch (Exception e) {
            LOG.error("Failed to create attachement URL", e);

            return super.createAttachmentRevisionURL(filename, web, name, revision, xwikidb, context);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.web.XWikiServletURLFactory#getURL(java.net.URL, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public String getURL(URL url, XWikiContext context)
    {
        if (url == null) {
            return "";
        }

        String path = Util.escapeURL(url.toString());

        if (url.getProtocol().equals("file")) {
            path = path.substring("file://".length());
        }

        return path;
    }
}
