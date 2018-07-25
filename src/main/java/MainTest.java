import manager.TaskManager;
import org.json.JSONObject;
import utility.ProxyManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class MainTest {

    public static void main(String[] args) throws IOException, InterruptedException {

//        System.setErr(new PrintStream(new File("error.txt")));
        Scanner in = new Scanner(System.in);

        while ( true ) {
//            String pageCount = "1"; // Количество страниц
            String pageCount = in.next();

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("min_id", "");
            parameters.put("q", "");
            parameters.put("search[category_id]", "1596");
            parameters.put("search[city_id]", "");
            parameters.put("search[dist]", "0");
            parameters.put("search[district_id]", "0");
            parameters.put("search[region_id]", "");
            parameters.put("view", "");
            parameters.put("max_pages", pageCount);
//            parameters.put("phones", "1");

            TaskManager.initTask("646421", parameters);
            System.gc();
            ProxyManager.clear();
        }
    }
}
