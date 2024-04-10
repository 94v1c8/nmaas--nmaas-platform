package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.AccessToken;

import java.util.List;

public interface CustomAccessTokenService {

    void invalidate(Long id);
    AccessToken createToken(Long userId);
    List<AccessToken> getAll(Long userId);

}
