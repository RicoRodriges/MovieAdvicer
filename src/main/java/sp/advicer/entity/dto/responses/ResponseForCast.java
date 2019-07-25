package sp.advicer.entity.dto.responses;

import lombok.Data;
import sp.advicer.entity.dto.actor.Actor;

import java.util.List;

@Data
public class ResponseForCast {
    List<Actor> cast;

}
