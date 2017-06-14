/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.web.dao;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import eu.europa.esig.dss.client.http.proxy.ProxyPreference;
import eu.europa.esig.dss.client.http.proxy.ProxyPreferenceManager;
import eu.europa.esig.dss.web.model.Preference;
import eu.europa.esig.dss.web.model.repository.PreferenceRepository;

public class ProxyPreferencesDao extends ProxyPreferenceManager {

	@Autowired
	private PreferenceRepository proxyPreferencesRepository;

	@Override
	public ProxyPreference get(String id) {
		return proxyPreferencesRepository.findOne(id);
	}

	@Override
	public List<ProxyPreference> getAll() {
		Iterable<Preference> iterable = proxyPreferencesRepository.findAll();
		List<ProxyPreference> result = new ArrayList<>();
		for(Preference preferenceImpl : iterable) {
			result.add((ProxyPreference) preferenceImpl);
		}
		return result;
	}

	@Override
	@Transactional
	public void update(String key, String value) {
		Preference proxyPreferenceImpl = new Preference();
		proxyPreferenceImpl.setKey(key);
		proxyPreferenceImpl.setValue(value);
		proxyPreferencesRepository.save(proxyPreferenceImpl);
		setChanged();
	}

}
