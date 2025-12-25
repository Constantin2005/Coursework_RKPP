// Файл: StudentClientGUI.java (View + Controller)
package client;

import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentClientGUI extends JFrame {
    private ServerConnector connector;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtLastName, txtFirstName, txtGroup, txtYear, txtSearch;

    public StudentClientGUI() {
        connector = new ServerConnector();
        if (!connector.connect()) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Картотека студентов");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель ввода данных
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
        inputPanel.add(new JLabel("Год поступления:"));
        txtYear = new JTextField();
        inputPanel.add(txtYear);

        JButton btnAdd = new JButton("Добавить");
        JButton btnRefresh = new JButton("Обновить");
        JButton btnDelete = new JButton("Удалить выбранного");
        inputPanel.add(btnAdd);
        inputPanel.add(btnRefresh);
        inputPanel.add(btnDelete);

        add(inputPanel, BorderLayout.NORTH);

        // Таблица для отображения
        String[] columns = {"ID", "Фамилия", "Имя", "Группа", "Год"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Поиск по фамилии:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);
        JButton btnSearch = new JButton("Найти");
        searchPanel.add(btnSearch);
        JButton btnShowAll = new JButton("Показать всех");
        searchPanel.add(btnShowAll);
        add(searchPanel, BorderLayout.SOUTH);

        // Обработчики событий (Controller логика)
        btnAdd.addActionListener(e -> addStudent());
        btnRefresh.addActionListener(e -> refreshTable());
        btnDelete.addActionListener(e -> deleteStudent());
        btnSearch.addActionListener(e -> searchStudents());
        btnShowAll.addActionListener(e -> refreshTable());

        // Загружаем данные при запуске
        refreshTable();
        setVisible(true);
    }

    private void addStudent() {
        try {
            String lastName = txtLastName.getText();
            String firstName = txtFirstName.getText();
            String group = txtGroup.getText();
            int year = Integer.parseInt(txtYear.getText());

            Student student = new Student(0, lastName, firstName, group, year); // ID установится на сервере
            if (connector.addStudent(student)) {
                JOptionPane.showMessageDialog(this, "Студент добавлен");
                clearInputFields();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка добавления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный год", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка связи с сервером: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        try {
            List<Student> students = connector.getAllStudents();
            updateTable(students);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudents() {
        try {
            List<Student> students = connector.findStudents(txtSearch.getText());
            updateTable(students);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка поиска", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                if (connector.deleteStudent(id)) {
                    JOptionPane.showMessageDialog(this, "Студент удален");
                    refreshTable();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите студента для удаления", "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{s.getId(), s.getLastName(), s.getFirstName(), s.getGroup(), s.getYearOfAdmission()});
        }
    }

    private void clearInputFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtGroup.setText("");
        txtYear.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentClientGUI::new);
    }
}