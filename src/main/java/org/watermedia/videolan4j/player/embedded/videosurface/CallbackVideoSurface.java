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

package org.watermedia.videolan4j.player.embedded.videosurface;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.watermedia.videolan4j.BufferFormat;
import org.watermedia.videolan4j.VideoLan4J;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.BufferCleanupCallback;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.BufferAllocatorCallback;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.RenderCallback;
import org.watermedia.videolan4j.binding.lib.LibVlc;
import org.watermedia.videolan4j.binding.internal.libvlc_display_callback_t;
import org.watermedia.videolan4j.binding.internal.libvlc_lock_callback_t;
import org.watermedia.videolan4j.binding.internal.libvlc_unlock_callback_t;
import org.watermedia.videolan4j.binding.internal.libvlc_video_cleanup_cb;
import org.watermedia.videolan4j.binding.internal.libvlc_video_format_cb;
import org.watermedia.videolan4j.player.base.MediaPlayer;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Implementation of a video surface that uses native callbacks to receive video frame data for rendering.
 */
public class CallbackVideoSurface extends VideoSurface implements libvlc_video_format_cb, libvlc_video_cleanup_cb, libvlc_lock_callback_t, libvlc_unlock_callback_t, libvlc_display_callback_t {

    private final BufferAllocatorCallback bufferAllocatorCallback;
    private final BufferCleanupCallback cleanupCallback;
    private final RenderCallback renderCallback;

    private final NativeBuffers nativeBuffers;

    private MediaPlayer mediaPlayer;

    private final BufferFormat bufferFormat;

    /**
     * Create a video surface.
     *
     * @param formatCallback callback providing the video buffer format
     * @param renderCallback callback used to render the video frame buffer
     * @param lock <code>true</code> if the video buffer should be locked; <code>false</code> if not
     * @param surfaceAdapter adapter to attach a video surface to a native media player
     */
    public CallbackVideoSurface(final BufferFormat bufferFormat, final BufferAllocatorCallback formatCallback, final RenderCallback renderCallback, final boolean lock, final VideoSurfaceAdapter surfaceAdapter, final BufferCleanupCallback cleanupCallback) {
        super(surfaceAdapter);
        this.bufferFormat = bufferFormat;
        this.bufferAllocatorCallback = formatCallback;
        this.renderCallback = renderCallback;
        this.nativeBuffers = new NativeBuffers(lock);
        this.cleanupCallback = cleanupCallback;
    }

    @Override
    public void attach(final MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        LibVlc.libvlc_video_set_format_callbacks(mediaPlayer.mediaPlayerInstance(), this, this);
        LibVlc.libvlc_video_set_callbacks(mediaPlayer.mediaPlayerInstance(), this, this, this, null);
    }


    @Override
    public int format(final PointerByReference opaque, final PointerByReference chromaPointer, final IntByReference widthPointer, final IntByReference heightPointer, final PointerByReference pitchesPointer, final PointerByReference linesPointer) {
        final int width = widthPointer.getValue();
        final int height = heightPointer.getValue();
        final byte[] chromaBytes = this.bufferFormat.getChroma().getBytes();
        final int[] pitches = this.bufferFormat.getPitches(width, height);
        final int[] lines = this.bufferFormat.getLines(width, height);

        VideoLan4J.LOGGER.info("Width: {} - Height: {} - Chroma: {} - Pitches: {} - Lines: {}", width, height, bufferFormat.getChroma(), Arrays.toString(pitches), Arrays.toString(lines));

        // APPLY FORMAT - (IGNORE WIDTH AND HEIGHT)
        chromaPointer.getPointer().write(0, chromaBytes, 0, Math.min(chromaBytes.length, 4));
        pitchesPointer.getPointer().write(0, pitches, 0, pitches.length);
        linesPointer.getPointer().write(0, lines, 0, lines.length);

        final int result = this.nativeBuffers.allocate(pitches, lines);
        this.bufferAllocatorCallback.allocatedBuffers(this.nativeBuffers.buffers());
        return result;
    }

    @Override
    public void cleanup(final Pointer opaque) {
        this.cleanupCallback.cleanupBuffers(this.nativeBuffers.buffers());
        this.nativeBuffers.free();
    }

    @Override
    public void display(final Pointer opaque, final Pointer picture) {
        this.renderCallback.display(this.mediaPlayer, this.nativeBuffers.buffers(), this.bufferFormat);
    }

    @Override
    public Pointer lock(final Pointer opaque, final PointerByReference planes) {
        final Pointer[] pointers = this.nativeBuffers.pointers();
        // WATERMeDIA PATCH - START
        try {
            this.semaphore.acquire();
            planes.getPointer().write(0, pointers, 0, pointers.length);
        } catch (final InterruptedException e) {
            throw new RuntimeException("Thread was interrupted", e);
        }
        this.semaphore.release();
        // WATERMeDIA PATCH - END
        return null;
    }

    @Override
    public void unlock(final Pointer opaque, final Pointer picture, final Pointer plane) {
    }
}
