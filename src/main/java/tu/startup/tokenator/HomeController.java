package tu.startup.tokenator;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@Controller
public class HomeController {

    private String clientId;

    private boolean isAuthenticated;

    private String accessToken;

    private String refreshToken;

    private String scopes;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("isAuthenticated", this.isAuthenticated);
        return "redirect:http://localhost:8383/index";
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("isAuthenticated", this.isAuthenticated);
        return "index";
    }

    @GetMapping("/oidc")
    public String oidcClient(Model model) {
        model.addAttribute("isAuthenticated", this.isAuthenticated);
        this.clientId = "oidc-client";
        if (isAuthenticated) {
            model.addAttribute("accessToken", this.accessToken);
            model.addAttribute("refreshToken", this.refreshToken);
            model.addAttribute("scopes", this.scopes);
            return "oidc";
        }
        return "index";
    }

    @GetMapping("/authorized/{clientId}")
    public String accessToken(Model model, @RequestParam String code, @PathVariable String clientId) {
        this.clientId = "oidc-client";
        if (!this.clientId.equals(clientId)) {
            model.addAttribute("clientId", this.clientId);
            return "authorized";
        }
        if (!this.isAuthenticated) {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.add("grant_type", "authorization_code");
            parameters.add("code", code);
            parameters.add("redirect_uri", "http://localhost:8383/authorized/" + clientId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBasicAuth("oidc-client", "secretto");

            String baseUrl = "http://localhost:8080";
            RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(parameters, headers, HttpMethod.POST, URI.create(baseUrl + "/oauth2/token"));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, String>>() {
            });
            this.isAuthenticated = true;
            this.accessToken = responseEntity.getBody().get("access_token");
            this.refreshToken = responseEntity.getBody().get("refresh_token");
            this.scopes = responseEntity.getBody().get("scope");
        }
        model.addAttribute("isAuthenticated", this.isAuthenticated);
        model.addAttribute("accessToken", this.accessToken);
        model.addAttribute("refreshToken", this.refreshToken);
        model.addAttribute("scopes", this.scopes);
        return "oidc";
    }
}
