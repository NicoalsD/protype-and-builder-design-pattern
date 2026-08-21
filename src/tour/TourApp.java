package tour;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.BorderFactory;
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
    private final JComboBox<Event> eventBox = new JComboBox<>();
    private final JLabel stopDetailLabel = new JLabel();
    private final JLabel remainingLabel = new JLabel();
    private final JLabel revenueLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
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
    }

    private void addClone(String city, String date) {
        Event clone = master.cloneFor(city, master.venue, date);
        eventBox.addItem(clone);
        eventBox.setSelectedItem(clone);
        statusLabel.setText("Cloned " + master.city + " -> " + city + " (Prototype deep copy)");
        refresh();
    }

    private void sell(TicketTier tier) {
        try {
            selected.sell(tier);
        } catch (IllegalStateException e) {
            // SOLD OUT: refresh() below shows the label
        }
        refresh();
    }

    private static String tierSummary(Event e) {
        StringBuilder sb = new StringBuilder();
        for (TicketTier t : e.tiers) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(t.name).append(" $").append(String.format("%,d", t.price));
        }
        return sb.toString();
    }

    private void refresh() {
        selected = (Event) eventBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        stopDetailLabel.setText("<html><b>" + selected.city + "</b> | " + selected.venue
                + " | " + selected.date + "<br>Tiers: " + tierSummary(selected) + "<br>Rider: "
                + String.join("; ", selected.rider) + "</html>");
        int remaining = selected.remaining();
        remainingLabel.setText(remaining + " of " + selected.capacity + " tickets left");
        capacityBar.setValue(100 * selected.sold / selected.capacity);
        capacityBar.setString(selected.sold + "/" + selected.capacity + " sold");
        revenueLabel.setText("Revenue: $" + String.format("%,d", selected.revenue) + " COP");
        soldOutLabel.setVisible(remaining == 0);
    }

    private static JPanel section(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private static JPanel buildBannerPanel() {
        JLabel banner = new JLabel("<html><b>How it works:</b> the master event is built once with the "
                + "<b>Builder</b> pattern. Every tour stop is a <b>Prototype</b> deep clone of the master. "
                + "Select a stop and sell tickets, or clone the master to add a new stop.</html>");
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(banner, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMasterPanel() {
        JPanel panel = section("Master event — built once via Builder");
        panel.add(new JLabel("<html><b>" + master.city + "</b> | " + master.venue + " | " + master.date
                + "<br>Capacity: " + master.capacity + " seats<br>Tiers: " + tierSummary(master)
                + "<br>Rider: " + String.join("; ", master.rider) + "</html>"));
        return panel;
    }

    private JPanel buildStopPanel() {
        JPanel panel = section("Tour stops — Prototype clones");
        eventBox.addActionListener(e -> refresh());
        eventBox.addItem(master);
        panel.add(eventBox);
        panel.add(stopDetailLabel);
        return panel;
    }

    private JPanel buildSalesPanel() {
        JPanel panel = section("Ticket sales");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (TicketTier tier : master.tiers) {
            JButton button = new JButton("Sell " + tier.name + " ($" + String.format("%,d", tier.price) + ")");
            button.addActionListener(e -> sell(tier));
            buttons.add(button);
        }
        panel.add(buttons);
        panel.add(remainingLabel);
        capacityBar.setStringPainted(true);
        panel.add(capacityBar);
        panel.add(revenueLabel);
        soldOutLabel.setForeground(Color.RED);
        soldOutLabel.setVisible(false);
        panel.add(soldOutLabel);
        return panel;
    }

    private JPanel buildClonePanel() {
        JPanel panel = section("Clone master to a new city");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cityField = new JTextField(12);
        JTextField dateField = new JTextField("2027-03-15", 10);
        JButton cloneButton = new JButton("Clone");
        cloneButton.addActionListener(e -> {
            if (!cityField.getText().isBlank()) {
                addClone(cityField.getText().trim(), dateField.getText().trim());
                cityField.setText("");
            }
        });
        row.add(new JLabel("City:"));
        row.add(cityField);
        row.add(new JLabel("Date:"));
        row.add(dateField);
        row.add(cloneButton);
        panel.add(row);
        panel.add(statusLabel);
        return panel;
    }

    private JFrame buildFrame() {
        JFrame frame = new JFrame("Concert Tour Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildBannerPanel());
        center.add(buildMasterPanel());
        center.add(buildStopPanel());
        center.add(buildSalesPanel());
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