package utility;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProxyManager {

    private static ArrayList<RequestConfig> PROXIES = new ArrayList<>();
    private static ArrayList<RequestConfig> WORK_PROXIES = new ArrayList<>();

    public static void loadProxies() {
        try {
            List<String> proxyApi = Files.readAllLines(Paths.get("proxy-list.txt"));

            for (String api : proxyApi) {
                URL obj = new URL(api);
                URLConnection conn = obj.openConnection();

                InputStream output = conn.getInputStream();
                Scanner s = new Scanner(output).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                for (String proxy : result.split("\r\n")) {
//            for (String proxy : proxys) {
                    String hostName = proxy.split(":")[0];
                    int port = Integer.parseInt(proxy.split(":")[1]);

                    PROXIES.add(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                            .setProxy(new HttpHost(hostName, port, "http"))
                            .setConnectionRequestTimeout(8 * 1000)
                            .setSocketTimeout(8 * 1000)
                            .setConnectTimeout(8 * 1000).build());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<RequestConfig> getProxy() {

        clear();
        if (PROXIES.size() == 0)
            loadProxies();

        PROXIES.removeAll(WORK_PROXIES);

        ArrayList<RequestConfig> resultList = new ArrayList<>(WORK_PROXIES.size() + PROXIES.size());
        resultList.addAll(WORK_PROXIES);
        resultList.addAll(PROXIES);

        return resultList;
    }

    public static void setWorkProxies(ArrayList<RequestConfig> works) {
        WORK_PROXIES.addAll(works);
    }

    public static void clear() {
        PROXIES.clear();
        WORK_PROXIES.clear();
    }
}
