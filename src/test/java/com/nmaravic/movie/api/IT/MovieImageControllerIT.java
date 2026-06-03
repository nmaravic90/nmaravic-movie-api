package com.nmaravic.movie.api.IT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieImageControllerIT extends BaseIntegrationTest {

    private Long movieId;

    private static final String INCEPTION_JSON = """
            {
                "title": "Inception",
                "overview": "A mind-bending thriller",
                "releaseYear": 2010,
                "director": "Christopher Nolan",
                "genres": ["ACTION"]
            }
            """;

    @BeforeEach
    void setUp() throws Exception {
        String responseBody = mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        movieId = extractId(responseBody);
    }

    @AfterEach
    void cleanUp() {
        movieImageRepository.deleteAll();
        movieRepository.deleteAll();
    }


    @Test
    void testUploadMovieImage_Cover_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("COVER"))
                .andExpect(jsonPath("$.url").isString());

        assertThat(movieImageRepository.findAllByMovie_MovieId(movieId)).hasSize(1);
    }

    @Test
    void testUploadMovieImage_Slide_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "slide.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "SLIDE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SLIDE"));
    }

    @Test
    void testUploadMovieImage_Returns409_WhenCoverAlreadyExists() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isCreated());

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isConflict());
    }

    @Test
    void testUploadMovieImage_Returns409_WhenSlideMaxReached() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "slide.jpg", "image/jpeg", "fake-image".getBytes()
        );

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                            .file(file)
                            .param("type", "SLIDE"))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "SLIDE"))
                .andExpect(status().isConflict());
    }

    @Test
    void testUploadMovieImage_Returns404_WhenMovieNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", 999L))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadMovieImage_Returns401_WhenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/movies/{movieId}/images", movieId)
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMovieImages_ReturnsEmptyList_WhenNoImages() throws Exception {
        mockMvc.perform(withUser(get("/movies/{movieId}/images", movieId)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetMovieImages_ReturnsImages_AfterUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isCreated());

        mockMvc.perform(withUser(get("/movies/{movieId}/images", movieId)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("COVER"));
    }

    @Test
    void testGetMovieImages_Returns404_WhenMovieNotFound() throws Exception {
        mockMvc.perform(withUser(get("/movies/{movieId}/images", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAllMovieImages_Returns204_AndRemovesImages() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );

        mockMvc.perform(withAdmin(multipart("/movies/{movieId}/images", movieId))
                        .file(file)
                        .param("type", "COVER"))
                .andExpect(status().isCreated());

        assertThat(movieImageRepository.findAllByMovie_MovieId(movieId)).hasSize(1);

        mockMvc.perform(withAdmin(delete("/movies/{movieId}/images", movieId)))
                .andExpect(status().isNoContent());

        assertThat(movieImageRepository.findAllByMovie_MovieId(movieId)).isEmpty();
    }

    @Test
    void testDeleteAllMovieImages_Returns404_WhenMovieNotFound() throws Exception {
        mockMvc.perform(withAdmin(delete("/movies/{movieId}/images", 999L)))
                .andExpect(status().isNotFound());
    }

    private Long extractId(String json) {
        String idStr = json.replaceAll(".*\"id\":(\\d+).*", "$1");
        return Long.parseLong(idStr);
    }
}