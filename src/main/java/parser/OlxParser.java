package parser;

import manager.Task;
import manager.TaskType;
import manager.entity.Ad;
import manager.entity.Owner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.*;

public class OlxParser {


    public static List<Task> parseAdsLink(List<Task> categoryTasks) {

        ArrayList<Task> result = new ArrayList<>();

        for (Task task : categoryTasks) {
            Document doc = Jsoup.parse(task.getRaw());
            Elements rawList = doc.select("tr.wrap");
            for (Element ad : rawList) {
                String link = ad.select("a.link").attr("href");
                link = link.substring(0, link.lastIndexOf(".html") + 5);
                String id = link.substring(link.lastIndexOf("ID") + 2, link.length() - 5);

                Task adTask = new Task(id, link, TaskType.AD);
                HashMap<String, String> taskParam = new HashMap<>();
                if (ad.select("td.promoted").size() > 0)
                    taskParam.put("promoted", "true");

                if (ad.select("span.abs").size() > 0)
                    taskParam.put("top", "true");


                Elements price = ad.select("div.list-item__price");
                if (price.size() > 0)
                    taskParam.put("price", price.get(0).text());

                price = ad.select("p.price strong");
                if (price.size() > 0) {
                    String cutPrice = price.get(0).text();
                    try {
                        cutPrice = cutPrice.substring(0, cutPrice.lastIndexOf(" "))
                                .replaceAll(" ", "");
                        taskParam.put("price", cutPrice);
                    } catch (Exception e) {
                        taskParam.put("price", cutPrice);
                    }
                }

                adTask.setParam(taskParam);
                result.add(adTask);
            }
        }

        return result;
    }

