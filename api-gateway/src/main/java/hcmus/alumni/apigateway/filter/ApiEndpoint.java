package hcmus.alumni.apigateway.filter;

import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiEndpoint {
    private HttpMethod method;
    private String path;
}
