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

import org.watermedia.videolan4j.player.base.MediaPlayer;

import java.util.concurrent.Semaphore;

/**
 * Encapsulation of a video surface.
 */
public abstract class VideoSurface {

    protected Semaphore semaphore;

    /**
     * Operating System specific video surface adapter implementation.
     */
    protected final VideoSurfaceAdapter videoSurfaceAdapter;

    /**
     * Create a new video surface wrapper.
     *
     * @param videoSurfaceAdapter video surface adapter implementation
     */
    protected VideoSurface(VideoSurfaceAdapter videoSurfaceAdapter) {
        this.videoSurfaceAdapter = videoSurfaceAdapter;
    }

    /**
     * Attach the video surface to a media player.
     * <p>
     * The video surface component <em>must</em> be visible at this point otherwise the native call will fail.
     *
     * @param mediaPlayer media player instance
     */
    public abstract void attach(MediaPlayer mediaPlayer);

    // WATERMeDIA Patch: JANKY HACK FOR BUFFER UPLOADING
    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    // WATERMeDIA Patch: JANKY HACK FOR BUFFER UPLOADING
    public void setSemaphore(Semaphore lock) {
        this.semaphore = lock;
    }
}
