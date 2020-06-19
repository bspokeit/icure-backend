/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taktik.icure.dto.filter.impl.service;


import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import org.taktik.icure.entities.embed.Service;
import org.taktik.icure.services.external.rest.handlers.JsonPolymorphismRoot;
import org.taktik.icure.services.external.rest.v1.dto.filter.FilterDto;

import java.util.Set;

@JsonPolymorphismRoot(FilterDto.class)
@JsonDeserialize(using= JsonDeserializer.None.class)
public class ServiceByContactsAndSubcontactsFilter extends FilterDto<Service> implements org.taktik.icure.dto.filter.service.ServiceByContactsAndSubcontactsFilter {
	String healthcarePartyId;
    Set<String> contacts;
	Set<String> subContacts;
	Long startValueDate;
	Long endValueDate;

	public ServiceByContactsAndSubcontactsFilter() {
	}

	public ServiceByContactsAndSubcontactsFilter(String healthcarePartyId, Set<String> contacts, Set<String> subContacts, Long startServiceValueDate, Long endServiceValueDate) {
        this.healthcarePartyId = healthcarePartyId;
        this.contacts = contacts;
        this.subContacts = subContacts;
        this.startValueDate = startServiceValueDate;
		this.endValueDate = endServiceValueDate;
	}

	@Override
	public String getHealthcarePartyId() {
		return healthcarePartyId;
	}

	public void setHealthcarePartyId(String healthcarePartyId) {
		this.healthcarePartyId = healthcarePartyId;
	}

    @Override
    public Set<String> getContacts() {
        return contacts;
    }

    public void setContacts(Set<String> contacts) {
        this.contacts = contacts;
    }

    @Override
    public Set<String> getSubContacts() {
        return subContacts;
    }

    public void setSubContacts(Set<String> subContacts) {
        this.subContacts = subContacts;
    }

    @Override
    public Long getStartValueDate() {
        return startValueDate;
    }

    public void setStartValueDate(Long startValueDate) {
        this.startValueDate = startValueDate;
    }

    @Override
    public Long getEndValueDate() {
        return endValueDate;
    }

    public void setEndValueDate(Long endValueDate) {
        this.endValueDate = endValueDate;
    }

	@Override
	public boolean matches(Service item) {
		return (healthcarePartyId == null || item.getDelegations().keySet().contains(healthcarePartyId))
				&& (contacts.contains(item.getContactId()))
				&& (Sets.intersection(subContacts,item.getSubContactIds()).size()>0);
	}
}