package backend.academy.scrapper.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class ScrapperException extends RuntimeException {

    private String description;
    private HttpStatus status;

    public ScrapperException(String description, HttpStatus status) {
        super(description);
        this.description = description;
        this.status = status;
    }
}
