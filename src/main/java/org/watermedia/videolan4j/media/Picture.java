/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2019 Caprica Software Limited.
 */

package org.watermedia.videolan4j.media;

import com.sun.jna.Pointer;
import org.watermedia.videolan4j.binding.internal.libvlc_picture_t;
import org.watermedia.videolan4j.binding.lib.LibVlc;
import org.watermedia.videolan4j.binding.lib.size_t.size_tByReference;

/**
 * Picture.
 */
public final class Picture {

    private final int width;

    private final int height;

    private final int stride;

    private final PictureType type;

    private final long time;

    private final byte[] buffer;

    private final int size;

    /**
     * Create a picture.
     *
     * @param picture native picture instance
     */
    public Picture(libvlc_picture_t picture) {
        this.width  = LibVlc.libvlc_picture_get_width(picture);
        this.height = LibVlc.libvlc_picture_get_height(picture);
        this.stride = LibVlc.libvlc_picture_get_stride(picture);
        this.type   = PictureType.pictureType(LibVlc.libvlc_picture_type(picture));
        this.time   = LibVlc.libvlc_picture_get_time(picture);
        this.buffer = initBuffer(picture);
        this.size   = this.buffer.length;
    }

    // Required by vlcj-pro
    public Picture(int width, int height, PictureType type, int stride, long time, byte[] buffer, int size) {
        this.width = width;
        this.height = height;
        this.type = type;
        this.stride = stride;
        this.time = time;
        this.buffer = buffer;
        this.size = size;
    }

    private byte[] initBuffer(libvlc_picture_t picture) {
        size_tByReference size = new size_tByReference();
        Pointer pointer = LibVlc.libvlc_picture_get_buffer(picture, size);
        return pointer.getByteArray(0, size.getValue().intValue());
    }

    /**
     * Get the picture width.
     *
     * @return width
     */
    public int width() {
        return width;
    }

    /**
     * Get the picture height.
     *
     * @return height
     */
    public int height() {
        return height;
    }

    /**
     * Get the picture stride (depth).
     *
     * @return stride
     */
    public int stride() {
        return stride;
    }

    /**
     * Get the picture type.
     *
     * @return type
     */
    public PictureType type() {
        return type;
    }

    /**
     * Get the timestamp when the picture was taken.
     *
     * @return time
     */
    public long time() {
        return time;
    }

    /**
     * Get the picture buffer.
     *
     * @return buffer
     */
    public byte[] buffer() {
        return buffer;
    }

    /**
     * Get the picture buffer size.
     *
     * @return buffer size
     */
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getClass().getSimpleName()).append('[');
        sb.append("width=").append(width).append(',');
        sb.append("height=").append(height).append(',');
        sb.append("stride=").append(stride).append(',');
        sb.append("type=").append(type).append(',');
        sb.append("time=").append(time).append(',');
        sb.append("size=").append(size).append(']');
        return sb.toString();
    }

}
