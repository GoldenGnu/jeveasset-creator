/*
 * Copyright 2009-2023 Contributors (see credits.txt)
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
package net.nikr.eve.io.esi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.troja.eve.esi.ApiClient;
import net.troja.eve.esi.ApiClientBuilder;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.ApiResponse;
import net.troja.eve.esi.HeaderUtil;
import net.troja.eve.esi.api.MarketApi;
import net.troja.eve.esi.api.UniverseApi;
import net.troja.eve.esi.model.DogmaAttribute;
import net.troja.eve.esi.model.NamesResponse;
import net.troja.eve.esi.model.TypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EsiUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(EsiUpdater.class);

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(50);

	private static final ApiClient CLIENT = new ApiClientBuilder().userAgent("jEveAssets XML Builder").build();
	public static final UniverseApi UNIVERSE_API = new UniverseApi(CLIENT);
	public static final MarketApi MARKET_API = new MarketApi(CLIENT);
	public static final String DATASOURCE = "tranquility";

	private static Integer errorLimit = null;
	private static Date errorReset = new Date();

	public static interface UpdateValue<T, V> extends Update<T> {
		public V getValue();
	}

	public static class UpdateValues<T, V> {
		private final T response;
		private final V value;

		public UpdateValues(T response, V value) {
			this.response = response;
			this.value = value;
		}

		public T getResponse() {
			return response;
		}

		public V getValue() {
			return value;
		}
	}

	public static interface Update<T> extends UpdatePage<T> {
		public ApiResponse<T> update() throws ApiException;
		@Override
		default ApiResponse<T> update(int page) throws ApiException {
			return update();
		}
	}

	public static interface UpdatePage<T> {
		public ApiResponse<T> update(int page) throws ApiException;
	}

	public static <T> T update(Update<T> update) {
		try {
			Updater<T> updater = new Updater<>(update);
			return updater.call();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static <T> List<T> update(Collection<? extends Update<T>> updates) {
		List<T> values = new ArrayList<>();
		try {
			List<Updater<T>> updaters = new ArrayList<>();
			for (Update<T> update : updates) {
				updaters.add(new Updater<>(update));
			}
			List<Future<T>> futures = EXECUTOR.invokeAll(updaters);
			for (Future<T> future : futures) {
				values.add(future.get());
			}
		} catch (InterruptedException | ExecutionException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return values;
	}

	public static <T, V> List<UpdateValues<T, V>> updateValues(Collection<? extends UpdateValue<T, V>> updates) {
		List<UpdateValues<T, V>> values = new ArrayList<>();
		try {
			List<ValueUpdater<T, V>> updaters = new ArrayList<>();
			for (UpdateValue<T, V> update : updates) {
				updaters.add(new ValueUpdater<>(update));
			}
			List<Future<UpdateValues<T, V>>> futures = EXECUTOR.invokeAll(updaters);
			for (Future<UpdateValues<T, V>> future : futures) {
				values.add(future.get());
			}
		} catch (InterruptedException | ExecutionException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return values;
	}

	public static <T> List<T> updatePage(UpdatePage<T> updatePage) {
		List<T> values = new ArrayList<>();
		try {
			PagesUpdater<T> updater = new PagesUpdater<>(updatePage);
			Integer xPages = updater.call();
			if (xPages == null || xPages < 1) {
				throw new RuntimeException("xPages is " + xPages);
			}
			List<Updater<T>> updaters = new ArrayList<>();
			for (int page = 1; page <= xPages; page++) {
				updaters.add(new Updater<>(updatePage, page));
			}
			List<Future<T>> futures = EXECUTOR.invokeAll(updaters);
			for (Future<T> future : futures) {
				values.add(future.get());
			}
		} catch (InterruptedException | ExecutionException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return values;
	}

	private static abstract class AbstractUpdater<T> {

		private final UpdatePage<T> update;
		private final int page;
		private int retries = 0;

		public AbstractUpdater(UpdatePage<T> update) {
			this.update = update;
			this.page = 1;
		}

		public AbstractUpdater(UpdatePage<T> update, int page) {
			this.update = update;
			this.page = page;
		}

		protected ApiResponse<T> update() {
			try {
				checkErrors(); //Update timeframe as needed
				ApiResponse<T> response = update.update(page);
				setErrorLimit(response.getHeaders());
				return response;
			} catch (ApiException ex) {
				setErrorLimit(ex.getResponseHeaders());
				retries++;
				if (ex.getCode() == 404) {
					LOG.warn("not found");
					return null;
				} else if (retries <= 3) {
					try {
						Thread.sleep(retries * 1000);
					} catch (InterruptedException ex1) {
						//No problem
					}
					LOG.warn("Failed to update (retrying):" + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody());
					return update();
				} else {
					LOG.error("Failed to update (retrying):" + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody(), ex);
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

	private static class Updater<T> extends AbstractUpdater<T> implements Callable<T> {

		private T data;

		public Updater(UpdatePage<T> update) {
			super(update);
		}

		public Updater(UpdatePage<T> update, int page) {
			super(update, page);
		}

		@Override
		public T call() throws Exception {
			return update().getData();
		}

		public T getData() {
			return data;
		}
	}

	private static class ValueUpdater<T, V> extends AbstractUpdater<T> implements Callable<UpdateValues<T, V>> {

		private final V value;

		public ValueUpdater(UpdateValue<T, V> update) {
			super(update);
			this.value = update.getValue();
		}

		@Override
		public UpdateValues<T, V> call() throws Exception {
			ApiResponse<T> response = update();
			return new UpdateValues<>(response.getData(), value);
		}
	}

	private static class PagesUpdater<T> extends AbstractUpdater<T> implements Callable<Integer> {

		public PagesUpdater(UpdatePage<T> update) {
			super(update);
		}

		@Override
		public Integer call() throws Exception {
			ApiResponse<T> response = update();
			return HeaderUtil.getXPages(response.getHeaders());
		}
	}

	public static class UpdateType implements UpdateValue<TypeResponse, Long> {

		private final long typeID;

		public UpdateType(long typeID) {
			this.typeID = typeID;
		}

		@Override
		public Long getValue() {
			return typeID;
		}

		@Override
		public ApiResponse<TypeResponse> update() throws ApiException {
			return UNIVERSE_API.getTypeWithHttpInfo(typeID, UniverseApi.COMPATIBILITY_DATE, null, null, null);
		}
	}

	public static class UpdateName implements UpdateValue<List<NamesResponse>, Long> {

		private final long id;

		public UpdateName(int id) {
			this.id = id;
		}

		@Override
		public Long getValue() {
			return id;
		}

		@Override
		public ApiResponse<List<NamesResponse>> update() throws ApiException {
			return UNIVERSE_API.postNamesWithHttpInfo(UniverseApi.COMPATIBILITY_DATE, Collections.singleton(id), null, null, null);
		}
	}

	public static class TypeData {
		private final long typeID;
		private final TypeResponse response;
		private final String name;
		private final Double packagedVolume;
		private final Double volume;
		private final Map<Long, Double> attributes = new HashMap<>();
		private Long metaGroupID;

		public TypeData(UpdateValues<TypeResponse, Long> response) {
			this(response.getValue(), response.getResponse());
		}

		public TypeData(long typeID, TypeResponse response) {
			this.typeID = typeID;
			this.response = response;
			if (response != null) {
				packagedVolume = response.getPackagedVolume();
				volume = response.getVolume();
				name = response.getName();
				if (response.getDogmaAttributes() != null) {
					for (DogmaAttribute attribute : response.getDogmaAttributes()) {
						attributes.put(attribute.getAttributeId(), attribute.getValue());
						if (attribute.getAttributeId() == 1692) { //1692 = meta group
							metaGroupID = attribute.getValue().longValue();
						}
						
					}
				}
			} else {
				packagedVolume = null;
				volume = null;
				name = null;
			}
			
		}

		public long getTypeID() {
			return typeID;
		}

		public boolean havePackagedVolume() {
			return response != null
					&& packagedVolume != null
					&& volume != null
					&& !Objects.equals(packagedVolume, volume);
		}

		public boolean haveName() {
			return response != null
					&& name != null;
		}

		public String getName() {
			return name;
		}

		public long getMetaGroupID() {
			return (metaGroupID == null || metaGroupID < 1) ? 1 : metaGroupID;
		}

		public Double getPackagedVolume() {
			return getOrDefault(packagedVolume, 0.0);
		}

		public Map<Long, Double> getAttributes() {
			return attributes;
		}

		private <T> T getOrDefault(T f, T defaultValue) {
			if (f != null) {
				return f;
			} else {
				return defaultValue;
			}
		}
	}

	private synchronized static void setErrorLimit(Map<String, List<String>> responseHeaders) {
		if (responseHeaders != null) {
			Integer limit = getHeaderInteger(responseHeaders, "x-esi-error-limit-remain");
			if (limit != null) {
				if (errorLimit != null) {
					if (limit < errorLimit) {
						LOG.warn("Error limit: " + limit);
					}
					errorLimit = Math.min(errorLimit, limit);
				} else {
					if (limit < 100) {
						LOG.warn("Error limit: " + limit);
					}
					errorLimit = limit;
				}
			}
			Integer reset = getHeaderInteger(responseHeaders, "x-esi-error-limit-reset");
			if (reset != null) {
				errorReset = new Date(System.currentTimeMillis() + (reset * 1000L));
			}
		}
	}

	private synchronized static void checkErrors() {
		if (errorLimit != null && errorLimit <= 50) { //Error limit reached
			try {
				long wait = (errorReset.getTime() + 1000) - System.currentTimeMillis();
				LOG.warn("Error limit reached waiting: " + milliseconds(wait));
				if (wait > 0) { //Negative values throws an Exception
					Thread.sleep(wait); //Wait until the error window is reset
				}
				//Reset
				errorReset = new Date(); //New timeframe
				errorLimit = null;  //No errors in this timeframe (yet)
			} catch (InterruptedException ex) {
				//No problem
			}
		}
	}

	private static String milliseconds(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;

		return String.format("%02ds %dms", second, millis);
	}

	private static Integer getHeaderInteger(Map<String, List<String>> responseHeaders, String headerName) {
		String errorResetHeader = HeaderUtil.getHeader(responseHeaders, headerName);
		if (errorResetHeader != null) {
			try {
				return Integer.valueOf(errorResetHeader);
			} catch (NumberFormatException ex) {
				//No problem
			}
		}
		return null;
	}
}
