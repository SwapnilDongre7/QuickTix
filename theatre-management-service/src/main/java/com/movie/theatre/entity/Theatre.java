package com.movie.theatre.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "theatres")
public class Theatre {

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private TheatreOwner owner;

    // Refers to cities.id from Catalogue Service
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    public Long getId() {
        return id;
    }

    public TheatreOwner getOwner() {
        return owner;
    }

    public void setOwner(TheatreOwner owner) {
        this.owner = owner;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
