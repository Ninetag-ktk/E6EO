package e6eo.finalproject.entityGoogle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class googleLists {
    private String kind;
    private String etag;
    private Object items;
}
