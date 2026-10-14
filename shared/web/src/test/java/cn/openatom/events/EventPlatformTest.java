package cn.openatom.events;

import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class EventPlatformTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired RegistrationRepository registrations;
    @Autowired EventRepository events;
    @Autowired JwtEncoder encoder;
    private String unique(String prefix) { return prefix+UUID.randomUUID().toString().replace("-","").substring(0,12); }
    private String account(String name) throws Exception {
        var body=json.writeValueAsString(Map.of("username",name,"password","password123"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        return json.readTree(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).at("/data/token").asText();
    }
    private String eventBody(String title,int capacity) throws Exception {
        return json.writeValueAsString(Map.of("title",title,"description","学习Java与并发控制","location","二教105",
                "eventTime",OffsetDateTime.now(ZoneOffset.UTC).plusDays(2).toString(),"capacity",capacity));
    }
    private long create(String token,String title,int capacity) throws Exception {
        return json.readTree(mvc.perform(post("/api/events").header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content(eventBody(title,capacity))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).at("/data/id").asLong();
    }
    @Test void authHashInjectionAndSevenDayResume() throws Exception {
        String name=unique("alice"),token=account(name);
        assertThat(users.findByUsername(name).orElseThrow().getPasswordHash()).startsWith("$2").isNotEqualTo("password123");
        var first=json.readTree(mvc.perform(post("/api/auth/login").header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("登录成功")).andReturn().getResponse().getContentAsString());
        var second=json.readTree(mvc.perform(post("/api/auth/login").header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(first.at("/data/expiresAt")).isEqualTo(second.at("/data/expiresAt"));
        assertThat(Duration.between(Instant.now(),Instant.parse(first.at("/data/expiresAt").asText())).toSeconds()).isBetween(604700L,604800L);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username",name,"password","wrongpassword"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"' OR 1=1 --\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username",name,"password","password123"))))
                .andExpect(status().isConflict());
    }
    @Test void expiredAndTamperedTokensAreRejected() throws Exception {
        String token=account(unique("expired"));
        JwtClaimsSet claims=JwtClaimsSet.builder().issuer("openatom-events").subject("1")
                .issuedAt(Instant.now().minusSeconds(700000)).expiresAt(Instant.now().minusSeconds(1)).build();
        String expired=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        mvc.perform(post("/api/auth/login").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(401));
        String[] parts=token.split("\\.");
        String tampered=parts[0]+"."+parts[1]+"."+(parts[2].charAt(0)=='A'?"B":"A")+parts[2].substring(1);
        mvc.perform(post("/api/auth/login").header("Authorization","Bearer "+tampered)).andExpect(status().isUnauthorized());
    }
    @Test void permissionsCrudSearchAndCacheEviction() throws Exception {
        String owner=account(unique("owner")),other=account(unique("visitor")), title=unique("Workshop");
        mvc.perform(get("/api/events")).andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isArray());
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(eventBody(title,3)))
                .andExpect(status().isUnauthorized());
        // Populate an empty keyword page before create, then verify mutation evicts it.
        mvc.perform(get("/api/events").param("keyword",title)).andExpect(jsonPath("$.data.totalElements").value(0));
        long id=create(owner,title,3);
        mvc.perform(get("/api/events").param("keyword",title)).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get("/api/events").param("keyword",title).param("status","UPCOMING")).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get("/api/events").param("keyword",title).param("status","ENDED")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(put("/api/events/"+id).header("Authorization","Bearer "+other)
                .contentType(MediaType.APPLICATION_JSON).content(eventBody(title,5))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/events/"+id).header("Authorization","Bearer "+other)).andExpect(status().isForbidden());
        mvc.perform(put("/api/events/"+id).header("Authorization","Bearer "+owner)
                .contentType(MediaType.APPLICATION_JSON).content(eventBody(title+"updated",4))).andExpect(status().isOk());
        mvc.perform(get("/api/events").param("keyword",title)).andExpect(jsonPath("$.data.content[0].capacity").value(4));
        mvc.perform(delete("/api/events/"+id).header("Authorization","Bearer "+owner)).andExpect(status().isOk());
        mvc.perform(get("/api/events").param("keyword",title)).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(post("/api/events/"+id+"/registrations").header("Authorization","Bearer "+other)).andExpect(status().isConflict());
        mvc.perform(get("/api/events/99999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/events").param("size","101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/events").param("status","BAD")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/events").param("keyword","' OR 1=1 --")).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
    }
    @Test void registrationIsUniqueAndCapacityCannotShrinkBelowUsage() throws Exception {
        String owner=account(unique("host")),guest=account(unique("guest")),title=unique("Capacity");
        long id=create(owner,title,2);
        mvc.perform(get("/api/events/"+id+"/registrations/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events/"+id+"/registrations").header("Authorization","Bearer "+guest)).andExpect(status().isOk());
        mvc.perform(post("/api/events/"+id+"/registrations").header("Authorization","Bearer "+guest)).andExpect(status().isConflict());
        mvc.perform(post("/api/events/"+id+"/registrations").header("Authorization","Bearer "+owner)).andExpect(status().isOk());
        mvc.perform(put("/api/events/"+id).header("Authorization","Bearer "+owner)
                .contentType(MediaType.APPLICATION_JSON).content(eventBody(title,1))).andExpect(status().isConflict());
        mvc.perform(get("/api/events/"+id)).andExpect(jsonPath("$.data.registeredCount").value(2));
        mvc.perform(get("/api/events/"+id+"/registrations/me").header("Authorization","Bearer "+guest))
                .andExpect(jsonPath("$.data.registered").value(true));
    }
    @Test void concurrentRegistrationsCannotOversell() throws Exception {
        String owner=account(unique("concurrent"));
        long id=create(owner,unique("race"),3);
        List<String> tokens=new ArrayList<>();
        for(int i=0;i<8;i++) tokens.add(account(unique("racer")));
        ExecutorService pool=Executors.newFixedThreadPool(8);
        CountDownLatch gate=new CountDownLatch(1);
        try {
            List<Future<Integer>> results=new ArrayList<>();
            for(String token:tokens) results.add(pool.submit(()->{
                gate.await();
                return mvc.perform(post("/api/events/"+id+"/registrations").header("Authorization","Bearer "+token))
                        .andReturn().getResponse().getStatus();
            }));
            gate.countDown();
            List<Integer> statuses=new ArrayList<>();
            for(Future<Integer> result:results) statuses.add(result.get(20,TimeUnit.SECONDS));
            assertThat(statuses).containsOnly(200,409);
            assertThat(statuses.stream().filter(x->x==200).count()).isEqualTo(3);
            assertThat(registrations.countByEventId(id)).isEqualTo(3);
            assertThat(events.findById(id).orElseThrow().getRegisteredCount()).isEqualTo(3);
        } finally { pool.shutdownNow(); }
    }
    @Test void malformedInputAndStaticPage() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        String owner=account(unique("valid"));
        mvc.perform(post("/api/events").header("Authorization","Bearer "+owner).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"bad\",\"capacity\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/events").param("page","-1")).andExpect(status().isBadRequest());
    }
}
