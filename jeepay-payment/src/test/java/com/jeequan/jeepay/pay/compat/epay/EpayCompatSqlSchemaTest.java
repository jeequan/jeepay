package com.jeequan.jeepay.pay.compat.epay;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class EpayCompatSqlSchemaTest {

    @Test
    void payOrderCallbackColumnsAreUnboundedForCompatUrls() throws IOException {
        String initSql = Files.readString(repoFile("docs/sql/init.sql"));
        String patchSql = Files.readString(repoFile("docs/sql/patch.sql"));

        assertThat(initSql).contains("`notify_url` TEXT NOT NULL COMMENT '异步通知地址'");
        assertThat(initSql).contains("`return_url` TEXT COMMENT '页面跳转地址'");
        assertThat(patchSql).contains("MODIFY COLUMN `notify_url` TEXT NOT NULL COMMENT '异步通知地址'");
        assertThat(patchSql).contains("MODIFY COLUMN `return_url` TEXT COMMENT '页面跳转地址'");
    }

    private static Path repoFile(String relativePath) {
        Path path = Path.of(relativePath);
        return Files.exists(path) ? path : Path.of("..").resolve(relativePath);
    }
}
