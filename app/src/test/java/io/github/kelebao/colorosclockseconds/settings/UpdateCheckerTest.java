package io.github.kelebao.colorosclockseconds.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 更新检查的 tag 解析单元测试（纯函数，不依赖 Android 运行时）。
 *
 * <p>覆盖三级取数里用到的两种解析：模块仓库 tag {@code 346-1.3.0} 取版本名，
 * 以及从网页 302 {@code Location} / atom feed 文本里抽 tag。</p>
 */
public class UpdateCheckerTest {

    @Test
    public void versionFromTag_stripsVersionCode() {
        assertEquals("1.3.0", UpdateChecker.versionFromTag("346-1.3.0"));
        assertEquals("1.2.0", UpdateChecker.versionFromTag("249-1.2.0"));
    }

    @Test
    public void versionFromTag_handlesPlainAndPrefixed() {
        assertEquals("1.0.4", UpdateChecker.versionFromTag("1.0.4"));
        assertEquals("1.3.0", UpdateChecker.versionFromTag("v1.3.0"));
        assertNull(UpdateChecker.versionFromTag(""));
        assertNull(UpdateChecker.versionFromTag(null));
    }

    @Test
    public void versionFromTagUrl_readsLocationHeader() {
        assertEquals("1.3.0", UpdateChecker.versionFromTagUrl(
                "https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds"
                        + "/releases/tag/346-1.3.0"));
    }

    @Test
    public void versionFromTagUrl_readsAtomEntryId() {
        String atom = "<?xml version=\"1.0\"?>\n<feed>\n<entry>\n"
                + "<id>tag:github.com,2008:Repository/1420903695/346-1.3.0</id>\n"
                + "<link rel=\"alternate\" href=\"https://github.com/Xposed-Modules-Repo/"
                + "io.github.kelebao.colorosclockseconds/releases/tag/346-1.3.0\"/>\n"
                + "</entry>\n</feed>";
        assertEquals("1.3.0", UpdateChecker.versionFromTagUrl(atom));
    }

    @Test
    public void versionFromTagUrl_returnsNullWhenAbsent() {
        assertNull(UpdateChecker.versionFromTagUrl(null));
        assertNull(UpdateChecker.versionFromTagUrl("{\"message\":\"API rate limit exceeded\"}"));
    }

    @Test
    public void compareVersions_basic() {
        assertEquals(0, UpdateChecker.compareVersions("1.3.0", "1.3.0"));
        assertEquals(1, Integer.signum(UpdateChecker.compareVersions("1.3.1", "1.3.0")));
        assertEquals(-1, Integer.signum(UpdateChecker.compareVersions("1.2.0", "1.3.0")));
        assertEquals(1, Integer.signum(UpdateChecker.compareVersions("1.10.0", "1.9.9")));
    }

    @Test
    public void compareVersions_ignoresFlavorSuffix() {
        // 模块仓库 tag=354-1.3.1 -> 远端 "1.3.1"；modern 包 BuildConfig.VERSION_NAME="1.3.1-api102"
        assertEquals(0, UpdateChecker.compareVersions("1.3.1", "1.3.1-api102"));
        assertEquals(0, UpdateChecker.compareVersions("1.3.1-api102", "1.3.1"));
        assertEquals(0, UpdateChecker.compareVersions("1.3.1-api102", "1.3.1-api102"));
        assertEquals(-1, Integer.signum(
                UpdateChecker.compareVersions("1.3.1-api102", "1.3.2")));
        assertEquals(1, Integer.signum(
                UpdateChecker.compareVersions("1.3.2", "1.3.1-api102")));
        assertEquals(0, UpdateChecker.compareVersions(
                "1.2.0-test.20261102-1500", "1.2.0"));
    }
}
