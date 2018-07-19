package utility;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.httpclient.FiberHttpClientBuilder;
import co.paralleluniverse.strands.SuspendableRunnable;
import manager.Task;
import manager.TaskType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RequestManager {

    private static CloseableHttpClient client = null;

    private static void initClient() {
        try {
            TrustStrategy acceptingTrustStrategy = (certificate, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

            int cores = Runtime.getRuntime().availableProcessors();
            client = FiberHttpClientBuilder.
                    create(cores).
                    setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).
                    setSSLContext(sslContext).
                    setMaxConnPerRoute(10000).
                    setMaxConnTotal(10000).build();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Не удалось инициализировать HTTP Client");
        }
    }

    public static List<Task> execute(List<Task> tasks) throws InterruptedException, IOException {

        if (client == null)
            initClient();

        HashSet<Task> result = new HashSet<>();

        ArrayList<RequestConfig> allProxy = ProxyManager.getProxy();
        ArrayList<RequestConfig> goodProxy = new ArrayList<>();

        final long startTime = new Date().getTime();
        final CountDownLatch success = new CountDownLatch(tasks.size());

        ArrayList<Task> taskMultiply = new ArrayList<>(tasks);

        Integer waveCount = 0;
        Integer parseSpeed = 0;
        Integer wave = 0;

        ArrayList<RequestConfig> proxys;
        while (tasks.size() > 0 && (taskMultiply.size() > (tasks.get(0).getTaskType() == TaskType.AD ? 25 : 0))) {

            if (wave != 0) {
                parseSpeed = ((parseSpeed * waveCount) + (wave - taskMultiply.size())) / ++waveCount;
                System.out.println("=============================================================");
                System.out.println("PARSE SPEED: " + parseSpeed);
                System.out.println("=============================================================");
            }
            wave = taskMultiply.size();


            tasks.clear();
            for (int i = 0; tasks.size() < allProxy.size() && tasks.size() < (taskMultiply.size() * 10); i++) {
                if (i == taskMultiply.size())
                    i = 0;

                tasks.add(taskMultiply.get(i));
            }

            final CountDownLatch cdl = new CountDownLatch(tasks.size());

            proxys = new ArrayList<>(goodProxy);
            goodProxy.clear();
            if (goodProxy.size() < tasks.size())
                proxys.addAll(allProxy);

            for (int i = 0; i < proxys.size() && i < tasks.size(); i++) {
                Task task = tasks.get(i);
                RequestConfig proxy = proxys.get(i);

                new Fiber<Void>((SuspendableRunnable) () -> {
                    HttpEntity entity = null;
                    try {
                        String taskUrl = URLDecoder.decode(task.getUrl(), StandardCharsets.UTF_8.toString())
                                .replaceAll("https", "http");
                        HttpGet request = new HttpGet(taskUrl);
                        request.setConfig(proxy);

                        CloseableHttpResponse response = client.execute(request);
//                        System.out.println(response.getStatusLine());
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            entity = response.getEntity();
                            String body = EntityUtils.toString(entity, "UTF-8");

                            if (body.contains("67284585bff59a69")) {
//                                tasks.removeAll(Collections.singleton(task));

                                task.setRaw(body);
                                result.add(task);

                                success.countDown();
                                goodProxy.add(proxy);
                            }
//                            } else
//                                Files.write(Paths.get("fail.txt"), (body + "\n\n\n\n\n\n").getBytes(), StandardOpenOption.APPEND);

                        }

//                    if (response.getStatusLine().getStatusCode() == 403)
//                        System.out.println(proxy.getProxy());

                        // end of snippet
                    } catch (IOException ex) {
//                        System.err.println("ERROR: " + ex.getMessage());
                    } finally {
                        cdl.countDown();
                        if (entity != null) {
                            try {
                                EntityUtils.consume(entity);
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }).start();

            }

            cdl.await(8, TimeUnit.SECONDS);
            taskMultiply.removeAll(result);
        }

//        ProxyManager.setWorkProxies(goodProxy);

        if (tasks.size() > 0) {
            System.out.println("=============================================================");
            System.out.println(
                    "Время затраченое на " + (tasks.get(0).getTaskType() == TaskType.AD ? "обьявления: " : "категории: ")
                            + (new Date().getTime() - startTime) + " ms");
//        System.out.println("TIME: " + (new Date().getTime() - startTime) + " ms");
//        System.out.println("SUCCESS: " + (tasks.size() - success.getCount()));
            System.out.println("=============================================================");
        }
        return new ArrayList<>(result);
    }

    public static void closeClient() throws IOException {
        client.close();
        client = null;
    }
}
