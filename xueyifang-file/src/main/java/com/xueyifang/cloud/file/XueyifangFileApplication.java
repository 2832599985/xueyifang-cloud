package com.xueyifang.cloud.file;

import com.xueyifang.cloud.file.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class XueyifangFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(XueyifangFileApplication.class, args);
    }
}
