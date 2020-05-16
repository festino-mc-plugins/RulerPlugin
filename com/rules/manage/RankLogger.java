package com.rules.manage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rules.main.Group;
import com.rules.utils.ReverseLineInputStream;
import com.rules.utils.Utils;

public class RankLogger {
	private static final RankLogger instance = new RankLogger();
	private RankLogger() {}
	public static RankLogger get() {
		return instance;
	}
	
	private static final String FILE_NAME = "rank_log.txt";
	
	private static String getPath() {
		return RulesFileManager.getPluginFolder() + FILE_NAME;
	}
	
	public void log(CommandSender initiator, UUID reranked, Group newGroup) {
		String initStr = initiator.getClass().getSimpleName();
		if (initiator instanceof Player) {
			initStr = ((Player)initiator).getName();
		}
		String msg = initStr + " перенёс " + Utils.getName(reranked) + " в " + newGroup.getName();
		File file = new File(getPath());
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.append(msg + "\n");
			writer.close();
		} catch (IOException e) {
			Logger.severe("Couldn't log \"" + msg + "\"");;
		}
	}
	
	/** @return <b>null</b> if param is invalid or error<br>
	 * 			<b>String[size]</b> else  */
	public String[] getPage(int size, int page) {
		if (size <= 0 || page <= 0) {
			return null;
		}
		
		File file = new File(getPath());
		try {
			String[] res = new String[size];
			BufferedReader in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(file)));

			int skip = size * (page - 1);
			String line = in.readLine();
			while (line != null && skip > 0) {
				skip--;
				line = in.readLine();
			}
			
			if (skip == 0) {
				int i = 0;
				while (line != null && i < size) {
					res[i] = line;
					i++;
					line = in.readLine();
				}
			}
			
			in.close();
			return res;
		} catch (IOException e) {
			Logger.severe("RankLogger couldn't read log (size=" + size + ", page=" + page + ")");
			e.printStackTrace();
			return null;
		}
	}
	
	public int getPageCount(int size) {
		try {
			int lines = countLines(getPath());
			return (lines + size - 1) / size;
		} catch (IOException e) {
			Logger.severe("RankLogger couldn't count lines (size=" + size + ")");
			e.printStackTrace();
			return 0;
		}
	}
	
	// https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java/453067#453067
	private static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];

	        int readChars = is.read(c);
	        if (readChars == -1) {
	            // bail out if nothing to read
	            return 0;
	        }

	        // make it easy for the optimizer to tune this loop
	        int count = 0;
	        while (readChars == 1024) {
	            for (int i=0; i<1024;) {
	                if (c[i++] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            System.out.println(readChars);
	            for (int i=0; i<readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        return count == 0 ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
}
