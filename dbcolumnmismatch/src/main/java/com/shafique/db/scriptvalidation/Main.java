package com.shafique.db.scriptvalidation;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Main.loadDDL();
    }

    private static void loadDDL(){
        try{
            FileInputStream ddlfile = new FileInputStream(new File("ddl_statements.xlsx"));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(ddlfile);

            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            Row header = rowIterator.next();
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                String[] dbcolumns = null;
                String tableName = null;
                if(row.getRowNum() != 0) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = cell.getStringCellValue().toUpperCase();
                        int startIndx = cellValue.indexOf("(") + 1;
                        int endIndx = cellValue.indexOf(")");
                        String fieldsString = cellValue.substring(startIndx, endIndx);
                        dbcolumns = fieldsString.split(",");
                        String scriptType = cellValue.substring(0, startIndx - 1);
                        tableName = getTableName(scriptType);
                    }
                    List<String> dataModelColumns = loadDataModel(tableName);
                    compareFields(tableName, dataModelColumns, dbcolumns);

                }

            }
            ddlfile.close();
        }catch(Exception e){
            //TODO handle exception here
        }
    }

    private static String getTableName(String scriptType){
        String tableName = null;
        if(scriptType.contains("CREATE ")){
            tableName = scriptType.substring(13).trim();
        }
        return tableName;
    }

    private static List<String> loadDataModel(String sheetName){
        List<String> columns = new ArrayList<String>();
       try{
           FileInputStream dmlfile = new FileInputStream(new File("datamodel.xlsx"));
           //Create Workbook instance holding reference to .xlsx file
           XSSFWorkbook workbook = new XSSFWorkbook(dmlfile);
           XSSFSheet sheet = workbook.getSheet(sheetName);
           Iterator<Row> rowIterator = sheet.iterator();

           while(rowIterator.hasNext()){
               Row row = rowIterator.next();
               if(row.getRowNum() != 0){
                   Cell cell = row.getCell(2);
                   columns.add(cell.getStringCellValue());
               }

           }

           dmlfile.close();
       }catch (Exception e){
           //TODO handle exception here
       }
       return columns;
    }

    private static void compareFields(String tableName, final List<String> dmFields, final String[] ddlFields){
        for(String ddlfield : ddlFields){
            ddlfield = ddlfield.trim();
            String[] field = ddlfield.split(" ");
            if(!dmFields.contains(field[0])){
                System.out.println("DataModel Sheet for "+tableName+ " does not have table field "+field[0]);
            }
        }
    }
}
