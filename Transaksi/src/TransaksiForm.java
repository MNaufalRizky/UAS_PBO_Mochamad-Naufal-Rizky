import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransaksiForm extends JFrame {
    private JTextField namaBarangField;
    private JTextField jumlahField;
    private JTextField hargaField;
    private JTextField idField;
    private JButton submitButton;
    private JButton deleteButton;
    private JTable transaksiTable;
    private DefaultTableModel tableModel;

    public TransaksiForm() {
        setTitle("Toko Sparepart King Sky Motor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Nama Barang:"));
        namaBarangField = new JTextField();
        panel.add(namaBarangField);

        panel.add(new JLabel("Jumlah:"));
        jumlahField = new JTextField();
        panel.add(jumlahField);

        panel.add(new JLabel("Harga:"));
        hargaField = new JTextField();
        panel.add(hargaField);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitForm();
            }
        });
        panel.add(submitButton);

        // Buat tabel untuk menampilkan data transaksi
        String[] columnNames = {"ID", "Nama Barang", "Jumlah", "Harga", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        transaksiTable = new JTable(tableModel);

        // Tambahkan tabel ke panel dengan JScrollPane
        JScrollPane scrollPane = new JScrollPane(transaksiTable);
        panel.add(scrollPane);

        // Tambahkan field dan tombol untuk menghapus data
        panel.add(new JLabel("ID:"));
        idField = new JTextField();
        panel.add(idField);

        deleteButton = new JButton("Hapus");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });
        panel.add(deleteButton);

        add(panel);

        // Tampilkan data saat form pertama kali dibuka
        displayData();
    }

    private void submitForm() {
        String namaBarang = namaBarangField.getText();
        int jumlah = Integer.parseInt(jumlahField.getText());
        double harga = Double.parseDouble(hargaField.getText());
        double total = jumlah * harga;

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Cek apakah barang sudah ada
            String checkQuery = "SELECT * FROM transaksi WHERE nama_barang = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, namaBarang);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Barang sudah ada, update data barang
                    int existingJumlah = rs.getInt("jumlah");
                    double existingHarga = rs.getDouble("harga");
                    double existingTotal = rs.getDouble("total");

                    // Perbarui jumlah dan total
                    jumlah += existingJumlah;
                    total = jumlah * harga;

                    String updateQuery = "UPDATE transaksi SET jumlah = ?, harga = ?, total = ? WHERE nama_barang = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, jumlah);
                        updateStmt.setDouble(2, harga);
                        updateStmt.setDouble(3, total);
                        updateStmt.setString(4, namaBarang);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Barang belum ada, tambahkan barang baru
                    String insertQuery = "INSERT INTO transaksi (nama_barang, jumlah, harga, total) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, namaBarang);
                        insertStmt.setInt(2, jumlah);
                        insertStmt.setDouble(3, harga);
                        insertStmt.setDouble(4, total);
                        insertStmt.executeUpdate();
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!");
            displayData(); // Refresh data after insertion/update
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void displayData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM transaksi";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                // Kosongkan tabel sebelum menambahkan data baru
                tableModel.setRowCount(0);

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String namaBarang = resultSet.getString("nama_barang");
                    int jumlah = resultSet.getInt("jumlah");
                    double harga = resultSet.getDouble("harga");
                    double total = resultSet.getDouble("total");

                    Object[] row = {id, namaBarang, jumlah, harga, total};
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteData() {
        int id = Integer.parseInt(idField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM transaksi WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                int rowsDeleted = statement.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                    displayData();
                } else {
                    JOptionPane.showMessageDialog(this, "ID tidak ditemukan.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TransaksiForm().setVisible(true);
            }
        });
    }
}
