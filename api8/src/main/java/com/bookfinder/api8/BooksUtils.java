package com.bookfinder.api8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

public class BooksUtils {

    private static final String API_KEY = "AIzaSyDLC2sT3l9REtMQkD7nqcyE1JjS7SEYfb8";

    private static final String API_URI = "https://www.googleapis.com/books/v1/volumes?q=";

    private static final String DEFAULT_AUTHOR = "Not specified";

    public static List<Book> fetchData(String searchTerms) {
        String jsonResponse = null;
        String urlString;

        try {
            urlString = new StringBuilder(API_URI)
                    .append(URLEncoder.encode(searchTerms, "UTF-8"))
                    .append("&langRestrict=").append("en")
                    .append("&filter=").append("ebooks")
                    .append("&key=").append(API_KEY)
                    .toString();

            URI uri = URI.create(urlString);
            jsonResponse = httpGetRequest(uri);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Book> books = extractBookFromJson(jsonResponse);

        return books;
    }

    private static List<Book> extractBookFromJson(String volumeJson) {
        if (volumeJson.isEmpty()) {
            return null;
        }

        List<Book> books = new ArrayList<Book>();

        JSONObject jsonObject = new JSONObject(volumeJson);
        JSONArray items = jsonObject.getJSONArray("items");

        String title = null, subtitle = null;
        int listPrice = 0, retailPrice = 0;
        String date = null, previewLink = null, description = null;

        for (int i = 0; i < items.length(); i++) {
            JSONObject currentVolume = items.getJSONObject(i);
            JSONObject currentVolumeInfo = currentVolume.getJSONObject("volumeInfo");
            JSONObject currentSaleInfo = currentVolume.getJSONObject("saleInfo");
            JSONObject currentListPrice = null;
            if (!currentSaleInfo.isNull("listPrice")) {
                currentListPrice = currentSaleInfo.getJSONObject("listPrice");
            }
            JSONObject currentRetailPrice = null;
            if (!currentSaleInfo.isNull("retailPrice")) {
                currentRetailPrice = currentSaleInfo.getJSONObject("retailPrice");
            }

            if (!currentVolumeInfo.isNull("title")) {
                title = currentVolumeInfo.getString("title");
            }

            if (!currentVolumeInfo.isNull("subtitle")) {
                subtitle = currentVolumeInfo.getString("subtitle");
            }

            if (currentSaleInfo.getString("saleability").equals("FOR_SALE")) {
                listPrice = currentListPrice.getInt("amount");
                retailPrice = currentRetailPrice.getInt("amount");
            }

            if (!currentVolumeInfo.isNull("publishedDate")) {
                date = currentVolumeInfo.getString("publishedDate");
            }

            if (!currentVolumeInfo.isNull("previewLink")) {
                previewLink = currentVolumeInfo.getString("previewLink");
            }

            if (!currentVolumeInfo.isNull("description")) {
                description = currentVolumeInfo.getString("description");
            }

            if (!currentVolumeInfo.isNull("authors")) {
                JSONArray authors = currentVolumeInfo.getJSONArray("authors");
                books.add(new Book(title, concatAuthors(authors), date, subtitle, previewLink, listPrice, retailPrice,
                        description));
            }
            
            books.add(new Book(title, DEFAULT_AUTHOR, date, subtitle, previewLink, listPrice, retailPrice,
                    description));
        }

        return books;
    }

    private static String concatAuthors(JSONArray authors) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < authors.length(); i++) {
            builder.append(authors.get(authors.length() - 1));
        }

        return builder.toString();
    }

    private static String httpGetRequest(URI url) throws IOException {
        CloseableHttpClient  httpClient = HttpClients.createDefault();
        HttpGet getRequest;
        CloseableHttpResponse response = null;
        InputStream in = null;
        String jsonResponse = null;
        try {
            getRequest = new HttpGet(url);

            getRequest.addHeader("accept", "application/json");

            response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                in = response.getEntity().getContent();
                jsonResponse = readFromStream(in);
            } else {
                System.out.println("ERROR! Response code: " + response.getStatusLine().getStatusCode());
            }

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }

           response.close();
           httpClient.close();
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        return output.toString();
    }

}
