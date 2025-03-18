package com.photoanalyzer.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import com.photoanalyzer.model.PhotoInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.photoanalyzer.core.PhotoAnalyzer;

/**
 * 照片分析主方法
 * @author tkee
 */
public class App 
{
    private static final Logger log = LogManager.getLogger(App.class); // 添加日志记录器

    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config/config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            log.error("加载配置文件时发生错误", e); // 使用Log4j记录错误
        }
        String defaultDirectoryPath = properties.getProperty("directory.path"); // 从配置文件中读取默认目录路径

        while (true) {
            log.info("请选择操作："); // 使用Log4j记录信息
            log.info("1. 输入目录"); // 使用Log4j记录信息
            log.info("2. 输入外接硬盘目录"); // 使用Log4j记录信息
            log.info("3. 退出"); // 使用Log4j记录信息
            String choice = scanner.nextLine();
            if ("3".equals(choice)) {
                break;
            } else if ("1".equals(choice) ||"2".equals(choice)) {
                log.info("请输入目录路径："); // 使用Log4j记录信息
                String directoryPath = scanner.nextLine();

                File directory;
                if (directoryPath.trim().isEmpty()) { // 检查输入是否为空
                    // 使用项目根目录作为基准解析相对路径
                    File projectRoot = new File(System.getProperty("user.dir"));
                    directory = new File(projectRoot, defaultDirectoryPath);
                    log.info("使用默认目录: " + directory.getAbsolutePath()); // 使用Log4j记录信息
                } else {
                    directory = new File(directoryPath);
                    log.info("使用绝对目录: " + directory.getAbsolutePath());
                }

                if (directory.exists() && directory.isDirectory()) {
                    List<PhotoInfo> photoInfos = new ArrayList<>(); // 新增：存储所有PhotoInfo对象的列表
                    try {
                        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (dir.getFileName().toString().startsWith(".")) {
                                    log.info("跳过隐藏目录: " + dir);
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                log.info("处理目录: " + file);
                                if (PhotoAnalyzer.isPhotoFile(file)) {
                                    PhotoInfo photoInfo = PhotoAnalyzer.extractPhotoInfo(file);
                                    if (photoInfo != null) {
                                        photoInfos.add(photoInfo);
                                    }
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        PhotoAnalyzer.writeToExcel(photoInfos); // 新增：遍历完成后将所有PhotoInfo对象写入Excel文件
                    } catch (IOException e) {
                        log.error("遍历目录时发生错误", e); // 使用Log4j记录错误
                    }
                } else {
                    log.error("目录不存在或不是目录，请重新输入。"); // 使用Log4j记录错误
                }
            } else {
                log.error("无效的选择，请重新输入。"); // 使用Log4j记录错误
            }
        }
        scanner.close();
    }
}