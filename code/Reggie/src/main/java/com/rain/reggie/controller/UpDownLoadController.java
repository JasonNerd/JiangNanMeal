package com.rain.reggie.controller;

import com.rain.reggie.common.BusinessException;
import com.rain.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("common")
@Slf4j
public class UpDownLoadController {
    @Value("${my-config.basic-cache-dir}")
    private String cacheRoot;

    @PostMapping("upload")
    public R<String> upload(MultipartFile file){
        log.info("接收到文件 {}, 大小 {}", file.getName(), file.getSize());

        File rootDir = new File(cacheRoot);
        if (!rootDir.exists())
            rootDir.mkdirs();

        String orgName = file.getOriginalFilename();
        String suffix = orgName.substring(orgName.lastIndexOf('.'));
        String newFileName = UUID.randomUUID() + suffix;

        try {
            file.transferTo(Paths.get(cacheRoot + newFileName));
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
        return R.success(cacheRoot + newFileName);
    }

    @GetMapping("/download")
    public R<String> download(String name, HttpServletResponse response){
        log.info("发送文件(用于客户端浏览器下载或展示): {}", name);
        try {
            FileInputStream inputStream = new FileInputStream(name);
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while (true){
                len = inputStream.read(buffer);
                if (len == -1)
                    break;
                outputStream.write(buffer);
                outputStream.flush();
            }
            inputStream.close();
            outputStream.close();
            return R.success("下载成功");
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
    }

}
