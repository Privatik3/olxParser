package manager;

import manager.entity.Ad;
import manager.handlers.AdHandler;
import manager.handlers.CategoryHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import socket.EventSocket;
import utility.RequestManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerTask {

    private String token;
    private int progress;
    private Status status;
    private int adsCount = 0;
    private List<Ad> result;
    private String resultLink;
    private HashMap<String, String> param = new HashMap<>();

    public ManagerTask(String token, HashMap<String, String> param) {
        this.token = token;
        this.param = param;
    }

    public String getToken() {
        return token;
    }

    public int getAdsCount() {
        return adsCount;
    }

    public String getResultLink() {
        return resultLink;
    }

    public void start() {

        try {
            /*ArrayList<Task> initList = initTasks(param);
            List<Task> ads = initList.stream()
                    .filter(el -> el.getTaskType() == TaskType.AD)
                    .collect(Collectors.toList());

            List<Task> category = initList.stream()
                    .filter(el -> el.getTaskType() == TaskType.CATEGORY)
                    .collect(Collectors.toList());

            long startTime = new Date().getTime();
            if (category.size() > 0)
                ads.addAll(updateCategory(category));

            List<Ad> result = updateAds(ads);

            Thread.sleep(1000);
            System.out.println("=============================================================");
            System.out.println(
                    "ПОЛУЧЕНО: " + result.size() + " результата | " +
                            "ПОЛНОЕ ВРЕМЯ: " + (new Date().getTime() - (startTime + 1000)) + " ms");
            System.out.println("=============================================================");

//        saveToDB(result);
//        saveToFile(result);
            RequestManager.closeClient();*/

            for (int i = 1; i <= 5; i++) {
                Thread.sleep(2000);
                EventSocket.sendMessage(token,
                        "{\"message\":\"status\",\"parameters\":[{\"name\":\"complete\",\"value\":\"" + i * 20 + "\"}]}");
            }

            this.resultLink = "https://www.ibm.com/developerworks/ru/library/j-5things4/index.html";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Task> updateCategory(List<Task> tasks) throws IOException, InterruptedException {

        List<Task> result = new ArrayList<>();

        CategoryHandler categoryHandler = new CategoryHandler();
        categoryHandler.setJob(tasks);
        result = categoryHandler.process();

        return result;
    }

    private List<Ad> updateAds(List<Task> adTasks) throws IOException, InterruptedException {

//        List<Ad> result = DbManager.updateAds(adTasks);
        List<Ad> result = new ArrayList<>();
        List<Task> filterTasks = adTasks.stream()
                .filter(el -> result.stream().noneMatch(e -> e.getId().equals(el.getId())))
                .collect(Collectors.toList());

        AdHandler adHandler = new AdHandler();
        adHandler.setJob(filterTasks);
        result.addAll(adHandler.process());

        return result;
    }

    private ArrayList<Task> initTasks(HashMap<String, String> parameters) throws IOException {

        ArrayList<Task> result = new ArrayList<>();
        int pages = Integer.parseInt(parameters.get("max_pages"));

        Document doc = Jsoup.connect("https://www.olx.ua/ajax/search/list/")
                .data(parameters)
                .post();

        Elements lastLink = doc.select("a[data-cy='page-link-last']");

        int resultPages = lastLink.size() > 0 ? Integer.parseInt(lastLink.get(0).text()) : 1;
        resultPages = pages < resultPages ? pages : resultPages;

        Elements rawList = doc.select("tr.wrap");
        for (Element ad : rawList) {
            String link = ad.select("a.link").attr("href");
            link = link.substring(0, link.lastIndexOf(".html") + 5);
            String id = link.substring(link.lastIndexOf("ID") + 2, link.length() - 5);

            result.add(new Task(id, link, TaskType.AD));
        }

        if (resultPages > 1) {
            String pageLink = lastLink.get(0).attr("href");
            pageLink = pageLink.substring(0, pageLink.lastIndexOf("page") + 5);

            for (int i = 2; i <= resultPages; i++) {
                result.add(new Task(String.valueOf(i), pageLink + i, TaskType.CATEGORY));
            }
        }

        return result;
    }
}
