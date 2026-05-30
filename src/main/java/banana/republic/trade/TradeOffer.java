package banana.republic.trade;

import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Representasi immutable sebuah penawaran dagang antar pemain.
 *
 * Satu TradeOffer berisi: offerer: pemain yang menawarkan target: pemain yang
 * dituju (null = tawaran ke semua pemain) offer: resource yang diberikan
 * offerer request: resource yang diminta offerer dari target
 *
 * Status offer berubah melalui accept(), #reject(), dan counter(TradeOffer)},
 * setiap transisi menghasilkan objek baru (immutable).
 */
public final class TradeOffer {

    public enum Status { PENDING, ACCEPTED, REJECTED, COUNTERED, CANCELLED }

    private final Player offerer;
    private final Player target; // null = broadcast ke semua
    private final Map<ResourceType, Integer> offer;
    private final Map<ResourceType, Integer> request;
    private final Status status;

    /**
     * pemain yang menawarkan; tidak boleh null target pemain yang dituju; null
     * berarti tawaran ke semua pemain resource yang diberikan offerer (hanya
     * resource dgn amount > 0) request resource yang diminta offerer dari
     * target
     */
    public TradeOffer(Player offerer, Player target,
                      Map<ResourceType, Integer> offer,
                      Map<ResourceType, Integer> request) {
        if (offerer == null)
            throw new IllegalArgumentException("Offerer tidak boleh null");
        if (offer == null || offer.isEmpty())
            throw new IllegalArgumentException("Offer tidak boleh kosong");
        if (request == null || request.isEmpty())
            throw new IllegalArgumentException("Request tidak boleh kosong");

        this.offerer = offerer;
        this.target = target;
        this.offer = Collections.unmodifiableMap(new EnumMap<>(offer));
        this.request = Collections.unmodifiableMap(new EnumMap<>(request));
        this.status = Status.PENDING;
    }

    private TradeOffer(TradeOffer src, Status newStatus) {
        this.offerer = src.offerer;
        this.target = src.target;
        this.offer = src.offer;
        this.request = src.request;
        this.status = newStatus;
    }

    /** Target menerima penawaran. */
    public TradeOffer accept() {
        if (status != Status.PENDING)
            throw new IllegalStateException("Offer bukan dalam status PENDING");
        return new TradeOffer(this, Status.ACCEPTED);
    }

    /** Target menolak penawaran. */
    public TradeOffer reject() {
        if (status != Status.PENDING)
            throw new IllegalStateException("Offer bukan dalam status PENDING");
        return new TradeOffer(this, Status.REJECTED);
    }

    /** Offerer membatalkan penawaran sebelum dijawab. */
    public TradeOffer cancel() {
        if (status != Status.PENDING)
            throw new IllegalStateException(
                "Hanya offer PENDING yang bisa dibatalkan");
        return new TradeOffer(this, Status.CANCELLED);
    }

    /**
     * Target mengajukan counter-offer. Counter-offer adalah TradeOffer baru
     * dari target ke offerer.
     *
     * counterOffer offer baru dari target; harus punya target = offerer asal
     * return TradeOffer ini dengan status COUNTERED
     */
    public TradeOffer counter(TradeOffer counterOffer) {
        if (status != Status.PENDING)
            throw new IllegalStateException("Offer bukan dalam status PENDING");
        if (counterOffer == null)
            throw new IllegalArgumentException(
                "Counter offer tidak boleh null");
        return new TradeOffer(this, Status.COUNTERED);
    }

    public Player getOfferer() { return offerer; }

    public Player getTarget() { return target; }

    public Status getStatus() { return status; }

    /** Resource yang diberikan offerer (unmodifiable). */
    public Map<ResourceType, Integer> getOffer() { return offer; }

    /** Resource yang diminta offerer dari target (unmodifiable). */
    public Map<ResourceType, Integer> getRequest() { return request; }

    /** true jika offer ditujukan ke semua pemain (broadcast). */
    public boolean isBroadcast() { return target == null; }

    /**
     * Total resource yang diberikan offerer untuk resource tertentu.
     * Mengembalikan 0 jika resource tersebut tidak ada di offer.
     */
    public int getOfferAmount(ResourceType type) {
        return offer.getOrDefault(type, 0);
    }

    /**
     * Total resource yang diminta offerer dari resource tertentu.
     */
    public int getRequestAmount(ResourceType type) {
        return request.getOrDefault(type, 0);
    }

    @Override
    public String toString() {
        return String.format("TradeOffer[%s → %s | offer=%s | request=%s | %s]",
                             offerer.getName(),
                             target != null ? target.getName() : "semua", offer,
                             request, status);
    }
}
