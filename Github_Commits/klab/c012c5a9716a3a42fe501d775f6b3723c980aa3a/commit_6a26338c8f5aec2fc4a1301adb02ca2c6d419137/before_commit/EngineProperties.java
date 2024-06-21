package org.integratedmodelling.klab.hub.api;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Properties;

import org.integratedmodelling.klab.auth.KlabCertificate;

public class EngineProperties implements IProperties {

    public EngineProperties(ProfileResource profile, Agreement agreement, LicenseConfiguration config) {
        if (config.getClass().getName().equals(BouncyConfiguration.class.getName())) {
            BouncyEngineProperties(profile, agreement, (BouncyConfiguration) config);
        }
    }

    private Optional<LocalDateTime> getExpirationTime(Agreement agreement) {
        if (agreement == null || !agreement.isExpirable()) {
            return Optional.empty();
        }
        return Optional.of(agreement.getValidDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
    }

    private void BouncyEngineProperties(ProfileResource profile, Agreement agreement, LicenseConfiguration config) {
        this.properties = new Properties();

        Optional<LocalDateTime> expirationTime = getExpirationTime(agreement);
        if (expirationTime.isPresent()) {
            this.properties.setProperty(KlabCertificate.KEY_EXPIRATION, expirationTime.get().toString());
        }

		this.properties.setProperty(KlabCertificate.KEY_USERNAME, profile.getUsername());
		this.properties.setProperty(KlabCertificate.KEY_EMAIL, profile.getEmail());
		if (agreement != null) { this.properties.setProperty(KlabCertificate.KEY_AGREEMENT, agreement.getId()); }
		this.properties.setProperty(KlabCertificate.KEY_SIGNATURE, config.getKeyString());
		this.properties.setProperty(KlabCertificate.KEY_PARTNER_NAME, config.getName());
		this.properties.setProperty(KlabCertificate.KEY_PARTNER_EMAIL, config.getEmail());
		this.properties.setProperty(KlabCertificate.KEY_PARTNER_HUB, config.getHubUrl());
		this.properties.setProperty(KlabCertificate.KEY_CERTIFICATE_TYPE, KlabCertificate.Type.ENGINE.toString());
		this.properties.setProperty(KlabCertificate.KEY_CERTIFICATE_LEVEL, KlabCertificate.Level.USER.toString());
    }


    private Properties properties;

    public Properties getProperties() {
        return properties;
    }
}
