package tour;

public class TicketTier implements Priced {
    final String name;
    final int price;

    public TicketTier(String name, int price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public int price() {
        return price;
    }
}