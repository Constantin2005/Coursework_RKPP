// Файл: src/model/Group.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {
    private int id;
    private String name;
    private int course; // 1-4 курс
    private Department department;
    private List<Student> students;

    public Group(int id, String name, int course, Department department) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.department = department;
        this.students = new ArrayList<>();
    }

    // Геттеры, сеттеры
}