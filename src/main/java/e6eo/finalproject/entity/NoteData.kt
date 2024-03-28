package e6eo.finalproject.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NoteData {
    private String observe;
    private String categoryId = null;
    private Map<String, Object> note;
}
