package library.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.Member;
import library.model.MemberType;
import library.service.LibraryService;

/** Screen for registering, searching and removing library members. */
public class MembersPanel extends RefreshablePanel {

    private static final long serialVersionUID = 1L;

    private final transient DefaultTableModel model = readOnlyModel(
            "ID", "Type", "Name", "Email", "Max Loans", "Extra Loan Days", "Fee Discount", "Items Borrowed");
    private final JTable table = createTable(model);

    private final JTextField searchField = Theme.field(24);
    private final JComboBox<MemberType> typeBox = Theme.styleCombo(new JComboBox<>(MemberType.values()), 140);
    private final JTextField nameField = Theme.field(18);
    private final JTextField emailField = Theme.field(22);

    public MembersPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
        setColumnWidths(table, 60, 90, 190, 240, 90, 120, 110, 120);
    }

    private JPanel buildSearchBar() {
        JButton searchButton = Theme.button("Search", Theme.Kind.PRIMARY);
        JButton clearButton = Theme.button("Show All", Theme.Kind.SECONDARY);

        JPanel row = Theme.row(14);
        row.add(Theme.labeled("SEARCH BY ID, NAME OR EMAIL", searchField));
        row.add(Theme.labeled(" ", searchButton));
        row.add(Theme.labeled(" ", clearButton));

        searchButton.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refresh();
        });

        JPanel card = Theme.card(new BorderLayout());
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Registered members"), BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildForm() {
        JPanel fields = Theme.row(14);
        fields.add(Theme.labeled("MEMBER TYPE", typeBox));
        fields.add(Theme.labeled("FULL NAME", nameField));
        fields.add(Theme.labeled("EMAIL", emailField));

        JButton addButton = Theme.button("Register Member", Theme.Kind.PRIMARY);
        JButton deleteButton = Theme.button("Delete Selected", Theme.Kind.DANGER);
        JPanel buttons = Theme.row(10);
        buttons.add(addButton);
        buttons.add(deleteButton);

        addButton.addActionListener(e -> addMember());
        deleteButton.addActionListener(e -> deleteMember());

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 14));
        body.setOpaque(false);
        body.add(fields);
        body.add(buttons);

        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Member details"), BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void addMember() {
        boolean ok = attempt(() -> {
            Member member = service.addMember((MemberType) typeBox.getSelectedItem(),
                    nameField.getText(), emailField.getText());
            showInfo("Registered " + member.getType().getLabel() + " " + member.getId() + ": " + member.getName());
        });
        if (ok) {
            nameField.setText("");
            emailField.setText("");
            onDataChanged.run();
        }
    }

    private void deleteMember() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select a member in the table first.");
            return;
        }
        if (!confirm("Delete member " + id + "?")) {
            return;
        }
        if (attempt(() -> service.removeMember(id))) {
            onDataChanged.run();
        }
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        for (Member m : service.searchMembers(searchField.getText())) {
            model.addRow(new Object[] {
                    m.getId(),
                    m.getType().getLabel(),
                    m.getName(),
                    m.getEmail(),
                    m.getMaxLoans(),
                    m.getExtraLoanDays(),
                    Math.round(m.getFeeDiscount() * 100) + "%",
                    service.countActiveLoans(m)
            });
        }
    }
}
