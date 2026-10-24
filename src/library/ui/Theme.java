package library.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * The look of the whole application in one place: colors, fonts and small
 * factory methods that build styled buttons, fields, tables and cards.
 *
 * Every screen asks Theme for its components, so changing a color here changes
 * the design everywhere. (This class only draws; it has no library logic.)
 */
public final class Theme {

    // ---------- color palette ----------
    public static final Color SIDEBAR = new Color(0x0F172A);
    public static final Color SIDEBAR_HOVER = new Color(0x1E293B);
    public static final Color SIDEBAR_TEXT = new Color(0x94A3B8);

    public static final Color ACCENT = new Color(0x4F46E5);
    public static final Color ACCENT_DARK = new Color(0x4338CA);
    public static final Color ACCENT_SOFT = new Color(0xE0E7FF);

    public static final Color BACKGROUND = new Color(0xF1F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color STRIPE = new Color(0xF8FAFC);
    public static final Color BORDER = new Color(0xE2E8F0);

    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color DANGER = new Color(0xB91C1C);

    private static String fontFamily;

    private Theme() {
        // utility class - not meant to be instantiated
    }

    // ---------- setup ----------

    /** Call once, before any window is created. */
    public static void install() {
        UIManager.put("nimbusBase", new Color(0x3730A3));
        UIManager.put("nimbusBlueGrey", new Color(0x94A3B8));
        UIManager.put("control", BACKGROUND);
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("nimbusSelectionBackground", ACCENT);
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("text", TEXT);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // keep the default look and feel
        }
        UIManager.getLookAndFeelDefaults().put("defaultFont", font(13f, Font.PLAIN));
    }

    private static String family() {
        if (fontFamily == null) {
            Set<String> names = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            fontFamily = Font.SANS_SERIF;
            for (String candidate : new String[] {"Segoe UI", "Inter", "Helvetica Neue", "Noto Sans", "DejaVu Sans"}) {
                if (names.contains(candidate)) {
                    fontFamily = candidate;
                    break;
                }
            }
        }
        return fontFamily;
    }

