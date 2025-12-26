package server;

import model.StudentCatalog;
import model.Student;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 12345;
    private StudentCatalog catalog;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        catalog = new StudentCatalog();
        System.out.println("Сервер картотеки студентов запущен на порту " + PORT);
        System.out.println("Ожидание подключений...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket, catalog).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private StudentCatalog catalog;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, StudentCatalog catalog) {
        this.socket = socket;
        this.catalog = catalog;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                String command = (String) in.readObject();
                System.out.println("Команда от клиента: " + command);

                switch (command) {
                    case "GET_ALL":
                        out.writeObject(catalog.getAllStudents());
                        break;
                    case "ADD":
                        Student studentToAdd = (Student) in.readObject();
                        boolean addResult = catalog.addStudent(studentToAdd);
                        out.writeObject(addResult ? "SUCCESS" : "ERROR");
                        break;
                    case "FIND":
                        String lastName = (String) in.readObject();
                        out.writeObject(catalog.findStudentsByLastName(lastName));
                        break;
                    case "DELETE":
                        int idToDelete = (int) in.readObject();
                        boolean deleteResult = catalog.deleteStudent(idToDelete);
                        out.writeObject(deleteResult ? "SUCCESS" : "ERROR");
                        break;
                    case "EXIT":
                        System.out.println("Клиент отключился: " + socket.getInetAddress());
                        return;
                    case "GET_BY_ID":
                        int id = (int) in.readObject();
                        Student student = null;
                        for (Student s : catalog.getAllStudents()) {
                            if (s.getId() == id) {
                                student = s;
                                break;
                            }
                        }
                        out.writeObject(student);
                        break;
                }
                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Соединение закрыто клиентом: " + socket.getInetAddress());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}