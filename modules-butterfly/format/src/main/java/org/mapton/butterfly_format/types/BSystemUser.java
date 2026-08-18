/*
 * Copyright 2026 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "project",
    "accessLevel",
    "created",
    "expires",
    "name",
    "organisation",
    "email",
    "domain",
    "initials",
    "lastLoginAdmin",
    "lastLoginProj",
    "lastLoginView",
    "loginsAdmin",
    "loginsProj",
    "loginsView"
})
public class BSystemUser {

    private int accessLevel;
    private LocalDateTime created;
    private String domain;
    private String email;
    private LocalDate expires;
    private Long id;
    private String initials;
    private LocalDateTime lastLoginAdmin;
    private LocalDateTime lastLoginProj;
    private LocalDateTime lastLoginView;
    private int loginsAdmin;
    private int loginsProj;
    private int loginsView;
    private String name;
    private String organisation;
    private String project;

    public int getAccessLevel() {
        return accessLevel;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getDomain() {
        return domain;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getExpires() {
        return expires;
    }

    public Long getId() {
        return id;
    }

    public String getInitials() {
        return initials;
    }

    public LocalDateTime getLastLoginAdmin() {
        return lastLoginAdmin;
    }

    public LocalDateTime getLastLoginProj() {
        return lastLoginProj;
    }

    public LocalDateTime getLastLoginView() {
        return lastLoginView;
    }

    public int getLoginsAdmin() {
        return loginsAdmin;
    }

    public int getLoginsProj() {
        return loginsProj;
    }

    public int getLoginsView() {
        return loginsView;
    }

    public String getName() {
        return name;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getProject() {
        return project;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setEmail(String eail) {
        this.email = eail;
    }

    public void setExpires(LocalDate expires) {
        this.expires = expires;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setLastLoginAdmin(LocalDateTime lastLoginAdmin) {
        this.lastLoginAdmin = lastLoginAdmin;
    }

    public void setLastLoginProj(LocalDateTime lastLoginProj) {
        this.lastLoginProj = lastLoginProj;
    }

    public void setLastLoginView(LocalDateTime lastLoginView) {
        this.lastLoginView = lastLoginView;
    }

    public void setLoginsAdmin(int loginsAdmin) {
        this.loginsAdmin = loginsAdmin;
    }

    public void setLoginsProj(int loginsProj) {
        this.loginsProj = loginsProj;
    }

    public void setLoginsView(int loginsView) {
        this.loginsView = loginsView;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
