package ru.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.json.simple.parser.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonReader {
    public static void main(String[] args) {

        try (InputStream inputStream = removeBom(new FileInputStream(args[0]))) {

            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray tickets = (JSONArray) jsonObject.get("tickets");
            Map<String, Long> minDepartureTime = new HashMap<>();

            minDurationTime(tickets, minDepartureTime);

            System.out.println("Min departure time: " + minDepartureTime);

            long differenceAvgAndMedianPrice = differenceAvgAndMedianPrice(tickets);

            System.out.println("Difference between average price and median price = "
                    + differenceAvgAndMedianPrice);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static long differenceAvgAndMedianPrice(JSONArray tickets) {
        List<Long> listPrice = new ArrayList<>();

        long averagePrice = 0;
        int count = 0;

        for (Object ticket : tickets) {

            JSONObject ticketObj = (JSONObject) ticket;

            if (!(ticketObj.get("origin_name").equals("Владивосток")
                    && ticketObj.get("destination_name").equals("Тель-Авив"))) {
                continue;
            }

            listPrice.add((long) ticketObj.get("price"));
            count++;
            averagePrice += (long) ticketObj.get("price");
        }
        return (count != 0) ? averagePrice / count - listPrice.get(listPrice.size() / 2) : 0;
    }

    private static void minDurationTime(JSONArray tickets, Map<String, Long> minDepartureTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyH:mm");

        for (Object ticket : tickets) {

            JSONObject ticketObj = (JSONObject) ticket;

            if (!(ticketObj.get("origin_name").equals("Владивосток")
                    && ticketObj.get("destination_name").equals("Тель-Авив"))) {
                continue;
            }

            LocalDateTime time1 = LocalDateTime.parse((String) ticketObj.get("departure_date")
                    + ticketObj.get("departure_time"), formatter);
            LocalDateTime time2 = LocalDateTime.parse((String) ticketObj.get("arrival_date")
                    + ticketObj.get("arrival_time"), formatter);

            Duration duration = Duration.between(time1, time2);

            String carrier = (String) ticketObj.get("carrier");

            if (duration.toMinutes() < minDepartureTime.getOrDefault(carrier, 99999L)) {
                minDepartureTime.put(carrier, duration.toMinutes());
            }
        }
    }

    private static InputStream removeBom(InputStream inputStream) throws IOException {
        assert inputStream != null;
        inputStream = new BufferedInputStream(inputStream);

        if (inputStream.markSupported()) {
            inputStream.mark(3);
            byte[] bom = new byte[3];
            int bytesRead = inputStream.read(bom);
            if (bytesRead == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
                return inputStream;
            } else {
                inputStream.reset();
            }
        }

        return inputStream;
    }
}
