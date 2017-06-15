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
package eu.europa.esig.dss.web.controller.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyManager;
import eu.europa.esig.dss.web.model.ProxyConfigForm;

/**
 * Controller for proxy edition
 */
@Controller
@RequestMapping(value = "/admin")
public class ProxyEditController {

	private static final Logger logger = LoggerFactory.getLogger(ProxyEditController.class);

	@Autowired
	private ProxyManager proxyPreferenceManager;

	/**
	 * @param model
	 *            The model attributes
	 * @return a view name
	 */
	@RequestMapping(value = { "", "/", "/proxy" }, method = RequestMethod.GET)
	public String showProxy(final Model model) {
		ProxyConfig proxyConfig = proxyPreferenceManager.getProxyConfig();
		ProxyConfigForm form = new ProxyConfigForm();
		form.fillForm(proxyConfig);
		model.addAttribute("proxyConfigForm", form);
		return "admin-proxy-list";
	}

	/**
	 * @param form
	 *            The proxy form bean
	 * @return a view name
	 */
	@RequestMapping(value = { "", "/", "/proxy" }, method = RequestMethod.POST)
	public String updatePreferences(@ModelAttribute("proxyConfigForm") final ProxyConfigForm proxyConfigForm) {
		proxyPreferenceManager.update(proxyConfigForm.getProxyConfig());
		return "redirect:/admin/proxy";
	}
}
