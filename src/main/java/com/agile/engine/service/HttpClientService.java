package com.agile.engine.service;

import com.agile.engine.model.Picture;
import com.agile.engine.repository.PictureRepository;
import com.agile.engine.util.TaskSchedulerUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Service("httpClientService")
public class HttpClientService {
    private final Logger log = LoggerFactory.getLogger(HttpClientService.class);

    public final String cronUpdate;
    private final ZoneId zoneId;

    private String attempts;

    private String pagination;

    private String apiKeyJson;

    private String initUrl;

    private String token;

    private final CloseableHttpClient closeableHttpClient;
    private final PictureRepository pictureRepository;
    //  private final ThreadPoolTaskScheduler reloadPicturesTaskScheduler;

    @Autowired
    public HttpClientService(@Value("${upstox.cron.update-schedulers}") String cronUpdate,
                             @Value("${upstox.time.zone}") String timeZone,
                             @Value("${com.agile.engine.attempts}") String attempts,
                             @Value("${com.agile.engine.pagination}") String pagination,
                             @Value("${com.agile.engine.apiKeyJson}") String apiKeyJson,
                             @Value("${com.agile.engine.initUrl}") String initUrl,
                             CloseableHttpClient closeableHttpClient,
                             PictureRepository pictureRepository
    ) {
        this.cronUpdate = cronUpdate;
        this.attempts = attempts;
        this.pagination = pagination;
        this.apiKeyJson = apiKeyJson;
        this.initUrl = initUrl;
        this.closeableHttpClient = closeableHttpClient;
        this.zoneId = ZoneId.of(timeZone);
        this.pictureRepository = pictureRepository;
        pictureRepository.deleteAll();
        this.token = proceedToken();
        //   this.reloadPicturesTaskScheduler = TaskSchedulerUtils.getThreadPoolTaskScheduler("ReloadPicturesSchedulers-");
        //   reloadPicturesScheduler();
        loadPicturesToDB();
    }

    public String proceedToken() {
        String token = null;
        try {
            for (int i = 0; i < Integer.parseInt(attempts); i++) {
                // HttpEntity entity = getTokenWithPost();
                token = checkGettingToken(getTokenWithPost());
                if (token != null) {
                    break;
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.error("Can't get token!");
        }
        if (token == null) {
            System.exit(1);
        }
        return token;
    }


    private String getTokenWithPost() throws IOException {
        String body = null;

        log.info("Getting token");
        log.info("initUrl = " + initUrl);
        HttpPost postRequest = new HttpPost(initUrl + "/auth");
        StringEntity stringEntity = new StringEntity(apiKeyJson);

        postRequest.setEntity(stringEntity);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json");

        postRequest.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9");
        postRequest.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        postRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/78.0.3904.70 Chrome/78.0.3904.70 Safari/537.36");

        CloseableHttpResponse response = closeableHttpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            body = EntityUtils.toString(response.getEntity());
        }
        log.info("Response:");
        log.info(response.getProtocolVersion().toString());
        log.info(String.valueOf(response.getStatusLine().getStatusCode()));
        log.info(response.getStatusLine().getReasonPhrase());
        log.info(response.getStatusLine().toString());
        log.info(body);

        return body;
    }

    private String checkGettingToken(String body) {
        String token = null;
        JSONObject jsonObject = parseStringToJson(body);
        String auth = jsonObject.get("auth").toString();
        log.info("auth =" + auth);
        log.info("token =" + jsonObject.get("token").toString());
        if ("true".equalsIgnoreCase(auth)) {
            token = jsonObject.get("token").toString();
        }
        return token;
    }

    public JSONObject parseStringToJson(String string) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = (JSONObject) parser.parse(string);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return jsonObject;
    }

/*
    private void reloadPicturesScheduler() {
        log.info("Cron Update: " + cronUpdate + " / ZoneId: " + zoneId);
        //cron expression
        reloadPicturesTaskScheduler.schedule(this::loadPicturesToDB,
                new CronTrigger(cronUpdate, TimeZone.getTimeZone(zoneId)));
    }
*/

    private String getPicturesWithGet(String params) throws IOException {
        String body = null;
        String url = initUrl + params;
        log.info("url = " + url);
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Bearer " + this.token);

        CloseableHttpResponse response = closeableHttpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == 200) {
            body = EntityUtils.toString(response.getEntity());
        }
        log.info("Response:");
        log.info(response.getProtocolVersion().toString());
        log.info(String.valueOf(response.getStatusLine().getStatusCode()));
        log.info(response.getStatusLine().getReasonPhrase());
        log.info(response.getStatusLine().toString());
        log.info(body);
        log.info("***");

        return body;
    }

    private List<Picture> parseEntityToPictures(String body) throws IOException {
        List<Picture> list = new ArrayList<>();
        JSONObject jsonObject = parseStringToJson(body);
        if (jsonObject.containsKey("pictures")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("pictures");
            for (Object object : jsonArray) {
                JSONObject jsonObjectTemp = (JSONObject) object;
                String bodyTemp = getPicturesWithGet("/images/" + jsonObjectTemp.get("id").toString());
                jsonObjectTemp = parseStringToJson(bodyTemp);
                Picture picture = this.parseJsonToPicture(jsonObjectTemp);
                list.add(picture);
            }
        } else if (jsonObject.containsKey("id")) {
            Picture picture = this.parseJsonToPicture(jsonObject);
            list.add(picture);
        }
        return list;
    }

    private Picture parseJsonToPicture(JSONObject jsonObject) {
        Picture picture = new Picture();
        if (jsonObject.containsKey("id")) {
            picture.setId_old(jsonObject.get("id").toString());
        }
        if (jsonObject.containsKey("author")) {
            picture.setAuthor(jsonObject.get("author").toString());
        }
        if (jsonObject.containsKey("camera")) {
            picture.setCamera(jsonObject.get("camera").toString());
        }
        if (jsonObject.containsKey("cropped_picture")) {
            picture.setFull_picture(jsonObject.get("cropped_picture").toString());
        }
        if (jsonObject.containsKey("full_picture")) {
            picture.setFull_picture(jsonObject.get("full_picture").toString());
        }
        return picture;
    }

    public void loadPicturesToDB() {
        try {
            String body = getPicturesWithGet("/images");

            if (body != null && !body.isBlank()) {
                List<Picture> list = parseEntityToPictures(body);
                if (!list.isEmpty()) {
                    pictureRepository.saveAll(list);
                }

                int i = 2;
                while (true) {
                    String params = "/images?page=" + i + "&limit=" + pagination;
                    body = getPicturesWithGet(params);
                    if (body != null && !body.isBlank()) {
                        list = parseEntityToPictures(body);
                        if (!list.isEmpty()) {
                            pictureRepository.saveAll(list);
                        }
                        i++;
                        continue;
                    }
                    if (body == null || body.isBlank()) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Can't load pictures to DB");
        }
    }
}
