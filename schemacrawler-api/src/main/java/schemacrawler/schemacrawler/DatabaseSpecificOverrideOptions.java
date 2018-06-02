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
package schemacrawler.schemacrawler;


import static sf.util.Utility.isBlank;

import java.util.Map;
import java.util.Optional;

import schemacrawler.crawl.MetadataRetrievalStrategy;
import schemacrawler.utility.TypeMap;

/**
 * Provides for database specific overrides for SchemaCrawler
 * functionality. This can add or inject database plugins, or override
 * defaults. It is recommended to build these options using factory
 * methods in SchemaCrawlerUtility.
 *
 * @author Sualeh Fatehi <sualeh@hotmail.com>
 */
public final class DatabaseSpecificOverrideOptions
  implements Options
{

  private final DatabaseServerType dbServerType;
  private final Optional<Boolean> supportsSchemas;
  private final Optional<Boolean> supportsCatalogs;
  private final MetadataRetrievalStrategy tableRetrievalStrategy;
  private final MetadataRetrievalStrategy tableColumnRetrievalStrategy;
  private final MetadataRetrievalStrategy pkRetrievalStrategy;
  private final MetadataRetrievalStrategy indexRetrievalStrategy;
  private final MetadataRetrievalStrategy fkRetrievalStrategy;
  private final MetadataRetrievalStrategy procedureRetrievalStrategy;
  private final MetadataRetrievalStrategy functionRetrievalStrategy;
  private final String identifierQuoteString;
  private final InformationSchemaViews informationSchemaViews;
  private final TypeMap typeMap;

  protected DatabaseSpecificOverrideOptions(final DatabaseSpecificOverrideOptionsBuilder builder)
  {
    final DatabaseSpecificOverrideOptionsBuilder bldr = builder == null? new DatabaseSpecificOverrideOptionsBuilder()
                                                                       : builder;
    dbServerType = bldr.getDatabaseServerType();
    supportsSchemas = bldr.getSupportsSchemas();
    supportsCatalogs = bldr.getSupportsCatalogs();
    tableRetrievalStrategy = bldr.getTableRetrievalStrategy();
    tableColumnRetrievalStrategy = bldr.getTableColumnRetrievalStrategy();
    pkRetrievalStrategy = bldr.getPrimaryKeyRetrievalStrategy();
    indexRetrievalStrategy = bldr.getIndexRetrievalStrategy();
    fkRetrievalStrategy = bldr.getForeignKeyRetrievalStrategy();
    procedureRetrievalStrategy = bldr.getProcedureRetrievalStrategy();
    functionRetrievalStrategy = bldr.getFunctionRetrievalStrategy();
    identifierQuoteString = bldr.getIdentifierQuoteString();
    informationSchemaViews = bldr.getInformationSchemaViewsBuilder()
      .toOptions();

    final Map<String, Class<?>> bldrTypeMap = bldr.getTypeMap();
    if (bldrTypeMap != null)
    {
      typeMap = new TypeMap(bldrTypeMap);
    }
    else
    {
      typeMap = null;
    }
  }

  public DatabaseServerType getDatabaseServerType()
  {
    return dbServerType;
  }

  public MetadataRetrievalStrategy getForeignKeyRetrievalStrategy()
  {
    return fkRetrievalStrategy;
  }

  public MetadataRetrievalStrategy getFunctionRetrievalStrategy()
  {
    return functionRetrievalStrategy;
  }

  public String getIdentifierQuoteString()
  {
    if (!hasOverrideForIdentifierQuoteString())
    {
      return "";
    }
    return identifierQuoteString;
  }

  public MetadataRetrievalStrategy getIndexRetrievalStrategy()
  {
    return indexRetrievalStrategy;
  }

  public InformationSchemaViews getInformationSchemaViews()
  {
    return informationSchemaViews;
  }

  public MetadataRetrievalStrategy getPrimaryKeyRetrievalStrategy()
  {
    return pkRetrievalStrategy;
  }

  public MetadataRetrievalStrategy getProcedureRetrievalStrategy()
  {
    return procedureRetrievalStrategy;
  }

  public MetadataRetrievalStrategy getTableColumnRetrievalStrategy()
  {
    return tableColumnRetrievalStrategy;
  }

  public MetadataRetrievalStrategy getTableRetrievalStrategy()
  {
    return tableRetrievalStrategy;
  }

  public TypeMap getTypeMap()
  {
    return typeMap;
  }

  public boolean hasOverrideForIdentifierQuoteString()
  {
    return !isBlank(identifierQuoteString);
  }

  public boolean hasOverrideForSupportsCatalogs()
  {
    return supportsCatalogs.isPresent();
  }

  public boolean hasOverrideForSupportsSchemas()
  {
    return supportsSchemas.isPresent();
  }

  public boolean hasOverrideForTypeMap()
  {
    return typeMap != null;
  }

  public boolean isSupportsCatalogs()
  {
    return supportsCatalogs.orElse(true);
  }

  public boolean isSupportsSchemas()
  {
    return supportsSchemas.orElse(true);
  }

}
