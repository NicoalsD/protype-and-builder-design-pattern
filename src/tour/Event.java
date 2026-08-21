package tour;

import java.util.ArrayList;
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

    // Prototype: copy constructor + facade. Tier/rider lists are copied; TicketTier
    // is immutable so a list copy is a safe deep copy here.
    private Event(Event other, String city, String venue, String date) {
        this.city = city;
        this.venue = venue;
        this.date = date;
        this.capacity = other.capacity;
        this.tiers = new ArrayList<>(other.tiers);
        this.rider = new ArrayList<>(other.rider);
        this.sold = other.sold;
        this.revenue = other.revenue;
    }

    public Event cloneFor(String city, String venue, String date) {
        return new Event(this, city, venue, date);
    }

    public int remaining() {
        return capacity - sold;
    }

    @Override
    public String toString() {
        return city + " | " + venue + " (" + date + ")";
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