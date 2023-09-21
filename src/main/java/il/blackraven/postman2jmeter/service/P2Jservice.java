package il.blackraven.postman2jmeter.service;

import il.blackraven.postman2jmeter.dto.P2JRequest;
import il.blackraven.postman2jmeter.dto.P2JResponse;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class P2Jservice implements IP2J {
    Logger log = LoggerFactory.getLogger(P2Jservice.class);
    @Override
    public P2JResponse convert(P2JRequest request) {
        String postmanConfigBase64 = request.getPostmanConfig();
        if(postmanConfigBase64 == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No postman config was given");

        String jmeterJMX = "";
        try {
            String file = saveConfigToFile(postmanConfigBase64);
            jmeterJMX = runConvertPostmanJmeter(file);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while converting: " + e.getMessage());
        }
        return new P2JResponse(Base64.encodeBase64String(jmeterJMX.getBytes(StandardCharsets.UTF_8)));
    }

    private String runConvertPostmanJmeter(String path) throws IOException, InterruptedException, ExecutionException {
        boolean isWindows = System.getProperty("os.name")
            .toLowerCase().startsWith("windows");

        log.info("Run converter:");
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "convert-postman-jmeter", "-p", path + "postman.cfg", "-j", path + "jmeterOut.jmx");
        } else {
            builder.command("sh", "-c", "convert-postman-jmeter", "-p", path + "postman.cfg", "-j", path + "jmeterOut.jmx");
        }
        log.info(builder.command().toString());
        Process process = builder.start();

        int exitCode = process.waitFor();
        if (exitCode == 1) {
            log.info("Convert successful!");
            Path file = Path.of(path, "jmeterOut.jmx");
            String result = Files.readString(file);
            log.info(result);
            return result;
        }
        return "";
    }

    private String saveConfigToFile(String postmanConfigBase64) throws IOException {
            byte[] postmanConfig = Base64.decodeBase64(postmanConfigBase64);
            log.info("Base64 postman config: " + postmanConfigBase64);
            String dirname = "out_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String fileName = "postman.cfg";
            Path path = Path.of("/usr", "app", "p2j", "files", dirname);
            Files.createDirectories(path);
            Files.write(Path.of(path.toString(), fileName), postmanConfig);
            log.info("Saved to file " + path);
            return path.toString();
    }
}
