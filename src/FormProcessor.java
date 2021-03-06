import java.util.*;
import java.net.*;
import java.io.*;

import netscape.javascript.*;
import org.json.simple.*;
import org.json.simple.JSONObject;
public class FormProcessor {

    public static void main(String[] args) throws Exception{
        FileWriter out = new FileWriter("data.json");
        BufferedReader inDone = new BufferedReader(new FileReader("ProcessedBusiness.txt"));
        ArrayList<String> doneBusinesses = new ArrayList<>();
        while(true) {
//            System.out.println("loop1");
            try {
                String check = inDone.readLine();
                if(check.equals("")) {
                    break;
                }
                doneBusinesses.add(check);
            } catch (Exception E) {
                break;
            }
        }
        URL url = new URL("https://docs.google.com/spreadsheets/d/e/2PACX-1vT25C_fbbg4UyrxT4xHD_plF5y37MTPLeSKg86d9UqYQmJzvgfNO9uWT1QQbxRI_kxyVLIV90qSN256/pub?output=csv");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        Scanner f = new Scanner(new InputStreamReader(conn.getInputStream()));
        f.nextLine();
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();
        while(f.hasNextLine()) {
            StringTokenizer tokenizer = new StringTokenizer(f.nextLine(), ",");
            String time = tokenizer.nextToken();
            String name = tokenizer.nextToken();
            System.out.println(name);
            String address = tokenizer.nextToken();
            address = address.replaceAll(" ", "+");
            address = address.replaceAll("#", "");
            String city = tokenizer.nextToken();
            city = city.replaceAll(" ", "+");
            String zip = tokenizer.nextToken();
            String website = tokenizer.nextToken();
            String state = tokenizer.nextToken();




            String options = tokenizer.nextToken();
            String giftCard = "No";
            String onsiteService = "No";
            String onlineService = "No";
            String outdoorDining  = "No";
            String orderGoodsOnline = "No";
            String takeout = "No";
            if(options.contains("Gift Cards")) {
                giftCard = "Yes";
            }
            if(options.contains("Provide On-site Services")) {
                onsiteService = "Yes";
            }
            if(options.contains("Provide Online Service (eg. Zoom classes)")) {
                onlineService = "Yes";
            }
            if(options.contains("Online / Phone Takeout")) {
                takeout = "Yes";
            }
            if(options.contains("Outdoor Dining")) {
                outdoorDining = "Yes";
            }
            if(options.contains("Order Goods Online")) {
                orderGoodsOnline = "Yes";
            }


            String instagramLink = "";
            String approvalStatus = "";
            try {
                instagramLink = tokenizer.nextToken();
            }catch (Exception E){
            }
            if(instagramLink.equals("Approved")) {
                approvalStatus = "Approved";
            }else if (instagramLink.equals("Rejected")) {
                approvalStatus = "Rejected";
            } else {
                try {
                    approvalStatus = tokenizer.nextToken();
                }catch (Exception E){
                    approvalStatus = "Approved";
                }
            }
            if(approvalStatus.equals("")) {
                approvalStatus = "Approved";
            }
            String lat = "0";
            String lng = "0";
            boolean viable = true;
            for(int i = 0; i < doneBusinesses.size(); i++) {
                if(doneBusinesses.get(i).contains(name)&&doneBusinesses.get(i).contains(zip)) {
                    viable = false;
                }
            }
            if(viable) {
                System.out.println("called google" + name);
                URL getCoords = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + ",+" + city + ",+" + state + "&key=AIzaSyA95JKIZi1nfDdRCpsBlDsdlTaBZ9Q-k7A");
                HttpURLConnection conn2 = (HttpURLConnection) getCoords.openConnection();
                conn2.setRequestMethod("GET");
                Scanner l = new Scanner(new InputStreamReader(conn2.getInputStream()));
                while (l.hasNextLine()) {
                    StringTokenizer tokenizer2 = new StringTokenizer(l.nextLine());
                    String check = tokenizer2.nextToken();
                    if (check.equals("\"lat\"")) {
                        tokenizer2.nextToken();
                        lat = tokenizer2.nextToken();
                        lat = lat.substring(0, lat.length() - 1);
                    } else if (check.equals("\"lng\"")) {
                        tokenizer2.nextToken();
                        lng = tokenizer2.nextToken();
                        lng = lng.substring(0, lng.length() - 1);
                    }
                    if (!lat.equals("0") && !lng.equals("0")) {
                        break;
                    }
                }
                doneBusinesses.add(name + " " + zip + " " + "lat: " + lat + " " + "lng: " + lng);
            }else {
                for(int i = 0; i < doneBusinesses.size(); i++) {
                    StringTokenizer l = new StringTokenizer(doneBusinesses.get(i));
                    if(doneBusinesses.get(i).contains(name)&&doneBusinesses.get(i).contains(zip)) {
                        while(true) {
                            if(l.nextToken().equals("lat:")) {
                                break;
                            }
                        }
                        lat = l.nextToken();
                        l.nextToken();
                        lng = l.nextToken();
                    }
                }
            }
                String image;
                String action;
                if (!approvalStatus.equals("Rejected")) {
                    JSONObject feature = new JSONObject();
                    feature.put("type", "Feature");
                    JSONObject geometry = new JSONObject();

                    JSONArray JSONArrayCoord = new JSONArray();

                    JSONArrayCoord.add(0, Double.parseDouble(lng));
                    JSONArrayCoord.add(1, Double.parseDouble(lat));
                    geometry.put("type", "Point");
                    geometry.put("coordinates", JSONArrayCoord);
                    feature.put("geometry", geometry);

                    JSONObject properties = new JSONObject();
                    properties.put("name", name);
                    properties.put("website", website);
                    properties.put("giftcard", giftCard);
                    properties.put("onlineService", onlineService);
                    properties.put("takeout", takeout);
                    properties.put("outdoorDining", outdoorDining);
                    properties.put("orderOnline", orderGoodsOnline);
                    properties.put("onsiteService", onsiteService);
                    properties.put("instagramLink", instagramLink);
                    feature.put("properties", properties);
                    features.add(feature);
                    featureCollection.put("features", features);
                }


        }
        out.write(featureCollection.toJSONString());
        out.close();
        PrintWriter outDone = new PrintWriter(new FileOutputStream("ProcessedBusiness.txt", false));
        for(int i = 0; i < doneBusinesses.size(); i++) {
            outDone.println(doneBusinesses.get(i));
        }
        outDone.close();

    }

}
