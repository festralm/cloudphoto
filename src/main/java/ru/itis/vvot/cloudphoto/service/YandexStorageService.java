package ru.itis.vvot.cloudphoto.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YandexStorageService {
    public static final String CREDENTIALS_PATH = System.getProperty("user.home") + "/.config/cloudphoto/cloudphotorc";
    private static String bucketName;
    private static String endpoint = "";
    private static String region = "";
    @Value("${image.extensions}")
    private List<String> imageExtensions;
    @Value("${site.index}")
    private String index;
    @Value("${site.error}")
    private String error;
    private AmazonS3 client;
    @Setter(onMethod_ = @Autowired)
    private FileService fileService;

    public void initClient() throws Exception {
        AWSCredentials credentials = new ProfileCredentialsProvider(CREDENTIALS_PATH, "DEFAULT").getCredentials();

        readProperties();
        client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }

    private void readProperties() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(CREDENTIALS_PATH))) {
            String line = br.readLine();

            while (line != null) {
                String bucketPrefix = "bucket = ";
                String endpointPrefix = "endpoint_url = ";
                String regionPrefix = "region = ";
                if (line.startsWith(bucketPrefix)) {
                    bucketName = line.substring(bucketPrefix.length());
                } else if (line.startsWith(endpointPrefix)) {
                    endpoint = line.substring(endpointPrefix.length());
                } else if (line.startsWith(regionPrefix)) {
                    region = line.substring(regionPrefix.length());
                }
                line = br.readLine();
            }
        }
    }

    public void checkAndCreateBucket(String bucketName) {
        boolean exists = client.doesBucketExistV2(bucketName);
        if (!exists) {
            client.createBucket(bucketName);
        }
    }

    public void uploadObject(String objectName, File object) {
        client.putObject(bucketName, objectName, object);
    }

    public void uploadHtml(String objectName, InputStream io) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        client.putObject(bucketName, objectName, io, metadata);
    }

    public S3Object getObject(String objectName) {
        return client.getObject(new GetObjectRequest(bucketName, objectName));
    }

    public ObjectListing listObjects(String prefix) {
        return client.listObjects(bucketName, prefix);
    }

    public ObjectListing listObjects() {
        return client.listObjects(bucketName, "");
    }

    public void deleteObjectsByPrefix(String prefix) {
        listObjects(prefix)
                .getObjectSummaries()
                .forEach(x -> deleteObject(x.getKey()));
    }

    public void deleteObject(String name) {
        client.deleteObject(bucketName, name);
    }

    public void makeSite() {
        AccessControlList acl = client.getBucketAcl(bucketName);
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        client.setBucketAcl(bucketName, acl);

        BucketWebsiteConfiguration website_config = new BucketWebsiteConfiguration(index, error);
        client.setBucketWebsiteConfiguration(bucketName, website_config);
    }

    public List<String> getImageNamesInAlbum(String album) {
        String prefix = album + "/";
        return listObjects(prefix)
                .getObjectSummaries()
                .stream()
                .map(x -> x.getKey().replace(prefix, ""))
                .filter(x -> !x.contains("/") && imageExtensions.contains(x.substring(x.lastIndexOf(".") + 1).toLowerCase()))
                .collect(Collectors.toList());
    }

    public String getObjectUrl(String album, String image) {
        URL url = client.getUrl(bucketName, album + "/" + image);
        return url.getPath().substring(1);
    }

    public List<String> getAlbumNames() {
        return listObjects()
                .getObjectSummaries()
                .stream()
                .filter(x -> x.getKey().contains("/"))
                .map(x -> {
                    String name = x.getKey();
                    return name.substring(0, name.indexOf("/"));
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean albumExists(String album) {
        String prefix = album + "/";
        return listObjects(prefix)
                .getObjectSummaries()
                .stream()
                .anyMatch(x -> x.getKey().startsWith(prefix));
    }

    public boolean photoExists(String album, String name) {
        String fullName = album + "/" + name;
        return listObjects(fullName)
                .getObjectSummaries()
                .stream()
                .anyMatch(x -> x.getKey().equals(fullName));
    }

    public String getBucketName() {
        return bucketName;

    }
}
