package manager.entity;

import java.util.ArrayList;
import java.util.Objects;

public class Owner {

    private String id;
    private String url;
    private String name;
    private String userSince;
    private String phoneUrl;
    private ArrayList<String> phones;

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserSince(String userSince) {
        this.userSince = userSince;
    }

    public void setPhoneUrl(String phoneUrl) {
        this.phoneUrl = phoneUrl;
    }

    public void setPhones(ArrayList<String> phones) {
        this.phones = phones;
    }

    public String getId() {
        return id.getClass() != null ? id : "";
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getUserSince() {
        return userSince;
    }

    public String getPhoneUrl() {
        return phoneUrl;
    }

    public ArrayList<String> getPhones() {
        return phones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Owner owner = (Owner) o;
        return Objects.equals(id, owner.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
