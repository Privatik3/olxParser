package manager;

import java.util.Objects;

public class Task {

    private String id;
    private String url;
    private TaskType taskType;
    private String raw;

    public Task(String id, String url, TaskType taskType) {
        this.id = id;
        this.url = url;
        this.taskType = taskType;
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
