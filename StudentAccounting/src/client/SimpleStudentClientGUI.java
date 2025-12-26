package client;

import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class SimpleStudentClientGUI extends JFrame {
    private ServerConnector connector;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtLastName, txtFirstName, txtGroup, txtYear;

    public SimpleStudentClientGUI() {
        connector = new ServerConnector();
        if (!connector.connect()) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Картотека студентов");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                connector.disconnect();
            }
        });

        // Простой интерфейс как в исходной версии
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // Панель ввода
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        inputPanel.add(new JLabel("Фамилия:"));
        txtLastName = new JTextField();
        inputPanel.add(txtLastName);
        inputPanel.add(new JLabel("Имя:"));
        txtFirstName = new JTextField();
        inputPanel.add(txtFirstName);
        inputPanel.add(new JLabel("Группа:"));
        txtGroup = new JTextField();
        inputPanel.add(txtGroup);
        inputPanel.add(new JLabel("Год:"));
        txtYear = new JTextField();
        inputPanel.add(txtYear);

        JButton btnAdd = new JButton("Добавить");
        btnAdd.addActionListener(e -> addStudent());
        JButton btnRefresh = new JButton("Обновить");
        btnRefresh.addActionListener(e -> refreshTable());
        inputPanel.add(btnAdd);
        inputPanel.add(btnRefresh);

        container.add(inputPanel, BorderLayout.NORTH);

        // Таблица
        String[] columns = {"ID", "Фамилия", "Имя", "Группа", "Год"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        container.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
        setVisible(true);
    }

    private void addStudent() {
        try {
            String lastName = txtLastName.getText().trim();
            String firstName = txtFirstName.getText().trim();
            String group = txtGroup.getText().trim();
            int year = Integer.parseInt(txtYear.getText().trim());

            Student student = new Student(0, lastName, firstName, group, year);
            if (connector.addStudent(student)) {
                JOptionPane.showMessageDialog(this, "Студент добавлен");
                clearFields();
                refreshTable();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        try {
            List<Student> students = connector.getAllStudents();
            tableModel.setRowCount(0);
            for (Student s : students) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getLastName(),
                        s.getFirstName(),
                        s.getGroup(),
                        s.getYearOfAdmission()
                });
            }
            setTitle("Картотека студентов (" + students.size() + " записей)");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtGroup.setText("");
        txtYear.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleStudentClientGUI());
    }
}