    public static List<Ad> parseAds(List<Task> adTasks) {

        List<Ad> result = new ArrayList<>();

        for (Task task : adTasks) {

            Ad ad = new Ad();

            ad.setId(task.getId());
            ad.setTop(task.getParam().containsKey("top"));
            ad.setPromoted(task.getParam().containsKey("promoted"));
            ad.setUrl(task.getUrl());

            Document doc = Jsoup.parse(task.getRaw());

            String title = "";
            try {
                title = doc.select("div.offer-titlebox h1").text();
            } catch (Exception ignored) {
            }
            ad.setTitle(title);

            String city = "";
            try {
                city = doc.select("div.offer-titlebox__details a.show-map-link").text();
            } catch (Exception ignored) {
            }
            ad.setCity(city);

            String serialNumber = "";
            try {
                serialNumber = doc.select("div.offer-titlebox__details em small").text();
                serialNumber = serialNumber.substring(serialNumber.indexOf(":") + 1).trim();
            } catch (Exception ignored) {
                continue;
            }
            ad.setSerialNumber(serialNumber);

            String category = "";
            try {
                StringBuilder categoryBuilder = new StringBuilder();
                Elements breadCrumb = doc.select("table#breadcrumbTop li");
                for (int i = 1; i < breadCrumb.size(); i++) {
                    String categoryItem = breadCrumb.get(i).text();
                    categoryItem = categoryItem.substring(0, categoryItem.lastIndexOf(" "));
                    categoryBuilder.append(categoryItem).append(" > ");
                }

                category = categoryBuilder.toString().substring(0, categoryBuilder.length() - 3);
            } catch (Exception ignored) {
            }
            ad.setCategory(category);

            Date fullDate = null;
            try {
                String rawDate = doc.select("div.offer-titlebox__details em").text();
                rawDate = rawDate.substring(rawDate.indexOf(" в ") + 3, rawDate.lastIndexOf(","));

                String time = rawDate.split(", ")[0];
                String date = rawDate.split(", ")[1];

                ArrayList<String> months = new ArrayList<>(Arrays.asList(
                        "января", "февраля", "марта", "апреля", "мая", "июня",
                        "июля", "августа", "сентября", "октября", "ноября", "декабря"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                fullDate = dateFormat.parse(
                        String.format("%s-%02d-%02d %s",
                                date.split(" ")[0], months.indexOf(date.split(" ")[1]),
                                Integer.parseInt(date.split(" ")[2]), time));

            } catch (Exception ignored) {
            }
            ad.setDate(fullDate);

            String description = "";
            try {
                description = doc.select("div#textContent").text();
            } catch (Exception ignored) {
            }
            ad.setDescription(description);

            String price = "";
            try {
                price = task.getParam().get("price");
            } catch (Exception ignored) {}
            ad.setPrice(price);

            String priceComment = "";
            try {
                priceComment = doc.select("div.price-label small").text();
            } catch (Exception ignored) {
            }
            ad.setPriceComment(priceComment);

            HashMap<String, String> characteristics = new HashMap<>();
            try {
                Elements characteristicsEl = doc.select("table.details td.col");
                for (Element characteristic : characteristicsEl) {
                    String key = characteristic.select("th").text();
                    String val = characteristic.select("td strong").text();

                    characteristics.put(key, val);
                }
            } catch (Exception ignored) {
            }
            ad.setCharacteristics(characteristics);

            ArrayList<String> photos = new ArrayList<>();
            try {
                Elements photosEl = doc.select("ul#bigGallery a");
                for (Element photo : photosEl) {
                    photos.add(photo.attr("href"));
                }
            } catch (Exception ignored) {
            }
            ad.setPhotos(photos);

            String view = "";
            try {
                view = doc.select("div#offerbottombar strong").text();
            } catch (Exception ignored) {
            }
            ad.setViews(view);

            // --------- OWNER ---------
            Owner owner = new Owner();

            String ownerID = "";
            try {
                String ownerLink = doc.select("div.offer-user__details h4 a").attr("href");
                String[] split = ownerLink.split("/");
                ownerID = split[split.length - 1];

                if (ownerID.contains("olx"))
                    ownerID = ownerID.substring(0, ownerID.indexOf(".olx"));
            } catch (Exception ignored) {
                System.err.println(ignored.getMessage());
            }
            owner.setId(ownerID);

            String ownerUrl = "";
            try {
                ownerUrl = doc.select("div.offer-user__details h4 a").attr("href");
            } catch (Exception ignored) {
                System.err.println(ignored.getMessage());
            }
            owner.setUrl(ownerUrl);

            String ownerName = "";
            try {
                ownerName = doc.select("div.offer-user__details h4").text();
                if (ownerName.equals(""))
                    continue;
            } catch (Exception ignored) {
                System.err.println(ignored.getMessage());
            }
            owner.setName(ownerName);

            String ownerSince = "";
            try {
                ownerSince = doc.select("span.user-since").text();
            } catch (Exception ignored) {
                System.err.println(ignored.getMessage());
            }
            owner.setUserSince(ownerSince);

            String ownerPhoneUrl = "";
            try {
                ownerPhoneUrl += "https://m.olx." + (task.getUrl().contains("olx.kz") ? "kz" : "ua") +
                        "/api/v1/offers/" +
                        serialNumber + "/phones";

//                String token = doc.select("section#body-container script").get(0).toString();
//                token = token.substring(token.indexOf("'") + 1, token.lastIndexOf("'"));

//                ownerPhoneUrl +=
//                        "http://www.olx.ua/ajax/misc/contact/phone/" + task.getId() +
//                        "/?pt=" + token;

            } catch (Exception ignored) {
                System.err.println(ignored.getMessage());
            }
            owner.setPhoneUrl(ownerPhoneUrl);

            ad.setOwner(owner);
            result.add(ad);
        }

        return result;
    }

    public static List<Owner> parsePhones(List<Task> phoneTasks) {

        List<Owner> result = new ArrayList<>();
        for (Task task : phoneTasks) {

            Owner owner = new Owner();
            owner.setId(task.getId());
            try {
                JSONObject obj = new JSONObject(task.getRaw());
                JSONArray jsonArray = obj.getJSONObject("data").getJSONArray("phones");

                ArrayList<String> phones = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    phones.add(jsonArray.get(i).toString());
                }
                owner.setPhones(phones);
            } catch (Exception e) {
                owner.setPhones(new ArrayList<>());
            }
            result.add(owner);
        }

        return result;
    }
}
