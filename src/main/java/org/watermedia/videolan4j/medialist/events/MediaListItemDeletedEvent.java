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

package org.watermedia.videolan4j.medialist.events;

import org.watermedia.videolan4j.binding.internal.libvlc_event_t;
import org.watermedia.videolan4j.binding.internal.libvlc_instance_t;
import org.watermedia.videolan4j.binding.internal.libvlc_media_t;
import org.watermedia.videolan4j.binding.internal.media_list_item_deleted;
import org.watermedia.videolan4j.medialist.MediaList;
import org.watermedia.videolan4j.medialist.MediaListEventListener;

/**
 * Encapsulation of a media list item deleted event.
 */
final class MediaListItemDeletedEvent extends MediaListEvent {

    /**
     * Native media instance that was deleted.
     */
    private final libvlc_media_t item;

    /**
     * Index from which the item was deleted.
     */
    private final int index;

    /**
     * Create a media list event.
     *
     * @param libvlcInstance native library instance
     * @param mediaList media list the event relates to
     * @param event native event
     */
    MediaListItemDeletedEvent(libvlc_instance_t libvlcInstance, MediaList mediaList, libvlc_event_t event) {
        super(libvlcInstance, mediaList);

        media_list_item_deleted itemDeletedEvent = ((media_list_item_deleted) event.u.getTypedValue(media_list_item_deleted.class));

        this.item = itemDeletedEvent.item;
        this.index         = itemDeletedEvent.index;
    }

    @Override
    public void notify(MediaListEventListener listener) {
        listener.mediaListItemDeleted(component, temporaryMediaRef(item), index);
    }

}
