package manager.handlers;

import manager.Task;
import org.apache.http.impl.client.CloseableHttpClient;
import parser.OlxParser;
import utility.RequestManager;

import java.io.IOException;
import java.util.List;

public class CategoryHandler {

    private List<Task> tasks;

    public void setJob(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> process() throws IOException, InterruptedException {
        List<Task> resultList = RequestManager.execute(tasks);
        return OlxParser.parseAdsLink(resultList);
    }
}
