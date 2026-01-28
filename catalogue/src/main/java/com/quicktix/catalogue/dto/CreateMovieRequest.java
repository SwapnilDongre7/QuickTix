package com.quicktix.catalogue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.quicktix.catalogue.enums.AgeRating;
import com.quicktix.catalogue.enums.MovieStatus;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequest {

	
	@NotBlank(message = "Title is mandatory")
	private String title;
	
	@NotBlank(message = "Description is mandatory")
    private String description;
    
	@NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    private Integer durationMinutes;
    
	@NotNull(message = "Release date is required")
    private LocalDate releaseDate;
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be >= 0")
    @DecimalMax(value = "5.0", message = "Rating must be <= 5")
    private BigDecimal rating;

    
    private MovieStatus status;
    
    @NotNull(message = "Age rating is required")
    private AgeRating ageRating;
    
    @NotEmpty(message = "Genre IDs are required")
    private Set<Integer> genreIds;
    
    @NotEmpty(message = "Language IDs are required")
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
	public LocalDate getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}
	public BigDecimal getRating() {
		return rating;
	}
	public void setRating(BigDecimal rating) {
		this.rating = rating;
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
}
