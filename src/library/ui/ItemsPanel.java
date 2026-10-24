package library.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.ItemType;
import library.model.LibraryItem;
import library.service.LibraryService;
import library.util.Money;

/** Screen for adding, searching, updating and deleting library items. */
public class ItemsPanel extends RefreshablePanel {

    private static final long serialVersionUID = 1L;

    private final transient DefaultTableModel model = readOnlyModel(
            "ID", "Type", "Title", "Author / Creator", "Year", "Details", "Loan Days", "Late Fee / Day", "Status");
    private final JTable table = createTable(model);

    private final JTextField searchField = Theme.field(22);
    private final JComboBox<String> filterBox = Theme.styleCombo(new JComboBox<>(), 150);

    private final JComboBox<ItemType> typeBox = Theme.styleCombo(new JComboBox<>(ItemType.values()), 140);
    private final JTextField titleField = Theme.field(16);
    private final JTextField creatorField = Theme.field(14);
    private final JTextField yearField = Theme.field(5);
    private final JLabel extraLabel = new JLabel();
    private final JTextField extraField = Theme.field(12);

    public ItemsPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);

        filterBox.addItem("All types");
        for (ItemType type : ItemType.values()) {
            filterBox.addItem(type.getLabel());
        }

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
        setColumnWidths(table, 50, 70, 190, 150, 50, 190, 80, 100, 80);

        typeBox.addActionListener(e -> updateExtraLabel());
        updateExtraLabel();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedIntoForm();
            }
        });
    }

    private JPanel buildSearchBar() {
        JButton searchButton = Theme.button("Search", Theme.Kind.PRIMARY);
        JButton clearButton = Theme.button("Show All", Theme.Kind.SECONDARY);

        JPanel row = Theme.row(14);
        row.add(Theme.labeled("SEARCH BY ID, TITLE OR AUTHOR", searchField));
        row.add(Theme.labeled("TYPE", filterBox));
        row.add(Theme.labeled(" ", searchButton));
        row.add(Theme.labeled(" ", clearButton));

        searchButton.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterBox.setSelectedIndex(0);
            refresh();
        });

        JPanel card = Theme.card(new BorderLayout());
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Library collection"), BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildForm() {
        JPanel fields = Theme.row(14);
        fields.add(Theme.labeled("TYPE", typeBox));
        fields.add(Theme.labeled("TITLE", titleField));
        fields.add(Theme.labeled("AUTHOR / CREATOR", creatorField));
        fields.add(Theme.labeled("YEAR", yearField));
        fields.add(Theme.labeled(extraLabel, extraField));

        JButton addButton = Theme.button("Add Item", Theme.Kind.PRIMARY);
        JButton updateButton = Theme.button("Update Selected", Theme.Kind.SECONDARY);
        JButton deleteButton = Theme.button("Delete Selected", Theme.Kind.DANGER);
        JButton clearButton = Theme.button("Clear Form", Theme.Kind.SECONDARY);
        JPanel buttons = Theme.row(10);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        clearButton.addActionListener(e -> clearForm());

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 14));
        body.setOpaque(false);
        body.add(fields);
        body.add(buttons);

        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Item details"), BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void updateExtraLabel() {
        ItemType type = (ItemType) typeBox.getSelectedItem();
        extraLabel.setText(type.getExtraLabel().toUpperCase());
    }

    private ItemType selectedFilter() {
        int index = filterBox.getSelectedIndex();
        return index <= 0 ? null : ItemType.values()[index - 1];
    }

    private void addItem() {
        boolean ok = attempt(() -> {
            LibraryItem item = service.addItem((ItemType) typeBox.getSelectedItem(),
                    titleField.getText(), creatorField.getText(), yearField.getText(), extraField.getText());
            showInfo("Added " + item.getType().getLabel() + " " + item.getId() + ": " + item.getTitle());
        });
        if (ok) {
            clearForm();
            onDataChanged.run();
        }
    }

    private void updateItem() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select an item in the table first.");
            return;
        }
        boolean ok = attempt(() -> {
            service.updateItem(id, titleField.getText(), creatorField.getText(),
                    yearField.getText(), extraField.getText());
            showInfo("Item " + id + " updated.");
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    private void deleteItem() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select an item in the table first.");
            return;
        }
        if (!confirm("Delete item " + id + "?")) {
            return;
        }
        boolean ok = attempt(() -> service.removeItem(id));
        if (ok) {
            clearForm();
            onDataChanged.run();
        }
    }

    private void loadSelectedIntoForm() {
        String id = selectedValue(table, 0);
        if (id == null) {
            return;
        }
        service.findItem(id).ifPresent(item -> {
            typeBox.setSelectedItem(item.getType());
            titleField.setText(item.getTitle());
            creatorField.setText(item.getCreator());
            yearField.setText(String.valueOf(item.getYear()));
            extraField.setText(item.getExtraValue());
        });
    }

    private void clearForm() {
        table.clearSelection();
        titleField.setText("");
        creatorField.setText("");
        yearField.setText("");
        extraField.setText("");
    }

    @Override
    public void refresh() {
        List<LibraryItem> found = service.searchItems(searchField.getText(), selectedFilter());
        model.setRowCount(0);
        for (LibraryItem item : found) {
            model.addRow(new Object[] {
                    item.getId(),
                    item.getType().getLabel(),
                    item.getTitle(),
                    item.getCreator(),
                    item.getYear(),
                    item.getExtraLabel() + ": " + item.getExtraValue(),
                    item.getLoanPeriodDays(),
                    Money.format(item.getLateFeePerDay()),
                    item.isAvailable() ? "Available" : "On loan"
            });
        }
    }
}
