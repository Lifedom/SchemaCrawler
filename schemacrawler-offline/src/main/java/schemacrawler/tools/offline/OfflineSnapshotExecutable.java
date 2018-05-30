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

package schemacrawler.tools.offline;


import schemacrawler.schema.*;
import schemacrawler.schemacrawler.DatabaseSpecificOptions;
import schemacrawler.schemacrawler.DatabaseSpecificOverrideOptions;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseExecutable;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.integration.serialization.XmlSerializedCatalog;
import schemacrawler.tools.offline.jdbc.OfflineConnection;
import schemacrawler.tools.options.OutputOptions;
import sf.util.SchemaCrawlerLogger;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;
import static schemacrawler.filter.ReducerFactory.*;

/**
 * A SchemaCrawler tools executable unit.
 *
 * @author Sualeh Fatehi
 */
public class OfflineSnapshotExecutable
  extends BaseExecutable
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(OfflineSnapshotExecutable.class.getName());

  private OutputOptions inputOptions;

  public OfflineSnapshotExecutable(final String command)
  {
    super(command);
  }

  @Override
  public void execute(final Connection connection,
                      final DatabaseSpecificOverrideOptions databaseSpecificOverrideOptions)
    throws Exception
  {
    checkConnection(connection);

    inputOptions = new OutputOptions();
    inputOptions.setCompressedInputFile(((OfflineConnection) connection)
      .getOfflineDatabasePath());

    databaseSpecificOptions = new DatabaseSpecificOptions(connection,
                                                          databaseSpecificOverrideOptions);

    final Catalog catalog = loadCatalog();

    executeOn(catalog, connection);
  }

  private void executeOn(final Catalog catalog, final Connection connection)
    throws Exception
  {
    loadOfflineSnapshotOptions();
    checkConnection(connection);

    requireNonNull(catalog, "No catalog provided");

    // Reduce all
    ((Reducible) catalog).reduce(Schema.class,
                                 getSchemaReducer(schemaCrawlerOptions));
    ((Reducible) catalog).reduce(Table.class,
                                 getTableReducer(schemaCrawlerOptions));
    ((Reducible) catalog).reduce(Routine.class,
                                 getRoutineReducer(schemaCrawlerOptions));
    ((Reducible) catalog).reduce(Synonym.class,
                                 getSynonymReducer(schemaCrawlerOptions));
    ((Reducible) catalog).reduce(Sequence.class,
                                 getSequenceReducer(schemaCrawlerOptions));

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(command);
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setDatabaseSpecificOptions(databaseSpecificOptions);
    executable.setAdditionalConfiguration(additionalConfiguration);
    executable.setOutputOptions(outputOptions);
    executable.executeOn(catalog, connection);
  }

  public void setInputOptions(final OutputOptions inputOptions)
  {
    this.inputOptions = inputOptions;
  }

  private void checkConnection(final Connection connection)
  {
    if (connection == null || !(connection instanceof OfflineConnection))
    {
      LOGGER
        .log(Level.SEVERE,
             "Offline database connection not provided for the offline snapshot");
    }
  }

  private Catalog loadCatalog()
    throws SchemaCrawlerException
  {
    final Reader snapshotReader;
    try
    {
      snapshotReader = inputOptions.openNewInputReader();
    }
    catch (final IOException e)
    {
      throw new SchemaCrawlerException("Cannot open input reader", e);
    }

    final XmlSerializedCatalog xmlDatabase = new XmlSerializedCatalog(snapshotReader);
    return xmlDatabase;
  }

  private void loadOfflineSnapshotOptions()
  {
    if (inputOptions == null)
    {
      inputOptions = new OutputOptions(additionalConfiguration);
    }
  }

}
