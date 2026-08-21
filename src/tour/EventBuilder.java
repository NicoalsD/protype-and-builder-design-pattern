package tour;

import java.util.ArrayList;
import java.util.List;

public class EventBuilder {
    String city = "";
    String venue = "";
    String date = "";
    int capacity;
    final List<TicketTier> tiers = new ArrayList<>();
    final List<String> rider = new ArrayList<>();

    public EventBuilder city(String city) {
        this.city = city;
        return this;
    }

    public EventBuilder venue(String venue) {
        this.venue = venue;
        return this;
    }

    public EventBuilder date(String date) {
        this.date = date;
        return this;
    }

    public EventBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public EventBuilder tier(String name, int price) {
        tiers.add(new TicketTier(name, price));
        return this;
    }

    public EventBuilder riderItem(String item) {
        rider.add(item);
        return this;
    }

    public Event build() {
        if (city.isBlank() || venue.isBlank() || date.isBlank()) {
            throw new IllegalStateException("city, venue and date are required");
        }
        if (capacity <= 0) {
            throw new IllegalStateException("capacity must be positive");
        }
        if (tiers.isEmpty()) {
            throw new IllegalStateException("at least one ticket tier required");
        }
        for (TicketTier t : tiers) {
            if (t.price <= 0) {
                throw new IllegalStateException("tier prices must be positive");
            }
        }
        return new Event(this);
    }
}