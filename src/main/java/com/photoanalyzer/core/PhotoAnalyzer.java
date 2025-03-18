package com.photoanalyzer.core;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.lang.GeoLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.photoanalyzer.model.PhotoInfo;

/**
 * 照片分析工具类，用于提取和处理照片的EXIF信息
 * @author tkee
 */
public class PhotoAnalyzer {
    private static final Logger log = LogManager.getLogger(PhotoAnalyzer.class); 

    /**
     * 判断文件是否为支持的照片格式
     * @param file 待检查的文件路径
     * @return 如果是支持的照片格式返回true，否则返回false
     */
    public static boolean isPhotoFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".JPG") ||fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
    }

    /**
     * 从照片文件中提取EXIF信息
     * @param file 照片文件路径
     * @return 包含照片信息的PhotoInfo对象，如果提取失败则返回null
     */
    public static PhotoInfo extractPhotoInfo(Path file) {
        try {
            // 首先检查文件是否存在且可读
            if (!file.toFile().exists()) {
                log.error("文件不存在: " + file);
                return null;
            }
            
            if (!file.toFile().canRead()) {
                log.error("无法读取文件 (权限问题): " + file);
                return null;
            }

            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
            ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            String dateTime = exifSubIFDDirectory != null ? exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) : "未知时间";
            String latitude = gpsDirectory != null ? gpsDirectory.getDescription(GpsDirectory.TAG_LATITUDE) : "未知纬度";
            String longitude = gpsDirectory != null ? gpsDirectory.getDescription(GpsDirectory.TAG_LONGITUDE) : "未知经度";

            long timestamp = convertDateTimeToTimestamp(dateTime,"");

            GeoLocation geoLocation = gpsDirectory != null ? gpsDirectory.getGeoLocation() : null;
            double formattedLatitude = geoLocation != null ? geoLocation.getLatitude() : Double.NaN;
            double formattedLongitude = geoLocation != null ? geoLocation.getLongitude() : Double.NaN;

            PhotoInfo photoInfo = new PhotoInfo(file.toString(), file.getFileName().toString(), dateTime, timestamp, latitude, longitude, formattedLatitude, formattedLongitude);
            log.info("Photo info: " + photoInfo);
            return photoInfo;
        } catch (SecurityException e) {
            log.error("访问文件的权限被拒绝: " + file, e);
            return null;
        } catch (Exception e) {
            log.error("处理文件时发生错误: " + file, e);
            return null;
        }
    }

    /**
     * 将照片信息列表写入Excel文件
     * @param photoInfos 待写入的照片信息列表
     */
    public static void writeToExcel(List<PhotoInfo> photoInfos) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = String.format("photoinfo_%s.xlsx", timestamp);
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(fileName)) {

            Sheet sheet = workbook.createSheet("photoinfo");

            // 创建列头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("FilePath");
            headerRow.createCell(1).setCellValue("FileName");
            headerRow.createCell(2).setCellValue("DateTime");
            headerRow.createCell(3).setCellValue("Timestamp");
            headerRow.createCell(4).setCellValue("Latitude");
            headerRow.createCell(5).setCellValue("Longitude");
            headerRow.createCell(6).setCellValue("FormattedLatitude");
            headerRow.createCell(7).setCellValue("FormattedLongitude");

            // 写入数据
            int rowNum = 1;
            for (PhotoInfo info : photoInfos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(info.getFilePath());
                row.createCell(1).setCellValue(info.getFileName());
                row.createCell(2).setCellValue(info.getDateTime());
                row.createCell(3).setCellValue(info.getTimestamp());
                row.createCell(4).setCellValue(info.getLatitude());
                row.createCell(5).setCellValue(info.getLongitude());
                row.createCell(6).setCellValue(info.getFormattedLatitude());
                row.createCell(7).setCellValue(info.getFormattedLongitude());
            }

            // 自动调整列宽
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fileOut);
            log.info("Excel文件已生成: " + fileName);
        } catch (Exception e) {
            log.error("生成Excel文件时发生错误", e);
        }
    }

    /**
     * 将日期时间字符串转换为时间戳
     * @param dateTime 日期时间字符串
     * @param pattern 日期时间格式，如果为空则使用默认格式
     * @return 时间戳
     */
    private static long convertDateTimeToTimestamp(String dateTime, String pattern) {
        try {
            if (dateTime == null || dateTime.trim().isEmpty()) {
                return 0L;
            }

            DateTimeFormatter formatter;
            if (pattern == null || pattern.trim().isEmpty()) {
                formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
            } else {
                formatter = DateTimeFormatter.ofPattern(pattern);
            }

            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            log.error("转换日期时间字符串时发生错误: " + dateTime, e);
            return 0L;
        }
    }
}