    public static Font font(float size, int style) {
        return new Font(family(), style, 12).deriveFont(size);
    }

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ---------- labels and small layout helpers ----------

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(15f, Font.BOLD));
        label.setForeground(TEXT);
        return label;
    }

    public static void styleCaption(JLabel label) {
        label.setFont(font(11.5f, Font.BOLD));
        label.setForeground(MUTED);
    }

    /** A small grey caption above a component (used for every form field). */
    public static JPanel labeled(String caption, JComponent component) {
        JLabel label = new JLabel(caption);
        return labeled(label, component);
    }

    public static JPanel labeled(JLabel caption, JComponent component) {
        styleCaption(caption);
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(caption, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    /** A transparent left-aligned row of components. */
    public static JPanel row(int gap) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        panel.setOpaque(false);
        return panel;
    }

    // ---------- cards ----------

    public static JPanel card(LayoutManager layout) {
        return new Card(layout, Color.WHITE);
    }

    /** A card used for one big number on the reports screen. */
    public static JPanel statCard(String caption, JLabel value, Color valueColor) {
        JPanel card = new Card(new BorderLayout(0, 6), Color.WHITE);
        JLabel title = new JLabel(caption);
        styleCaption(title);
        value.setFont(font(22f, Font.BOLD));
        value.setForeground(valueColor);
        value.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private static final class Card extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fill;

        Card(LayoutManager layout, Color fill) {
            super(layout);
            this.fill = fill;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(fill);
            g2.fill(shape);
            g2.setColor(BORDER);
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- buttons ----------

    public enum Kind { PRIMARY, SECONDARY, DANGER }

    public static JButton button(String text, Kind kind) {
        return new RoundedButton(text, kind);
    }

    private static final class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Kind kind;

        RoundedButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(font(13f, Font.BOLD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 38, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isArmed() && getModel().isPressed();
            Color background;
            Color foreground;
            Color outline;
            switch (kind) {
                case PRIMARY -> {
                    background = pressed ? new Color(0x3730A3) : hover ? ACCENT_DARK : ACCENT;
                    foreground = Color.WHITE;
                    outline = null;
                }
                case DANGER -> {
                    background = pressed ? new Color(0xFEE2E2) : hover ? new Color(0xFEF2F2) : Color.WHITE;
                    foreground = DANGER;
                    outline = new Color(0xFCA5A5);
                }
                default -> {
                    background = pressed ? new Color(0xE2E8F0) : hover ? STRIPE : Color.WHITE;
                    foreground = TEXT;
                    outline = new Color(0xCBD5E1);
                }
            }
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(background);
            g2.fill(shape);
            if (outline != null) {
                g2.setColor(outline);
                g2.draw(shape);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(foreground);
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ---------- text fields and combo boxes ----------

    private static Border fieldBorder(Color line) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        field.setUI(new BasicTextFieldUI());
        field.setFont(font(13f, Font.PLAIN));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setSelectionColor(ACCENT_SOFT);
        field.setSelectedTextColor(TEXT);
        field.setBorder(fieldBorder(new Color(0xCBD5E1)));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(fieldBorder(ACCENT));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(fieldBorder(new Color(0xCBD5E1)));
            }
        });
        return field;
    }

    public static <T> JComboBox<T> styleCombo(JComboBox<T> box) {
        box.setUI(new FlatComboUI());
        box.setFont(font(13f, Font.PLAIN));
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT);
        box.setBorder(BorderFactory.createLineBorder(new Color(0xCBD5E1), 1, true));
        box.setPreferredSize(new Dimension(Math.max(box.getPreferredSize().width, 130), 38));
        box.setRenderer(new ComboRenderer());
        return box;
    }

    public static <T> JComboBox<T> styleCombo(JComboBox<T> box, int width) {
        styleCombo(box);
        box.setPreferredSize(new Dimension(width, 38));
        return box;
    }

    public static <T> JComboBox<T> combo(ComboBoxModel<T> model, int width) {
        return styleCombo(new JComboBox<>(model), width);
    }

    /** Padding and colors for the items in a combo box. */
    private static final class ComboRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
            label.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
            label.setFont(font(13f, Font.PLAIN));
            label.setBackground(isSelected ? ACCENT_SOFT : Color.WHITE);
            label.setForeground(TEXT);
            return label;
        }
    }

    private static final class FlatComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            BasicArrowButton arrow = new BasicArrowButton(SwingConstants.SOUTH, Color.WHITE, Color.WHITE, MUTED,
                    Color.WHITE);
            arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return arrow;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(new Color(0xCBD5E1)));
            return popup;
        }
    }

    // ---------- tables ----------

    public static JScrollPane scroll(JComponent view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        return pane;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(font(13f, Font.PLAIN));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setDefaultRenderer(Number.class, new CellRenderer());
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
    }

    /** Striped rows; status words get a color so they can be read at a glance. */
    private static final class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            String text = value == null ? "" : value.toString();
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            label.setBackground(isSelected ? ACCENT_SOFT : (row % 2 == 0 ? Color.WHITE : STRIPE));
            label.setFont(font(13f, Font.PLAIN));
            label.setForeground(TEXT);
            if (text.equals("Available") || text.equals("On time")) {
                label.setForeground(SUCCESS);
                label.setFont(font(13f, Font.BOLD));
            } else if (text.equals("On loan")) {
                label.setForeground(WARNING);
                label.setFont(font(13f, Font.BOLD));
            } else if (text.startsWith("OVERDUE")) {
                label.setForeground(DANGER);
                label.setFont(font(13f, Font.BOLD));
            }
            return label;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                    value == null ? "" : value.toString().toUpperCase(), false, false, row, column);
            label.setOpaque(true);
            label.setBackground(new Color(0xF8FAFC));
            label.setForeground(MUTED);
            label.setFont(font(11f, Font.BOLD));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            return label;
        }
    }

    // ---------- sidebar navigation button ----------

    /** One entry of the side menu: an icon drawn with shapes plus a label. */
    public static final class NavButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final int icon;
        private boolean active;

        public NavButton(String text, int icon) {
            super(text);
            this.icon = icon;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(font(14f, Font.BOLD));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(200, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            if (active) {
                g2.setColor(ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            } else if (getModel().isRollover()) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            }
            Color color = active ? Color.WHITE : SIDEBAR_TEXT;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g2, 16, (getHeight() - 20) / 2);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(color);
            g2.drawString(getText(), 48, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }

        /** 0 = books, 1 = person, 2 = swap arrows, 3 = bars. */
        private void drawIcon(Graphics2D g2, int x, int y) {
            switch (icon) {
                case 0 -> {
                    g2.drawRoundRect(x + 2, y + 1, 14, 18, 4, 4);
                    g2.drawLine(x + 7, y + 1, x + 7, y + 19);
                }
                case 1 -> {
                    g2.drawOval(x + 5, y + 1, 8, 8);
                    g2.drawArc(x + 1, y + 11, 16, 14, 0, 180);
                }
                case 2 -> {
                    g2.drawLine(x + 1, y + 6, x + 17, y + 6);
                    g2.drawLine(x + 13, y + 2, x + 17, y + 6);
                    g2.drawLine(x + 13, y + 10, x + 17, y + 6);
                    g2.drawLine(x + 17, y + 15, x + 1, y + 15);
                    g2.drawLine(x + 5, y + 11, x + 1, y + 15);
                    g2.drawLine(x + 5, y + 19, x + 1, y + 15);
                }
                default -> {
                    g2.drawLine(x + 3, y + 19, x + 3, y + 12);
                    g2.drawLine(x + 9, y + 19, x + 9, y + 6);
                    g2.drawLine(x + 15, y + 19, x + 15, y + 1);
                }
            }
        }
    }

    /** The round logo at the top of the sidebar. */
    public static final class Logo extends JComponent {
        private static final long serialVersionUID = 1L;

        public Logo() {
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(ACCENT);
            g2.fill(new RoundRectangle2D.Float(0, 0, 44, 44, 14, 14));
            g2.setColor(Color.WHITE);
            int[] leftX = {10, 21, 21, 10};
            int[] leftY = {13, 16, 33, 30};
            int[] rightX = {23, 34, 34, 23};
            int[] rightY = {16, 13, 30, 33};
            g2.fillPolygon(leftX, leftY, 4);
            g2.fillPolygon(rightX, rightY, 4);
            g2.dispose();
        }
    }
}
