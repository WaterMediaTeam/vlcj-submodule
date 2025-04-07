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

package org.watermedia.videolan4j.player.embedded.videosurface.callback;

import org.watermedia.videolan4j.player.embedded.videosurface.CallbackVideoSurface;
import org.watermedia.videolan4j.tools.Chroma;

import java.util.Arrays;

/**
 * Specifies the formats used by the {@link CallbackVideoSurface}.
 * <p>
 * The buffer will contain data of the given width and height in the format specified by the chroma parameter. A buffer
 * can consist of multiple planes depending on the format of the data. For each plane the pitch and height in lines must
 * be supplied.
 * <p>
 * For example, RV32 format has only one plane. Its pitch is width * 4, and its number of lines is the same as the
 * height.
 */
public class BufferFormat {

    /**
     * Chroma (pixel colour format).
     */
    private final Chroma chroma;

    /**
     * Pixel width of the video.
     */
    private final int width;

    /**
     * Pixel height of the video.
     */
    private final int height;

    /**
     * Pitch size for each plane.
     */
    private final int[] pitches;

    /**
     * Number of lines in each plane.
     */
    private final int[] lines;

    /**
     * Constructs a new BufferFormat instance with the given parameters.
     *
     * @param chroma a VLC buffer type, e.g. RV32, I420, YV12, etc.
     * @param width the width must be major than 0
     * @param height the height must be major than 0
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public BufferFormat(final Chroma chroma, final int width, final int height) {
        this.validate(width, height);
        this.chroma = chroma;
        this.width = width;
        this.height = height;
        this.pitches = chroma.getPitches(width);
        this.lines = chroma.getLines(height);
    }

    /**
     * Get the pixel format.
     *
     * @return pixel format
     */
    public final Chroma getChroma() {
        return this.chroma;
    }

    /**
     * Get the width.
     *
     * @return width
     */
    public final int getWidth() {
        return this.width;
    }

    /**
     * Get the height.
     *
     * @return height
     */
    public final int getHeight() {
        return this.height;
    }

    /**
     * Get the pitches for each plane.
     *
     * @return pitches
     */
    public final int[] getPitches() {
        return this.pitches;
    }

    /**
     * Get the number of lines for each plane.
     *
     * @return lines
     */
    public final int[] getLines() {
        return this.lines;
    }

    /**
     * Get the number of planes in the buffer.
     *
     * @return number of planes
     */
    public final int getPlaneCount() {
        return this.pitches.length;
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '[' +
                "chroma=" + this.chroma + ',' +
                "width=" + this.width + ',' +
                "height=" + this.height + ',' +
                "pitches=" + Arrays.toString(this.pitches) + ',' +
                "lines=" + Arrays.toString(this.lines) + ']';
    }

    /**
     * Validate the buffer format.
     * <p>
     * Incorrect parameter values can cause fatal crashes, so all are checked here
     * to mitigate.
     *
     * @param width
     * @param height
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validate(final int width, final int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than zero");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than zero");
        }
    }

}
