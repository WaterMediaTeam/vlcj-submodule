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

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.watermedia.videolan4j.ByteBufferFactory;
import org.watermedia.videolan4j.VideoLan4J;
import org.watermedia.videolan4j.binding.lib.Kernel32;
import org.watermedia.videolan4j.binding.lib.LibC;
import org.watermedia.videolan4j.binding.lib.size_t;

import java.nio.ByteBuffer;

final class NativeBuffers {

    private final boolean lockBuffers;

    /**
     * Native memory buffers, one for each plane.
     */
    private ByteBuffer[] nativeBuffers;

    /**
     * Native memory pointers to each byte buffer.
     */
    private Pointer[] pointers;

    public NativeBuffers(final boolean lockBuffers) {
        this.lockBuffers = lockBuffers;
    }

    /**
     *
     * Memory must be aligned correctly (on a 32-byte boundary) for the libvlc API functions, this is all taken care of
     * by the {@link ByteBufferFactory}.
     *
     * @return
     */
    int allocate(final int[] pitches, final int[] lines) {
        final int planeCount = pitches.length;
        this.nativeBuffers = new ByteBuffer[planeCount];
        this.pointers = new Pointer[planeCount];
        for (int i = 0; i < planeCount; i ++) {
            final ByteBuffer buffer = ByteBufferFactory.alloc(pitches[i] * lines[i]);
            if (!ByteBufferFactory.isAligned(ByteBufferFactory.address(buffer))) {
                VideoLan4J.LOGGER.warn("Detected an unaligned buffer. this might lead in I/O issues");
            }
            this.nativeBuffers[i] = buffer;
            this.pointers[i] = Pointer.createConstant(ByteBufferFactory.address(buffer));
            if (this.lockBuffers) {
                if (!Platform.isWindows()) {
                    LibC.INSTANCE.mlock(this.pointers[i], new NativeLong(buffer.capacity()));
                } else {
                    Kernel32.INSTANCE.VirtualLock(this.pointers[i], new size_t(buffer.capacity()));
                }
            }
        }
        return this.nativeBuffers.length;
    }

    void free() {
        if (this.nativeBuffers != null) {
            if (this.lockBuffers) {
                for (int i = 0; i < this.nativeBuffers.length; i++) {
                    if (!Platform.isWindows()) {
                        LibC.INSTANCE.munlock(this.pointers[i], new NativeLong(this.nativeBuffers[i].capacity()));
                    } else {
                        Kernel32.INSTANCE.VirtualUnlock(this.pointers[i], new size_t(this.nativeBuffers[i].capacity()));
                    }
                }
            }
            // WATERMeDIA
            for(final ByteBuffer buffer: this.nativeBuffers) {
                ByteBufferFactory.dealloc(buffer);
            }
            this.nativeBuffers = null;
            this.pointers = null;
        }
    }

    ByteBuffer[] buffers() {
        return this.nativeBuffers;
    }

    Pointer[] pointers() {
        return this.pointers;
    }

}
