package manager.handlers;

import manager.Task;
import manager.entity.Ad;
import org.apache.http.impl.client.CloseableHttpClient;
import parser.OlxParser;
import utility.RequestManager;

import java.io.IOException;
import java.util.List;

public class AdHandler {

    private List<Task> tasks;

    public void setJob(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Ad> process() throws IOException, InterruptedException {

        List<Task> resultList = RequestManager.execute(tasks);
        return OlxParser.parseAds(resultList);
    }
}
