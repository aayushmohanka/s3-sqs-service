package com.aws.tas_service.sqs;

import com.aws.tas_service.s3.S3Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.aws.tas_service.s3.S3Service.extractFilename;


@Component
public class SQSEventListener {
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

    public SQSEventListener(ObjectMapper objectMapper, S3Service s3Service) {
        this.objectMapper = objectMapper;
        this.s3Service = s3Service;
    }

    @SqsListener("${aws.sqs.queue}")
    public void onMessage(String message) throws Exception {

        System.out.println("Data from SQS" + message);
        JsonNode root = objectMapper.readTree(message);
        JsonNode record = root.get("Records").get(0);
        String eventType = record.get("eventName").textValue();
        if(eventType.equals("ObjectCreated:Put")) {
            String key = record.get("s3")
                    .get("object")
                    .get("key")
                    .asText();

            try (InputStream in = s3Service.download(key);
                 BufferedInputStream bis = new BufferedInputStream(in);
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(extractFilename(key)))) {

                byte[] buffer = new byte[8192];
                int n;
                while ((n = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }

            System.out.println("Downloaded to: " + new File(extractFilename(key)).getAbsolutePath());
        } else {
            System.out.println("Not a supported event type: " + eventType);
        }

    }
}
