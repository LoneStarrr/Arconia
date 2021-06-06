package lonestarrr.arconia.common.core.helper;

/**
 * Exceptions raised by BlockPattern methods. They indicate something's wrong with the file format.
 */
public class BlockPatternException extends Exception {
    public BlockPatternException(String message) {
        super(message);
    }
}
