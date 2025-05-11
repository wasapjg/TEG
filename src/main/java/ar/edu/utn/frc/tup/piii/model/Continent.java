import jakarta.persistence.*;

@Entity @Table(name = "continents") public class Continent { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    public Continent() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}

