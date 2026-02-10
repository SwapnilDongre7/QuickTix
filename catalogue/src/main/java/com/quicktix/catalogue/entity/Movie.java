package com.quicktix.catalogue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.quicktix.catalogue.enums.AgeRating;
import com.quicktix.catalogue.enums.MovieStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
//import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
@AllArgsConstructor
// @Builder
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	private LocalDate releaseDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "age_rating")
	private AgeRating ageRating;

	@Enumerated(EnumType.STRING)
	private MovieStatus status = MovieStatus.RUNNING;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "rating", precision = 2, scale = 1)
	private BigDecimal rating;

	@Column(name = "poster_url")
	private String posterUrl;

	@Column(name = "background_url")
	private String backgroundUrl;

	@Column(name = "cast_members", columnDefinition = "TEXT")
	private String castMembers;

	@Column(name = "crew_members", columnDefinition = "TEXT")
	private String crewMembers;

	@Column(name = "view_count", nullable = false)
	private Long viewCount = 0L;

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private Set<Genre> genres = new HashSet<>();

	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(name = "movie_languages", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "language_id"))
	private Set<Language> languages = new HashSet<>();

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Movie() {
	}

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

	public String getBackgroundUrl() {
		return backgroundUrl;
	}

	public void setBackgroundUrl(String backgroundUrl) {
		this.backgroundUrl = backgroundUrl;
	}

	public String getCastMembers() {
		return castMembers;
	}

	public void setCastMembers(String castMembers) {
		this.castMembers = castMembers;
	}

	public String getCrewMembers() {
		return crewMembers;
	}

	public void setCrewMembers(String crewMembers) {
		this.crewMembers = crewMembers;
	}

	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	public Set<Genre> getGenres() {
		return genres;
	}

	public void setGenres(Set<Genre> genres) {
		this.genres = genres;
	}

	public Set<Language> getLanguages() {
		return languages;
	}

	public void setLanguages(Set<Language> languages) {
		this.languages = languages;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public AgeRating getAgeRating() {
		return ageRating;
	}

	public void setAgeRating(AgeRating ageRating) {
		this.ageRating = ageRating;
	}

	public MovieStatus getStatus() {
		return status;
	}

	public void setStatus(MovieStatus status) {
		this.status = status;
	}

}
