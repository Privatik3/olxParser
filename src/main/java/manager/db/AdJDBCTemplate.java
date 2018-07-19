package manager.db;

import manager.Task;
import manager.entity.Ad;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdJDBCTemplate {

    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplateObject;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplateObject = new NamedParameterJdbcTemplate(dataSource);
    }

    public void create(List<Ad> ads) {
        String SQL =
                "insert into ad (id, serial_number, title, category, price, priceComment, city, date, description, views) " +
                        "values (:id, :sn, :tittle, :category, :price, :priceCom, :city, :date, :desc, :view)" +
                        "ON DUPLICATE KEY UPDATE\n" +
                        "  serial_number = VALUES(serial_number),\n" +
                        "  title = VALUES(title),\n" +
                        "  category = VALUES(category),\n" +
                        "  price = VALUES(price),\n" +
                        "  priceComment = VALUES(priceComment),\n" +
                        "  city = VALUES(city),\n" +
                        "  date = VALUES(date),\n" +
                        "  description = VALUES(description),\n" +
                        "  views = VALUES(views)";

        List<Object[]> args = new ArrayList<>(ads.size());
        for (Ad ad : ads) {

            Object[] arg = new Object[]{ad.getId(), ad.getSerialNumber(), ad.getTitle(),
                    ad.getCategory(), ad.getPrice(), ad.getPriceComment(), ad.getCity(),
                    ad.getDate(), ad.getDescription(), ad.getViews()};
            args.add(arg);
        }

        MapSqlParameterSource[] parameters = new MapSqlParameterSource[ads.size()];
        for (int i = 0; i < ads.size(); i++) {
            Ad ad = ads.get(i);
            MapSqlParameterSource parameter = new MapSqlParameterSource();
            parameter.addValue("id", ad.getId());
            parameter.addValue("sn", ad.getSerialNumber());
            parameter.addValue("tittle", ad.getTitle());
            parameter.addValue("category", ad.getCategory());
            parameter.addValue("price", ad.getPrice());
            parameter.addValue("priceCom", ad.getPriceComment());
            parameter.addValue("city", ad.getCity());
            parameter.addValue("date", ad.getDate());
            parameter.addValue("desc", ad.getDescription());
            parameter.addValue("view", ad.getViews());

            parameters[i] = parameter;
        }

        try {
            jdbcTemplateObject.batchUpdate(SQL, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Ad> getAdsList(Set<String> ids) {

        String SQL = "SELECT * FROM ad WHERE id IN (:ids)";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", ids);

        return jdbcTemplateObject.query(SQL, parameters, (rs, rowNum) -> {
            Ad ad = new Ad();
            ad.setId(rs.getString(1));
            ad.setSerialNumber(rs.getString(2));
            ad.setTitle(rs.getString(3));
            ad.setCategory(rs.getString(4));
            ad.setPrice(rs.getString(5));
            ad.setPriceComment(rs.getString(6));
            ad.setCity(rs.getString(7));
            try {
                ad.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString(8)));
            } catch (ParseException ignored) { }
            ad.setDescription(rs.getString(9));
            ad.setViews(rs.getString(10));
            return ad;
        });
    }

}
