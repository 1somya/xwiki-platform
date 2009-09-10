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
package com.xpn.xwiki.plugin.watchlist;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.scheduler.SchedulerPlugin;

/**
 * Manager for WatchList jobs.
 * 
 * @version $Id$
 */
public class WatchListJobManager
{
    /**
     * WatchList Job class.
     */
    public static final String WATCHLIST_JOB_CLASS = "XWiki.WatchListJobClass";

    /**
     * WatchList Job email template property name.
     */
    public static final String WATCHLIST_JOB_EMAIL_PROP = "template";

    /**
     * WatchList Job last fire time property name.
     */
    public static final String WATCHLIST_JOB_LAST_FIRE_TIME_PROP = "last_fire_time";

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(WatchListJobManager.class);

    /**
     * XWiki Rights class name.
     */
    private static final String XWIKI_RIGHTS_CLASS = "XWiki.XWikiRights";

    /**
     * Default XWiki Administrator.
     */
    private static final String DEFAULT_ADMIN = "XWiki.Admin";

    /**
     * Set watchlist common documents fields.
     * 
     * @param doc document used for this job.
     * @return true if the fields have been modified, false otherwise
     */
    private boolean setWatchListCommonDocumentsFields(XWikiDocument doc)
    {
        boolean needsUpdate = false;

        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor(DEFAULT_ADMIN);
        }

        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator(DEFAULT_ADMIN);
        }

        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.WatchListClass");
        }

        return needsUpdate;
    }

    /**
     * Create or update the watchlist job class properties.
     * 
     * @param watchListJobClass document in which the class must be created
     * @param context the XWiki context
     * @return true if the class properties have been created or modified
     */
    private boolean initWatchListJobClassProperties(XWikiDocument watchListJobClass, XWikiContext context)
    {
        boolean needsUpdate = false;
        BaseClass bclass = watchListJobClass.getxWikiClass();

        bclass.setName(WATCHLIST_JOB_CLASS);
        needsUpdate |= bclass.addTextField(WATCHLIST_JOB_EMAIL_PROP, "Email template to use", 30);
        needsUpdate |=
            bclass.addDateField(WATCHLIST_JOB_LAST_FIRE_TIME_PROP, "Last notifier fire time", "dd/MM/yyyy HH:mm:ss", 1);

        return needsUpdate;
    }

    /**
     * Creates the WatchList xwiki class.
     * 
     * @param context Context of the request
     * @throws XWikiException if class fields cannot be created
     */
    private void initWatchListJobClass(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        boolean needsUpdate = false;

        try {
            doc = context.getWiki().getDocument(WATCHLIST_JOB_CLASS, context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            String[] spaceAndName = WATCHLIST_JOB_CLASS.split(".");
            doc.setSpace(spaceAndName[0]);
            doc.setName(spaceAndName[1]);
            needsUpdate = true;
        }

        needsUpdate = initWatchListJobClassProperties(doc, context);
        needsUpdate = setWatchListCommonDocumentsFields(doc);

        if (StringUtils.isBlank(doc.getContent())) {
            needsUpdate = true;
            doc.setContent("= XWiki Watchlist Notification Job Class =");
            doc.setSyntaxId(XWikiDocument.XWIKI20_SYNTAXID);
        }

        if (needsUpdate) {
            context.getWiki().saveDocument(doc, "", true, context);
        }
    }

    /**
     * Create the watchlist job object in the scheduler job document.
     * 
     * @param doc Scheduler job document
     * @param emailTemplate email template to use for the job
     * @param context the XWiki context
     * @return true if the document has been updated, false otherwise
     * @throws XWikiException if the object creation fails
     */
    private boolean createWatchListJobObject(XWikiDocument doc, String emailTemplate, XWikiContext context) 
        throws XWikiException
    {
        BaseObject obj = null;
        boolean needsupdate = false;

        obj = doc.getObject(WATCHLIST_JOB_CLASS);
        if (obj == null) {
            doc.createNewObject(WATCHLIST_JOB_CLASS, context);
            needsupdate = true;
        }

        obj = doc.getObject(WATCHLIST_JOB_CLASS);

        if (StringUtils.isBlank(obj.getStringValue(WATCHLIST_JOB_EMAIL_PROP))) {
            obj.setStringValue(WATCHLIST_JOB_EMAIL_PROP, emailTemplate);
            needsupdate = true;
        }

        if (obj.getDateValue(WATCHLIST_JOB_LAST_FIRE_TIME_PROP) == null) {
            obj.setDateValue(WATCHLIST_JOB_LAST_FIRE_TIME_PROP, new Date());
            needsupdate = true;
        }

        return needsupdate;
    }

    /**
     * Create the XWiki rights object in the scheduler job document.
     * 
     * @param doc Scheduler job document
     * @param context the XWiki context
     * @return true if the document has been updated, false otherwise
     * @throws XWikiException if the object creation fails
     */
    private boolean createWatchListJobRightsObject(XWikiDocument doc, XWikiContext context) throws XWikiException
    {
        BaseObject rights = doc.getObject(XWIKI_RIGHTS_CLASS);
        if (rights == null) {
            int index = doc.createNewObject(XWIKI_RIGHTS_CLASS, context);
            rights = doc.getObject(XWIKI_RIGHTS_CLASS, index);
            rights.setLargeStringValue("groups", "XWiki.XWikiAdminGroup");
            rights.setStringValue("levels", "edit,delete");
            rights.setIntValue("allow", 1);
            return true;
        }

        return false;
    }

    /**
     * Creates a WatchList job in the XWiki Scheduler application (XWiki Object).
     * 
     * @param docName Name of the document storing the job (example: Scheduler.WatchListDailyNotifier)
     * @param name Job name (example: Watchlist daily notifier)
     * @param nameResource (example: platform.plugin.watchlist.job.daily)
     * @param emailTemplate email template to use for this job (example: XWiki.WatchListMessage)
     * @param cron CRON expression (see quartz CRON expressions)
     * @param context Context of the request
     * @throws XWikiException if the jobs creation fails.
     */
    private void initWatchListJob(String docName, String name, String nameResource, String emailTemplate, String cron,
        XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        boolean needsUpdate = false;
        BaseObject job = null;

        try {
            doc = context.getWiki().getDocument(docName, context);

            job = doc.getObject(SchedulerPlugin.XWIKI_JOB_CLASS);
            if (job == null) {
                needsUpdate = true;
                int index = doc.createNewObject(SchedulerPlugin.XWIKI_JOB_CLASS, context);
                job = doc.getObject(SchedulerPlugin.XWIKI_JOB_CLASS, index);
                job.setStringValue("jobName", name);
                job.setStringValue("jobClass", WatchListJob.class.getName());
                job.setStringValue("cron", cron);
                job.setStringValue("contextUser", DEFAULT_ADMIN);
                job.setStringValue("contextLang", "en");
                job.setStringValue("contextDatabase", "xwiki");
            }

            needsUpdate = createWatchListJobRightsObject(doc, context);
            needsUpdate = createWatchListJobObject(doc, emailTemplate, context);
            needsUpdate = setWatchListCommonDocumentsFields(doc);
            
            if (StringUtils.isBlank(doc.getTitle())) {
                needsUpdate = true;
                doc.setTitle("$msg.get('" + nameResource +  "')");                
            }

            if (StringUtils.isBlank(doc.getContent())) {
                needsUpdate = true;
                doc.setContent("{{include document=\"XWiki.SchedulerJobSheet\"/}}");
                doc.setSyntaxId(XWikiDocument.XWIKI20_SYNTAXID);
            }

            if (needsUpdate) {
                context.getWiki().saveDocument(doc, "", true, context);
                ((SchedulerPlugin) context.getWiki().getPlugin("scheduler", context)).scheduleJob(job, context);
            }
        } catch (Exception e) {
            LOG.error("Cannot initialize WatchListJob", e);
        }
    }

    /**
     * Create default WatchList jobs in the wiki.
     * 
     * @param context Context of the request
     * @throws XWikiException When a job creation fails
     */
    public void init(XWikiContext context) throws XWikiException
    {
        initWatchListJobClass(context);
        initWatchListJob("Scheduler.WatchListHourlyNotifier", "WatchList hourly notifier", 
            "watchlist.job.hourly", WatchListNotifier.DEFAULT_EMAIL_TEMPLATE, "0 0 * * * ?", context);
        initWatchListJob("Scheduler.WatchListDailyNotifier", "WatchList daily notifier", 
            "watchlist.job.daily", WatchListNotifier.DEFAULT_EMAIL_TEMPLATE, "0 0 0 * * ?", context);
        initWatchListJob("Scheduler.WatchListWeeklyNotifier", "WatchList weekly notifier", 
            "watchlist.job.weekly", WatchListNotifier.DEFAULT_EMAIL_TEMPLATE, "0 0 0 ? * MON", context);
    }
}
