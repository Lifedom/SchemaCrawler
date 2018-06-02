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
package schemacrawler.tools.executable;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import sf.util.SchemaCrawlerLogger;

/**
 * Allows chaining multiple executables with the same configuration. The
 * catalog is obtained just once, and passed on from executable to
 * executable for efficiency in execution.
 */
abstract class BaseCommandChain
  extends BaseSchemaCrawlerCommand
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(BaseCommandChain.class.getName());

  private final List<SchemaCrawlerCommand> executables;
  protected final CommandRegistry commandRegistry;

  protected BaseCommandChain(final String command)
    throws SchemaCrawlerException
  {
    super(command);

    commandRegistry = new CommandRegistry();
    executables = new ArrayList<>();
  }

  public final SchemaCrawlerCommand addNext(final SchemaCrawlerCommand executable)
  {
    if (executable != null)
    {
      executables.add(executable);
    }
    return executable;
  }

  protected final void executeChain(final Catalog catalog,
                                    final Connection connection)
    throws Exception
  {
    if (executables.isEmpty())
    {
      LOGGER.log(Level.INFO, "No commands to execute");
      return;
    }

    for (final SchemaCrawlerCommand executable: executables)
    {
      if (executable instanceof BaseSchemaCrawlerCommand)
      {
        final BaseSchemaCrawlerCommand stagedExecutable = (BaseSchemaCrawlerCommand) executable;
        executable.setDatabaseSpecificOptions(databaseSpecificOptions);
        stagedExecutable.executeOn(catalog, connection);
      }
    }
  }

}
