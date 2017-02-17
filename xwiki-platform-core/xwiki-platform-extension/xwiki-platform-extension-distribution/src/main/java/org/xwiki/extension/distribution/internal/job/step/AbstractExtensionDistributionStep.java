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
package org.xwiki.extension.distribution.internal.job.step;

import javax.inject.Inject;

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.internal.validator.AbstractExtensionValidator;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.internal.InstallJob;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;

import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * @version $Id$
 * @since 9.1RC1
 */
public abstract class AbstractExtensionDistributionStep extends AbstractDistributionStep
{
    /**
     * Used to manipulate jobs.
     */
    @Inject
    protected JobExecutor jobExecutor;

    /**
     * @param stepId the identifier of the step
     */
    public AbstractExtensionDistributionStep(String stepId)
    {
        super(stepId);
    }

    protected void install(ExtensionId extensionId) throws JobException, InterruptedException
    {
        install(extensionId, getNamespace().toString());
    }

    protected void install(ExtensionId extensionId, String namespace) throws JobException, InterruptedException
    {
        // Install the default UI
        InstallRequest installRequest = new InstallRequest();
        installRequest
            .setId(ExtensionRequest.getJobId(ExtensionRequest.JOBID_ACTION_PREFIX, extensionId.getId(), namespace));
        installRequest.addExtension(extensionId);
        installRequest.addNamespace(namespace);

        // Indicate if it's allowed to do modification on root namespace
        installRequest.setRootModificationsAllowed(true);

        installRequest.setInteractive(false);

        installRequest.setExtensionProperty(AbstractExtensionValidator.PROPERTY_USERREFERENCE,
            XWikiRightService.SUPERADMIN_USER_FULLNAME);

        this.jobExecutor.execute(InstallJob.JOBTYPE, installRequest).join();
    }
}
