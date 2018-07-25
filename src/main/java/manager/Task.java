package manager;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Task {

    private String id;
    private String url;
    private TaskType taskType;
    private HashMap<String, String> param;
    private String raw;

    public Task(String id, String url, TaskType taskType) {
        this.id = id;
        this.url = url;
        this.taskType = taskType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public HashMap<String, String> getParam() {
        return param;
    }

    public void setParam(HashMap<String, String> param) {
        this.param = param;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
