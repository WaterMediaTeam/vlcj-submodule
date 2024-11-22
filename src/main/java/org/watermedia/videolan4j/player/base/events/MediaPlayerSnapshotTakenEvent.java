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

package org.watermedia.videolan4j.player.base.events;

import org.watermedia.videolan4j.binding.internal.libvlc_event_t;
import org.watermedia.videolan4j.binding.internal.media_player_snapshot_taken;
import org.watermedia.videolan4j.player.base.MediaPlayer;
import org.watermedia.videolan4j.player.base.MediaPlayerEventListener;

/**
 * Encapsulation of a media player snapshot taken event.
 */
final class MediaPlayerSnapshotTakenEvent extends MediaPlayerEvent {

    private final String filename;

    MediaPlayerSnapshotTakenEvent(MediaPlayer mediaPlayer, libvlc_event_t event) {
        super(mediaPlayer);
        this.filename = ((media_player_snapshot_taken) event.u.getTypedValue(media_player_snapshot_taken.class)).filename;
    }

    @Override
    public void notify(MediaPlayerEventListener listener) {
        listener.snapshotTaken(mediaPlayer, filename);
    }

}
