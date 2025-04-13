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

package io.cdap.directives.aggregates;

import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.List;

/**
 * A directive that aggregates byte size and time duration values across rows.
 * Computes sum or average of specified columns, converts results to target
 * units, and outputs a single aggregated row.
 */
@Plugin(type = Directive.TYPE)
public class AggregateSizeDuration implements Directive {

    private String sourceSizeColumn;
    private String sourceTimeColumn;
    private String targetSizeColumn;
    private String targetTimeColumn;
    private String sizeUnit;
    private String timeUnit;
    private String aggregationType;

    // Running totals; totalSize is a long to hold fractional sizes.
    private long totalSize = 0;
    private long totalTime = 0;
    private long count = 0;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("AggregateSizeDuration");
        builder.define("sourceSizeColumn", TokenType.COLUMN_NAME);
        builder.define("sourceTimeColumn", TokenType.COLUMN_NAME);
        builder.define("targetSizeColumn", TokenType.COLUMN_NAME);
        builder.define("targetTimeColumn", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.TEXT, true);
        builder.define("timeUnit", TokenType.TEXT, true);
        builder.define("aggregationType", TokenType.TEXT, true);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        try {
            ColumnName sourceSize = (ColumnName) args.value("sourceSizeColumn");
            ColumnName sourceTime = (ColumnName) args.value("sourceTimeColumn");
            ColumnName targetSize = (ColumnName) args.value("targetSizeColumn");
            ColumnName targetTime = (ColumnName) args.value("targetTimeColumn");

            this.sourceSizeColumn = sourceSize.value();
            this.sourceTimeColumn = sourceTime.value();
            this.targetSizeColumn = targetSize.value();
            this.targetTimeColumn = targetTime.value();

            if (args.contains("sizeUnit")) {
                Text unit = (Text) args.value("sizeUnit");
                this.sizeUnit = unit.value();
            }
            if (args.contains("timeUnit")) {
                Text tunit = (Text) args.value("timeUnit");
                this.timeUnit = tunit.value();
            }
            if (args.contains("aggregationType")) {
                Text aggTypeText = (Text) args.value("aggregationType");
                this.aggregationType = aggTypeText.value();
            }
        } catch (Exception e) {
            throw new DirectiveParseException(
                    "Failed to parse arguments for AggregateSizeDuration directive.", e);
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context)
            throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {
        for (Row row : rows) {
            try {
                Object sizeVal = row.getValue(sourceSizeColumn);
                Object timeVal = row.getValue(sourceTimeColumn);

                if (sizeVal == null || timeVal == null) {
                    throw new DirectiveExecutionException(
                            "Null encountered in required fields: " + sourceSizeColumn + " or " + sourceTimeColumn);
                }

                // Get the size in bytes as a long.
                long sizeBytes = new ByteSize(sizeVal.toString()).getBytes();
                long durationMillis = new TimeDuration(timeVal.toString()).getMilliseconds();

                totalSize += sizeBytes;
                totalTime += durationMillis;
                count++;
            } catch (Exception e) {
                throw new DirectiveExecutionException("Error processing row: " + row.toString(), e);
            }
        }
        return Collections.emptyList();
    }

    public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
        long finalSizeBytes = totalSize;
        long finalTimeMillis = totalTime;

        if ("avg".equalsIgnoreCase(aggregationType) && count > 0) {
            finalSizeBytes = totalSize / count;
            finalTimeMillis = totalTime / count;
        }

        String sizeTargetUnit = (sizeUnit != null && !sizeUnit.isEmpty()) ? sizeUnit : "MB";
        String timeTargetUnit = (timeUnit != null && !timeUnit.isEmpty()) ? timeUnit : "s";

        double convertedSize;
        long convertedTime;
        try {
            convertedSize = ByteSize.convertBytesToUnit(finalSizeBytes, sizeTargetUnit);
        } catch (IllegalArgumentException e) {
            throw new DirectiveExecutionException("Invalid size unit specified: " + sizeTargetUnit, e);
        }
        try {
            convertedTime = TimeDuration.convertMillisecondsToUnit(finalTimeMillis, timeTargetUnit);
        } catch (IllegalArgumentException e) {
            throw new DirectiveExecutionException("Invalid time unit specified: " + timeTargetUnit, e);
        }

        Row resultRow = new Row();
        resultRow.add(targetSizeColumn, convertedSize);
        resultRow.add(targetTimeColumn, convertedTime);

        return Collections.singletonList(resultRow);
    }

    @Override
    public void destroy() {
        // No resources to clean up in this implementation.
    }
}
