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

package org.taktik.icure.dto.filter.impl.contact;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import org.taktik.icure.entities.Contact;
import org.taktik.icure.services.external.rest.handlers.JsonPolymorphismRoot;
import org.taktik.icure.services.external.rest.v1.dto.filter.FilterDto;

import java.util.List;

@JsonPolymorphismRoot(FilterDto.class)
@JsonDeserialize(using= JsonDeserializer.None.class)
public class ContactByHcPartyPatientTagCodeDateFilter extends FilterDto<Contact> implements org.taktik.icure.dto.filter.contact.ContactByHcPartyPatientTagCodeDateFilter {
	String healthcarePartyId;
	@Deprecated
    String patientSecretForeignKey;
	List<String> patientSecretForeignKeys;
	String tagType;
	String tagCode;
	String codeType;
	String codeCode;
	Long startServiceValueDate;
	Long endServiceValueDate;

	public ContactByHcPartyPatientTagCodeDateFilter() {
	}

	public ContactByHcPartyPatientTagCodeDateFilter(String healthcarePartyId, List<String> patientSecretForeignKeys, String tagType, String tagCode, String codeType, String codeCode, Long startServiceValueDate, Long endServiceValueDate) {
		this.healthcarePartyId = healthcarePartyId;
		this.patientSecretForeignKeys = patientSecretForeignKeys;
		this.tagType = tagType;
		this.tagCode = tagCode;
		this.codeType = codeType;
		this.codeCode = codeCode;
		this.startServiceValueDate = startServiceValueDate;
		this.endServiceValueDate = endServiceValueDate;
	}

	@Override
	public String getHealthcarePartyId() {
		return healthcarePartyId;
	}

	public void setHealthcarePartyId(String healthcarePartyId) {
		this.healthcarePartyId = healthcarePartyId;
	}

	@Override
	public List<String> getPatientSecretForeignKeys() {
		return patientSecretForeignKeys;
	}

	public void setPatientSecretForeignKeys(List<String> patientSecretForeignKeys) {
		this.patientSecretForeignKeys = patientSecretForeignKeys;
	}

    @Override
    public String getPatientSecretForeignKey() {
        return patientSecretForeignKey;
    }

    public void setPatientSecretForeignKey(String patientSecretForeignKey) {
        this.patientSecretForeignKey = patientSecretForeignKey;
    }

    @Override
	public String getTagType() {
		return tagType;
	}

	public void setTagType(String tagType) {
		this.tagType = tagType;
	}

	@Override
	public String getTagCode() {
		return tagCode;
	}

	public void setTagCode(String tagCode) {
		this.tagCode = tagCode;
	}

	@Override
	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	@Override
	public String getCodeCode() {
		return codeCode;
	}

	public void setCodeCode(String codeCode) {
		this.codeCode = codeCode;
	}

	public Long getStartServiceValueDate() {
		return startServiceValueDate;
	}

	public void setStartServiceValueDate(Long startServiceValueDate) {
		this.startServiceValueDate = startServiceValueDate;
	}

	public Long getEndServiceValueDate() {
		return endServiceValueDate;
	}

	public void setEndServiceValueDate(Long endServiceValueDate) {
		this.endServiceValueDate = endServiceValueDate;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(healthcarePartyId, tagType, tagCode, codeType, codeCode, startServiceValueDate, endServiceValueDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ContactByHcPartyPatientTagCodeDateFilter other = (ContactByHcPartyPatientTagCodeDateFilter) obj;
		return Objects.equal(this.healthcarePartyId, other.healthcarePartyId) && Objects.equal(this.patientSecretForeignKeys, other.patientSecretForeignKeys) && Objects.equal(this.tagType, other.tagType) && Objects.equal(this.tagCode, other.tagCode) && Objects.equal(this.codeType, other.codeType) && Objects.equal(this.codeCode, other.codeCode) && Objects.equal(this.startServiceValueDate, other.startServiceValueDate) && Objects.equal(this.endServiceValueDate, other.endServiceValueDate);
	}

	@Override
	public boolean matches(Contact item) {
		return (healthcarePartyId == null || item.getDelegations().keySet().contains(healthcarePartyId))
				&& (patientSecretForeignKeys == null || (item.getSecretForeignKeys() != null && item.getSecretForeignKeys().stream().filter(patientSecretForeignKeys::contains).findAny().isPresent()))
				&& (tagType == null || item.getServices().stream().filter(s ->
				(s.getTags().stream().filter(t -> tagType.equals(t.getType()) && (tagCode == null || tagCode.equals(t.getCode()))).findAny().isPresent())
						&& (codeType == null || (s.getCodes().stream().filter(c -> codeType.equals(c.getType()) && (codeCode == null || codeCode.equals(c.getCode()))).findAny().isPresent()))
						&& (startServiceValueDate == null || (s.getValueDate() != null && s.getValueDate() > startServiceValueDate) || (s.getOpeningDate() != null && s.getOpeningDate() > startServiceValueDate))
						&& (endServiceValueDate == null || (s.getValueDate() != null && s.getValueDate() < endServiceValueDate) || (s.getOpeningDate() != null && s.getOpeningDate() < endServiceValueDate))
		).findAny().isPresent());
	}
}