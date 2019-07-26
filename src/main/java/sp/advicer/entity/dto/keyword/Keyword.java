package sp.advicer.entity.dto.keyword;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Keyword {
    private Integer id;
    private String name;
}
