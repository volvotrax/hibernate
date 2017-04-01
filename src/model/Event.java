package model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Event {

    @Id
    @GeneratedValue
    private Long idEvent;

    private String name;

    private String description;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name="EventArtists",
            joinColumns=
            @JoinColumn(name="idEvent"),
            inverseJoinColumns=
            @JoinColumn(name="idArtist")
    )
    private Set<Artist> artists = new HashSet<Artist>();

    public Event() {
    }

    public Event(String name, String description) {
        this.description = description;
        this.name = name;
    }

    public Set<Artist> getArtists() {
        return artists;
    }

    public void setArtists(Set<Artist> artists) {
        this.artists = artists;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(Long idEvent) {
        this.idEvent = idEvent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Event{" +
                "description='" + description + '\'' +
                ", idEvent=" + idEvent +
                ", name='" + name + '\'' +
                '}';
    }
}
