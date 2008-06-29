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
package com.xpn.xwiki.plugin.mailsender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.render.XWikiVelocityRenderer;

/**
 * Plugin that brings powerful mailing capabilities.
 * 
 * @see MailSender
 * @version $Id$
 */
public class MailSenderPlugin extends XWikiDefaultPlugin
{
    /**
     * Log object to log messages in this class.
     */
    private static final Log LOG = LogFactory.getLog(MailSenderPlugin.class);

    public static int ERROR_TEMPLATE_EMAIL_OBJECT_NOT_FOUND = -2;

    public static int ERROR = -1;

    public static final String EMAIL_XWIKI_CLASS_NAME = "XWiki.Mail";

    public static final String ID = "mailsender";

    protected static final String URL_SEPARATOR = "/";

    /**
     * {@inheritDoc}
     * 
     * @see XWikiDefaultPlugin#XWikiDefaultPlugin(String,String,com.xpn.xwiki.XWikiContext)
     */
    public MailSenderPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
        init(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#init(XWikiContext)
     */
    public void init(XWikiContext context)
    {
        try {
            initMailClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#virtualInit(XWikiContext)
     */
    public void virtualInit(XWikiContext context)
    {
        try {
            initMailClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#getPluginApi(XWikiPluginInterface, XWikiContext)
     */
    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new MailSenderPluginApi((MailSenderPlugin) plugin, context);
    }

    /**
     * Split comma separated list of emails
     * 
     * @param email comma separated list of emails
     * @return An array containing the emails
     */
    public static String[] parseAddresses(String email)
    {
        if (email == null) {
            return null;
        }
        email = email.trim();
        String[] emails = email.split(",");
        for (int i = 0; i < emails.length; i++) {
            emails[i] = emails[i].trim();
        }
        return emails;
    }

    /**
     * Filters a list of emails : removes illegal addresses
     * 
     * @param email List of emails
     * @return An Array containing the correct adresses
     */
    private static InternetAddress[] toInternetAddresses(String email) throws AddressException
    {
        String[] mails = parseAddresses(email);
        if (mails == null) {
            return null;
        }

        InternetAddress[] address = new InternetAddress[mails.length];
        for (int i = 0; i < mails.length; i++) {
            address[i] = new InternetAddress(mails[i]);
        }
        return address;
    }

    /**
     * Creates the Mail XWiki Class
     * 
     * @param context Context of the request
     * @return the Mail XWiki Class
     */
    protected BaseClass initMailClass(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;

        try {
            doc = xwiki.getDocument(EMAIL_XWIKI_CLASS_NAME, context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            String[] spaceAndName = EMAIL_XWIKI_CLASS_NAME.split(".");
            doc.setSpace(spaceAndName[0]);
            doc.setName(spaceAndName[1]);
            needsUpdate = true;
        }

        BaseClass bclass = doc.getxWikiClass();
        bclass.setName(EMAIL_XWIKI_CLASS_NAME);
        needsUpdate |= bclass.addTextField("subject", "Subject", 40);
        needsUpdate |= bclass.addTextField("language", "Language", 5);
        needsUpdate |= bclass.addTextAreaField("text", "Text", 80, 15);
        needsUpdate |= bclass.addTextAreaField("html", "HTML", 80, 15);

        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.XWikiClasses");
        }
        if (StringUtils.isBlank(doc.getContent())) {
            needsUpdate = true;
            doc.setContent("#includeForm(\"XWiki.XWikiMailSheet\"");
        }

        if (needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
        return bclass;
    }

    /**
     * Add attachments to a multipart message
     * 
     * @param mpart Multipart message
     * @param attachments List of attachments
     */
    public void addAttachments(Multipart mpart, List attachments, XWikiContext context)
        throws XWikiException, IOException, MessagingException
    {
        if (attachments != null) {
            Iterator attachmentIt = attachments.iterator();
            while (attachmentIt.hasNext()) {
                Attachment at = (Attachment) attachmentIt.next();
                XWikiAttachment att = at.getAttachment();
                String name = att.getFilename();
                byte[] stream = att.getContent(context);
                File temp = File.createTempFile("tmpfile", ".tmp");
                FileOutputStream fos = new FileOutputStream(temp);
                fos.write(stream);
                fos.close();
                MimeBodyPart part = new MimeBodyPart();
                DataSource source = new FileDataSource(temp);
                part.setDataHandler(new DataHandler(source));
                part.setHeader("Content-ID", "<" + name + ">");
                part.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
                String mimeType = MimeTypesUtil.getMimeTypeFromFilename(name);
                part.setHeader("Content-Type", " " + mimeType + "; name=\"" + name + "\"");

                part.setFileName(name);
                mpart.addBodyPart(part);
                temp.deleteOnExit();
            }
        }
    }

    /**
     * Creates a MIME message (message with binary content carrying capabilities) from an existing
     * Mail
     * 
     * @param mail The original Mail object
     * @param session Mail session
     * @return The MIME message
     */
    private MimeMessage createMimeMessage(Mail mail, Session session, XWikiContext context)
        throws MessagingException, XWikiException, IOException
    {
        // this will also check for email error
        InternetAddress from = new InternetAddress(mail.getFrom());
        InternetAddress[] to = toInternetAddresses(mail.getTo());
        InternetAddress[] cc = toInternetAddresses(mail.getCc());
        InternetAddress[] bcc = toInternetAddresses(mail.getBcc());

        if ((to == null) && (cc == null) && (bcc == null)) {
            LOG.info("No recipient -> skipping this email");
            return null;
        }

        MimeMessage message = new MimeMessage(session);
        message.setSentDate(new Date());
        message.setFrom(from);

        if (to != null) {
            message.setRecipients(javax.mail.Message.RecipientType.TO, to);
        }

        if (cc != null) {
            message.setRecipients(javax.mail.Message.RecipientType.CC, cc);
        }

        if (bcc != null) {
            message.setRecipients(javax.mail.Message.RecipientType.BCC, bcc);
        }

        message.setSubject(mail.getSubject(), "UTF-8");

        for (Map.Entry<String, String> header : mail.getHeaders().entrySet()) {
            message.setHeader(header.getKey(), header.getValue());
        }

        if (mail.getHtmlPart() != null || mail.getAttachments() != null) {
            Multipart multipart = createMimeMultipart(mail, context);
            message.setContent(multipart);
        } else {
            message.setText(mail.getTextPart());
        }

        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    /**
     * Creates a Multipart MIME Message (multiple content-types within the same message) from an
     * existing mail
     * 
     * @param mail The original Mail
     * @return The Multipart MIME message
     */
    public Multipart createMimeMultipart(Mail mail, XWikiContext context)
        throws MessagingException, XWikiException, IOException
    {

        if (mail.getHtmlPart() == null && mail.getAttachments() != null) {

            Multipart multipart = new MimeMultipart("mixed");
            BodyPart part = new MimeBodyPart();
            part.setContent(mail.getTextPart(), "text/plain");
            multipart.addBodyPart(part);
            addAttachments(multipart, mail.getAttachments(), context);
            return multipart;
        } else {

            Multipart alternativeMultipart = new MimeMultipart("alternative");

            BodyPart part;

            part = new MimeBodyPart();
            part.setText(mail.getTextPart());
            alternativeMultipart.addBodyPart(part);

            // Multipart html_mp = new MimeMultipart("related");

            part = new MimeBodyPart();

            part.setContent(processImageUrls(mail.getHtmlPart()), "text/html");
            part.setHeader("Content-Disposition", "inline");
            part.setHeader("Content-Transfer-Encoding", "quoted-printable");

            alternativeMultipart.addBodyPart(part);

            if (mail.getAttachments() != null && mail.getAttachments().size() > 0) {

                Multipart mixedMultipart = new MimeMultipart("mixed");
                part = new MimeBodyPart();
                part.setContent(alternativeMultipart);
                mixedMultipart.addBodyPart(part);

                addAttachments(mixedMultipart, mail.getAttachments(), context);
                return mixedMultipart;
            }
            return alternativeMultipart;
        }
    }

    /**
     * Evaluates a String property containing Velocity
     * 
     * @param property The String property
     * @param context Context of the request
     * @return The evaluated String
     */
    protected String evaluate(String property, Context context) throws Exception
    {
        String value = (String) context.get(property);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, property, value);
        stringWriter.close();
        return stringWriter.toString();
    }

    /**
     * Get a file name from its path
     * 
     * @param path The file path
     * @return The file name
     */
    protected String getFileName(String path)
    {
        return path.substring(path.lastIndexOf(URL_SEPARATOR) + 1);
    }

    public String getName()
    {
        return ID;
    }

    /**
     * Init a Mail Properties map (exs: smtp, host)
     * 
     * @return The properties
     */
    private Properties initProperties(MailConfiguration mailConfiguration)
    {
        Properties properties = new Properties();

        // Note: The full list of available properties that we can set is defined here:
        // http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html

        properties.put("mail.smtp.port", Integer.toString(mailConfiguration.getPort()));
        properties.put("mail.smtp.host", mailConfiguration.getHost());
        properties.put("mail.smtp.localhost", "localhost");
        properties.put("mail.host", "localhost");
        properties.put("mail.debug", "false");

        if (mailConfiguration.getFrom() != null) {
            properties.put("mail.smtp.from", mailConfiguration.getFrom());
        }

        return properties;
    }

    /**
     * Prepares a Mail Velocity context
     * 
     * @param fromAddr Mail from
     * @param toAddr Mail to
     * @param ccAddr Mail cc
     * @param bccAddr Mail bcc
     * @param vcontext The Velocity context to prepare
     * @return The prepared context
     */
    public VelocityContext prepareVelocityContext(String fromAddr, String toAddr, String ccAddr,
        String bccAddr, VelocityContext vcontext, XWikiContext context)
    {
        if (vcontext == null) {
            vcontext = new VelocityContext();
        }

        vcontext.put("from.name", fromAddr);
        vcontext.put("from.address", fromAddr);
        vcontext.put("to.name", toAddr);
        vcontext.put("to.address", toAddr);
        vcontext.put("to.cc", ccAddr);
        vcontext.put("to.bcc", bccAddr);
        vcontext.put("bounce", fromAddr);

        return vcontext;
    }

    /**
     * Transform Images URLs to point on Message parts (cid: MIME Multipart)
     * 
     * @param html The HTML message
     * @return The Transformed HTML message
     */
    private String processImageUrls(String html)
    {
        // this method/design has to be improved

        Pattern img =
            Pattern.compile("src=[a-zA-Z0-9/_\"]*.(png|jpg|gif|jpeg){1}",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = img.matcher(html);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String found = matcher.group(0);
            String replace = found.substring(found.lastIndexOf("/") + 1);
            matcher.appendReplacement(sb, "src=\"cid:" + replace);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Send a single Mail
     * 
     * @param mailItem The Mail to send
     * @return True if the the email has been sent
     */
    public boolean sendMail(Mail mailItem, XWikiContext context) throws MessagingException,
        UnsupportedEncodingException
    {
        // TODO: Fix the need to instantiate a new XWiki API object
        com.xpn.xwiki.api.XWiki xwikiApi =
            new com.xpn.xwiki.api.XWiki(context.getWiki(), context);
        return sendMail(mailItem, new MailConfiguration(xwikiApi), context);
    }

    /**
     * Send a single Mail
     * 
     * @param mailItem The Mail to send
     * @return True if the the email has been sent
     */
    public boolean sendMail(Mail mailItem, MailConfiguration mailConfiguration,
        XWikiContext context) throws MessagingException, UnsupportedEncodingException
    {
        ArrayList mailList = new ArrayList();
        mailList.add(mailItem);
        return sendMails(mailList, mailConfiguration, context);
    }

    /**
     * Send a Collection of Mails (multiple emails)
     * 
     * @param emails Mail Collection
     * @return True in any case (TODO ?)
     */
    public boolean sendMails(Collection emails, XWikiContext context) throws MessagingException,
        UnsupportedEncodingException
    {
        // TODO: Fix the need to instantiate a new XWiki API object
        com.xpn.xwiki.api.XWiki xwikiApi =
            new com.xpn.xwiki.api.XWiki(context.getWiki(), context);
        return sendMails(emails, new MailConfiguration(xwikiApi), context);
    }

    /**
     * Send a Collection of Mails (multiple emails)
     * 
     * @param emails Mail Collection
     * @return True in any case (TODO ?)
     */
    public boolean sendMails(Collection emails, MailConfiguration mailConfiguration,
        XWikiContext context) throws MessagingException, UnsupportedEncodingException
    {
        Session session = null;
        Transport transport = null;
        int emailCount = emails.size();
        int count = 0;
        int sendFailedCount = 0;
        try {
            for (Iterator emailIt = emails.iterator(); emailIt.hasNext();) {
                count++;

                Mail mail = (Mail) emailIt.next();
                LOG.info("Sending email: " + mail.toString());

                if ((transport == null) || (session == null)) {
                    Properties props = initProperties(mailConfiguration);
                    session = Session.getDefaultInstance(props, null);
                    transport = session.getTransport("smtp");
                    transport.connect();
                }

                try {

                    MimeMessage message = createMimeMessage(mail, session, context);
                    if (message == null) {
                        continue;
                    }

                    transport.sendMessage(message, message.getAllRecipients());

                    // close the connection every other 100 emails
                    if ((count % 100) == 0) {
                        try {
                            if (transport != null) {
                                transport.close();
                            }
                        } catch (MessagingException ex) {
                            LOG.error("MessagingException has occured.", ex);
                        }
                        transport = null;
                        session = null;
                    }
                } catch (SendFailedException ex) {
                    sendFailedCount++;
                    LOG.error("SendFailedException has occured.", ex);
                    LOG.error("Detailed email information" + mail.toString());
                    if (emailCount == 1) {
                        throw ex;
                    }
                    if ((emailCount != 1) && (sendFailedCount > 10)) {
                        throw ex;
                    }
                } catch (MessagingException mex) {
                    LOG.error("MessagingException has occured.", mex);
                    LOG.error("Detailed email information" + mail.toString());
                    if (emailCount == 1) {
                        throw mex;
                    }
                } catch (XWikiException e) {
                    LOG.error("XWikiException has occured.", e);
                } catch (IOException e) {
                    LOG.error("IOException has occured.", e);
                }
            }
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (MessagingException ex) {
                LOG.error("MessagingException has occured.", ex);
            }

            LOG.info("sendEmails: Email count = " + emailCount + " sent count = " + count);
        }
        return true;
    }

    /**
     * Uses an XWiki document to build the message subject and context, based on variables stored in
     * the VelocityContext. Sends the email.
     * 
     * @param templateDocFullName Full name of the template to be used (example:
     *            XWiki.MyEmailTemplate). The template needs to have an XWiki.Email object attached
     * @param from Email sender
     * @param to Email recipient
     * @param cc Email Carbon Copy
     * @param bcc Email Hidden Carbon Copy
     * @param language Language of the email
     * @param vcontext Velocity context passed to the velocity renderer
     * @return True if the email has been sent
     */
    public int sendMailFromTemplate(String templateDocFullName, String from, String to,
        String cc, String bcc, String language, VelocityContext vcontext, XWikiContext context)
        throws XWikiException
    {

        VelocityContext updatedVelocityContext =
            prepareVelocityContext(from, to, cc, bcc, vcontext, context);
        XWiki xwiki = context.getWiki();
        XWikiDocument doc = xwiki.getDocument(templateDocFullName, context);
        BaseObject obj = doc.getObject(EMAIL_XWIKI_CLASS_NAME, "language", language);
        if (obj == null) {
            obj = doc.getObject(EMAIL_XWIKI_CLASS_NAME, "language", "en");
        }
        if (obj == null) {
            LOG.error("No mail object found in the document " + templateDocFullName);
            return ERROR_TEMPLATE_EMAIL_OBJECT_NOT_FOUND;
        }
        String subjectContent = obj.getStringValue("subject");
        String txtContent = obj.getStringValue("text");
        String htmlContent = obj.getStringValue("html");

        String subject =
            XWikiVelocityRenderer.evaluate(subjectContent, templateDocFullName,
                updatedVelocityContext, context);
        String msg =
            XWikiVelocityRenderer.evaluate(txtContent, templateDocFullName,
                updatedVelocityContext, context);
        String html =
            XWikiVelocityRenderer.evaluate(htmlContent, templateDocFullName,
                updatedVelocityContext, context);

        Mail mail = new Mail();
        mail.setFrom((String) updatedVelocityContext.get("from.address"));
        mail.setTo((String) updatedVelocityContext.get("to.address"));
        mail.setCc((String) updatedVelocityContext.get("to.cc"));
        mail.setBcc((String) updatedVelocityContext.get("to.bcc"));
        mail.setSubject(subject);
        mail.setTextPart(msg);
        mail.setHtmlPart(html);
        try {
            sendMail(mail, context);
            return 0;
        } catch (Exception e) {
            LOG.error("sendEmailFromTemplate: " + templateDocFullName + " vcontext: "
                + updatedVelocityContext, e);
            return ERROR;
        }
    }
}
