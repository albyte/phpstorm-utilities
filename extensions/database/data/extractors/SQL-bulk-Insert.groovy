/*
 * Available context bindings:
 *   COLUMNS     List<DataColumn>
 *   ROWS        Iterable<DataRow>
 *   OUT         { append() }
 *   FORMATTER   { format(row, col); formatValue(Object, col) }
 *   TRANSPOSED  Boolean
 * plus ALL_COLUMNS, TABLE, DIALECT
 *
 * where:
 *   DataRow     { rowNumber(); first(); last(); data(): List<Object>; value(column): Object }
 *   DataColumn  { columnNumber(), name() }
 */

SEP = ", "
QUOTE     = "\'"
NEWLINE   = System.getProperty("line.separator")

ROWS_LIST = ROWS.toList()
ROWS_MAX_IDX = ROWS_LIST.size() - 1

if (ROWS_MAX_IDX >= 0) {
    OUT.append("INSERT INTO ")
    if (TABLE == null) OUT.append("MY_TABLE")
    else OUT.append(TABLE.getParent().getName()).append(".").append(TABLE.getName())
    OUT.append(" (")

    COLUMNS.eachWithIndex { column, idx ->
        OUT.append(column.name()).append(idx != COLUMNS.size() - 1 ? SEP : "")
    }
    OUT.append(") VALUES ").append(NEWLINE)
}
def record(columns, dataRow, rowIdx) {
    OUT.append("(")
    columns.eachWithIndex { column, idx ->
        def skipQuote = dataRow.value(column).toString().isNumber() || dataRow.value(column) == null
        def stringValue = FORMATTER.format(dataRow, column)
        if (DIALECT.getFamilyId().isMysql()) stringValue = stringValue.replace("\\", "\\\\")
        OUT.append(skipQuote ? "": QUOTE).append(stringValue.replace(QUOTE, QUOTE + QUOTE))
           .append(skipQuote ? "": QUOTE).append(idx != columns.size() - 1 ? SEP : "")
    }
    OUT.append(")").append(rowIdx != ROWS_MAX_IDX ? SEP : ";" ).append(NEWLINE)
}
ROWS_LIST.eachWithIndex { row, idx -> record(COLUMNS, row, idx) }
