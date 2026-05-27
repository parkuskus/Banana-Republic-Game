package banana.republic.trade;

/**
 * Hasil validasi sebuah aksi trade (berhasil atau gagal beserta alasannya).
 *
 * Dipakai oleh TradeValidator dan dikembalikan ke UI agar bisa menampilkan
 * pesan error yang informatif tanpa melempar exception.
 */
public final class ValidationResult {

    private final boolean valid;
    private final String reason;

    private ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    /** Membuat hasil validasi sukses. */
    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }

    /** Membuat hasil validasi gagal dengan pesan alasan. */
    public static ValidationResult fail(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                "Reason tidak boleh null/kosong pada hasil gagal");
        }
        return new ValidationResult(false, reason);
    }

    public boolean isValid() { return valid; }

    public String getReason() { return reason; }

    @Override
    public String toString() {
        return valid ? "ValidationResult[OK]"
                     : "ValidationResult[FAIL: " + reason + "]";
    }
}
