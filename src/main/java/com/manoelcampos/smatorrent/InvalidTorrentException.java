package com.manoelcampos.smatorrent;

/**
 * Class to raise exceptions when try to parse a invalid
 * torrent file.
 *
 * @author Manoel Campos da Silva Filho
 */
public class InvalidTorrentException extends Exception {
    private static final long serialVersionUID = 326914220348328507L;

    public InvalidTorrentException(final String message) {
        super(message);
    }
}
