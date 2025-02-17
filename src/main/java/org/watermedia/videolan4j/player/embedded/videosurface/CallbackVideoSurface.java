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
    public CallbackVideoSurface(BufferFormat bufferFormat, final BufferAllocatorCallback formatCallback, final RenderCallback renderCallback, final boolean lock, final VideoSurfaceAdapter surfaceAdapter, final BufferCleanupCallback cleanupCallback) {
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
    public int format(final PointerByReference opaque, final PointerByReference chroma, final IntByReference width, final IntByReference height, final PointerByReference pitches, final PointerByReference lines) {
        this.applyBufferFormat(this.bufferFormat, chroma, width, height, pitches, lines);
        final int result = this.nativeBuffers.allocate(this.bufferFormat, width.getValue(), height.getValue());
        this.bufferAllocatorCallback.allocatedBuffers(this.nativeBuffers.buffers());
        return result;
    }

    /**
     * Set the desired video format properties - space for these structures is already allocated by LibVlc, we
     * simply fill the existing memory.
     * <p>
     * The {@link BufferFormat} class restricts the chroma to maximum four bytes, so we don't need check it here, we
     * do however need to check if it is less than four.
     *
     * @param chroma
     * @param width
     * @param height
     * @param pitches
     * @param lines
     */
    private void applyBufferFormat(final BufferFormat bufferFormat, final PointerByReference chroma, final IntByReference width, final IntByReference height, final PointerByReference pitches, final PointerByReference lines) {
        final byte[] chromaBytes = bufferFormat.getChroma().getBytes();
        chroma.getPointer().write(0, chromaBytes, 0, Math.min(chromaBytes.length, 4));
        final int w = width.getValue();
        final int h = height.getValue();
        final int[] pitchValues = bufferFormat.getPitches(w, h);
        final int[] lineValues = bufferFormat.getLines(w, h);
        pitches.getPointer().write(0, pitchValues, 0, pitchValues.length);
        lines.getPointer().write(0, lineValues, 0, lineValues.length);
    }

    @Override
    public void cleanup(final Pointer opaque) {
        this.cleanupCallback.cleanupBuffers(CallbackVideoSurface.this.nativeBuffers.buffers());
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
