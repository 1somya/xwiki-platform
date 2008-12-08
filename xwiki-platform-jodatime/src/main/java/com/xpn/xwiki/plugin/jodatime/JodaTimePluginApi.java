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

package com.xpn.xwiki.plugin.jodatime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.PluginApi;

/**
 * Api for the JodaTimePlugin
 */
public class JodaTimePluginApi extends PluginApi<JodaTimePlugin>
{
    public JodaTimePluginApi(JodaTimePlugin plugin, XWikiContext context)
    {
        super(plugin, context);
    }

    /**
     * @see org.joda.time.DateTime#DateTime()
     * @see JodaTimePlugin#getDateTime()
     */
    public DateTime getDateTime()
    {
        return getInternalPlugin().getDateTime();
    }

    /**
     * @see org.joda.time.DateTime#DateTime(int, int, int, int, int, int, int)
     * @see JodaTimePlugin#getDateTime(int, int, int, int, int, int, int)
     */
    public DateTime getDateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour,
        int secondOfMinute, int millisOfSecond)
    {
        return getInternalPlugin().getDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute,
            millisOfSecond);
    }

    /**
     * @see org.joda.time.DateTime#DateTime(long)
     * @see JodaTimePlugin#getDateTime(long)
     */
    public DateTime getDateTime(long instant)
    {
        return getInternalPlugin().getDateTime(instant);
    }

    /**
     * @see org.joda.time.MutableDateTime#MutableDateTime()
     * @see JodaTimePlugin#getMutableDateTime()
     */
    public MutableDateTime getMutableDateTime()
    {
        return getInternalPlugin().getMutableDateTime();
    }

    /**
     * @see org.joda.time.MutableDateTime#MutableDateTime(int, int, int, int, int, int, int)
     * @see JodaTimePlugin#getMutableDateTime(int, int, int, int, int, int, int)
     */
    public MutableDateTime getMutableDateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay,
        int minuteOfHour, int secondOfMinute, int millisOfSecond)
    {
        return getInternalPlugin().getMutableDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour,
            secondOfMinute, millisOfSecond);
    }

    /**
     * @see org.joda.time.MutableDateTime#MutableDateTime(long)
     * @see JodaTimePlugin#getMutableDateTime(long)
     */
    public MutableDateTime getMutableDateTime(long instant)
    {
        return getInternalPlugin().getMutableDateTime(instant);
    }

    /**
     * @see org.joda.time.format.DateTimeFormat#forPattern(String)
     * @see JodaTimePlugin#getDateTimeFormatterForPattern(String, XWikiContext)
     */
    public DateTimeFormatter getDateTimeFormatterForPattern(String pattern)
    {
        return getInternalPlugin().getDateTimeFormatterForPattern(pattern, getXWikiContext());
    }

    /**
     * @see org.joda.time.format.DateTimeFormat#forStyle(String)
     * @see JodaTimePlugin#getDateTimeFormatterForStyle(String, XWikiContext)
     */
    public DateTimeFormatter getDateTimeFormatterForStyle(String style)
    {
        return getInternalPlugin().getDateTimeFormatterForStyle(style, getXWikiContext());
    }

    /**
     * @see org.joda.time.DateTimeZone#forID(String)
     * @see JodaTimePlugin#getTimezone(String)
     */
    public DateTimeZone getTimezone(String locationOrOffset)
    {
        return getInternalPlugin().getTimezone(locationOrOffset);
    }

    /**
     * @see org.joda.time.DateTimeZone#forOffsetHours(int)
     * @see JodaTimePlugin#getTimezone(int)
     */
    public DateTimeZone getTimezone(int offsetHours)
    {
        return getInternalPlugin().getTimezone(offsetHours);
    }

    /**
     * @see org.joda.time.DateTimeZone#forOffsetHoursMinutes(int, int)
     * @see JodaTimePlugin#getTimezone(int, int)
     */
    public DateTimeZone getTimezone(int offsetHours, int offsetMinutes)
    {
        return getInternalPlugin().getTimezone(offsetHours, offsetMinutes);
    }
}
