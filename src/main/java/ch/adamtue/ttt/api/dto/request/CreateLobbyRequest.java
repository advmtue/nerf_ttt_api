package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;

public class CreateLobbyRequest {
    @NotBlank
    private String name;

    // Name
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }
}
