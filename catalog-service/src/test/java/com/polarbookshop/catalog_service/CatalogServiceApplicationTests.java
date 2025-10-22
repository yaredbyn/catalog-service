package com.polarbookshop.catalog_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.catalog_service.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper; // For JSON serialization/deserialization

	@Test
	void whenGetRequestWithIdThenBookReturned() throws Exception {
		var bookIsbn = "1231231230";
		var bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);

		// Create the book first
		String createdBookJson = mockMvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bookToCreate)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		Book expectedBook = objectMapper.readValue(createdBookJson, Book.class);

		// Fetch the book
		mockMvc.perform(get("/books/" + bookIsbn))
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.isbn").value(expectedBook.isbn()));
	}

	@Test
	void whenPostRequestThenBookCreated() throws Exception {
		var expectedBook = new Book("1231231231", "Title", "Author", 9.90);

		mockMvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(expectedBook)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.isbn").value(expectedBook.isbn()));
	}

	@Test
	void whenPutRequestThenBookUpdated() throws Exception {
		var bookIsbn = "1231231232";
		var bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);

		// Create the book
		String createdBookJson = mockMvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bookToCreate)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		Book createdBook = objectMapper.readValue(createdBookJson, Book.class);
		var bookToUpdate = new Book(createdBook.isbn(), createdBook.title(), createdBook.author(), 7.95);

		mockMvc.perform(put("/books/" + bookIsbn)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bookToUpdate)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(bookToUpdate.price()));
	}

	@Test
	void whenDeleteRequestThenBookDeleted() throws Exception {
		var bookIsbn = "1231231233";
		var bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);

		// Create the book
		mockMvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bookToCreate)))
				.andExpect(status().isCreated());

		// Delete the book
		mockMvc.perform(delete("/books/" + bookIsbn))
				.andExpect(status().isNoContent());

		// Verify it is deleted
		mockMvc.perform(get("/books/" + bookIsbn))
				.andExpect(status().isNotFound())
				.andExpect(result ->
						assertThat(result.getResponse().getContentAsString())
								.isEqualTo("The book with ISBN " + bookIsbn + " was not found.")
				);
	}
}
