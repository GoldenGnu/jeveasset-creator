/*
 * Copyright 2009, Niklas Kyster Rasmussen, Flaming Candle
 *
 * This file is part of XML Creator for jEveAssets
 *
 * XML Creator for jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * XML Creator for jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XML Creator for jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve.io.ftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import net.nikr.eve.Program;
import net.nikr.eve.io.ProgressMonitor;
import net.nikr.eve.io.creator.Creators;
import net.nikr.eve.io.xml.XmlException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.LoggerFactory;

public class FtpWriter {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(FtpWriter.class);

	public static void upload(FtpData ftpData, ProgressMonitor progressMonitor) {
		// get an ftpClient object  
		FTPClient ftpClient = new FTPClient();
		try {
			// pass directory path on server to connect  
			ftpClient.connect(ftpData.getHost());

			// pass username and password, returned true if authentication is  
			// successful  
			boolean login = ftpClient.login(ftpData.getUsername(),
					ftpData.getPassword());

			if (login) {
				int total = (Creators.values().length * 2) + 4;
				progressMonitor.setValue(0);
				progressMonitor.setMinimum(0);
				progressMonitor.setMaximum(total);
				int count = 0;

				//Data files
				for (Creators creators : Creators.values()) {
					uploadFile(ftpData, ftpClient, creators.getCreator().getFilename());
					count++;
					progressMonitor.setValue(count);
					uploadFile(ftpData, ftpClient, creators.getCreator().getFilename() + ".md5");
					count++;
					progressMonitor.setValue(count);
				}
				//Version == data.dat (renamed on upload)
				uploadFile(ftpClient,
						Creators.VERSION.getCreator().getFilename(),
						ftpData.getUrl() + "update_version.dat");
				count++;
				progressMonitor.setValue(count);
				
				//Old update version file list (backward compatibility)
				createUpdateFile();
				uploadFile(ftpData, ftpClient, "update_files.dat");
				count++;
				progressMonitor.setValue(count);

				//New update version file list
				uploadFile(ftpData, ftpClient, "list.php");
				count++;
				progressMonitor.setValue(count);

				//Data zip
				uploadFile(ftpClient, "data.zip", "eve.nikr.net/jeveassets/data.zip");
				count++;
				progressMonitor.setValue(count);
				// logout the user, returned true if logout successfully  
				boolean logout = ftpClient.logout();
				if (logout) {
					System.out.println("Connection close...");
				}
			} else {
				System.out.println("Connection fail...");
			}

		} catch (SocketException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (XmlException ex) {
			LOG.error(ex.getMessage(), ex);
		} finally {
			try {
				ftpClient.disconnect();
			} catch (IOException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}
	}

	private static void uploadFile(FtpData ftpData, FTPClient ftpClient, String filename) throws FileNotFoundException, XmlException, IOException {
		String url = ftpData.getUrl() + filename.replace(File.separator, "/");
		uploadFile(ftpClient, filename, url);
	}

	private static void uploadFile(FTPClient ftpClient, String filename, String url) throws FileNotFoundException, XmlException, IOException {
		File file = new File(Program.getFilename(filename));
		FileInputStream inputStream = new FileInputStream(file);
		createDirectory(ftpClient, url);
		LOG.info("Uploading: " + file.getAbsoluteFile() + " To: " + url);
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		boolean storeFile = ftpClient.storeFile(url, inputStream);
		if (!storeFile) {
			throw new IOException("Failed to upload file: '" + url + "'.  error='" + ftpClient.getReplyString() + "'");
		}
	}

	private static void createDirectory(FTPClient ftpClient, String dirTree) throws IOException {

		boolean dirExists = true;
		dirTree = dirTree.substring(0, dirTree.lastIndexOf("/"));

		//tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
		String[] directories = dirTree.split("/");
		for (String dir : directories) {
			if (!dir.isEmpty()) {
				if (dirExists) {
					dirExists = ftpClient.changeWorkingDirectory(dir);
				}
				if (!dirExists) {
					if (!ftpClient.makeDirectory(dir)) {
						throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + ftpClient.getReplyString() + "'");
					} else {
						LOG.info("'" + dir + "' created");
					}
					if (!ftpClient.changeWorkingDirectory(dir)) {
						throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + ftpClient.getReplyString() + "'");
					}
				}
			}
		}
		if (!ftpClient.changeWorkingDirectory("/")) {
			throw new IOException("Unable to change remote root directory '/'.  error='" + ftpClient.getReplyString() + "'");
		}
	}

	private static void createUpdateFile() {
		BufferedWriter writer = null;
		try {
			//create a temporary file
			String filename = Program.getFilename("update_files.dat");
			File file = new File(filename);
			writer = new BufferedWriter(new FileWriter(file));
			boolean first = true;
			for (Creators creators : Creators.values()) {
				if (first) {
					first = false;
				} else {
					writer.write("\r\n");
				}
				writer.write(creators.getCreator().getFilename());
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (XmlException ex) {
			LOG.error(ex.getMessage(), ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
		}
	}
}
