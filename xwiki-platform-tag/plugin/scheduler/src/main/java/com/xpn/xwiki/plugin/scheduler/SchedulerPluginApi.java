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
package com.xpn.xwiki.plugin.scheduler;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A Scheduler plugin to plan execution of Jobs from XWiki with cron expressions. The plugin uses
 * Quartz's scheduling library. <p/> Jobs are represented by {@link com.xpn.xwiki.api.Object}
 * XObjects, instances of the {@link SchedulerPlugin#XWIKI_JOB_CLASS} XClass. These XObjects do
 * store a job name, the implementation class name of the job to be executed, the cron expression to
 * precise when the job should be fired, and possibly a groovy script with the job's program. <p/>
 * The plugin offers a {@link GroovyJob} Groovy Job wrapper to execute groovy scripts (typically for
 * use inside the Wiki), but can also be used with any Java class implementing {@link
 * org.quartz.Job}
 *
 * @version $Id: $
 */
public class SchedulerPluginApi extends Api
{
    /**
     * Log object to log messages in this class.
     */
    private static final Log LOG = LogFactory.getLog(SchedulerPluginApi.class);

    /**
     * The instance of the actual implementation class of the plugin
     */
    private SchedulerPlugin plugin;

    public SchedulerPluginApi(SchedulerPlugin plugin, XWikiContext context)
    {
        super(context);
        setPlugin(plugin);
    }

    /**
     * Return the trigger state of the given {@link com.xpn.xwiki.plugin.scheduler.SchedulerPlugin#XWIKI_JOB_CLASS}
     * XObject job. Possible values are : None (the trigger does not exists yet, or has been
     * deleted), Normal, Blocked, Complete, Error and Paused
     *
     * @param object the XObject job to give the state of
     * @return a String representing this state
     */
    public String getStatus(Object object)
    {
        try {
            return getJobStatus(object.getXWikiObject()).getValue();
        } catch (Exception e) {
            context.put("error", e.getMessage());
            return null;
        }
    }

    /**
     * Return the trigger state as a ${@link JobState}, that holds both the integer trigger's inner
     * value of the state and a String as a human readable representation of that state
     */
    public JobState getJobStatus(BaseObject object)
        throws SchedulerException, SchedulerPluginException
    {
        return plugin.getJobStatus(object);
    }

    public JobState getJobStatus(Object object) throws SchedulerException, SchedulerPluginException
    {
        return plugin.getJobStatus(retrieveBaseObject(object));
    }

    /**
     * This function allow to retrieve a com.xpn.xwiki.objects.BaseObject from a
     * com.xpn.xwiki.api.Object without that the current user needs programming rights (as in
     * com.xpn.xwiki.api.Object#getXWikiObject(). The function is used internally by this api class
     * and allows wiki users to call methods from the scheduler without having programming right.
     * The programming right is only needed at script execution time.
     *
     * @return object the unwrapped version of the passed api object
     */
    private BaseObject retrieveBaseObject(Object object) throws SchedulerPluginException
    {
        String docName = object.getName();
        int objNb = object.getNumber();
        try {

            XWikiDocument jobHolder = context.getWiki().getDocument(docName, context);
            BaseObject jobObject = jobHolder.getObject(SchedulerPlugin.XWIKI_JOB_CLASS, objNb);
            return jobObject;
        }
        catch (XWikiException e) {
            throw new SchedulerPluginException(
                SchedulerPluginException.ERROR_SCHEDULERPLUGIN_UNABLE_TO_RETRIEVE_JOB,
                "Job in document [" + docName + "] with object number [" + objNb +
                    "] could not be retrieved.", e);
        }
    }

    /**
     * Schedule the given XObject to be executed according to its parameters. Errors are returned in
     * the context map. Scheduling can be called for example: <code> #if($xwiki.scheduler.scheduleJob($job)!=true)
     * #error($context.get("error") #else #info("Job scheduled") #end </code> Where $job is an
     * XObject, instance of the {@link SchedulerPlugin#XWIKI_JOB_CLASS} XClass
     *
     * @param object the XObject to be scheduled, an instance of the XClass XWiki.SchedulerJobClass
     * @return true on success, false on failure
     */
    public boolean scheduleJob(Object object)
    {
        try {
            return scheduleJob(retrieveBaseObject(object));
        }
        catch (Exception e) {
            // we don't need to push the exception message in the context here
            // as it should already have been pushed by the throwing exception
            return false;
        }
    }

    public boolean scheduleJob(BaseObject object)
    {
        try {
            plugin.scheduleJob(object, context);
            return true;
        } catch (Exception e) {
            context.put("error", e.getMessage());
            return false;
        }
    }

    /**
     * Schedule all {@link com.xpn.xwiki.plugin.scheduler.SchedulerPlugin#XWIKI_JOB_CLASS} XObjects
     * stored inside the given Wiki document, according to each XObject own parameters.
     *
     * @param document the document holding the XObjects Jobs to be scheduled
     * @return true on success, false on failure.
     */
    public boolean scheduleJobs(Document document)
    {
        boolean result = true;
        try {
            XWikiDocument doc = context.getWiki().getDocument(document.getFullName(), context);
            List objects = doc.getObjects(SchedulerPlugin.XWIKI_JOB_CLASS);
            for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
                Object object = (Object) iterator.next();
                result &= scheduleJob(object);
            }
        }
        catch (Exception e) {
            context.put("error", e.getMessage());
            return false;
        }
        return result;
    }

    /**
     * Pause the given XObject job by pausing all of its current triggers. Can be called the same
     * way as {@link #scheduleJob}
     *
     * @param object the wrapped XObject Job to be paused
     * @return true on success, false on failure.
     */
    public boolean pauseJob(Object object)
    {
        try {
            return pauseJob(retrieveBaseObject(object));
        }
        catch (Exception e) {
            // we don't need to push the exception message in the context here
            // as it should already have been pushed by the throwing exception
            return false;
        }
    }

    public boolean pauseJob(BaseObject object)
    {
        try {
            plugin.pauseJob(object, context);
            LOG.debug("Pause Job : " + object.getStringValue("jobName"));
            return true;
        } catch (XWikiException e) {
            context.put("error", e.getMessage());
            return false;
        }
    }

    /**
     * Resume a XObject job that is in a {@link JobState#STATE_PAUSED} state. Can be called the same
     * way as {@link #scheduleJob}
     *
     * @param object the wrapped XObject Job to be paused
     * @return true on success, false on failure.
     */
    public boolean resumeJob(Object object)
    {
        try {
            return resumeJob(retrieveBaseObject(object));
        }
        catch (Exception e) {
            // we don't need to push the exception message in the context here
            // as it should already have been pushed by the throwing exception
            return false;
        }
    }

    public boolean resumeJob(BaseObject object)
    {
        try {
            plugin.resumeJob(object, context);
            LOG.debug("Resume Job : " + object.getStringValue("jobName"));
            return true;
        } catch (XWikiException e) {
            context.put("error", e.getMessage());
            return false;
        }
    }

    /**
     * Unschedule a XObject job by deleting it from the jobs table. Can be called the same way as
     * {@link #scheduleJob}
     *
     * @param object the wrapped XObject Job to be paused
     * @return true on success, false on failure.
     */
    public boolean unscheduleJob(Object object)
    {
        try {
            return unscheduleJob(retrieveBaseObject(object));
        }
        catch (Exception e) {
            // we don't need to push the exception message in the context here
            // as it should already have been pushed by the throwing exception
            return false;
        }
    }

    public boolean unscheduleJob(BaseObject object)
    {
        try {
            plugin.unscheduleJob(object, context);
            LOG.debug("Delete Job : " + object.getStringValue("jobName"));
            return true;
        } catch (XWikiException e) {
            context.put("error", e.getMessage());
            return false;
        }
    }

    /**
     * Give, for a XObject job in a {@JobState#STATE_NORMAL} state, the next date at which the job
     * will be executed, according to its cron expression. Errors are returned in the context map.
     * Can be called for example: <code> #set($firetime = $xwiki.scheduler.getNextFireTime($job))
     * #if (!$firetime || $firetime=="") #error($context.get("error") #else #info("Fire time :
     * $firetime") #end </code> Where $job is an XObject, instance of the {@link
     * SchedulerPlugin#XWIKI_JOB_CLASS} XClass
     *
     * @param object the wrapped XObject for which to give the fire date
     * @return the date the job will be executed
     */
    public Date getNextFireTime(Object object)
    {
        try {
            return getNextFireTime(retrieveBaseObject(object));
        }
        catch (Exception e) {
            // we don't need to push the exception message in the context here
            // as it should already have been pushed by the throwing exception
            return null;
        }
    }

    public Date getNextFireTime(BaseObject object)
    {
        try {
            return plugin.getNextFireTime(object);
        }
        catch (SchedulerPluginException e) {
            context.put("error", e.getMessage());
            return null;
        }
    }

    public void setPlugin(SchedulerPlugin plugin)
    {
        this.plugin = plugin;
    }

    public XWikiPluginInterface getPlugin()
    {
        return plugin;
    }
}
