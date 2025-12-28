package client;

import model.Student;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class SimpleStudentClientGUI extends JFrame {
    private ServerConnector connector;  // Подключение к серверу
    private JTable table; // Таблица для отображения студентов
    private DefaultTableModel tableModel;// Модель данных таблицы
    private JTextField txtLastName, txtFirstName, txtMiddleName, txtGroup, txtYear, txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnExport, btnSortName, btnSortGroup, btnShowAll;
    private int currentEditId = -1; // ID студента, которого редактирую

    public SimpleStudentClientGUI() {
        connector = new ServerConnector();
        if (!connector.connect()) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Картотека студентов");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                connector.disconnect();
            }
        });

        Container container = getContentPane();
        container.setLayout(new BorderLayout(5, 5));

        JPanel inputPanel = createInputPanel();
        container.add(inputPanel, BorderLayout.NORTH);

        JPanel searchPanel = createSearchPanel();
        container.add(searchPanel, BorderLayout.SOUTH);

        JPanel tablePanel = createTablePanel();
        container.add(tablePanel, BorderLayout.CENTER);

        loadData();
        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Добавить/Изменить студента"));

        // Первая строка - поля ввода
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        fieldsPanel.add(new JLabel("Фамилия:"));
        txtLastName = new JTextField(10);
        fieldsPanel.add(txtLastName);

        fieldsPanel.add(new JLabel("Имя:"));
        txtFirstName = new JTextField(10);
        fieldsPanel.add(txtFirstName);

        fieldsPanel.add(new JLabel("Отчество:"));
        txtMiddleName = new JTextField(10);
        fieldsPanel.add(txtMiddleName);

        fieldsPanel.add(new JLabel("Группа:"));
        txtGroup = new JTextField(8);
        fieldsPanel.add(txtGroup);

        fieldsPanel.add(new JLabel("Год:"));
        txtYear = new JTextField(6);
        fieldsPanel.add(txtYear);

        panel.add(fieldsPanel);

        // Вторая строка - кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        btnAdd = new JButton("Добавить");
        btnAdd.addActionListener(e -> addStudent());
        buttonPanel.add(btnAdd);

        btnEdit = new JButton("Изменить выбранного");
        btnEdit.addActionListener(e -> editStudent());
        buttonPanel.add(btnEdit);

        JButton btnClear = new JButton("Очистить");
        btnClear.addActionListener(e -> clearFields());
        buttonPanel.add(btnClear);

        JButton btnCancelEdit = new JButton("Отменить редактирование");
        btnCancelEdit.addActionListener(e -> cancelEdit());
        buttonPanel.add(btnCancelEdit);

        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Поиск"));

        panel.add(new JLabel("Поиск:"));
        txtSearch = new JTextField(15);
        panel.add(txtSearch);

        JButton btnSearch = new JButton("Найти");
        btnSearch.addActionListener(e -> searchStudents());
        panel.add(btnSearch);

        btnShowAll = new JButton("Все студенты");
        btnShowAll.addActionListener(e -> loadData());
        panel.add(btnShowAll);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Таблица
        String[] columns = {"ID", "Фамилия", "Имя", "Отчество", "Группа", "Год", "Курс"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок под таблицей
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        btnDelete = new JButton("Удалить");
        btnDelete.addActionListener(e -> deleteStudent());
        controlPanel.add(btnDelete);

        btnExport = new JButton("Экспорт в CSV");
        btnExport.addActionListener(e -> exportData());
        controlPanel.add(btnExport);

        btnSortName = new JButton("Сортировать по фамилии");
        btnSortName.addActionListener(e -> sortByName());
        controlPanel.add(btnSortName);

        btnSortGroup = new JButton("Сортировать по группе");
        btnSortGroup.addActionListener(e -> sortByGroup());
        controlPanel.add(btnSortGroup);

        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addStudent() {
        try {
            String lastName = txtLastName.getText().trim();
            String firstName = txtFirstName.getText().trim();
            String middleName = txtMiddleName.getText().trim();
            String group = txtGroup.getText().trim();
            String yearText = txtYear.getText().trim();

            // Проверка обязательных полей
            if (lastName.isEmpty() || firstName.isEmpty() || group.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все обязательные поля!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ПРОВЕРКА ОТЧЕСТВА
            if (middleName.isEmpty()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Отчество не указано. Продолжить без отчества?",
                        "Подтверждение",
                        JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    txtMiddleName.requestFocus();
                    return;
                }
            }

            int year = Integer.parseInt(yearText);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (year < 2000 || year > currentYear) {
                JOptionPane.showMessageDialog(this, "Некорректный год (2000-" + currentYear + ")", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student student = new Student(currentEditId, lastName, firstName, middleName, group, year);

            if (currentEditId == -1) {
                // Добавление нового студента
                if (connector.addStudent(student)) {
                    JOptionPane.showMessageDialog(this, "Студент успешно добавлен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении студента", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Изменение существующего студента
                student.setId(currentEditId); // Устанавливаем правильный ID
                if (updateStudentOnServer(student)) {
                    JOptionPane.showMessageDialog(this, "Студент успешно изменен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadData();
                    currentEditId = -1;
                    btnAdd.setText("Добавить");
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при изменении студента", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный формат года", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean updateStudentOnServer(Student student) {
        // Простая реализация: удаляем старого и добавляем нового
        try {
            if (connector.deleteStudent(student.getId())) {
                // Сбрасываем ID, чтобы сервер назначил новый
                student.setId(0);
                return connector.addStudent(student);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void editStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Выберите студента для изменения", "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentEditId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            Student student = connector.getStudentById(currentEditId);
            if (student == null) {
                JOptionPane.showMessageDialog(this, "Студент не найден", "Ошибка", JOptionPane.ERROR_MESSAGE);
                currentEditId = -1;
                return;
            }

            // Заполняем поля данными выбранного студента
            txtLastName.setText(student.getLastName());
            txtFirstName.setText(student.getFirstName());
            txtMiddleName.setText(student.getMiddleName());
            txtGroup.setText(student.getGroup());
            txtYear.setText(String.valueOf(student.getYearOfAdmission()));

            // Меняем текст кнопки
            btnAdd.setText("Сохранить изменения");

            JOptionPane.showMessageDialog(this,
                    "Редактирование студента ID: " + currentEditId +
                            "\nВнесите изменения и нажмите 'Сохранить изменения'",
                    "Редактирование", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            currentEditId = -1;
        }
    }

    private void cancelEdit() {
        currentEditId = -1;
        btnAdd.setText("Добавить");
        clearFields();
        JOptionPane.showMessageDialog(this, "Редактирование отменено", "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Выберите студента для удаления", "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = tableModel.getValueAt(selectedRow, 1) + " " + tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить студента " + name + "?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (connector.deleteStudent(id)) {
                    JOptionPane.showMessageDialog(this, "Студент успешно удален", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    if (currentEditId == id) {
                        cancelEdit(); // Если удаляем редактируемого студента
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchStudents() {
        String searchText = txtSearch.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            loadData();
            return;
        }

        try {
            java.util.List<Student> allStudents = connector.getAllStudents();
            java.util.List<Student> filtered = new ArrayList<>();

            for (Student s : allStudents) {
                if (s.getLastName().toLowerCase().contains(searchText) ||
                        s.getFirstName().toLowerCase().contains(searchText) ||
                        s.getMiddleName().toLowerCase().contains(searchText) ||
                        s.getGroup().toLowerCase().contains(searchText) ||
                        String.valueOf(s.getYearOfAdmission()).contains(searchText)) {
                    filtered.add(s);
                }
            }

            updateTable(filtered);
            setTitle("Картотека студентов (найдено: " + filtered.size() + ")");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при поиске", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportData() {
        try {
            java.util.List<Student> students = connector.getAllStudents();

            StringBuilder csv = new StringBuilder();
            csv.append("ID;Фамилия;Имя;Отчество;Группа;Год поступления;Курс\n");

            for (Student s : students) {
                int course = calculateCourse(s.getYearOfAdmission());
                csv.append(s.getId()).append(";")
                        .append(s.getLastName()).append(";")
                        .append(s.getFirstName()).append(";")
                        .append(s.getMiddleName()).append(";")
                        .append(s.getGroup()).append(";")
                        .append(s.getYearOfAdmission()).append(";")
                        .append(course).append("\n");
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить как CSV");
            fileChooser.setSelectedFile(new File("students_export.csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                Files.write(file.toPath(), csv.toString().getBytes());
                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в файл:\n" + file.getAbsolutePath(),
                        "Экспорт завершен",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при экспорте: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sortByName() {
        try {
            java.util.List<Student> students = connector.getAllStudents();
            students.sort((s1, s2) -> s1.getLastName().compareToIgnoreCase(s2.getLastName()));
            updateTable(students);
            setTitle("Картотека студентов (отсортировано по фамилии)");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при сортировке", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sortByGroup() {
        try {
            java.util.List<Student> students = connector.getAllStudents();
            students.sort((s1, s2) -> s1.getGroup().compareToIgnoreCase(s2.getGroup()));
            updateTable(students);
            setTitle("Картотека студентов (отсортировано по группе)");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при сортировке", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        try {
            java.util.List<Student> students = connector.getAllStudents();
            updateTable(students);
            setTitle("Картотека студентов (" + students.size() + " студентов)");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(java.util.List<Student> students) {
        tableModel.setRowCount(0);

        for (Student s : students) {
            int course = calculateCourse(s.getYearOfAdmission());
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getLastName(),
                    s.getFirstName(),
                    s.getMiddleName(),
                    s.getGroup(),
                    s.getYearOfAdmission(),
                    course
            });
        }
    }

    private int calculateCourse(int admissionYear) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int course = currentYear - admissionYear + 1;
        if (course < 1) course = 1;
        if (course > 6) course = 6;
        return course;
    }

    private void clearFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtMiddleName.setText("");
        txtGroup.setText("");
        txtYear.setText("");
        txtLastName.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SimpleStudentClientGUI();
        });
    }
}