package google;

/*
 * BEFORE RUNNING:
 * ---------------
 * 1. If not already done, enable the Google Sheets API
 *    and check the quota for your project at
 *    https://console.developers.google.com/apis/api/sheets
 * 2. Install the Java client library on Maven or Gradle. Check installation
 *    instructions at https://github.com/google/google-api-java-client.
 *    On other build systems, you can add the jar files to your project from
 *    https://developers.google.com/resources/api-libraries/download/sheets/v4/java
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import manager.entity.Ad;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SheetsExample {

    private static Drive driveService;
    private static Sheets sheetsService;

    private static HttpTransport HTTP_TRANSPORT;
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static String KEY_FILE = "olx-parser.json";
    private static String APPLICATION_NAME = "OLX Parser";

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            sheetsService = createSheetsService();
            driveService = createDriveService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateSheet(String title, List<Ad> ads, boolean isPhoneEnable) throws IOException {

        // 1. CREATE NEW SPREADSHEET
        Spreadsheet requestBody = new Spreadsheet();
        SpreadsheetProperties spreadProp = new SpreadsheetProperties();
        spreadProp.setTitle(title);
        spreadProp.setLocale("ru_RU");
        spreadProp.setTimeZone("Europe/Moscow");
        requestBody.setProperties(spreadProp);

        List<Sheet> sheets = new ArrayList<>();

        // -------------------- MAIN SHEET --------------------
        Sheet mainSheet = new Sheet();

        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("Объявления");
        GridProperties gridProperties = new GridProperties();
        gridProperties.setFrozenRowCount(1);
        sheetProperties.setGridProperties(gridProperties);
        mainSheet.setProperties(sheetProperties);

        List<GridData> gData = new ArrayList<>();
        GridData gridData = new GridData();
        gData.add(gridData);

        List<RowData> rData = new ArrayList<>();

        // -------------------- SET HEADERS --------------------
        rData.add(getRowHeaders(isPhoneEnable));

        // -------------------- SET VALUES --------------------
        for (Ad ad : ads) {

            try {
                RowData rowVal = new RowData();
                List<CellData> clValues = new ArrayList<>();

                clValues.add(getCellData(ad.getTitle()));
                try {
                    clValues.add(getCellData(Integer.parseInt(ad.getPrice())));
                } catch (Exception e) {
                    clValues.add(getCellData(ad.getPrice()));
                }

                clValues.add(getCellData(Integer.parseInt(ad.getViews().equals("") ? "0" : ad.getViews())));
                clValues.add(getCellData((ad.isTop() ? "1 " : "") + (ad.isPromoted() ? "2" : "")));
                clValues.add(getCellData(ad.getCity()));
                try {
                    clValues.add(getCellData(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(ad.getDate())));
                } catch (Exception e) {
                    clValues.add(getCellData(""));
                }
                clValues.add(getCellData(ad.getPhotos().size()));
                clValues.add(getCellData(ad.getDescription()));
                clValues.add(getCellData(ad.getDescription().length()));
                if (isPhoneEnable) {
                    ArrayList<String> phones = ad.getOwner().getPhones();

                    String phoneInfo = phones.size() > 0 ? "" : "  ";
                    for (String phone : phones)
                        phoneInfo += phone + ", ";

                    clValues.add(getCellData(phoneInfo.substring(0, phoneInfo.length() - 2)));
                }
                clValues.add(getCellData(ad.getOwner().getName()));
                clValues.add(getCellData(ad.getOwner().getId()));
                clValues.add(getCellData(ad.getOwner().getUserSince()));
                clValues.add(getCellData(ad.getUrl()));
                clValues.add(getCellData(ad.getSerialNumber()));

                rowVal.setValues(clValues);
                rData.add(rowVal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // -------------------- SET VALUES ( END ) --------------------

        gridData.setRowData(rData);
        mainSheet.setData(gData);
        sheets.add(mainSheet);
        // -------------------- MAIN SHEET ( END ) --------------------

        // -------------------- SORTS SHEETS --------------------
        sheets.add(getSortSheet("Цены (сорт)", "=SORT('Объявления'!A2:O20000,2,FALSE)", isPhoneEnable));
        sheets.add(getSortSheet("Просмотры (сорт)", "=SORT('Объявления'!A2:O20000,3,FALSE)", isPhoneEnable));
        sheets.add(getSortSheet("Платные услуги (сорт)", "=SORT('Объявления'!A2:O20000,4,FALSE)", isPhoneEnable));
        sheets.add(getSortSheet("Дата (сорт)", "=SORT('Объявления'!A2:O20000,6,FALSE)", isPhoneEnable));

        // -------------------- STATISTIC SHEET --------------------
        sheets.add(getStatisticSheet(ads));

        requestBody.setSheets(sheets);
        Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);

        Spreadsheet response = request.execute();

        // TODO: Change code below to process the `response` object:
        System.out.println(response.getSpreadsheetUrl());

        // 2. PUBLISH SPREADSHEAT VIA DRIVE API
        String fileId = response.getSpreadsheetId();
        setPermission(fileId);

        return response.getSpreadsheetUrl();
    }

    private static Sheet getStatisticSheet(List<Ad> ads) {
        Sheet sheet = new Sheet();

        SheetProperties sheetProp = new SheetProperties();
        sheetProp.setTitle("Статистика");
        GridProperties gridProp = new GridProperties();
        sheetProp.setGridProperties(gridProp);
        sheet.setProperties(sheetProp);

        List<GridData> gData = new ArrayList<>();
        GridData gridData = new GridData();
        gData.add(gridData);

        List<RowData> rData = new ArrayList<>();

        // -------------------- SET VALUES --------------------
        rData.add(new RowData().setValues(Arrays.asList(
            new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Всего:")),
            new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Объявлений:")),
            new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=COUNTA('Объявления'!A2:A20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Просмотров:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=SUM('Объявления'!C2:C20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Платных услуг:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=COUNTIFS('Объявления'!D2:D20000;\"1\")"))
        )));
        rData.add(new RowData());

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Цена:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Минимальная:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=MIN('Объявления'!B2:B20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Средняя:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=AVERAGE('Объявления'!B2:B20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Максимальная:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=MAX('Объявления'!B2:B20000)"))
        )));
        rData.add(new RowData());

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Просмотры:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Минимальная:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=MIN('Объявления'!C2:C20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Средняя:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=AVERAGE('Объявления'!C2:C20000)"))
        )));

        rData.add(new RowData().setValues(Arrays.asList(
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("")),
                new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Максимальная:")),
                new CellData().setUserEnteredValue(new ExtendedValue().setFormulaValue("=MAX('Объявления'!C2:C20000)"))
        )));

        // -------------------- SET VALUES ( END ) --------------------

        gridData.setRowData(rData);
        sheet.setData(gData);
        return sheet;
    }

    private static Sheet getSortSheet(String title, String formula, boolean isPhoneEnable) {
        Sheet sheet = new Sheet();

        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle(title);
        GridProperties gridProperties = new GridProperties();
        gridProperties.setFrozenRowCount(1);
        sheetProperties.setGridProperties(gridProperties);
        sheet.setProperties(sheetProperties);

        List<GridData> gData = new ArrayList<>();
        GridData gridData = new GridData();
        gData.add(gridData);

        List<RowData> rData = new ArrayList<>();

        // -------------------- SET HEADERS --------------------
        rData.add(getRowHeaders(isPhoneEnable));

        // -------------------- SET VALUES --------------------
        RowData rowVal = new RowData();
        List<CellData> clValues = new ArrayList<>();

        CellData cell = new CellData();

        ExtendedValue exValue = new ExtendedValue();
        exValue.setFormulaValue(formula);
        cell.setUserEnteredValue(exValue);
        clValues.add(cell);

        rowVal.setValues(clValues);
        rData.add(rowVal);
        // -------------------- SET VALUES ( END ) --------------------

        gridData.setRowData(rData);
        sheet.setData(gData);
        return sheet;
    }

    private static RowData getRowHeaders(boolean isPhoneEnable) {
        RowData rowData = new RowData();
        List<CellData> clHeaders = new ArrayList<>();

        clHeaders.add(getCellData("Заголовок"));
        clHeaders.add(getCellData("Цена"));
        clHeaders.add(getCellData("Количество просмотров"));
        clHeaders.add(getCellData("Платные услуги"));
        clHeaders.add(getCellData("Адрес"));
        clHeaders.add(getCellData("Дата размещения"));
        clHeaders.add(getCellData("Фото (шт)"));
        clHeaders.add(getCellData("Текст (описание)"));
        clHeaders.add(getCellData("Кол-во знаков"));
        if (isPhoneEnable)
            clHeaders.add(getCellData("Телефон"));
        clHeaders.add(getCellData("Имя продавца"));
        clHeaders.add(getCellData("ID продавца"));
        clHeaders.add(getCellData("История продавца"));
        clHeaders.add(getCellData("Ссылка"));
        clHeaders.add(getCellData("Номер объявления"));

        for (CellData cell : clHeaders) {
            CellFormat format = new CellFormat();
            Color color = new Color();

            color.setRed((float) 201 / 255);
            color.setGreen((float) 218 / 255);
            color.setBlue((float) 248 / 255);

            format.setBackgroundColor(color);
            format.setTextFormat(new TextFormat().setBold(true));

            cell.setUserEnteredFormat(format);
        }

        rowData.setValues(clHeaders);
        return rowData;
    }

    private static CellData getCellData(Integer val) {
        CellData cell = new CellData();

        ExtendedValue exValue = new ExtendedValue();
        exValue.setNumberValue(Double.valueOf(val));
        cell.setUserEnteredValue(exValue);

        return cell;
    }

    private static CellData getCellData(String val) {
        CellData cell = new CellData();

        ExtendedValue exValue = new ExtendedValue();
        exValue.setStringValue(val);
        cell.setUserEnteredValue(exValue);

        return cell;
    }

    private static void setPermission(String fileId) throws IOException {
        BatchRequest batch = driveService.batch();
        /*Permission userPermission = new Permission()
                .setType("group")
                .setRole("writer")
                .setEmailAddress("olx-parser@googlegroups.com");*/

        Permission userPermission = new Permission()
                .setType("anyone")
                .setRole("writer");

        driveService.permissions().create(fileId, userPermission)
                .setFields("id")
                .queue(batch, new JsonBatchCallback<Permission>() {
                    @Override
                    public void onSuccess(Permission permission, HttpHeaders httpHeaders) {
                    }

                    @Override
                    public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
                    }
                });

        batch.execute();
    }

    private static Drive createDriveService() throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(KEY_FILE), HTTP_TRANSPORT, JSON_FACTORY);
        credential = credential.createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Sheets createSheetsService() throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(KEY_FILE), HTTP_TRANSPORT, JSON_FACTORY);
        credential = credential.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}