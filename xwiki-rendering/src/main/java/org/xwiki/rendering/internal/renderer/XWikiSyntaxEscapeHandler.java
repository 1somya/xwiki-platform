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
package org.xwiki.rendering.internal.renderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.rendering.renderer.RendererState;

/**
 * Escape characters that would be confused for XWiki wiki syntax if they were not escaped.
 * 
 * @version $Id$
 * @since 1.7
 */
public class XWikiSyntaxEscapeHandler
{
    public static final Pattern SPACE_PATTERN = Pattern.compile("([ \t])");

    private static final Pattern LIST_PATTERN =
        Pattern.compile("\\p{Blank}*((\\*+[:;]*)|([1*]+\\.[:;]*)|([:;]+))\\p{Blank}+");

    private static final Pattern SECTION_PATTERN = Pattern.compile("\\p{Blank}*(=+)");

    private static final Pattern DOUBLE_CHARS_PATTERN = Pattern.compile("\\/\\/|\\*\\*|__|--|\\^\\^|,,|##|\\\\\\\\");

    private static final String ESCAPE_CHAR = "~";

    public void escape(StringBuffer accumulatedBuffer, RendererState state, boolean escapeLastChar,
        Pattern escapeFirstIfMatching)
    {
        // Escape tilde symbol (i.e. the escape character).
        // Note: This needs to be the first replacement since other replacements below also use the tilde symbol
        replaceAll(accumulatedBuffer, ESCAPE_CHAR, ESCAPE_CHAR + ESCAPE_CHAR);

        // When in a paragraph we need to escape symbols that are at beginning of lines and that could be confused
        // with list items or sections.
        if (state.isInParagraph() && state.isTextOnNewLine()) {

            // Look for list pattern at beginning of line and escape the first character only (it's enough)
            escapeFirstMatchedCharacter(LIST_PATTERN, accumulatedBuffer);

            // Look for section pattern at beginning of line and escape the first character only (it's enough)
            escapeFirstMatchedCharacter(SECTION_PATTERN, accumulatedBuffer);
        }
        
        if (escapeFirstIfMatching != null) {
            escapeFirstMatchedCharacter(escapeFirstIfMatching, accumulatedBuffer);
        }

        // When in a section we need to escape "=" symbols since otherwise they would be confused for end of section
        // characters.
        if (state.isInSection()) {
            replaceAll(accumulatedBuffer, "=", ESCAPE_CHAR + "=");
        }

        // Escape "[[" if not in a link.
        if (!state.isInLink()) {
            replaceAll(accumulatedBuffer, "[[", ESCAPE_CHAR + "[" + ESCAPE_CHAR + "[");
        }

        // Escape verbatim "{{{"
        replaceAll(accumulatedBuffer, "{{{", ESCAPE_CHAR + "{" + ESCAPE_CHAR + "{" + ESCAPE_CHAR + "{");

        // Escape "{{"
        replaceAll(accumulatedBuffer, "{{", ESCAPE_CHAR + "{" + ESCAPE_CHAR + "{");

        // Escape reserved keywords
        Matcher matcher = DOUBLE_CHARS_PATTERN.matcher(accumulatedBuffer.toString());
        for (int i = 0; matcher.find(); i = i + 2) {
            accumulatedBuffer.replace(matcher.start() + i, matcher.end() + i, ESCAPE_CHAR + matcher.group().charAt(0)
                + ESCAPE_CHAR + matcher.group().charAt(1));
        }

        // Escape ":" in "image:something", "attach:something" and "mailto:something"
        // Note: even though there are some restriction in the URI specification as to what character is valid after
        // the ":" character following the scheme we only check for characters greater than the space symbol for
        // simplicity.
        escapeURI(accumulatedBuffer, "image:");
        escapeURI(accumulatedBuffer, "attach:");
        escapeURI(accumulatedBuffer, "mailto:");
        
        // Escape last character if we're told to do so. This is to handle cases such as:
        // - onWord("hello:") followed by onFormat(ITALIC) which would lead to "hello://" if the ":" wasn't escaped
        // - onWord("{") followed by onMacro() which would lead to "{{{" if the "{" wasn't escaped
        if (escapeLastChar) {
            accumulatedBuffer.replace(accumulatedBuffer.length() - 1, accumulatedBuffer.length(), ESCAPE_CHAR
                + accumulatedBuffer.charAt(accumulatedBuffer.length() - 1));
        }
    }

    private void escapeURI(StringBuffer accumulatedBuffer, String match)
    {
        int pos = accumulatedBuffer.indexOf(match);
        if (pos > -1 && accumulatedBuffer.length() > pos + match.length()
            && accumulatedBuffer.charAt(pos + match.length()) > 32) {
            // Escape the ":" symbol
            accumulatedBuffer.replace(pos + match.length() - 1, pos + match.length(), "~:");
        }
    }

    private void replaceAll(StringBuffer accumulatedBuffer, String match, String replacement)
    {
        int pos = -replacement.length();
        while ((pos + replacement.length() < accumulatedBuffer.length())
            && ((pos = accumulatedBuffer.indexOf(match, pos + replacement.length())) != -1)) {
            accumulatedBuffer.replace(pos, pos + match.length(), replacement);
        }
    }

    private void escapeFirstMatchedCharacter(Pattern pattern, StringBuffer accumulatedBuffer)
    {
        Matcher matcher = pattern.matcher(accumulatedBuffer);
        if (matcher.lookingAt()) {
            // Escape the first character
            accumulatedBuffer.replace(matcher.start(1), matcher.start(1) + 1, ESCAPE_CHAR + matcher.group(1).charAt(0));
        }
    }
}
