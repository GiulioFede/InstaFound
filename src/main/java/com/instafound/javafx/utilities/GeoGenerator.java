package com.instafound.javafx.utilities;

import com.instafound.javafx.model.GeoData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GeoGenerator {

    private Workbook workbook;
    // hashmap che collega regione a lista province
    private HashMap<String, ArrayList<String>> regionProvinceHM;
    // collega province a cap
    private HashMap<String, String> provinceCapHM;

    public GeoGenerator() { //il path si riferisce al percorso dove trovare il file excel da cui prelevare i dati delle regioni, province,...
        regionProvinceHM = new HashMap();
        provinceCapHM = new HashMap();
        loadData();

    }

    private void loadData() {

        try {
            FileInputStream file = new FileInputStream(new File(new File("").getAbsolutePath()+"\\src\\main\\resources\\com\\instafound\\javafx\\provinceitaliane.xlsx"));
            workbook = new XSSFWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        Row row;

        for (int i = 1; i < 111; i++) {
            if (i != 23 && i != 64){
                row = workbook.getSheetAt(0).getRow(i);
                String provincia = null;
                String regione = null;
                String cap = null;

                Cell c1 = row.getCell(1);
                Cell c2 = row.getCell(2);
                Cell c3 = row.getCell(3);
                provincia = formatter.formatCellValue(c1);
                regione = formatter.formatCellValue(c2);
                // se la regione è gia contenuta nell hashmap
                // ci inserisce la provincia nuova,
                // se no ci inserisce regione e provincia
                if (!regionProvinceHM.containsKey(regione)) {
                    ArrayList itemsList = new ArrayList();
                    itemsList.add(provincia);
                    regionProvinceHM.put(regione, itemsList);


                } else {
                    ArrayList itemsList = regionProvinceHM.get(regione);
                    itemsList.add(provincia);
                    regionProvinceHM.put(regione, itemsList);
                }

                cap = formatter.formatCellValue(c3);
                provinceCapHM.put(provincia, cap);

            }
        }
    }
    // prende regione a caso tramite index a random
    // prende provincia a caso tra quelle della regione
    // prende cap della provincia
    // concatena e restituisce

    public GeoData getRandomLocation() {

        StringBuilder result = new StringBuilder();

        Object[] keys = regionProvinceHM.keySet().toArray();
        int index = new Random().nextInt(keys.length);
        String regione = (String) regionProvinceHM.keySet().toArray()[index];
        ArrayList<String> provinces = regionProvinceHM.get(regione);
        index = (int) (Math.random() * provinces.size());
        String provincia = provinces.get(index);
        String rangeCap = provinceCapHM.get(provincia);
        String[] split = rangeCap.split(" - ");
        Integer min = Integer.valueOf(split[0]);
        Integer max = Integer.valueOf(split[1]);
        // scelgo cap in quel range

        Random r = new Random();
        int cap = r.nextInt((max - min) + 1) + min ;

        return new GeoData(regione,provincia,cap);
    }

}
