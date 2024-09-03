package com.example.ordersystem;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class imgeTest {
    @PostMapping(value = "/imageTest", consumes = "multipart/form-data")
    public void imageTest(@RequestPart MultipartFile image ) {
        System.out.println(image.toString());
    }
}
