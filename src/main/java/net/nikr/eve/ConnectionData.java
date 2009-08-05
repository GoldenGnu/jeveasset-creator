/*
 * Copyright 2009, Niklas Kyster Rasmussen
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

package net.nikr.eve;


public class ConnectionData {
	String host;
	String port;
	String database;
	String username;
	String password;

	public ConnectionData() {
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getConnectionUrl(){
		return "jdbc:sqlserver://127.0.0.1:1433;"
					+ "databaseName=eve_export;"
					+ "user=sa;"
					+ "password=sa;";
		/*
		return "jdbc:sqlserver://"+host+":"+port+";"
					+ "databaseName="+database+";"
					+ "user="+username+";"
					+ "password="+password+";";
		 */
	}
}
