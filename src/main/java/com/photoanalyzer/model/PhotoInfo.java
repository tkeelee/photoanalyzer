package com.photoanalyzer.model;

/**
 * 自定义实体类，用于存储照片文件的信息
 */
public class PhotoInfo {
    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 拍摄时间
     */
    private String dateTime;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 格式化纬度
     */
    private double formattedLatitude;

    /**
     * 格式化经度
     */
    private double formattedLongitude;

    public PhotoInfo(String filePath, String fileName, String dateTime, long timestamp, String latitude, String longitude, double formattedLatitude, double formattedLongitude) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.formattedLatitude = formattedLatitude;
        this.formattedLongitude = formattedLongitude;
    }

    /**
     * 获取文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 获取文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 获取拍摄时间
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * 获取时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取纬度
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * 获取经度
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * 获取格式化纬度
     */
    public double getFormattedLatitude() {
        return formattedLatitude;
    }

    /**
     * 获取格式化经度
     */
    public double getFormattedLongitude() {
        return formattedLongitude;
    }

    @Override
    public String toString() {
        return "PhotoInfo{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", timestamp=" + timestamp +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", formattedLatitude=" + formattedLatitude +
                ", formattedLongitude=" + formattedLongitude +
                '}';
    }
}