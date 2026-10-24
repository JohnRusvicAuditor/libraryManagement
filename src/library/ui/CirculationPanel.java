package library.ui;

import java.awt.BorderLayout;
import java.time.LocalDate;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.LibraryItem;
import library.model.Loan;
import library.model.Member;
import library.service.LibraryService;
import library.util.Money;
import library.util.Validator;

/** Screen for borrowing and returning items (the main transaction screen). */
public class CirculationPanel extends RefreshablePanel {

    private static final long serialVersionUID = 1L;

    private final transient DefaultComboBoxModel<LibraryItem> itemModel = new DefaultComboBoxModel<>();
    private final transient DefaultComboBoxModel<Member> memberModel = new DefaultComboBoxModel<>();
    private final JComboBox<LibraryItem> itemBox = Theme.combo(itemModel, 300);
    private final JComboBox<Member> memberBox = Theme.combo(memberModel, 260);
    private final JTextField borrowDateField = Theme.field(9);

    private final transient DefaultTableModel loanModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Borrowed", "Due", "Status");
    private final JTable loanTable = createTable(loanModel);
    private final JTextField returnDateField = Theme.field(9);

    public CirculationPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        borrowDateField.setText(LocalDate.now().toString());
        returnDateField.setText(LocalDate.now().toString());
        add(buildBorrowPanel(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildReturnPanel(), BorderLayout.SOUTH);
        setColumnWidths(loanTable, 80, 260, 240, 120, 120, 160);
    }

    private JPanel buildBorrowPanel() {
        JButton borrowButton = Theme.button("Borrow", Theme.Kind.PRIMARY);
        borrowButton.addActionListener(e -> borrow());

        JPanel row = Theme.row(14);
        row.add(Theme.labeled("AVAILABLE ITEM", itemBox));
        row.add(Theme.labeled("MEMBER", memberBox));
        row.add(Theme.labeled("BORROW DATE (YYYY-MM-DD)", borrowDateField));
        row.add(Theme.labeled(" ", borrowButton));

        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Borrow an item"), BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Active loans"), BorderLayout.NORTH);
        card.add(Theme.scroll(loanTable), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReturnPanel() {
        JButton returnButton = Theme.button("Return Selected Item", Theme.Kind.PRIMARY);
        returnButton.addActionListener(e -> returnSelected());

        JPanel row = Theme.row(14);
        row.add(Theme.labeled("RETURN DATE (YYYY-MM-DD)", returnDateField));
        row.add(Theme.labeled(" ", returnButton));

        JPanel card = Theme.card(new BorderLayout(0, 12));
        card.add(Theme.sectionTitle("Return the selected loan"), BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private void borrow() {
        LibraryItem item = (LibraryItem) itemBox.getSelectedItem();
        Member member = (Member) memberBox.getSelectedItem();
        if (item == null) {
            showError("There is no available item to borrow.");
            return;
        }
        if (member == null) {
            showError("Register a member first.");
            return;
        }
        boolean ok = attempt(() -> {
            LocalDate date = Validator.parseDate("Borrow date", borrowDateField.getText());
            Loan loan = service.borrowItem(item.getId(), member.getId(), date);
            showInfo(member.getName() + " borrowed \"" + item.getTitle() + "\".\nDue date: " + loan.getDueDate());
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    private void returnSelected() {
        String idText = selectedValue(loanTable, 0);
        if (idText == null) {
            showError("Select a loan in the table first.");
            return;
        }
        boolean ok = attempt(() -> {
            LocalDate date = Validator.parseDate("Return date", returnDateField.getText());
            Loan loan = service.returnItem(Integer.parseInt(idText), date);
            long late = loan.getDaysLate(date);
            String message = "\"" + loan.getItem().getTitle() + "\" returned.\n";
            message += late > 0
                    ? "Days late: " + late + "\nLate fee: " + Money.format(loan.getFeeCharged())
                    : "Returned on time. No fee.";
            showInfo(message);
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    @Override
    public void refresh() {
        Object selectedItem = itemBox.getSelectedItem();
        Object selectedMember = memberBox.getSelectedItem();

        itemModel.removeAllElements();
        for (LibraryItem item : service.getAvailableItems()) {
            itemModel.addElement(item);
        }
        memberModel.removeAllElements();
        for (Member member : service.searchMembers("")) {
            memberModel.addElement(member);
        }
        if (selectedItem != null && itemModel.getIndexOf(selectedItem) >= 0) {
            itemBox.setSelectedItem(selectedItem);
        }
        if (selectedMember != null && memberModel.getIndexOf(selectedMember) >= 0) {
            memberBox.setSelectedItem(selectedMember);
        }

        LocalDate today = LocalDate.now();
        loanModel.setRowCount(0);
        for (Loan loan : service.getActiveLoans()) {
            loanModel.addRow(new Object[] {
                    loan.getId(),
                    loan.getItem().toString(),
                    loan.getMember().toString(),
                    loan.getBorrowDate(),
                    loan.getDueDate(),
                    loan.isOverdue(today) ? "OVERDUE (" + loan.getDaysLate(today) + " days)" : "On time"
            });
        }
    }
}
