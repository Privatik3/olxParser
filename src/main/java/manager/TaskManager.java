package manager;

import manager.db.DbManager;
import manager.entity.Ad;
import socket.EventSocket;
import utility.ProxyManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskManager {

    private static CopyOnWriteArrayList<ManagerTask> tasks = new CopyOnWriteArrayList<>();

    static {
        System.setErr(null);
        doTask();
    }

    public static void initTask(String token, HashMap<String, String> parameters) throws IOException, InterruptedException {

        ManagerTask task = new ManagerTask(token, parameters);
        tasks.add(task);

        EventSocket.sendMessage(token, "{\"message\":\"query\",\"parameters\":[{\"name\":\"position\",\"value\":\"" + tasks.size() + "\"}]}");
    }

    private static void updateQuery() {
        for (ManagerTask task : tasks) {
            EventSocket.sendMessage(task.getToken(),
                    "{\"message\":\"query\",\"parameters\":[{\"name\":\"position\",\"value\":\"" + (tasks.indexOf(task) + 1) + "\"}]}");
        }
    }

    private static void doTask() {
        Thread demon = new Thread(() -> {
            while (true) {
                try {
                    if (tasks.size() > 0) {
                        ManagerTask task = tasks.get(0);
                        tasks.remove(task);

                        boolean isExist = EventSocket.allTokens.containsKey(task.getToken());
                        if (!isExist) continue;

                        task.start();

                        EventSocket.sendResult(task);
                        updateQuery();

                        System.gc();
                        ProxyManager.clear();
                    }
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            }
        });
        demon.setDaemon(true);
        demon.start();
    }

    private static void saveToDB(List<Ad> result) {
        DbManager.saveResult(result);
    }

    private static void saveToFile(List<Ad> result) throws IOException {


        List<String> data = new ArrayList<>(result.size() + 1);
        data.add("Заголовок;Номер объявления;Категория;Цена;Область;Дата создания;Описание;Просмотры");
        for (Ad ad : result)
            data.add(ad.toString());

        Files.write(Paths.get("result.csv"), data, Charset.forName("UTF-8"));
    }


}
