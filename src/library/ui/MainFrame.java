package library.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import library.service.LibraryService;

/**
 * The main application window: a dark sidebar for navigation, a header with
 * the page title, and a CardLayout that shows one RefreshablePanel at a time.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String[] TITLES = {"Items", "Members", "Borrow / Return", "Reports"};
    private static final String[] SUBTITLES = {
        "Manage books, magazines and DVDs in the collection.",
        "Register students and faculty and manage their accounts.",
        "Lend items to members and process returns with late fees.",
        "Library statistics, overdue items and loan history."
    };

    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final List<RefreshablePanel> panels = new ArrayList<>();
    private final List<Theme.NavButton> navButtons = new ArrayList<>();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();

    public MainFrame(LibraryService service) {
        super("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());

        Runnable refreshAll = () -> panels.forEach(RefreshablePanel::refresh);
        panels.add(new ItemsPanel(service, refreshAll));
        panels.add(new MembersPanel(service, refreshAll));
        panels.add(new CirculationPanel(service, refreshAll));
        panels.add(new ReportsPanel(service, refreshAll));
        for (int i = 0; i < panels.size(); i++) {
            pages.add(panels.get(i), TITLES[i]);
        }
        pages.setOpaque(false);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(pages, BorderLayout.CENTER);

        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        refreshAll.run();
        navigate(0);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new Theme.Logo(), BorderLayout.WEST);
        JPanel names = new JPanel(new GridLayout(2, 1));
        names.setOpaque(false);
        JLabel name = new JLabel("Library");
        name.setFont(Theme.font(17f, java.awt.Font.BOLD));
        name.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Management System");
        sub.setFont(Theme.font(11f, java.awt.Font.PLAIN));
        sub.setForeground(Theme.SIDEBAR_TEXT);
        names.add(name);
        names.add(sub);
        brand.add(names, BorderLayout.CENTER);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        side.add(brand);
        side.add(Box.createVerticalStrut(32));

        for (int i = 0; i < TITLES.length; i++) {
            final int index = i;
            Theme.NavButton button = new Theme.NavButton(TITLES[i], i);
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.addActionListener(e -> navigate(index));
            navButtons.add(button);
            side.add(button);
            side.add(Box.createVerticalStrut(6));
        }
        side.add(Box.createVerticalGlue());

        JLabel foot = new JLabel("Java OOP Project");
        foot.setFont(Theme.font(11f, java.awt.Font.PLAIN));
        foot.setForeground(Theme.SIDEBAR_TEXT);
        foot.setAlignmentX(LEFT_ALIGNMENT);
        side.add(foot);
        return side;
    }

    private JPanel buildHeader() {
        pageTitle.setFont(Theme.font(26f, java.awt.Font.BOLD));
        pageTitle.setForeground(Theme.TEXT);
        pageSubtitle.setFont(Theme.font(13f, java.awt.Font.PLAIN));
        pageSubtitle.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(pageTitle);
        left.add(pageSubtitle);

        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));
        date.setFont(Theme.font(13f, java.awt.Font.PLAIN));
        date.setForeground(Theme.MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(left, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);
        return header;
    }

    /** Shows one page. Polymorphism: every page is a RefreshablePanel that refreshes itself. */
    private void navigate(int index) {
        cards.show(pages, TITLES[index]);
        panels.get(index).refresh();
        pageTitle.setText(TITLES[index]);
        pageSubtitle.setText(SUBTITLES[index]);
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == index);
        }
    }

    private JMenuBar buildMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        fileMenu.add(exit);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Library Management System\nJava OOP Project - Encapsulation, Inheritance,\n"
                        + "Polymorphism and Abstraction.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(about);

        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }
}
