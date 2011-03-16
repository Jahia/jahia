package org.apache.jackrabbit.core.state;

import org.apache.jackrabbit.core.id.ItemId;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/16/11
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class NoISMLocking implements ISMLocking {
    private final ReadLock readLock = new ReadLock() {

        public void release() {
        }
    };

   private final WriteLock writeLock = new WriteLock() {

        public void release() {
        }

        public ReadLock downgrade() {
            return readLock;
        }
    };

    public ReadLock acquireReadLock(ItemId id) throws InterruptedException {
        return readLock;
    }

    public WriteLock acquireWriteLock(ChangeLog changeLog) throws InterruptedException {
        return writeLock;
    }
}
