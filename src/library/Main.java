package library;

import javax.swing.SwingUtilities;

import library.service.LibraryService;
import library.service.SampleData;
import library.ui.MainFrame;
import library.ui.Theme;

/** Entry point of the Library Management System. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            LibraryService service = new LibraryService();
            SampleData.load(service);
            new MainFrame(service).setVisible(true);
        });
    }
}
