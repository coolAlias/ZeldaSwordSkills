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
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import zeldaswordskills.lib.ModInfo;

public class LogHelper
{
	private static Logger logger = Logger.getLogger(ModInfo.NAME.replace(" ", ""));
	private static Handler consoleHandler = new ConsoleHandler();
	private static Formatter zssFormatter = new ZSSLogFormatter();

	/**
	 * Calls {@link #init(Level)} with level of Level.INFO
	 */
	public static void init() {
		init(Level.INFO);
	}

	/**
	 * Sets up the logger to output messages of up to the level given
	 */
	public static void init(Level level) {
		logger.setLevel(level);
		consoleHandler.setFormatter(zssFormatter);
		consoleHandler.setLevel(logger.getLevel());
		logger.setUseParentHandlers(false); // otherwise some messages appear 2+ times
		logger.addHandler(consoleHandler);
	}

	public static void log(Level logLevel, String message) {
		logger.log(logLevel, message);
	}

	public static void severe(String message) {
		logger.log(Level.SEVERE, message);
	}

	public static void warning(String message) {
		logger.log(Level.WARNING, message);
	}

	public static void info(String message) {
		logger.log(Level.INFO, message);
	}

	public static void fine(String message) {
		logger.log(Level.FINE, message);
	}

	public static void finer(String message) {
		logger.log(Level.FINER, message);
	}

	public static void finest(String message) {
		logger.log(Level.FINEST, message);
	}
}

/**
 * 
 * Copied from cpw.mods.fml.relauncher.FMLLogFormatter
 *
 */
final class ZSSLogFormatter extends Formatter {
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	public String format(LogRecord record)
	{
		StringBuilder msg = new StringBuilder();
		msg.append("[" + dateFormat.format(Long.valueOf(record.getMillis())) + "] ");
		if (record.getLoggerName() != null) {
			msg.append("[" + record.getLoggerName() + "] ");
		} else {
			msg.append("[] ");
		}

		String level = record.getLevel().getLocalizedName();
		if ((level != null) && (level.length() > 0)) {
			msg.append("[" + level + "] ");
		} else {
			msg.append(" ");
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
