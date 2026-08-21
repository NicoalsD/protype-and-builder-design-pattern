package tour;

import java.util.List;

public class Event {
    final String city;
    final String venue;
    final String date;
    final int capacity;
    final List<TicketTier> tiers;
    final List<String> rider;
    int sold;
    int revenue;

    Event(EventBuilder b) {
        this.city = b.city;
        this.venue = b.venue;
        this.date = b.date;
        this.capacity = b.capacity;
        this.tiers = b.tiers;
        this.rider = b.rider;
    }

    public int remaining() {
        return capacity - sold;
    }

    public void sell(TicketTier tier) {
        // ponytail: shared capacity pool, single sold counter; per-tier caps or
        // per-tier sales tracking need a Map<TicketTier,Integer>.
        if (sold >= capacity) {
            throw new IllegalStateException("SOLD OUT");
        }
        sold++;
        revenue += tier.price;
    }
}