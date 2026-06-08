package com.qqweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    /**
     * 保存上传的图片
     */
    public String saveImage(MultipartFile file) throws IOException {
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("文件大小不能超过 10MB");
        }

        // 检查文件扩展名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IOException("文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase();
        }

        boolean allowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equals(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IOException("不支持的文件类型，仅支持 jpg/png/gif/webp");
        }

        // 生成唯一文件名：日期_UUID.扩展名
        String datePrefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String newFileName = datePrefix + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 按日期创建子目录
        Path dateDir = Paths.get(uploadPath, new SimpleDateFormat("yyyyMM").format(new Date()));
        Files.createDirectories(dateDir);

        // 保存文件
        Path targetPath = dateDir.resolve(newFileName);
        file.transferTo(targetPath.toFile());

        // 返回可访问的 URL
        String datePath = new SimpleDateFormat("yyyyMM").format(new Date());
        return urlPrefix + "/" + datePath + "/" + newFileName;
    }
}
