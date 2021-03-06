/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.net.pi.runtime;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Data of a counter cell of a protocol-independent pipeline.
 */
@Beta
public final class PiCounterCellData {

    private final PiCounterCellId cellId;
    private final long packets;
    private final long bytes;

    /**
     * Creates a new counter cell data for the given cell identifier, number of packets and bytes.
     *
     * @param cellId  counter cell identifier
     * @param packets number of packets
     * @param bytes   number of bytes
     */
    public PiCounterCellData(PiCounterCellId cellId, long packets, long bytes) {
        this.cellId = cellId;
        this.packets = packets;
        this.bytes = bytes;
    }

    /**
     * Returns the cell identifier.
     *
     * @return cell identifier
     */
    public PiCounterCellId cellId() {
        return cellId;
    }

    /**
     * Returns the packet count value contained by this cell.
     *
     * @return number of packets
     */
    public long packets() {
        return packets;
    }

    /**
     * Returns the byte count value contained by this cell.
     *
     * @return number of bytes
     */
    public long bytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PiCounterCellData)) {
            return false;
        }
        PiCounterCellData that = (PiCounterCellData) o;
        return packets == that.packets &&
                bytes == that.bytes &&
                Objects.equal(cellId, that.cellId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cellId, packets, bytes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cellId", cellId)
                .add("packets", packets)
                .add("bytes", bytes)
                .toString();
    }
}
