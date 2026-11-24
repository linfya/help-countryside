package com.eng.util;

import cn.hutool.core.util.RandomUtil;
import com.eng.config.CosProperties;
import com.eng.domain.vo.VideosVO;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.ciModel.snapshot.SnapshotRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 腾讯云对象存储（COS）上传工具。
 * 上传文件、上传视频（同时截取首帧作为封面）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentCosUtil {

    private final CosProperties cosProperties;

    /**
     * 上传普通文件。
     *
     * @param file          待上传文件
     * @param fileSuperName 文件路径前缀，例如 blogFile/1/
     * @return 文件的公网访问地址，上传失败返回 null
     */
    public String upload(MultipartFile file, String fileSuperName) {
        COSClient cosClient = initCos();
        try (InputStream inputStream = file.getInputStream()) {
            String filePath = buildFilePath(file.getOriginalFilename(), fileSuperName);
            cosClient.putObject(new PutObjectRequest(cosProperties.getBucketName(), filePath, inputStream, null));
            cosClient.setBucketAcl(cosProperties.getBucketName(), CannedAccessControlList.PublicRead);
            return cosProperties.getUrl() + "/" + filePath;
        } catch (Exception e) {
            log.error("文件上传失败，文件名：{}", file.getOriginalFilename(), e);
            return null;
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 上传视频，并通过数据万象截取第 1 秒的画面作为封面。
     *
     * @param file          待上传视频
     * @param fileSuperName 文件路径前缀，例如 blogFile/1/
     * @return 视频地址与封面地址，上传失败返回 null
     */
    public VideosVO uploadVideos(MultipartFile file, String fileSuperName) {
        COSClient cosClient = initCos();
        try (InputStream inputStream = file.getInputStream()) {
            String filePath = buildFilePath(file.getOriginalFilename(), fileSuperName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(inputStream.available());
            cosClient.putObject(new PutObjectRequest(cosProperties.getBucketName(), filePath, inputStream, objectMetadata));
            cosClient.setBucketAcl(cosProperties.getBucketName(), CannedAccessControlList.PublicRead);

            String imageName = "blogvedioImage/" + RandomUtil.randomString(6) + ".jpg";
            SnapshotRequest request = new SnapshotRequest();
            request.setBucketName(cosProperties.getBucketName());
            request.getInput().setObject(filePath);
            request.getOutput().setBucket(cosProperties.getBucketName());
            request.getOutput().setRegion(cosProperties.getRegion());
            request.getOutput().setObject(imageName);
            request.setTime("1");
            cosClient.generateSnapshot(request);

            return new VideosVO(cosProperties.getUrl() + "/" + filePath, cosProperties.getUrl() + "/" + imageName);
        } catch (Exception e) {
            log.error("视频上传失败，文件名：{}", file.getOriginalFilename(), e);
            return null;
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 生成不重复的对象键：前缀 + 随机串 + 原扩展名。
     */
    private String buildFilePath(String fileName, String fileSuperName) {
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        return fileSuperName + RandomUtil.randomString(6) + fileType;
    }

    private COSClient initCos() {
        BasicCOSCredentials credentials = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        return new COSClient(credentials, clientConfig);
    }
}
