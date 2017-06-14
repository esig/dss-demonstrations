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

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyPreferenceDao;
import eu.europa.esig.dss.client.http.proxy.ProxyProps;
import eu.europa.esig.dss.web.model.db.DBProxyConfig;
import eu.europa.esig.dss.web.model.repository.ProxyConfigRepository;

public class ProxyConfigDbDao implements ProxyPreferenceDao {

	@Autowired
	private ProxyConfigRepository proxyPreferencesRepository;

	@Override
	public ProxyConfig get() {
		DBProxyConfig dbProxyConfig = proxyPreferencesRepository.findAll().iterator().next();
		if(dbProxyConfig != null) {
			ProxyConfig proxyConfig = new ProxyConfig();
			
			ProxyProps proxyPropsHttp = new ProxyProps();
			proxyPropsHttp.setEnabled(dbProxyConfig.getHttpProps().isEnabled());
			proxyPropsHttp.setExcludedHost(dbProxyConfig.getHttpProps().getExcludedHost());
			proxyPropsHttp.setHost(dbProxyConfig.getHttpProps().getHost());
			proxyPropsHttp.setPassword(dbProxyConfig.getHttpProps().getPassword());
			proxyPropsHttp.setPort(dbProxyConfig.getHttpProps().getPort());
			proxyPropsHttp.setUser(dbProxyConfig.getHttpProps().getUser());
			
			ProxyProps proxyPropsHttps = new ProxyProps();
			proxyPropsHttps.setEnabled(dbProxyConfig.getHttpsProps().isEnabled());
			proxyPropsHttps.setExcludedHost(dbProxyConfig.getHttpsProps().getExcludedHost());
			proxyPropsHttps.setHost(dbProxyConfig.getHttpsProps().getHost());
			proxyPropsHttps.setPassword(dbProxyConfig.getHttpsProps().getPassword());
			proxyPropsHttps.setPort(dbProxyConfig.getHttpsProps().getPort());
			proxyPropsHttps.setUser(dbProxyConfig.getHttpsProps().getUser());
			
			proxyConfig.setHttpProps(proxyPropsHttp);
			proxyConfig.setHttpsProps(proxyPropsHttps);
			
			return proxyConfig;
		}
		return null;
	}

	@Override
	@Transactional
	public void update(ProxyConfig proxyConfig) {
		DBProxyConfig dbProxyConfig = proxyPreferencesRepository.findAll().iterator().next();
		if(dbProxyConfig != null && proxyConfig != null) {
			dbProxyConfig.getHttpProps().setEnabled(proxyConfig.getHttpProps().isEnabled());
			dbProxyConfig.getHttpProps().setExcludedHost(proxyConfig.getHttpProps().getExcludedHost());
			dbProxyConfig.getHttpProps().setHost(proxyConfig.getHttpProps().getHost());
			dbProxyConfig.getHttpProps().setPassword(proxyConfig.getHttpProps().getPassword());
			dbProxyConfig.getHttpProps().setPort(proxyConfig.getHttpProps().getPort());
			dbProxyConfig.getHttpProps().setUser(proxyConfig.getHttpProps().getUser());
			
			dbProxyConfig.getHttpsProps().setEnabled(proxyConfig.getHttpsProps().isEnabled());
			dbProxyConfig.getHttpsProps().setExcludedHost(proxyConfig.getHttpsProps().getExcludedHost());
			dbProxyConfig.getHttpsProps().setHost(proxyConfig.getHttpsProps().getHost());
			dbProxyConfig.getHttpsProps().setPassword(proxyConfig.getHttpsProps().getPassword());
			dbProxyConfig.getHttpsProps().setPort(proxyConfig.getHttpsProps().getPort());
			dbProxyConfig.getHttpsProps().setUser(proxyConfig.getHttpsProps().getUser());
			proxyPreferencesRepository.save(dbProxyConfig);
		}
	}

}
