# CDAP Wrangler - Byte Size and Time Duration Enhancements

## Overview

This project enhances the CDAP Wrangler library by adding support for parsing and processing byte size (e.g., KB, MB, GB) and time duration (e.g., ms, seconds) units. These enhancements enable users to easily handle and perform calculations on columns representing data sizes or time intervals within Wrangler recipes. The modifications include changes to the grammar, API, core parser, and the introduction of a new directive for aggregation.

## Enhancements

### 1. **Byte Size and Time Duration Parsers**
- **Byte Size (e.g., 10KB, 100MB)**: Supports parsing byte size units and converting them into a canonical unit (bytes).
- **Time Duration (e.g., 10ms, 5s)**: Supports parsing time duration units and converting them into a canonical unit (nanoseconds).
  
These units can now be used within Wrangler recipes to perform calculations on data that involves byte sizes or time durations.

### 2. **Grammar Modification**
- Modified the **ANTLR grammar** (`Directives.g4`) to add lexer rules for **BYTE_SIZE** and **TIME_DURATION** units.
- Introduced new parser rules to handle these units, including `byteSizeArg` and `timeDurationArg`.

### 3. **API Changes**
- **New Java Classes**: Created two new classes, `ByteSize.java` and `TimeDuration.java`, in the `wrangler-api` module. These classes extend the `Token` class and parse the unit strings (e.g., "10KB", "150ms") in their constructors.
- Methods were added to retrieve the canonical value for each unit:
  - `ByteSize.getBytes()` to return the value in bytes.
  - `TimeDuration.getNanoseconds()` to return the value in nanoseconds.

### 4. **Core Parser Updates**
- Updated the **parser** to handle the new tokens (`BYTE_SIZE` and `TIME_DURATION`).
- Added visit methods to the parser to properly handle `byteSizeArg` and `timeDurationArg` within recipes.

### 5. **New Directive - Aggregate Stats**
- A new directive, **aggregate-stats**, was implemented to aggregate byte size and time duration values.
- The directive takes the following parameters:
  - Source columns for byte size and time duration.
  - Target columns for the aggregated total or average values.
- It supports optional unit conversions (e.g., converting bytes to MB) and aggregation types (total, average, etc.).

### 6. **Testing**
- **Unit Tests**: Added unit tests for `ByteSize` and `TimeDuration` classes to ensure correct parsing and conversion to canonical units.
- **Parser Tests**: Updated the parser tests to validate recipes using the new syntax and ensure invalid syntax is rejected.
- **Directive Tests**: Added comprehensive tests for the **aggregate-stats** directive to ensure it performs correct aggregation and unit conversions.

## Example Usage

### Recipe Example

Here is an example of how you can use the new `aggregate-stats` directive in a Wrangler recipe:

```plaintext
aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec
```

This recipe will:
- Aggregate the values from the `data_transfer_size` column (in bytes).
- Aggregate the values from the `response_time` column (in milliseconds).
- Convert the aggregated byte size to megabytes and store it in the `total_size_mb` column.
- Convert the aggregated time duration to seconds and store it in the `total_time_sec` column.

### Sample Input Data

| data_transfer_size | response_time |
|--------------------|---------------|
| 1024               | 150           |
| 2048               | 200           |
| 3072               | 250           |

### Expected Output

| total_size_mb | total_time_sec |
|----------------|----------------|
| 0.003         | 0.0008         |

## Steps to Use

1. **Clone the Repository**

```bash
git clone https://github.com/your-username/wrangler.git
```

2. **Add the Original Repository as a Remote**

```bash
git remote add upstream https://github.com/data-integrations/wrangler.git
```

3. **Build the Project**

Navigate to the root directory and run:

```bash
mvn clean install
```

4. **Run Tests**

To verify the changes and ensure everything works as expected:

```bash
mvn test
```

## Contributions

1. Fork the repository and clone it to your local machine.
2. Make the necessary modifications.
3. Test the changes to ensure functionality.
4. Commit the changes to your fork and submit a pull request.
