package tour;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TourApp {
    private final Event master;
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tour stops");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    private final Map<DefaultMutableTreeNode, Event> nodeToEvent = new LinkedHashMap<>();
    private final JTree stopTree = new JTree(treeModel);
    private final Map<JButton, TicketTier> tierButtons = new LinkedHashMap<>();
    private final JLabel stopDetailLabel = new JLabel();
    private final JLabel remainingLabel = new JLabel();
    private final JLabel revenueLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar capacityBar = new JProgressBar(0, 100);
    private final JLabel soldOutLabel = new JLabel("SOLD OUT");
    private final JPanel sellButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
        rootNode.add(addNode(master, true));
        for (TicketTier tier : master.tiers) {
            JButton button = new JButton("Sell " + tier.name + " ($" + String.format("%,d", tier.price) + ")");
            button.addActionListener(e -> sell(tier));
            tierButtons.put(button, tier);
            sellButtonsPanel.add(button);
        }
    }

    private DefaultMutableTreeNode addNode(Event e, boolean isMaster) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeLabel(e, isMaster));
        nodeToEvent.put(node, e);
        return node;
    }

    private void updateNodeText(DefaultMutableTreeNode node) {
        node.setUserObject(nodeLabel(nodeToEvent.get(node), nodeToEvent.get(node) == master));
        treeModel.nodeChanged(node);
    }

    private static String nodeLabel(Event e, boolean isMaster) {
        return e.city + " | " + e.venue + " (" + e.date + ")" + (isMaster ? " [MASTER]" : "")
                + " — " + e.sold + "/" + e.capacity + " sold";
    }

    private void addClone(String city, String date) {
        Event clone = master.cloneFor(city, master.venue, date);
        DefaultMutableTreeNode node = addNode(clone, false);
        treeModel.insertNodeInto(node, rootNode, rootNode.getChildCount());
        stopTree.setSelectionPath(new javax.swing.tree.TreePath(node.getPath()));
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
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) stopTree.getLastSelectedPathComponent();
        selected = nodeToEvent.get(node);
        if (selected == null) {
            return;
        }
        updateNodeText(node);
        int remaining = selected.remaining();
        stopDetailLabel.setText("<html><b>" + selected.city + "</b> | " + selected.venue
                + " | " + selected.date + "<br>Tiers: " + tierSummary(selected)
                + "<br>Rider: " + String.join("; ", selected.rider) + "</html>");
        remainingLabel.setText(remaining + " of " + selected.capacity + " tickets left");
        capacityBar.setValue(100 * selected.sold / selected.capacity);
        capacityBar.setString(selected.sold + "/" + selected.capacity + " sold");
        revenueLabel.setText("Revenue: $" + String.format("%,d", selected.revenue) + " COP");
        soldOutLabel.setVisible(remaining == 0);
        for (JButton button : tierButtons.keySet()) {
            button.setEnabled(remaining > 0);
        }
    }

    private JPanel buildStopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tour stops (clones hang from the master)"));
        stopTree.setRootVisible(false);
        stopTree.setShowsRootHandles(true);
        stopTree.addTreeSelectionListener(e -> refresh());
        stopTree.setSelectionRow(0);

        JPanel cloneForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cityField = new JTextField(10);
        JTextField dateField = new JTextField("2027-03-15", 8);
        JButton cloneButton = new JButton("Clone");
        cloneButton.addActionListener(e -> {
            if (!cityField.getText().isBlank()) {
                addClone(cityField.getText().trim(), dateField.getText().trim());
                cityField.setText("");
            }
        });
        cloneForm.add(new JLabel("New city:"));
        cloneForm.add(cityField);
        cloneForm.add(new JLabel("Date:"));
        cloneForm.add(dateField);
        cloneForm.add(cloneButton);
        cloneForm.add(statusLabel);

        panel.add(new JScrollPane(stopTree), BorderLayout.CENTER);
        panel.add(cloneForm, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder("Ticket sales"));

        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(stopDetailLabel);
        detailPanel.add(Box.createVerticalStrut(8));

        capacityBar.setStringPainted(true);
        capacityBar.setPreferredSize(new Dimension(260, 22));
        capacityBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        soldOutLabel.setForeground(Color.RED);
        soldOutLabel.setVisible(false);
        revenueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        remainingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        capacityBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        soldOutLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sellButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(detailPanel);
        panel.add(sellButtonsPanel);
        panel.add(remainingLabel);
        panel.add(capacityBar);
        panel.add(revenueLabel);
        panel.add(soldOutLabel);
        return panel;
    }

    private JFrame buildFrame() {
        JFrame frame = new JFrame("Concert Tour Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildStopPanel(), new JScrollPane(buildSalesPanel()));
        split.setResizeWeight(0.45);
        split.setBorder(null);
        frame.add(split, BorderLayout.CENTER);
        frame.setSize(980, 560);
        frame.setMinimumSize(new Dimension(720, 480));
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
        check(base.cloneFor("C", "V", "D").sold == 0, "a clone must start with fresh sales");
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