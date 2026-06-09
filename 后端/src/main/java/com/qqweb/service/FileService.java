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

    private Path absoluteUploadPath;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    /**
     * 初始化：解析上传路径为绝对路径并创建目录
     */
    @jakarta.annotation.PostConstruct
    public void init() throws IOException {
        Path p = Paths.get(uploadPath);
        if (!p.isAbsolute()) {
            // 相对于 JVM 工作目录解析
            p = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        }
        this.absoluteUploadPath = p.normalize();
        Files.createDirectories(this.absoluteUploadPath);
        System.out.println("[FILE] 上传目录: " + this.absoluteUploadPath);
    }

    /**
     * 获取绝对上传路径（供 WebMvcConfig 使用）
     */
    public String getAbsoluteUploadPath() {
        return this.absoluteUploadPath.toString();
    }

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
        String dateDirName = new SimpleDateFormat("yyyyMM").format(new Date());
        Path dateDir = absoluteUploadPath.resolve(dateDirName);
        Files.createDirectories(dateDir);

        // 保存文件
        Path targetPath = dateDir.resolve(newFileName);
        file.transferTo(targetPath.toFile());
        System.out.println("[FILE] 图片已保存: " + targetPath);

        // 返回可访问的 URL
        return urlPrefix + "/" + dateDirName + "/" + newFileName;
    }
}
