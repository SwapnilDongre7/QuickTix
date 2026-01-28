package com.quicktix.catalogue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.quicktix.catalogue.enums.AgeRating;
import com.quicktix.catalogue.enums.MovieStatus;

public class MovieResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    
    private boolean upcoming;
    
    private BigDecimal rating;
    private AgeRating ageRating;
    private MovieStatus status;
    
    private String posterUrl;
    private String backgroundUrl;
    
    private List<String> cast;
    private List<String> crew;
    
    private Long viewCount;

    private Set<String> genres;
    private Set<String> languages;
	
    // Getters and Setters
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public String getPosterUrl() {
		return posterUrl;
	}
	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}
	public Long getViewCount() {
		return viewCount;
	}
	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}
	public Set<String> getGenres() {
		return genres;
	}
	public void setGenres(Set<String> genres) {
		this.genres = genres;
	}
	public Set<String> getLanguages() {
		return languages;
	}
	public void setLanguages(Set<String> languages) {
		this.languages = languages;
	}
	public boolean isUpcoming() {
		return upcoming;
	}
	public void setUpcoming(boolean upcoming) {
		this.upcoming = upcoming;
	}
	public AgeRating getAgeRating() {
		return ageRating;
	}
	public void setAgeRating(AgeRating ageRating) {
		this.ageRating = ageRating;
	}
	public String getBackgroundUrl() {
		return backgroundUrl;
	}
	public void setBackgroundUrl(String backgroundUrl) {
		this.backgroundUrl = backgroundUrl;
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
