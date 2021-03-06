/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import java.util.Map;

public class TwitterAPIConfiguration extends TwitterResponseObject implements TwitterResponse {
    public int getCharactersReservedPerMedia() {
        throw new UnsupportedOperationException();
    }

    public int getMaxMediaPerUpload() {
        throw new UnsupportedOperationException();
    }

    public String[] getNonUsernamePaths() {
        throw new UnsupportedOperationException();
    }

    public int getPhotoSizeLimit() {
        throw new UnsupportedOperationException();
    }

    public Map<Integer, MediaEntity.Size> getPhotoSizes() {
        throw new UnsupportedOperationException();
    }

    public int getShortUrlLength() {
        throw new UnsupportedOperationException();
    }

    public int getShortUrlLengthHttps() {
        throw new UnsupportedOperationException();
    }
}
