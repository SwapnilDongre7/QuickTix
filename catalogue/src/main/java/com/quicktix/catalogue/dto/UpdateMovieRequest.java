package com.quicktix.catalogue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.quicktix.catalogue.enums.AgeRating;
import com.quicktix.catalogue.enums.MovieStatus;

public class UpdateMovieRequest {

    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private BigDecimal rating;
    private AgeRating ageRating;
    private MovieStatus status;
    
    
    
    private Set<Integer> genreIds;
    private Set<Integer> languageIds;
    
    private List<String> cast;
    private List<String> crew;
    
   
	
	// Getters and Setters
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getDurationMinutes() {
		return durationMinutes;
	}
	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
	public MovieStatus getStatus() {
		return status;
	}
	public void setStatus(MovieStatus status) {
		this.status = status;
	}
	public BigDecimal getRating() {
		return rating;
	}
	public void setRating(BigDecimal rating) {
		this.rating = rating;
	}

	 public AgeRating getAgeRating() {
			return ageRating;
		}
		public void setAgeRating(AgeRating ageRating) {
			this.ageRating = ageRating;
		}
		public List<String> getCast() {
			return cast;
		}
		public void setCast(List<String> cast) {
			this.cast = cast;
		}
		public List<String> getCrew() {
			return crew;
		}
		public void setCrew(List<String> crew) {
			this.crew = crew;
		}
		public LocalDate getReleaseDate() {
			return releaseDate;
		}
		public void setReleaseDate(LocalDate releaseDate) {
			this.releaseDate = releaseDate;
		}
		public Set<Integer> getGenreIds() {
			return genreIds;
		}
		public void setGenreIds(Set<Integer> genreIds) {
			this.genreIds = genreIds;
		}
		public Set<Integer> getLanguageIds() {
			return languageIds;
		}
		public void setLanguageIds(Set<Integer> languageIds) {
			this.languageIds = languageIds;
		}
}
