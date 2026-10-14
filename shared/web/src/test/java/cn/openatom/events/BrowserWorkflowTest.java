package cn.openatom.events;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.assertj.core.api.Assertions.*;

/** Browser uses real Spring MVC/security/database through stdio because sandbox blocks loopback. */
@SpringBootTest @AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="BROWSER_EXE",matches=".+")
class BrowserWorkflowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Test void completeBrowserWorkflow() throws Exception {
        Process child=new ProcessBuilder("node","tools/browser-check.cjs").directory(new File(System.getProperty("basedir",System.getProperty("user.dir"))))
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try(var requests=new BufferedReader(new InputStreamReader(child.getInputStream(),StandardCharsets.UTF_8));
            var replies=new BufferedWriter(new OutputStreamWriter(child.getOutputStream(),StandardCharsets.UTF_8))) {
            String line;
            while((line=requests.readLine())!=null) {
                JsonNode message=json.readTree(line);
                if(message.has("done")) break;
                String path=message.get("path").asText();
                if(path.equals("/")) path="/index.html";
                var request=MockMvcRequestBuilders.request(HttpMethod.valueOf(message.get("method").asText()),path);
                if(message.has("body")) request.content(message.get("body").asText());
                var headers=message.get("headers");
                if(headers!=null) headers.fields().forEachRemaining(x->request.header(x.getKey(),x.getValue().asText()));
                var response=mvc.perform(request).andReturn().getResponse();
                replies.write(json.writeValueAsString(Map.of("status",response.getStatus(),
                        "contentType",Optional.ofNullable(response.getContentType()).orElse("text/plain"),
                        "body",response.getContentAsString(StandardCharsets.UTF_8))));
                replies.newLine(); replies.flush();
            }
        }
        assertThat(child.waitFor()).isZero();
    }
}
