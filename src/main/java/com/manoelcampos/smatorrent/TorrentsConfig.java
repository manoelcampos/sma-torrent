package com.manoelcampos.smatorrent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the structure of the XML config file containing the registered torrents.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TorrentsConfig {
    @XmlElement(name = "torrent")
    private List<TorrentConfigEntry> torrents = new ArrayList<>();

    public List<TorrentConfigEntry> getTorrents() {
        return torrents;
    }

    public void setTorrents(final List<TorrentConfigEntry> torrents) {
        this.torrents = torrents;
    }

    public int size(){
        return torrents.size();
    }

    public void add(final TorrentConfigEntry entry){
        torrents.add(entry);
    }

    public TorrentConfigEntry get(final int i){
        return torrents.get(i);
    }

    public void remove(final int row) {
        torrents.remove(row);
    }

    public void setFieldValue(final Object value, final int row, final int col){
        final TorrentConfigEntry t = torrents.get(row);
        t.setFieldValue(value, col);
    }
}
