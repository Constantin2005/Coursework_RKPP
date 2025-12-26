package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Department implements Serializable {
    private int id;
    private String name;
    private String code;
    private List<Group> groups;

    public Department(int id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.groups = new ArrayList<>();
    }

    // Геттеры, сеттеры, equals, hashCode, toString
}