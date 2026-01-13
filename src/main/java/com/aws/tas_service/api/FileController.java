package com.aws.tas_service.api;

import com.aws.tas_service.s3.S3Service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.UUID;

import static com.aws.tas_service.s3.S3Service.extractFilename;

@RestController
@RequestMapping("/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws Exception {
        if (file.getSize() > 200 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }

        try (InputStream inputStream = file.getInputStream()) {
            s3Service.uploadStream(
                    file.getOriginalFilename(),
                    inputStream,
                    file.getSize(),
                    file.getContentType()
            );
        }

        return ResponseEntity.ok("Uploaded successfully");
    }

    @PutMapping("/upload-url")
    public ResponseEntity<String> getUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType
    ) {

        String objectKey = "uploads/" + UUID.randomUUID() + "-" + fileName;
        String uploadUrl =
                s3Service.generateUploadUrl(objectKey, contentType);
        return ResponseEntity.ok(
                uploadUrl
        );
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String key) {

        StreamingResponseBody stream = out -> {
            try (InputStream in = s3Service.download(key)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + extractFilename(key) + "\"")
                .body(stream);
    }



}
