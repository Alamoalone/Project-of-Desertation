package com.blackshoe.esthetecoreservice.service;

import com.blackshoe.esthetecoreservice.entity.Photo;
import com.blackshoe.esthetecoreservice.entity.PhotoChecksum;
import com.blackshoe.esthetecoreservice.exception.CopyrightException;
import com.blackshoe.esthetecoreservice.exception.PhotoErrorResult;
import com.blackshoe.esthetecoreservice.exception.PhotoException;
import com.blackshoe.esthetecoreservice.repository.PhotoChecksumRepository;
import com.blackshoe.esthetecoreservice.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoChecksumServiceImpl implements PhotoChecksumService {

    private final PhotoChecksumRepository photoChecksumRepository;

    private final PhotoRepository photoRepository;

    @Override
    @Transactional
    public void addPhotoChecksum(MultipartFile file, UUID photoId) {
        final String checksum;

        try {
            checksum = calculateMD5(file.getBytes());
        } catch (IOException e) {
            throw new PhotoException(PhotoErrorResult.PHOTO_HASH_FAILED);
        }

        Photo photo = photoRepository.findByPhotoId(photoId)
                .orElseThrow(() -> new PhotoException(PhotoErrorResult.PHOTO_NOT_FOUND));

        PhotoChecksum photoChecksum = PhotoChecksum.builder()
                .checksum(checksum)
                .build();

        photo.setPhotoChecksum(photoChecksum);

        photoChecksumRepository.save(photoChecksum);
    }

    @Override
    public void validatePhotoChecksumExist(MultipartFile file) {

        final String checksum;

        try {
            checksum = calculateMD5(file.getBytes());
        } catch (IOException e) {
            throw new PhotoException(PhotoErrorResult.PHOTO_HASH_FAILED);
        }

        if (photoChecksumRepository.existsByChecksum(checksum)) {
            PhotoChecksum photoChecksum = photoChecksumRepository.findByChecksum(checksum).get();

            throw new CopyrightException(
                    "이미 업로드된 이미지는 등록할 수 없습니다.",
                    photoChecksum.getPhoto().getPhotoId().toString()
            );
        }
    }

    @Override
    @Transactional
    public void testAddPhotoChecksum(MultipartFile file, UUID photoId) {

        final String checksum;

        try {
            checksum = calculateMD5(file.getBytes());
        } catch (IOException e) {
            throw new PhotoException(PhotoErrorResult.PHOTO_HASH_FAILED);
        }

        if (photoChecksumRepository.existsByChecksum(checksum)) {
            photoChecksumRepository.deleteAllByChecksum(checksum);
        }

        PhotoChecksum photoChecksum = PhotoChecksum.builder()
                .checksum(checksum)
                .build();

        photoChecksumRepository.save(photoChecksum);
    }

    @Override
    @Transactional
    public void testValidatePhotoChecksumExist(MultipartFile file) {

        final String checksum;

        try {
            checksum = calculateMD5(file.getBytes());
        } catch (IOException e) {
            throw new PhotoException(PhotoErrorResult.PHOTO_HASH_FAILED);
        }

        if (photoChecksumRepository.existsByChecksum(checksum)) {

            final PhotoChecksum photoChecksum;

            try {
                photoChecksum = photoChecksumRepository.findByChecksum(checksum).get();

                photoChecksumRepository.delete(photoChecksum);
            } catch (Exception e) {
                log.error("error on photo checksum delete", e);
                throw new RuntimeException(e.getMessage());
            }

            throw new CopyrightException(
                    "이미 업로드된 이미지는 등록할 수 없습니다.",
                    photoChecksum.getPhoto().getPhotoId().toString()
            );
        }
    }

    private String calculateMD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte[] digest = md.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new PhotoException(PhotoErrorResult.INVALID_HASH_ALGORITHM);
        }
    }
}
