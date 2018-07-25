package manager.entity;

import java.util.*;

public class Ad {

    private String id;
    private String url;
    private String serialNumber;
    private String title;
    private String category;
    private String price;
    private String priceComment;

    private boolean top;
    private boolean promoted;

    private String city;
    private Date date;
    private HashMap<String, String> characteristics = new HashMap<>();
    private ArrayList<String> photos = new ArrayList<>();
    private String description;
    private String views;
    private Owner owner;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPriceComment(String priceComment) {
        this.priceComment = priceComment;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCharacteristics(HashMap<String, String> characteristics) {
        this.characteristics = characteristics;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceComment() {
        return priceComment;
    }

    public String getCity() {
        return city;
    }

    public Date getDate() {
        return date;
    }

    public HashMap<String, String> getCharacteristics() {
        return characteristics;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public String getDescription() {
        return description;
    }

    public String getViews() {
        return views;
    }

    public Owner getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        String result = title + ';' +
                serialNumber + ';' +
                category + ';' +
                price + ';' +
//                priceComment + ';' +
                city + ';' +
                date + ';';

        /*for (Map.Entry<String, String> characteristic : characteristics.entrySet()) {
            result += characteristic.getKey() + " -> " + characteristic.getValue() + " | ";
        }
        result = result.substring(0, result.length() - 3) + ";";

        for (String photo : photos)
            result += photo + " | ";
        result = result.substring(0, result.length() - 3) + ";";*/

        result += description.replaceAll(";", "") + ';' +
                views;

        return result;
    }
}
