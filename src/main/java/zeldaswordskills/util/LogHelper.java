/**
    Copyright (C) <2014> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.common.FMLLog;

public class LogHelper
{
	private static Logger logger = Logger.getLogger(ModInfo.NAME.replace(" ", ""));
	private static Handler consoleHandler = new ConsoleHandler();
	private static Formatter zssFormatter = new ZSSLogFormatter();

	/**
	 * Sets up the logger to output messages of up to the level given
	 */
	public static void init(Level level) {
		logger.setLevel(level);
		// only use custom handler when necessary to output FINE or lower logs
		if (level.intValue() < Level.INFO.intValue()) { 
			consoleHandler.setFormatter(zssFormatter);
			consoleHandler.setLevel(logger.getLevel());
			logger.addHandler(consoleHandler);
		} else { // otherwise use FML logger formatting
			logger.setParent(FMLLog.getLogger());
		}
	}

	public static void log(Level logLevel, String message) {
		logger.log(logLevel, message);
	}
}

/**
 * 
 * Copied from cpw.mods.fml.relauncher.FMLLogFormatter
 *
 */
final class ZSSLogFormatter extends Formatter {
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	//private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String format(LogRecord record)
	{
		StringBuilder msg = new StringBuilder();
		//msg.append(this.dateFormat.format(Long.valueOf(record.getMillis())));
		Level lvl = record.getLevel();

		String name = lvl.getLocalizedName();
		if ( name == null ) {
			name = lvl.getName();
		}

		if ((name != null) && (name.length() > 0)) {
			msg.append("[" + name + "] ");
		} else {
			msg.append(" ");
		}

		if (record.getLoggerName() != null) {
			msg.append("["+record.getLoggerName()+"] ");
		} else {
			msg.append("[] ");
		}
		msg.append(formatMessage(record));
		msg.append(LINE_SEPARATOR);
		Throwable thr = record.getThrown();

		if (thr != null) {
			StringWriter thrDump = new StringWriter();
			thr.printStackTrace(new PrintWriter(thrDump));
			msg.append(thrDump.toString());
		}

		return msg.toString();
	}
}
