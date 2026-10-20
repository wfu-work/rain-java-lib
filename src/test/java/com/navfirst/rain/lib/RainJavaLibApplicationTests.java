package com.navfirst.rain.lib;

import com.navfirst.rain.lib.services.RainLibService;
import com.navfirst.rain.lib.exceptions.RainLibException;
import com.navfirst.rain.lib.domains.RainConfig;
import com.navfirst.rain.lib.domains.RainRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class RainJavaLibApplicationTests {

    private final RainLibService rainLibService;

    private final ApplicationContext applicationContext;

    @Test
    void testVersion() {
        String version = this.rainLibService.getVersion();
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"), version);
        log.info("算法版本：{}", version);
    }

    @Test
    void testSingleServiceBean() {
        assertEquals(1, this.applicationContext.getBeansOfType(RainLibService.class).size());
    }

    @Test
    void testCreateWithoutModelDir() {
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.create(new RainConfig()));
        assertEquals("INVALID_ARGUMENT", error.getCode());
        assertEquals("modelDir is required", error.getMsg());
        assertEquals(error.getMsg(), error.getMessage());
    }

    @Test
    void testCreateWithNullConfig() {
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.create(null));
        assertEquals("INVALID_ARGUMENT", error.getCode());
    }

    @Test
    void testPredictWithUnknownHandle() {
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.predict(0L, new RainRequest()));
        assertEquals("INVALID_HANDLE", error.getCode());
        assertEquals("unknown or closed instance handle", error.getMessage());
    }

    @Test
    void testPredictWithNullRequest() {
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.predict(0L, null));
        assertEquals("INVALID_ARGUMENT", error.getCode());
    }

    @Test
    void testCloseWithUnknownHandle() {
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.close(0L));
        assertEquals("INVALID_HANDLE", error.getCode());
    }

    @Test
    void testUtf8ModelPath(@TempDir Path directory) {
        String modelDir = directory.resolve("站点 模型").toString();
        RainConfig config = RainConfig.builder().modelDir(modelDir).build();
        RainLibException error = assertThrows(RainLibException.class,
                () -> this.rainLibService.create(config));
        assertEquals("MODEL_ERROR", error.getCode());
        assertTrue(error.getMessage().contains(modelDir), error.getMessage());
    }

}
