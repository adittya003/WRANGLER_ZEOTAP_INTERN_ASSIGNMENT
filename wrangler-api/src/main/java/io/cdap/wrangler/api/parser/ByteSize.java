/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a ByteSize token used in parsing directives.
 * Accepts strings like "100B", "10KB", "5MB", "2GB", "1TB", "3PB"
 * and converts them to bytes.
 */
public class ByteSize implements Token {

    private final long bytes;

    public ByteSize(String input) {
        this.bytes = parseBytes(input);
    }

    /**
     * Parses a human-readable byte size string into its equivalent bytes.
     *
     * Supported formats:
     * - "B" for bytes
     * - "KB" for kilobytes (1024 bytes)
     * - "MB" for megabytes (1024^2 bytes)
     * - "GB" for gigabytes (1024^3 bytes)
     * - "TB" for terabytes (1024^4 bytes)
     * - "PB" for petabytes (1024^5 bytes)
     *
     * @param input The input string like "10MB", "2GB", "5TB", etc.
     * @return The number of bytes as a long
     * @throws IllegalArgumentException if the unit is unrecognized
     */
    private long parseBytes(String input) {
        input = input.trim().toUpperCase();
        if (input.endsWith("PB")) {
            return Long.parseLong(input.replace("PB", "")) * 1024L * 1024 * 1024 * 1024 * 1024;
        }
        if (input.endsWith("TB")) {
            return Long.parseLong(input.replace("TB", "")) * 1024L * 1024 * 1024 * 1024;
        }
        if (input.endsWith("GB")) {
            return Long.parseLong(input.replace("GB", "")) * 1024L * 1024 * 1024;
        }
        if (input.endsWith("MB")) {
            return Long.parseLong(input.replace("MB", "")) * 1024L * 1024;
        }
        if (input.endsWith("KB")) {
            return Long.parseLong(input.replace("KB", "")) * 1024L;
        }
        if (input.endsWith("B")) {
            return Long.parseLong(input.replace("B", ""));
        }
        throw new IllegalArgumentException("Invalid byte size: " + input);
    }

    public static long convertBytesToUnit(long bytes, String unit) {
        switch (unit.toLowerCase()) {
            case "b":
                return bytes;
            case "kb":
                return bytes / 1024;
            case "mb":
                return bytes / (1024 * 1024);
            case "gb":
                return bytes / (1024 * 1024 * 1024);
            case "tb":
                return bytes / (1024L * 1024 * 1024 * 1024);
            case "pb":
                return bytes / (1024L * 1024 * 1024 * 1024 * 1024);
            default:
                throw new IllegalArgumentException("Unsupported size unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(bytes);
    }
}
