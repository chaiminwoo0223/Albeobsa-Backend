package skhu.jijijig.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.json.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import skhu.jijijig.domain.dto.MemberDTO;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.repository.MemberRepository;
import skhu.jijijig.token.TokenProvider;
import skhu.jijijig.token.TokenRevoker;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final TokenRevoker tokenRevoker;

    @Value("${spring.security.oauth2.google-client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.google-client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.google-redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.google-token-url}")
    private String googleTokenUrl;

    @Value("${spring.security.oauth2.google-userInfo-uri}")
    private String googleUserInfoUri;

    public String getGoogleTokens(String code) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(googleTokenUrl, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String json = response.getBody();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> responseMap = gson.fromJson(json, type);
            if (responseMap != null && responseMap.containsKey("access_token")) {
                return responseMap.get("access_token");
            } else {
                throw new IllegalStateException("응답에 액세스 토큰이 포함되어 있지 않습니다.");
            }
        } else {
            throw new IllegalStateException("Google 액세스 토큰을 검색하지 못했습니다: " + response.getStatusCode());
        }
    }

    public MemberDTO getMemberDTO(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = googleUserInfoUri + "?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<MemberDTO> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {});
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new IllegalStateException("사용자 정보를 검색하지 못했습니다.");
        }
    }

    public TokenDTO googleLoginSignup(String code) {
        String accessToken = getGoogleTokens(code);
        MemberDTO memberDTO = getMemberDTO(accessToken);
        Member member = memberRepository.findByEmail(memberDTO.getEmail())
                .orElseGet(() -> memberRepository.save(Member.fromDTO(memberDTO)));
        return tokenProvider.createTokens(member);
    }

    public TokenDTO refreshAccessToken(String refreshToken) {
        return tokenProvider.renewToken(refreshToken);
    }

    public void deactivateTokens(String accessToken, String refreshToken) {
        tokenRevoker.addToBlackList(accessToken);
        tokenRevoker.addToBlackList(refreshToken);
    }
}