package max.lab.r2app.reactiveapp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table("app_user")
public class AppUser {
    @NotEmpty
    @Size(min = 5, max = 20)
    @Id
    private String id;

    @NotEmpty
    @Size(max = 200)
    @Column("first_name")
    private String firstName;

    @NotEmpty
    @Size(max = 200)
    @Column("last_name")
    private String lastName;

    @Size(min = 1, max = 200)
    @Column("middle_name")
    private String middleName;

    @NotEmpty
    @Size(max = 50)
    private String gender;

    @NotNull
    @Min(1)
    @Max(150)
    private Integer age;

    @NotEmpty
    @Size(max = 200)
    private String province;

    @NotEmpty
    @Size(max = 200)
    private String city;
}
