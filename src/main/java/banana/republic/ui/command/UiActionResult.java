package banana.republic.ui.command;

public final class UiActionResult {
    private final boolean success;
    private final String message;

    private UiActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static UiActionResult success(String message) {
        return new UiActionResult(true, message);
    }

    public static UiActionResult failure(String message) {
        return new UiActionResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
