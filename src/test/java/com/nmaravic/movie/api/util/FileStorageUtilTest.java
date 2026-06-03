package com.nmaravic.movie.api.util;

import com.nmaravic.movie.api.exception.FileStorageException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileStorageUtilTest {

    @TempDir
    Path tempDir;

    private static final String SERVER_BASE_URL = "http://localhost:8080/images";

    @Test
    void testSaveFile_Success_CreatesFileWithCorrectContent() throws IOException {
        byte[] content = "fake-image-content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", content);

        FileStorageUtil.saveFile(tempDir.toString(), 1L, file, "cover.jpg");

        Path expectedPath = tempDir.resolve("1").resolve("cover.jpg");
        assertThat(expectedPath).exists();
        assertThat(Files.readAllBytes(expectedPath)).isEqualTo(content);
    }

    @Test
    void testSaveFile_CreatesDirectories_WhenTheyDontExist() {
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", "data".getBytes());

        FileStorageUtil.saveFile(tempDir.toString(), 42L, file, "cover.jpg");

        Path expectedDir = tempDir.resolve("42");
        assertThat(expectedDir).isDirectory();
    }

    @Test
    void testSaveFile_OverwritesExistingFile() throws IOException {
        Path movieDir = tempDir.resolve("1");
        Files.createDirectories(movieDir);
        Files.write(movieDir.resolve("cover.jpg"), "old-content".getBytes());

        byte[] newContent = "new-content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", newContent);

        FileStorageUtil.saveFile(tempDir.toString(), 1L, file, "cover.jpg");

        assertThat(Files.readAllBytes(movieDir.resolve("cover.jpg"))).isEqualTo(newContent);
    }

    @Test
    void testSaveFile_ThrowsFileStorageException_WhenIOExceptionOccurs() throws IOException {
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk error"));

        ThrowableAssert.ThrowingCallable callable =
                () -> FileStorageUtil.saveFile(tempDir.toString(), 1L, file, "cover.jpg");

        assertThatThrownBy(callable)
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to save image");
    }

    @Test
    void testDeleteFile_Success_DeletesExistingFile() throws IOException {
        Path movieDir = tempDir.resolve("1");
        Files.createDirectories(movieDir);
        Path file = movieDir.resolve("cover.jpg");
        Files.write(file, "data".getBytes());

        assertThat(file).exists();

        String fileUrl = SERVER_BASE_URL + "/1/cover.jpg";
        FileStorageUtil.deleteFile(tempDir.toString(), fileUrl, SERVER_BASE_URL);

        assertThat(file).doesNotExist();
    }

    @Test
    void tetsDeleteFile_DoesNotThrow_WhenFileDoesNotExist() {
        String fileUrl = SERVER_BASE_URL + "/1/nonexistent.jpg";
        assertThatNoException().isThrownBy(() ->
                FileStorageUtil.deleteFile(tempDir.toString(), fileUrl, SERVER_BASE_URL)
        );
    }

    @Test
    void testDeleteFile_ResolvesCorrectPath_FromUrl() throws IOException {
        Path movieDir = tempDir.resolve("1");
        Files.createDirectories(movieDir);
        Path slideFile = movieDir.resolve("slide1.jpg");
        Files.write(slideFile, "slide-data".getBytes());

        String fileUrl = SERVER_BASE_URL + "/1/slide1.jpg";
        FileStorageUtil.deleteFile(tempDir.toString(), fileUrl, SERVER_BASE_URL);

        assertThat(slideFile).doesNotExist();
    }

    @Test
    void testDeleteMovieFolder_Success_DeletesExistingFolder() throws IOException {
        Path movieDir = tempDir.resolve("1");
        Files.createDirectories(movieDir);

        assertThat(movieDir).exists();

        FileStorageUtil.deleteMovieFolder(tempDir.toString(), 1L);

        assertThat(movieDir).doesNotExist();
    }

    @Test
    void testDeleteMovieFolder_DoesNotThrow_WhenFolderDoesNotExist() {
        assertThatNoException().isThrownBy(() ->
                FileStorageUtil.deleteMovieFolder(tempDir.toString(), 999L)
        );
    }

    @Test
    void testDeleteMovieFolder_DeletesCorrectFolder() throws IOException {
        Path folder1 = tempDir.resolve("1");
        Path folder2 = tempDir.resolve("2");
        Files.createDirectories(folder1);
        Files.createDirectories(folder2);

        FileStorageUtil.deleteMovieFolder(tempDir.toString(), 1L);

        assertThat(folder1).doesNotExist();
        assertThat(folder2).exists();
    }
}