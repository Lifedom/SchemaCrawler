/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.utility;


import static java.util.Objects.requireNonNull;
import static sf.util.DatabaseUtility.executeSql;
import static sf.util.DatabaseUtility.executeSqlForLong;
import static sf.util.DatabaseUtility.executeSqlForScalar;
import static sf.util.TemplatingUtility.expandTemplate;
import static sf.util.Utility.isBlank;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import schemacrawler.schema.Column;
import schemacrawler.schema.JavaSqlTypeGroup;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.InclusionRuleWithRegularExpression;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;
import sf.util.UtilityMarker;

@UtilityMarker
public final class QueryUtility
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(QueryUtility.class.getName());

  public static ResultSet executeAgainstSchema(final Query query,
                                               final Statement statement,
                                               final InclusionRule schemaInclusionRule)
    throws SQLException
  {
    requireNonNull(query, "No query provided");
    final String sql = getQuery(query, schemaInclusionRule);
    LOGGER.log(Level.FINE,
               new StringFormat("Executing %s: %n%s", query.getName(), sql));
    return executeSql(statement, sql);
  }

  public static ResultSet executeAgainstTable(final Query query,
                                              final Statement statement,
                                              final Table table,
                                              final boolean isAlphabeticalSortForTableColumns,
                                              final Identifiers identifiers)
    throws SQLException
  {
    requireNonNull(query, "No query provided");
    requireNonNull(identifiers, "No identifiers provided");

    final String sql = getQuery(query,
                                table,
                                isAlphabeticalSortForTableColumns,
                                identifiers);
    LOGGER.log(Level.FINE,
               new StringFormat("Executing %s: %n%s", query.getName(), sql));
    return executeSql(statement, sql);
  }

  public static long executeForLong(final Query query,
                                    final Connection connection,
                                    final Table table,
                                    final Identifiers identifiers)
    throws SchemaCrawlerException
  {
    requireNonNull(query, "No query provided");
    final String sql = getQuery(query, table, true, identifiers);
    LOGGER.log(Level.FINE,
               new StringFormat("Executing %s: %n%s", query.getName(), sql));
    return executeSqlForLong(connection, sql);
  }

  public static Object executeForScalar(final Query query,
                                        final Connection connection)
    throws SchemaCrawlerException
  {
    requireNonNull(query, "No query provided");
    final String sql = getQuery(query);
    LOGGER.log(Level.FINE,
               new StringFormat("Executing %s: %n%s", query.getName(), sql));
    return executeSqlForScalar(connection, sql);
  }

  public static Object executeForScalar(final Query query,
                                        final Connection connection,
                                        final Table table,
                                        final Identifiers identifiers)
    throws SchemaCrawlerException
  {
    requireNonNull(query, "No query provided");
    final String sql = getQuery(query, table, true, identifiers);
    LOGGER.log(Level.FINE,
               new StringFormat("Executing %s: %n%s", query.getName(), sql));
    return executeSqlForScalar(connection, sql);
  }

  private static String getColumnsListAsString(final List<Column> columns,
                                               final boolean omitLargeObjectColumns,
                                               final Identifiers identifiers)
  {
    final List<String> columnsList = new ArrayList<>();
    for (int i = 0; i < columns.size(); i++)
    {
      final Column column = columns.get(i);
      final JavaSqlTypeGroup javaSqlTypeGroup = column.getColumnDataType()
        .getJavaSqlType().getJavaSqlTypeGroup();
      if (!(omitLargeObjectColumns
            && (javaSqlTypeGroup == JavaSqlTypeGroup.large_object
                || javaSqlTypeGroup == JavaSqlTypeGroup.object)))
      {
        columnsList.add(identifiers.quoteName(column.getName()));
      }
    }
    return String.join(", ", columnsList);
  }

  private static String getQuery(final Query query)
  {
    return expandTemplate(query.getQuery());
  }

  /**
   * Gets the query with parameters substituted.
   *
   * @param schemaInclusionRule
   *        Schema inclusion rule
   * @return Ready-to-execute query
   */
  private static String getQuery(final Query query,
                                 final InclusionRule schemaInclusionRule)
  {
    final Map<String, String> properties = new HashMap<>();

    properties.put("schemas", ".*");
    if (schemaInclusionRule != null
        && schemaInclusionRule instanceof InclusionRuleWithRegularExpression)
    {
      final String schemaInclusionPattern = ((InclusionRuleWithRegularExpression) schemaInclusionRule)
        .getInclusionPattern().pattern();
      if (!isBlank(schemaInclusionPattern))
      {
        properties.put("schemas", schemaInclusionPattern);
      }
    }

    String sql = query.getQuery();
    sql = expandTemplate(sql, properties);
    sql = expandTemplate(sql);

    return sql;
  }

  private static String getQuery(final Query query,
                                 final Table table,
                                 final boolean isAlphabeticalSortForTableColumns,
                                 final Identifiers identifiers)
  {
    final Map<String, String> tableProperties = new HashMap<>();
    if (table != null)
    {
      final NamedObjectSort columnsSort = NamedObjectSort
        .getNamedObjectSort(isAlphabeticalSortForTableColumns);
      final List<Column> columns = table.getColumns();
      Collections.sort(columns, columnsSort);

      final Schema schema = table.getSchema();
      if (schema != null)
      {
        final String schemaName = identifiers.quoteFullName(schema);
        tableProperties.put("schema", schemaName);
      }
      tableProperties.put("table", identifiers.quoteFullName(table));
      tableProperties.put("tablename", table.getName());
      tableProperties.put("columns",
                          getColumnsListAsString(columns, false, identifiers));
      tableProperties.put("orderbycolumns",
                          getColumnsListAsString(columns, true, identifiers));
      tableProperties.put("tabletype", table.getTableType().toString());
    }

    String sql = query.getQuery();
    sql = expandTemplate(sql, tableProperties);
    sql = expandTemplate(sql);

    return sql;
  }

  private QueryUtility()
  {
    // Prevent instantiation
  }

}
