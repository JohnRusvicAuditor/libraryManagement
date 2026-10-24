package library.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.Loan;
import library.service.LibraryService;
import library.util.Money;
import library.util.Validator;

/** Screen with library statistics, the overdue report and the full loan history. */
public class ReportsPanel extends RefreshablePanel {

    private static final long serialVersionUID = 1L;

    private final JLabel totalItems = new JLabel("0");
    private final JLabel availableItems = new JLabel("0");
    private final JLabel members = new JLabel("0");
    private final JLabel activeLoans = new JLabel("0");
    private final JLabel overdueLoans = new JLabel("0");
    private final JLabel feesCollected = new JLabel("0");

    private final JTextField asOfField = Theme.field(9);
    private final transient DefaultTableModel overdueModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Due", "Days Late", "Fee If Returned Today");
    private final transient DefaultTableModel historyModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Borrowed", "Due", "Returned", "Fee Charged");

    public ReportsPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        asOfField.setText(LocalDate.now().toString());
        add(buildStatsBar(), BorderLayout.NORTH);

        JPanel overdueCard = Theme.card(new BorderLayout(0, 12));
        overdueCard.add(buildOverdueHeader(), BorderLayout.NORTH);
        overdueCard.add(Theme.scroll(createTable(overdueModel)), BorderLayout.CENTER);

        JPanel historyCard = Theme.card(new BorderLayout(0, 12));
        historyCard.add(Theme.sectionTitle("Loan history"), BorderLayout.NORTH);
        historyCard.add(Theme.scroll(createTable(historyModel)), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);
        tables.add(overdueCard);
        tables.add(historyCard);
        add(tables, BorderLayout.CENTER);
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 6, 14, 0));
        bar.setOpaque(false);
        bar.add(Theme.statCard("TOTAL ITEMS", totalItems, Theme.TEXT));
        bar.add(Theme.statCard("AVAILABLE", availableItems, Theme.SUCCESS));
        bar.add(Theme.statCard("MEMBERS", members, Theme.TEXT));
        bar.add(Theme.statCard("ACTIVE LOANS", activeLoans, Theme.ACCENT));
        bar.add(Theme.statCard("OVERDUE", overdueLoans, Theme.DANGER));
        bar.add(Theme.statCard("FEES COLLECTED", feesCollected, Theme.SUCCESS));
        return bar;
    }

    private JPanel buildOverdueHeader() {
        JButton button = Theme.button("Update Report", Theme.Kind.SECONDARY);
        button.addActionListener(e -> {
            if (attempt(() -> Validator.parseDate("Report date", asOfField.getText()))) {
                refresh();
            }
        });

        JPanel controls = Theme.row(10);
        controls.add(Theme.labeled("SHOW OVERDUE AS OF (YYYY-MM-DD)", asOfField));
        controls.add(Theme.labeled(" ", button));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.sectionTitle("Overdue report"), BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    @Override
    public void refresh() {
        LocalDate asOf;
        try {
            asOf = Validator.parseDate("Report date", asOfField.getText());
        } catch (IllegalArgumentException ex) {
            asOf = LocalDate.now();
            asOfField.setText(asOf.toString());
        }

        totalItems.setText(String.valueOf(service.getTotalItems()));
        availableItems.setText(String.valueOf(service.getAvailableItemCount()));
        members.setText(String.valueOf(service.getTotalMembers()));
        activeLoans.setText(String.valueOf(service.getActiveLoans().size()));
        overdueLoans.setText(String.valueOf(service.getOverdueLoans(asOf).size()));
        feesCollected.setText(Money.format(service.getTotalFeesCollected()));

        overdueModel.setRowCount(0);
        for (Loan loan : service.getOverdueLoans(asOf)) {
            long late = loan.getDaysLate(asOf);
            double fee = loan.getMember().applyDiscount(loan.getItem().calculateLateFee(late));
            overdueModel.addRow(new Object[] {
                    loan.getId(), loan.getItem().toString(), loan.getMember().toString(),
                    loan.getDueDate(), late, Money.format(fee)
            });
        }

        historyModel.setRowCount(0);
        for (Loan loan : service.getAllLoans()) {
            historyModel.addRow(new Object[] {
                    loan.getId(), loan.getItem().toString(), loan.getMember().toString(),
                    loan.getBorrowDate(), loan.getDueDate(),
                    loan.isReturned() ? loan.getReturnDate().toString() : "-",
                    loan.isReturned() ? Money.format(loan.getFeeCharged()) : "-"
            });
        }
    }
}
