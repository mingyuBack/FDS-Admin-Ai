package kdt.project.fds.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FDS_CONFIG", schema = "FDS_ADMIN")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FdsConfig {
    @Id
    @Column(name = "CONFIG_KEY")
    private String configKey;

    @Column(name = "CONFIG_VALUE")
    private String configValue;

    @Column(name = "DESCRIPTION")
    private String description;
}