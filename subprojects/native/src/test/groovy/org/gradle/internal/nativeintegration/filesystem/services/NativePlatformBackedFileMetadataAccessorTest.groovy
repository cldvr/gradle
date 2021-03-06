/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.nativeintegration.filesystem.services

import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.file.Files
import org.gradle.api.JavaVersion
import org.gradle.internal.file.FileMetadataSnapshot
import org.gradle.internal.nativeintegration.filesystem.FileMetadataAccessor
import org.gradle.internal.os.OperatingSystem
import org.gradle.util.UsesNativeServices

import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributeView

@UsesNativeServices
class NativePlatformBackedFileMetadataAccessorTest extends AbstractFileMetadataAccessorTest {

    @Override
    FileMetadataAccessor getAccessor() {
        return new NativePlatformBackedFileMetadataAccessor(Native.get(Files.class))
    }

    @Override
    void assertSameLastModified(FileMetadataSnapshot metadataSnapshot, File file) {
        assert maybeRoundLastModified(metadataSnapshot.lastModified) == maybeRoundLastModified(lastModifiedViaJavaNio(file))
    }

    private static maybeRoundLastModified(long lastModified) {
        // Java 8 on Unix only captures the seconds in lastModified, so we cut it off the value returned from the filesystem as well
        return (JavaVersion.current().java9Compatible || OperatingSystem.current().windows)
            ? lastModified
            : lastModified.intdiv(1000) * 1000
    }

    private static long lastModifiedViaJavaNio(File file) {
        return java.nio.file.Files.getFileAttributeView(file.toPath(), BasicFileAttributeView, LinkOption.NOFOLLOW_LINKS).readAttributes().lastModifiedTime().toMillis()
    }
}
