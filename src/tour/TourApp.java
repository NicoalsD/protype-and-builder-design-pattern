package tour;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TourApp {
    private final Event master;
    private final List<Event> tour = new ArrayList<>();
    private final JComboBox<Event> eventBox = new JComboBox<>();
    private final JLabel remainingLabel = new JLabel();
    private final JLabel revenueLabel = new JLabel();
    private final JProgressBar capacityBar = new JProgressBar(0, 100);
    private final JLabel soldOutLabel = new JLabel("SOLD OUT");
    private Event selected;

    private TourApp() {
        master = new EventBuilder()
                .city("Bogota")
                .venue("Coliseo Live")
                .date("2026-11-20")
                .capacity(30)
                .tier("VIP", 250000)
                .tier("General", 120000)
                .riderItem("Backline + soundcheck")
                .riderItem("Catering for 10")
                .build();
        tour.add(master);
    }

    private void addClone(String city, String venue, String date) {
        Event clone = master.cloneFor(city, venue, date);
        tour.add(clone);
        eventBox.addItem(clone);
        eventBox.setSelectedItem(clone);
        refresh();
    }

    private void sell(TicketTier tier) {
        try {
            selected.sell(tier);
        } catch (IllegalStateException e) {
            soldOutLabel.setVisible(true);
        }
        refresh();
    }

    private void refresh() {
        selected = (Event) eventBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        int remaining = selected.remaining();
        remainingLabel.setText(remaining + " of " + selected.capacity + " tickets left");
        capacityBar.setValue(100 * selected.sold / selected.capacity);
        revenueLabel.setText("Revenue: $" + selected.revenue + " COP");
        soldOutLabel.setVisible(remaining == 0);
    }

    private JPanel buildTierPanel() {
        JPanel panel = new JPanel();
        for (TicketTier tier : master.tiers) {
            JButton button = new JButton("Sell " + tier.name + " ($" + tier.price + ")");
            button.addActionListener(e -> sell(tier));
            panel.add(button);
        }
        return panel;
    }

    private JPanel buildClonePanel() {
        JPanel panel = new JPanel();
        JTextField cityField = new JTextField(12);
        JButton cloneButton = new JButton("Clone to city");
        cloneButton.addActionListener(e -> {
            if (!cityField.getText().isBlank()) {
                addClone(cityField.getText(), master.venue, "2027-03-15");
                cityField.setText("");
            }
        });
        panel.add(cityField);
        panel.add(cloneButton);
        return panel;
    }

    private JFrame buildFrame() {
        JFrame frame = new JFrame("Concert Tour Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel masterLabel = new JLabel(
                "Master event: " + master.city + " | " + master.venue + " | " + master.date
                        + " | " + master.capacity + " seats | " + master.rider);
        eventBox.addActionListener(e -> refresh());
        eventBox.addItem(master);

        soldOutLabel.setForeground(Color.RED);
        soldOutLabel.setVisible(false);
        capacityBar.setStringPainted(true);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(masterLabel);
        center.add(eventBox);
        center.add(buildTierPanel());
        center.add(remainingLabel);
        center.add(capacityBar);
        center.add(revenueLabel);
        center.add(soldOutLabel);
        center.add(buildClonePanel());

        frame.add(center, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private void start() {
        refresh();
        buildFrame().setVisible(true);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void selfChecks() {
        boolean rejected = false;
        try {
            new EventBuilder().city("X").venue("Y").date("Z").capacity(0)
                    .tier("VIP", 100).build();
        } catch (IllegalStateException e) {
            rejected = true;
        }
        check(rejected, "builder must reject zero capacity");

        Event base = new EventBuilder().city("A").venue("V").date("D").capacity(5)
                .tier("General", 100).build();
        Event clone = base.cloneFor("B", "V", "D");
        clone.sell(clone.tiers.get(0));
        check(base.sold == 0, "selling the clone must not touch the master");
        check(base.tiers != clone.tiers, "tier lists must be deep-copied");

        for (int i = 0; i < 5; i++) {
            base.sell(base.tiers.get(0));
        }
        boolean soldOut = false;
        try {
            base.sell(base.tiers.get(0));
        } catch (IllegalStateException e) {
            soldOut = e.getMessage().equals("SOLD OUT");
        }
        check(soldOut, "selling past capacity must throw SOLD OUT");
    }

    public static void main(String[] args) {
        selfChecks();
        System.out.println("Self-checks passed. Launching GUI...");
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("GUI skipped: headless environment (no display). Run locally to see it.");
            return;
        }
        SwingUtilities.invokeLater(() -> new TourApp().start());
    }
}