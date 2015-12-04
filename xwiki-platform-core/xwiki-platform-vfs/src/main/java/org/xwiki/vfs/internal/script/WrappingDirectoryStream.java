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
package org.xwiki.vfs.internal.script;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Wrap the {@link DirectoryStream} returned by {@link net.java.truevfs.access.TFileSystem} since the returned class is
 * private and this makes Velocity fail to call {@link DirectoryStream#iterator()} on it.
 * <p/>
 * See <a href="https://java.net/jira/browse/TRUEVFS-158">TRUEVFS-158</a>
 *
 * @version $Id$
 * @since 7.4M2
 */
public class WrappingDirectoryStream implements DirectoryStream<Path>
{
    private DirectoryStream<Path> wrappedDirectoryStream;

    /**
     * @param wrappedDirectoryStream the instance we're wrapping
     */
    public WrappingDirectoryStream(DirectoryStream<Path> wrappedDirectoryStream)
    {
        this.wrappedDirectoryStream = wrappedDirectoryStream;
    }

    @Override
    public Iterator<Path> iterator()
    {
        return this.wrappedDirectoryStream.iterator();
    }

    @Override
    public void close() throws IOException
    {
        this.wrappedDirectoryStream.close();
    }
}
