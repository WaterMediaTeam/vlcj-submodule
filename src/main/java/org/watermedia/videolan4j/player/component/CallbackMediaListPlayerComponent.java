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

package org.watermedia.videolan4j.player.component;

import org.watermedia.videolan4j.BufferFormat;
import org.watermedia.videolan4j.medialist.MediaList;
import org.watermedia.videolan4j.medialist.MediaListRef;
import org.watermedia.videolan4j.player.component.callback.CallbackImagePainter;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.BufferCleanupCallback;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.BufferAllocatorCallback;
import org.watermedia.videolan4j.player.embedded.videosurface.callback.RenderCallback;
import org.watermedia.videolan4j.player.list.MediaListPlayer;
import org.watermedia.videolan4j.factory.MediaPlayerFactory;
import org.watermedia.videolan4j.player.embedded.fullscreen.FullScreenStrategy;

/**
 * Implementation of a callback "direct-rendering" media list player.
 * <p>
 * This component renders video frames received via native callbacks.
 * <p>
 * The component may be added directly to a user interface layout.
 * <p>
 * When the component is no longer needed, it should be released by invoking the {@link #release()} method.
 */
@SuppressWarnings("serial")
public class CallbackMediaListPlayerComponent extends CallbackMediaListPlayerComponentBase {

    /**
     * Media list player.
     */
    private final MediaListPlayer mediaListPlayer;

    /**
     * Media list.
     */
    private final MediaList mediaList;

    /**
     * Construct a callback media list player component.
     * <p>
     * This component will provide a reasonable default implementation, but a client application is free to override
     * these defaults with their own implementation.
     * <p>
     * To rely on the defaults and have this component render the video, do not supply a <code>renderCallback</code>.
     * <p>
     * If a client application wishes to perform its own rendering, provide a <code>renderCallback</code>, a
     * <code>BufferFormatCallback</code>, and optionally (but likely) a <code>videoSurfaceComponent</code> if the client
     * application wants the video surface they are rendering in to be incorporated into this component's layout.
     *
     * @param mediaPlayerFactory media player factory
     * @param fullScreenStrategy full screen strategy
     * @param inputEvents keyboard/mouse input event configuration
     * @param lockBuffers <code>true</code> if the native video buffer should be locked; <code>false</code> if not
     * @param imagePainter image painter (video renderer)
     * @param renderCallback render callback
     * @param bufferAllocatorCallback buffer format callback
     * @param cleanupCallback lightweight video surface component
     */
    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, boolean lockBuffers, BufferFormat bufferFormat, CallbackImagePainter imagePainter, RenderCallback renderCallback, BufferAllocatorCallback bufferAllocatorCallback, BufferCleanupCallback cleanupCallback) {
        super(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferAllocatorCallback, lockBuffers, bufferFormat, imagePainter, cleanupCallback, renderCallback);

        this.mediaListPlayer = mediaPlayerFactory().mediaPlayers().newMediaListPlayer();
        this.mediaListPlayer.mediaPlayer().setMediaPlayer(mediaPlayer());
        this.mediaListPlayer.events().addMediaListPlayerEventListener(this);

        this.mediaList = mediaPlayerFactory().media().newMediaList();
        this.mediaList.events().addMediaListEventListener(this);

        applyMediaList();

        onAfterConstruct();
    }

    /**
     * Construct a callback media list player component for intrinsic rendering (by this component).
     *
     * @param mediaPlayerFactory media player factory
     * @param fullScreenStrategy full screen strategy
     * @param inputEvents keyboard/mouse input event configuration
     * @param lockBuffers <code>true</code> if the native video buffer should be locked; <code>false</code> if not
     * @param imagePainter image painter (video renderer)
     */
    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, boolean lockBuffers, BufferFormat bufferFormat, CallbackImagePainter imagePainter) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, lockBuffers, bufferFormat, imagePainter, null, null, null);
    }

    /**
     * Construct a callback media list player component for external rendering (by the client application).
     *
     * @param mediaPlayerFactory media player factory
     * @param fullScreenStrategy full screen strategy
     * @param inputEvents keyboard/mouse input event configuration
     * @param lockBuffers <code>true</code> if the native video buffer should be locked; <code>false</code> if not
     * @param renderCallback render callback
     * @param bufferAllocatorCallback buffer format callback
     * @param cleanupCallback lightweight video surface component
     */
    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, boolean lockBuffers, BufferFormat bufferFormat, RenderCallback renderCallback, BufferAllocatorCallback bufferAllocatorCallback, BufferCleanupCallback cleanupCallback) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, lockBuffers, bufferFormat, null, renderCallback, bufferAllocatorCallback, cleanupCallback);
    }

    /**
     * Construct a callback media list player component with LibVLC initialisation arguments and reasonable defaults.
     *
     * @param libvlcArgs LibVLC initialisation arguments
     */
    public CallbackMediaListPlayerComponent(String... libvlcArgs) {
        this(new MediaPlayerFactory(libvlcArgs), null, null, true, null, null);
    }

    /**
     * Construct a callback media list player component with reasonable defaults.
     */
    public CallbackMediaListPlayerComponent() {
        this(null, null, null, true, null, null, null, null);
    }

    private void applyMediaList() {
        MediaListRef mediaListRef = mediaList.newMediaListRef();
        try {
            this.mediaListPlayer.list().setMediaList(mediaListRef);
        }
        finally {
            mediaListRef.release();
        }
    }

    /**
     * Get the embedded media list player reference.
     * <p>
     * An application uses this handle to control the media player, add listeners and so on.
     *
     * @return media list player
     */
    public final MediaListPlayer mediaListPlayer() {
        return mediaListPlayer;
    }

    @Override
    protected final void onBeforeRelease() {
        mediaListPlayer.release();
        mediaList.release();
    }

}
