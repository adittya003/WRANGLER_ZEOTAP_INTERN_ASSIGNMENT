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
 * Represents a TimeDuration token used in parsing directives.
 * Accepts strings like "10s", "5m", "2h", "100ms", "3d", "1w", "500us",
 * "1000ns"
 * and converts them to milliseconds.
 */
public class TimeDuration implements Token {

    // Stores the duration in milliseconds after parsing.
    private final long milliseconds;

    /**
     * Constructs the TimeDuration token by parsing the input.
     *
     * @param input The time duration string (e.g., "10s", "100ms").
     */
    public TimeDuration(String input) {
        this.milliseconds = parseDuration(input);
    }

    /**
     * Parses a human-readable time duration string into milliseconds.
     * Only whole-number durations are supported.
     *
     * @param input The input string (e.g., "10s", "5m").
     * @return The duration in milliseconds as a long.
     * @throws IllegalArgumentException if the unit is unrecognized.
     */
    private long parseDuration(String input) {
        input = input.trim().toLowerCase();
        if (input.endsWith("ns")) {
            return Long.parseLong(input.replace("ns", "").trim()) / 1_000_000;
        }
        if (input.endsWith("us")) {
            return Long.parseLong(input.replace("us", "").trim()) / 1_000;
        }
        if (input.endsWith("ms")) {
            return Long.parseLong(input.replace("ms", "").trim());
        }
        if (input.endsWith("s")) {
            return Long.parseLong(input.replace("s", "").trim()) * 1000L;
        }
        if (input.endsWith("m")) {
            return Long.parseLong(input.replace("m", "").trim()) * 60 * 1000L;
        }
        if (input.endsWith("h")) {
            return Long.parseLong(input.replace("h", "").trim()) * 60 * 60 * 1000L;
        }
        if (input.endsWith("d")) {
            return Long.parseLong(input.replace("d", "").trim()) * 24 * 60 * 60 * 1000L;
        }
        if (input.endsWith("w")) {
            return Long.parseLong(input.replace("w", "").trim()) * 7 * 24 * 60 * 60 * 1000L;
        }
        throw new IllegalArgumentException("Invalid time duration: " + input);
    }

    /**
     * Converts milliseconds to the specified unit.
     *
     * @param millis The milliseconds.
     * @param unit   The target unit (e.g., "s", "m").
     * @return The converted value as a long.
     */
    public static long convertMillisecondsToUnit(long millis, String unit) {
        switch (unit.toLowerCase()) {
            case "ms":
                return millis;
            case "s":
                return millis / 1000;
            case "m":
                return millis / (1000 * 60);
            case "h":
                return millis / (1000 * 60 * 60);
            case "d":
                return millis / (1000 * 60 * 60 * 24);
            case "w":
                return millis / (1000 * 60 * 60 * 24 * 7);
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public Object value() {
        return milliseconds;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(milliseconds);
    }
}
