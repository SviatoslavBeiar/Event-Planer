package socialMediaApp.api.exp;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String m) { super(m); }
}
