package manager;

import google.SheetsExample;
import manager.entity.Ad;
import manager.entity.Owner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import parser.OlxParser;
import socket.EventSocket;
import utility.RequestManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ManagerTask {

    private String token;
    private String resultLink;
    private HashMap<String, String> param = new HashMap<>();
    private boolean isPhoneEnable;
    private String country = "ua";
    private String title = "";

    public ManagerTask(String token, HashMap<String, String> param) {
        this.token = token;
        this.param = param;

        if (param.containsKey("country")) {
            country = param.get("country").toLowerCase();
            param.remove("country");
        }

        if (param.containsKey("title")) {
            title = param.get("title");
            param.remove("title");
        }

        isPhoneEnable = country.equals("ua") && param.containsKey("phones");
        isPhoneEnable = false;
        param.remove("phones");


    }

    public String getToken() {
        return token;
    }

    public String getResultLink() {
        return resultLink;
    }

    public void start() {

        try {
            ArrayList<Task> initList = initTasks(param);
            List<Task> ads = initList.stream()
                    .filter(el -> el.getTaskType() == TaskType.AD)
                    .collect(Collectors.toList());

            List<Task> category = initList.stream()
                    .filter(el -> el.getTaskType() == TaskType.CATEGORY)
                    .collect(Collectors.toList());

            EventSocket.checkToken(token);
            long startTime = new Date().getTime();
            if (category.size() > 0)
                ads.addAll(updateCategory(category));

            EventSocket.checkToken(token);
            List<Ad> result = updateAds(ads);

            EventSocket.checkToken(token);
            if (isPhoneEnable) {
                ArrayList<Task> phoneTasks = new ArrayList<>();
                result.forEach(e -> {
                    Task task = new Task(e.getOwner().getId(), e.getOwner().getPhoneUrl(), TaskType.PHONE);
                    phoneTasks.add(task);
                });

                List<Owner> phones = updatePhotos(new ArrayList<>(phoneTasks));

                for (Ad ad : result) {
                    String ownerId = ad.getOwner().getId();
                    Optional<Owner> phone = phones.stream()
                            .filter(e -> e.getId().equals(ownerId)).findFirst();

                    if (phone.isPresent())
                        ad.getOwner().setPhones(phone.get().getPhones());
                    else
                        ad.getOwner().setPhones(new ArrayList<>());
                }
            }


            System.out.println("=============================================================");
            System.out.println(
                    "ПОЛУЧЕНО: " + result.size() + " результата | " +
                            "ПОЛНОЕ ВРЕМЯ: " + (new Date().getTime() - (startTime)) + " ms");
            System.out.println("=============================================================");

//        saveToDB(result);
//        saveToFile(result);
            RequestManager.closeClient();

            this.resultLink = SheetsExample.generateSheet(title, result, isPhoneEnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Owner> updatePhotos(List<Task> phoneTasks) throws Exception {

//        List<Ad> result = DbManager.updateAds(adTasks);
        List<Owner> result = new ArrayList<>();
        /*List<Task> filterTasks = adTasks.stream()
                .filter(el -> result.stream().noneMatch(e -> e.getId().equals(el.getId())))
                .collect(Collectors.toList());*/

        List<Task> resultList = RequestManager.execute(token, phoneTasks);
        result.addAll(OlxParser.parsePhones(resultList));

        return result;
    }

    public List<Task> updateCategory(List<Task> tasks) throws Exception {
        List<Task> result = RequestManager.execute(token, tasks);
        return OlxParser.parseAdsLink(result);
    }

    private List<Ad> updateAds(List<Task> adTasks) throws Exception {

//        List<Ad> result = DbManager.updateAds(adTasks);
        List<Ad> result = new ArrayList<>();
        List<Task> filterTasks = adTasks.stream()
                .filter(el -> result.stream().noneMatch(e -> e.getId().equals(el.getId())))
                .collect(Collectors.toList());

        List<Task> resultList = RequestManager.execute(token, filterTasks);
        result.addAll(OlxParser.parseAds(resultList));

        return result;
    }

    private ArrayList<Task> initTasks(HashMap<String, String> parameters) throws IOException {

        int pages = Integer.parseInt(parameters.get("max_pages"));
        parameters.remove("max_pages");

        Document doc = Jsoup.connect("https://www.olx." + country + "/ajax/search/list/")
                .data(parameters)
                .post();

        Elements lastLink = doc.select("a[data-cy='page-link-last']");

        int resultPages = lastLink.size() > 0 ? Integer.parseInt(lastLink.get(0).text()) : 1;
        resultPages = pages < resultPages ? pages : resultPages;

        Task firstPage = new Task("1", "", TaskType.CATEGORY);
        firstPage.setRaw(doc.toString());

        ArrayList<Task> result = new ArrayList<>(OlxParser.parseAdsLink(Collections.singletonList(firstPage)));

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
