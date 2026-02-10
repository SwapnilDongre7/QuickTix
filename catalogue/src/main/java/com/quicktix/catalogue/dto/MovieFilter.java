package com.quicktix.catalogue.dto;

import com.quicktix.catalogue.enums.MovieStatus;

public class MovieFilter {

	private Integer genreId;
	private Integer languageId;
	private String rating;
	private MovieStatus status;

	// Getters and Setters
	public Integer getGenreId() {
		return genreId;
	}

	public void setGenreId(Integer genreId) {
		this.genreId = genreId;
	}

	public Integer getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public MovieStatus getStatus() {
		return status;
	}

	public void setStatus(MovieStatus status) {
		this.status = status;
	}

}

