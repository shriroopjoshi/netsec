package exceptions;

/**
 *
 * @author shriroop
 */
public class TamperedMessageException extends Exception {

    public TamperedMessageException() {
        super("This data has been tampered with!");
    }

}
