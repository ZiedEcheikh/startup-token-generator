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

    @GetMapping("/")
    public String home() {
        return "redirect:http://localhost:8383/index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/oidc")
    public String oidcClient() {
        this.clientId = "oidc-client";
        return "oidc";
    }

    @GetMapping("/authorized/{clientId}")
    public String accessToken(Model model, @RequestParam String code, @PathVariable String clientId) {
        if (!this.clientId.equals(clientId)) {
            model.addAttribute("clientId", this.clientId);
            return "authorized";
        }

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
        model.addAttribute("accessToken", responseEntity.getBody().get("access_token"));

        return "authorized";
    }
}
