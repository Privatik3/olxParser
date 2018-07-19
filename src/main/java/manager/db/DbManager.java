package manager.db;

import manager.Task;
import manager.entity.Ad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DbManager {

    private static AdJDBCTemplate adJDBCTemplate;

    static {
        ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
        adJDBCTemplate = (AdJDBCTemplate) context.getBean("adJDBCTemplate");
    }

    public static void saveResult(List<Ad> result) {
        adJDBCTemplate.create(result);
    }


    public static List<Ad> updateAds(List<Task> adTasks) {

        Set<String> ids = new HashSet<>();
        for (Task task : adTasks)
            ids.add(task.getId());

        return adJDBCTemplate.getAdsList(ids);
    }
}
