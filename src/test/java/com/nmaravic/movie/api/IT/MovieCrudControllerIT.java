package com.nmaravic.movie.api.IT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieCrudControllerIT extends BaseIntegrationTest {

    @AfterEach
    void cleanUp() {
        movieRepository.deleteAll();
    }

    private static final String INCEPTION_JSON = """
            {
                "title": "Inception",
                "overview": "A mind-bending thriller",
                "releaseYear": 2010,
                "director": "Christopher Nolan",
                "genres": ["ACTION"]
            }
            """;

    private static final String INTERSTELLAR_JSON = """
            {
                "title": "Interstellar",
                "overview": "Space odyssey",
                "releaseYear": 2014,
                "director": "Christopher Nolan",
                "genres": ["SCI_FI"]
            }
            """;

    @Test
    void testCreateMovie_Returns201_AndPersistsMovie() throws Exception {
        mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.releaseYear").value(2010))
                .andExpect(jsonPath("$.director").value("Christopher Nolan"))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(movieRepository.existsByTitleAndReleaseYear("Inception", 2010)).isTrue();
    }

    @Test
    void testCreateMovie_Returns409_WhenDuplicate() throws Exception {
        mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void testCreateMovie_Returns401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteMovie_Returns204_AndRemovesFromDb() throws Exception {
        String responseBody = mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = extractMovieId(responseBody);

        mockMvc.perform(withAdmin(delete("/movies/{id}", id)))
                .andExpect(status().isNoContent());

        assertThat(movieRepository.existsById(id)).isFalse();
    }

    @Test
    void testDeleteMovie_Returns404_WhenNotFound() throws Exception {
        mockMvc.perform(withAdmin(delete("/movies/{id}", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testReplaceMovie_Returns200_AndUpdatesAllFields() throws Exception {
        String responseBody = mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = extractMovieId(responseBody);

        mockMvc.perform(withAdmin(put("/movies/{id}", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INTERSTELLAR_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.releaseYear").value(2014));
    }

    @Test
    void testReplaceMovie_Returns404_WhenNotFound() throws Exception {
        mockMvc.perform(withAdmin(put("/movies/{id}", 999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void testUpdateMovie_Returns200_AndUpdatesOnlyProvidedFields() throws Exception {
        String responseBody = mockMvc.perform(withAdmin(post("/movies"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INCEPTION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = extractMovieId(responseBody);

        String patchBody = """
                {
                    "title": "Inception Updated"
                }
                """;

        mockMvc.perform(withAdmin(patch("/movies/{id}", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception Updated"))
                .andExpect(jsonPath("$.releaseYear").value(2010))
                .andExpect(jsonPath("$.director").value("Christopher Nolan"));
    }

    @Test
    void testUpdateMovie_Returns404_WhenNotFound() throws Exception {
        String patchBody = """
                {
                    "title": "Inception Updated"
                }
                """;

        mockMvc.perform(withAdmin(patch("/movies/{id}", 999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isNotFound());
    }

    private Long extractMovieId(String json) throws Exception {
        return new ObjectMapper()
                .readTree(json)
                .get("id")
                .asLong();
    }
}